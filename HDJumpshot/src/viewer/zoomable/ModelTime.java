/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.zoomable;

import java.awt.Window;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Stack;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JScrollBar;
import javax.swing.event.EventListenerList;

import base.drawable.TimeBoundingBox;

import viewer.common.Dialogs;
import viewer.common.Parameters;
import de.hd.pvs.TraceFormat.util.Epoch;

/*
   This is the Model( as in MVC architecture ) that determines the 
   transformation between user __time__ coordinates and the graphics 
   __pixel__ coordinates.  Since this classs is extended from the 
   DefaultBoundedRangeModel class which is then being used as 
   the model INSIDE the ScrollbarTime, JScrollBar, so the __pixel__ 
   interface of this class is synchronized with that of ScrollbarTime 
   constantly during the program execution.  i.e.  
      ModelTime.getMinimum()  == ScrollbarTime.getMinimum()
      ModelTime.getMaximum()  == ScrollbarTime.getMaximum()
      ModelTime.getValue()    == ScrollbarTime.getValue()
      ModelTime.getExtent()   == ScrollbarTime.getExtent()
   All( or most ) accesses to this class should be through 
   the __time__ interface.  The only class that needs to access
   the __pixel__ interface of this classs is the VIEW object
   of the ViewportTime class.  i.e. method like
      setViewPixelsPerUnitTime()
      getViewPixelsPerUnitTime()
      updatePixelCoords()
   are accessed by RulerTime class.
 */
public class ModelTime extends DefaultBoundedRangeModel
implements AdjustmentListener
{
	private int    MAX_SCROLLBAR_PIXELS = 1073741824;  // = 2^(30)

	private static double NORMAL_ZOOMFACTOR = 2 ;
	
	// user coordinates of the time axis of the viewport
	private double tGlobal_min;
	private double tGlobal_max;
	
	// current position:
	private double tView_init;
	private double tView_extent;

	
	// pixel coordinates of the time axis of the viewport
	// are buried inside the superclass DefaultBoundedRangeModel

	// screen properties
	private int iViewWidth;// No. of View pixel per unit time


	// special purpose ChangeListeners, TimeListeners, to avoid conflict with
	// the EventListenerList, listenerList, in DefaultBoundedRangeModel
	private ModelTimePanel     params_display;
	private EventListenerList  time_listener_list;
	// internal global variable for use in fireTimeChanged()
	private TimeEvent          time_chg_evt;

	private Window             root_window;
	private JScrollBar         scrollbar;

	private Stack<TimeBoundingBox> zoom_undo_stack;
	private Stack<TimeBoundingBox> zoom_redo_stack;

	public ModelTime( final Window  top_window,
			Epoch  init_global_time,
			Epoch  final_global_time )
	{
		params_display     = null;
		time_chg_evt       = null;
		time_listener_list = new EventListenerList();
		zoom_undo_stack    = new Stack<TimeBoundingBox>();
		zoom_redo_stack    = new Stack<TimeBoundingBox>();

		root_window        = top_window;
		setTimeGlobalMinimum( init_global_time );
		setTimeGlobalMaximum( final_global_time );
	}

	/*
        None of the setTimeXXXXXX() functions updates the __Pixel__ coordinates
	 */
	 private void setTimeGlobalMinimum( Epoch init_global_time )
	 {
		 tGlobal_min    = init_global_time.getDouble();
		 tView_init     = tGlobal_min;
	 }

	 private void setTimeGlobalMaximum( Epoch final_global_time )
	 {
		 tGlobal_max    = final_global_time.getDouble();
		 tView_extent   = getGlobalExtend();
	 }
	 
	 private double getScollbarPixelPerTime(double extend){
		 return (double) MAX_SCROLLBAR_PIXELS / extend;
	 }

	 private double getGlobalExtend(){
		 return tGlobal_max - tGlobal_min;
	 }
	 
	 // tGlobal_min & tGlobal_max cannot be changed by setTimeViewPosition()
	 private void setTimeViewPosition( double cur_view_init )
	 {
		 if ( cur_view_init < tGlobal_min )
			 tView_init     = tGlobal_min;
		 else {
			 if ( cur_view_init > tGlobal_max - tView_extent )
				 tView_init     = tGlobal_max - tView_extent;
			 else
				 tView_init     = cur_view_init;
		 }
	 }

	 // tGlobal_min & tGlobal_max cannot be changed by setTimeViewExtent()
	 private void setTimeViewExtent( double cur_view_extent )
	 {
		 if ( cur_view_extent < getGlobalExtend() ) {
			 tView_extent   = cur_view_extent;
			 if ( tView_init  > tGlobal_max - tView_extent ) {
				 tView_init      = tGlobal_max - tView_extent;
			 }
		 }
		 else {
			 tView_extent   = getGlobalExtend();
			 tView_init     = tGlobal_min;
		 }
	 }

	 public double getTimeGlobalMinimum()
	 {
		 return tGlobal_min;
	 }

	 public double getTimeGlobalMaximum()
	 {
		 return tGlobal_max;
	 }

	 
	 public double getTimeViewPosition()
	 {
		 return tView_init;
	 }

	 public double getTimeViewExtent()
	 {
		 return tView_extent;
	 }

	 /*
       Set the Number of Pixels in the Viewport window.
       if iView_width is NOT set, pixel coordinates cannot be updated.
	  */
	 public void setViewPixelsPerUnitTime( int width )
	 {
		 iViewWidth = width;
	 }


	public double getViewPixelsPerUnitTime(){
		return iViewWidth / tView_extent;
	}

	 public double computeTimeViewExtent( double time_per_pixel )
	 {
		 return iViewWidth * time_per_pixel;
	 }

	 public void setScrollBar( JScrollBar sb )
	 {
		 scrollbar = sb;
	 }

	 public void removeScrollBar()
	 {
		 scrollbar = null;
	 }

	 public void setParamDisplay( ModelTimePanel tl )
	 {
		 params_display = tl;
	 }

	 public void removeParamDisplay( ModelTimePanel tl )
	 {
		 params_display = null;
	 }

	 public void addTimeListener( TimeListener tl )
	 {
		 time_listener_list.add( TimeListener.class, tl );
	 }

	 public void removeTimeListener( TimeListener tl )
	 {
		 time_listener_list.remove( TimeListener.class, tl );
	 }

	 // Notify __ALL__ listeners that have registered interest for
	 // notification on this event type.  The event instance 
	 // is lazily created using the parameters passed into 
	 // the fire method.
	 protected void fireTimeChanged()
	 {
		 // Guaranteed to return a non-null array
		 Object[] listeners = time_listener_list.getListenerList();
		 // Process the listeners last to first, notifying
		 // those that are interested in this event
		 for ( int i = listeners.length-2; i>=0; i-=2 ) {
			 if ( listeners[i] == TimeListener.class ) {
				 // Lazily create the event:
				 if ( time_chg_evt == null )
					 time_chg_evt = new TimeEvent( this );
				 ( (TimeListener) listeners[i+1] ).timeChanged( time_chg_evt );
			 }
		 }
	 }

	 /*
       time2pixel() and pixel2time() are local to this object.
       They have nothing to do the ones in ScrollableObject
       (i.e. RulerTime/CanvasXXXXline).  In general, no one but
       this object and possibly ScrollbarTime needs to access
       the following time2pixel() and pixel2time() because the
       ratio to flip between pixel and time is related to scrollbar.
	  */
	 private int time2pixel( double time_coord )
	 {
		 return (int) Math.round( ( time_coord - tGlobal_min )
				 * getViewPixelsPerUnitTime() );
	 }

	 private double pixel2time( int pixel_coord )
	 {
		 return (double) pixel_coord / getViewPixelsPerUnitTime() + tGlobal_min;
	 }

	 private int timeRange2pixelRange( double time_range )
	 {
		 return (int) Math.round( time_range * getViewPixelsPerUnitTime() );
	 }

	 private double pixelRange2timeRange( int pixel_range )
	 {
		 return (double) pixel_range / getViewPixelsPerUnitTime();
	 }

	 public void updatePixelCoords()
	 {
		 // super.setRangeProperties() calls super.fireStateChanged();
		 super.setRangeProperties( time2pixel( tView_init ),
				 timeRange2pixelRange( tView_extent ),
				 time2pixel( tGlobal_min ),
				 time2pixel( tGlobal_max ),
				 super.getValueIsAdjusting() );
		 // fireTimeChanged();
	 }

	 public void updateTimeCoords()
	 {
			 tView_init     = pixel2time( super.getValue() );
			 tView_extent   = pixelRange2timeRange( super.getExtent() );
			 
			 fireTimeChanged();
	 }

	 public double getTimeZoomFactor()
	 {
		 return NORMAL_ZOOMFACTOR;
	 }

	 public double getTimeZoomFocus()
	 {
		 return tView_extent * 0.5 + tView_init;
	 }

	 /*
        Zoom Level
	  */
	 public double getZoomFaktor()
	 {
		 return getGlobalExtend() / tView_extent;
	 }

	 private void setScrollBarIncrements()
	 {
		 /*
            This needs to be called after updatePixelCoords()
		  */
		 int sb_block_incre, sb_unit_incre;
		 if ( scrollbar != null ) {
			 sb_block_incre = super.getExtent();
			 if ( sb_block_incre <= 0 ) {
				 Dialogs.error( root_window,
						 "You have reached the Zoom limit! "
						 + "Time ScrollBar has 0 BLOCK Increment. "
						 + "Zoom out or risk crashing the viewer." );
				 return;
			 }
			 scrollbar.setBlockIncrement( sb_block_incre );
			 sb_unit_incre  =  timeRange2pixelRange( tView_extent
					 * Parameters.TIME_SCROLL_UNIT_RATIO );
			 if ( sb_unit_incre <= 0 ) {
				 Dialogs.error( root_window,
						 "You have reached the Zoom limit! "
						 + "Time ScrollBar has 0 UNIT Increment. "
						 + "Zoom out or risk crashing the viewer." );
				 sb_unit_incre = 0;
				 return;
			 }
			 
			 scrollbar.setUnitIncrement( sb_unit_incre );
		 }
	 }

	 // tView_change is  the time measured in second.
	 public void scroll( double tView_change )
	 {
		 this.setTimeViewPosition( tView_init + tView_change );
		 this.updatePixelCoords();
		 // this.setScrollBarIncrements();
	 }

	 // iView_change is measured in image or viewport pixel coordinates in pixel.
	 // NOT measured in scrollbar's model, ie DefaultBoundRangeModel, coodinates
	 public void scroll( int iView_change )
	 {
		 double tView_change = (double) iView_change / getViewPixelsPerUnitTime();
		 this.scroll( tView_change );
	 }

	 // iView_change is measured in image or viewport pixel coordinates in pixel.
	 // The following function allows scroll pass tGlobal_min and tGlobal_max.
	 // In general, it is not desirable, so Avoid using this scroll() function.
	 public void scroll( int iView_change, boolean isValueAdjusting )
	 {
		 double tView_change = (double) iView_change / getViewPixelsPerUnitTime();
		 int iScrollbar_change = this.timeRange2pixelRange( tView_change );
		 super.setRangeProperties( super.getValue() + iScrollbar_change,
				 super.getExtent(),
				 super.getMinimum(),
				 super.getMaximum(),
				 isValueAdjusting );
		 tView_init     = pixel2time( super.getValue() );
		 
		 fireTimeChanged();
	 }



	 /*
        Zoom Operations
	  */
	 public void zoomHome()
	 {
		 pushCurrentStateOnZoomStackAndClean();	

		 this.setTimeViewPosition( tGlobal_min );
		 this.setTimeViewExtent( getGlobalExtend() );		 

		 this.updatePixelCoords();
		 this.setScrollBarIncrements();
	 }

	 private void pushCurrentStateOnZoomStackAndClean( )
	 {
		 TimeBoundingBox vport_timebox;
		 vport_timebox = new TimeBoundingBox();
		 vport_timebox.setEarliestTime( tView_init );
		 vport_timebox.setLatestFromEarliest( tView_extent );
		 zoom_undo_stack.push( vport_timebox );

		 // remove all stack from redo:
		 zoom_redo_stack.clear();
	 }
	 
	 public void zoomIn()
	 {
		 this.pushCurrentStateOnZoomStackAndClean();
		 
		 zoom(tView_init + tView_extent / NORMAL_ZOOMFACTOR / 2.0, tView_extent / NORMAL_ZOOMFACTOR);
	 }

	 public void zoomOut()
	 {
		 this.pushCurrentStateOnZoomStackAndClean();

		 // take special care if we are outside of possible scaling.
		 double extend = tView_extent * NORMAL_ZOOMFACTOR;
		 double offset = tView_init - tView_extent / NORMAL_ZOOMFACTOR / 2.0;
		 
		 if( offset < tGlobal_min) offset = tGlobal_min;
		 
		 if ( extend + offset > getGlobalExtend() ) extend = getGlobalExtend() - offset;
		 
		 zoom(offset, extend);		 
	 }

	 public void zoomRapidly( double new_tView_init, double new_tView_extent )
	 {
		 this.pushCurrentStateOnZoomStackAndClean();
		 zoom(new_tView_init, new_tView_extent);
	 }

	 private void zoom( double new_tView_init, double new_tView_extent )
	 {
		 if(new_tView_init < tGlobal_min){
			 new_tView_init = tGlobal_min; 
		 }
		 
		 if(new_tView_init >= tGlobal_max){
			 new_tView_init = tGlobal_min; 
		 }

		 if(new_tView_extent + new_tView_init > tGlobal_max + tGlobal_min){
			 new_tView_extent = tGlobal_max - new_tView_init;
		 }
		 
		 this.setTimeViewExtent( new_tView_extent );
		 this.setTimeViewPosition( new_tView_init );

		 this.updatePixelCoords();
		 this.setScrollBarIncrements();
		 this.updateTimeCoords();

		 this.fireTimeChanged();
	 }

	 public void zoomUndo()
	 {
		 if ( ! zoom_undo_stack.empty() ) {
			 TimeBoundingBox vport_timebox;

			 vport_timebox = new TimeBoundingBox();
			 vport_timebox.setEarliestTime( tView_init );
			 vport_timebox.setLatestFromEarliest( tView_extent );
			 zoom_redo_stack.push(vport_timebox);

			 vport_timebox = zoom_undo_stack.pop();
			 this.zoom( vport_timebox.getEarliestTime(), vport_timebox.getDuration() );			 
		 }
	 }

	 public void zoomRedo()
	 {
		 if ( ! zoom_redo_stack.empty() ) {			 
			 TimeBoundingBox vport_timebox;

			 vport_timebox = new TimeBoundingBox();
			 vport_timebox.setEarliestTime( tView_init );
			 vport_timebox.setLatestFromEarliest( tView_extent );
			 zoom_undo_stack.push(vport_timebox);

			 
			 vport_timebox = zoom_redo_stack.pop();
			 this.zoom( vport_timebox.getEarliestTime(), vport_timebox.getDuration() );
		 }
	 }

	 public boolean isZoomUndoStackEmpty()
	 {
		 return zoom_undo_stack.empty();
	 }

	 public boolean isZoomRedoStackEmpty()
	 {
		 return zoom_redo_stack.empty();
	 }


	 public void adjustmentValueChanged( AdjustmentEvent evt )
	 {
		 if ( Debug.isActive() ) {
			 Debug.println( "ModelTime: AdjustmentValueChanged()'s START: " );
			 Debug.println( "adj_evt = " + evt );
		 }

		 if ( Debug.isActive() )
			 Debug.println( "ModelTime(before) = " + this.toString() );
		 this.updateTimeCoords();
		 if ( Debug.isActive() )
			 Debug.println( "ModelTime(after) = " + this.toString() );

		 // notify all TimeListeners of changes from Adjustment Listener
		 this.fireTimeChanged();
		 if ( Debug.isActive() )
			 Debug.println( "ModelTime: AdjustmentValueChanged()'s END: " );
	 }

	 public String toString()
	 {
		 String str_rep = super.toString() + ",  "
		 + "tGlobal_min=" + tGlobal_min + ", "
		 + "tGlobal_max=" + tGlobal_max + ", "
		 + "tView_init=" + tView_init + ", "
		 + "tView_extent=" + tView_extent + ", "
		 ;

		 return getClass().getName() + "{" + str_rep + "}";
	 }
}
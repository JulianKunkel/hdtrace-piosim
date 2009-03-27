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

import viewer.common.Debug;
import viewer.common.Parameters;
import viewer.common.TimeEvent;
import viewer.common.TimeListener;
import de.hd.pvs.TraceFormat.util.Epoch;
import drawable.TimeBoundingBox;

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
public class ModelTime extends DefaultBoundedRangeModel implements AdjustmentListener
{
	private static final long serialVersionUID = -7160539618754681624L;

	private static double NORMAL_ZOOMFACTOR = 2 ;
	
	// user coordinates of the time axis of the viewport
	private Epoch tGlobal_min;
	private Epoch tGlobal_max;
	
	// current position:
	private double tView_init;
	private double tView_extent;

	
	// pixel coordinates of the time axis of the viewport
	// are buried inside the superclass DefaultBoundedRangeModel

	// screen properties
	private int iViewWidth;// No. of View pixel per unit time

	private JScrollBar         scrollbar;

	// special purpose ChangeListeners, TimeListeners, to avoid conflict with
	// the EventListenerList, listenerList, in DefaultBoundedRangeModel
	final private EventListenerList  time_listener_list = new EventListenerList();
	// internal global variable for use in fireTimeChanged()
	final private TimeEvent          time_chg_evt =  new TimeEvent( this );

	final private Stack<TimeBoundingBox> zoom_undo_stack = new Stack<TimeBoundingBox>();
	final private Stack<TimeBoundingBox> zoom_redo_stack = new Stack<TimeBoundingBox>();

	public ModelTime( final Window  top_window, Epoch  init_global_time, Epoch  final_global_time )
	{
		setTimeGlobalMinimum( init_global_time );
		setTimeGlobalMaximum( final_global_time );
	}
	
	/*
        None of the setTimeXXXXXX() functions updates the __Pixel__ coordinates
	 */
	 private void setTimeGlobalMinimum( Epoch init_global_time )
	 {
		 tGlobal_min    = init_global_time;
		 tView_init     = 0;
	 }

	 private void setTimeGlobalMaximum( Epoch final_global_time )
	 {
		 tGlobal_max    = final_global_time;
		 tView_extent   = getTimeGlobalDuration();
	 }
	 
	 /**
	  * The runtime of the traces/statistics
	  * @return
	  */
	 public double getTimeGlobalDuration(){
		 return tGlobal_max.subtract(tGlobal_min).getDouble();
	 }
	 
	 // tGlobal_min & tGlobal_max cannot be changed by setTimeViewPosition()
	 private void setTimeViewPosition( double cur_view_init )
	 {
		 if ( cur_view_init < 0 ){
			 tView_init     = 0;
		 }else {
			 if ( cur_view_init > getTimeGlobalDuration() - tView_extent )
				 tView_init     = getTimeGlobalDuration() - tView_extent;
			 else
				 tView_init     = cur_view_init;
		 }
	 }

	 // tGlobal_min & tGlobal_max cannot be changed by setTimeViewExtent()
	 private void setTimeViewExtent( double cur_view_extent )
	 {
		 if ( cur_view_extent < getTimeGlobalDuration() ) {
			 tView_extent   = cur_view_extent;
			 if ( tView_init  > getTimeGlobalDuration() - tView_extent ) {
				 tView_init      = getTimeGlobalDuration() - tView_extent;
			 }
		 }
		 else {
			 tView_extent   = getTimeGlobalDuration();
			 tView_init     = 0;
		 }
	 }

	 public Epoch getTimeGlobalMinimum()
	 {
		 return tGlobal_min;
	 }

	 public Epoch getTimeGlobalMaximum()
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
		 return (int) Math.round( time_coord * getViewPixelsPerUnitTime() );
	 }

	 private double pixel2time( int pixel_coord )
	 {
		 return (double) pixel_coord / getViewPixelsPerUnitTime();
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
		 super.setRangeProperties( time2pixel( tView_init ),
				 timeRange2pixelRange( tView_extent ), 0, time2pixel( getTimeGlobalDuration() ), super.getValueIsAdjusting() );
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
		 return getTimeGlobalDuration() / tView_extent;
	 }

	 private void setScrollBarIncrements() throws IllegalStateException
	 {
		 /*
            This needs to be called after updatePixelCoords()
		  */
		 int sb_block_incre, sb_unit_incre;
		 if ( scrollbar != null ) {
			 sb_block_incre = super.getExtent();
			 if ( sb_block_incre <= 0 ) {
				 throw new IllegalStateException(
						 "You have reached the Zoom limit! "
						 + "Time ScrollBar has 0 BLOCK Increment. "
						 + "Zoom out or risk crashing the viewer." );
			 }
			 scrollbar.setBlockIncrement( sb_block_incre );
			 sb_unit_incre  =  timeRange2pixelRange( tView_extent * Parameters.TIME_SCROLL_UNIT_RATIO );
			 if ( sb_unit_incre <= 0 ) {
				 throw new IllegalStateException( "You have reached the Zoom limit! "
						 + "Time ScrollBar has 0 UNIT Increment. "
						 + "Zoom out or risk crashing the viewer." );
			 }
			 
			 scrollbar.setUnitIncrement( sb_unit_incre );
		 }
	 }

	 // tView_change is  the time measured in second.
	 public void scroll( double tView_change )
	 {
		 this.setTimeViewPosition( tView_init + tView_change );
		 this.updatePixelCoords();
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
		 
		 this.setTimeViewPosition( 0 );
		 this.setTimeViewExtent( getTimeGlobalDuration() );		 

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
		 zoomIn(tView_init + tView_extent / NORMAL_ZOOMFACTOR / 2.0);
	 }

	 public void zoomOut(double offsetTime)
	 {
		 this.pushCurrentStateOnZoomStackAndClean();

		 // take special care if we are outside of possible scaling.
		 double extend = tView_extent * NORMAL_ZOOMFACTOR;
		 
		 if( offsetTime < 0) offsetTime = 0;
		 
		 if ( extend + offsetTime > getTimeGlobalDuration() ) extend = getTimeGlobalDuration() - offsetTime;
		 
		 zoom(offsetTime, extend);		
	 }
	 
	 public void zoomIn(double offsetTime)
	 {
		 final double oldtView_extent = tView_extent;
		 final double oldtView_init  = tView_init; 
		 
		 // do not permit a too deep zooming
		 try{
			 this.pushCurrentStateOnZoomStackAndClean();
		 
			 zoom(offsetTime, tView_extent / NORMAL_ZOOMFACTOR);
		 }catch(IllegalStateException e){
			 zoom(oldtView_init, oldtView_extent);
			 zoom_undo_stack.pop();
			 
			 //Dialogs.error( root_window,
			 return;
		 }	
	 }
	 
	 
	 public void zoomOut()
	 {
		 double offset = tView_init - tView_extent / NORMAL_ZOOMFACTOR / 2.0;
		 zoomOut(offset);
	 }

	 public void zoomRapidly( double new_tView_init, double new_tView_extent )
	 {	 
		 final double oldtView_extent = tView_extent;
		 final double oldtView_init  = tView_init; 
		 
		 // do not permit a too deep zooming
		 try{
			 this.pushCurrentStateOnZoomStackAndClean();

			 zoom(new_tView_init, new_tView_extent);
		 }catch(IllegalStateException e){
			 zoom(oldtView_init, oldtView_extent);
			 zoom_undo_stack.pop();
			 
			 //Dialogs.error( root_window,
			 return;
		 }
	 }

	 private void zoom( double new_tView_init, double new_tView_extent ) throws IllegalStateException
	 {
		 if(new_tView_init < 0){
			 new_tView_init = 0; 
		 }
		 
		 if(new_tView_init >= getTimeGlobalDuration()){
			 new_tView_init = 0; 
		 }

		 if(new_tView_extent + new_tView_init > getTimeGlobalDuration()){
			 new_tView_extent = getTimeGlobalDuration() - new_tView_init;
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

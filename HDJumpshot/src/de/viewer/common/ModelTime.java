
 /** Version Control Information $Id: ModelTime.java 374 2009-06-09 15:42:10Z kunkel $
  * @lastmodified    $Date: 2009-06-09 17:42:10 +0200 (Di, 09. Jun 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 374 $ 
  */

//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.


/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author Anthony Chan (Jumpshot 4), Julian M. Kunkel
 */

package de.viewer.common;


import java.util.LinkedList;
import java.util.Stack;

import de.drawable.TimeBoundingBox;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Class manages the current viewport (start, extend), global maximum, zoom operations and history.
 */
public class ModelTime 
{
	private static final long serialVersionUID = -7160539618754681624L;

	private static double NORMAL_ZOOMFACTOR = 2 ;
	
	// user coordinates of the time axis of the viewport
	private Epoch globalMin = Epoch.ZERO;
	private Epoch globalMax  = Epoch.ZERO;
	
	// the duration between GlobalMin/Max, derived value
	private double globalDuration;
	
	// current position:
	private double viewInit;
	private double viewExtent;

	// special purpose ChangeListeners, TimeListeners, to avoid conflict with
	// the EventListenerList, listenerList, in DefaultBoundedRangeModel
	final private LinkedList<TimeListener>  time_listener_list = new LinkedList<TimeListener>();
	
	// internal global variable for use in fireTimeChanged()
	final private TimeEvent          time_chg_evt =  new TimeEvent( this );

	final private LinkedList<TimeBoundingBox> zoom_undo_stack = new LinkedList<TimeBoundingBox>();
	final private Stack<TimeBoundingBox> zoom_redo_stack = new Stack<TimeBoundingBox>();

	boolean enableFireTimeUpdate = true;
	
	public ModelTime( Epoch  init_global_time, Epoch  final_global_time )
	{
		adjustGlobalTime(init_global_time, final_global_time);

		viewInit     = 0;
		viewExtent   = getGlobalDuration();
	}
	
	/*
        None of the setTimeXXXXXX() functions updates the __Pixel__ coordinates
	 */
	 public void setGlobalMinimum( Epoch init_global_time )
	 {
		 globalMin    = init_global_time;
		
		 globalDuration = globalMax.subtract(globalMin).getDouble();
	 }

	 public void setGlobalMaximum( Epoch final_global_time )
	 {
		 globalMax    = final_global_time;
		 
		 globalDuration = globalMax.subtract(globalMin).getDouble();
	 }
	 
	 /**
	  * The runtime of the traces/statistics
	  * @return
	  */
	 public double getGlobalDuration(){
		 return globalDuration;
	 }
	 
	 private void setViewPosition( double cur_view_init )
	 {
		 if ( cur_view_init < 0 ){
			 viewInit     = 0;
		 }else {
			 if ( cur_view_init > getGlobalDuration() - viewExtent )
				 viewInit     = getGlobalDuration() - viewExtent;
			 else
				 viewInit     = cur_view_init;
		 }
	 }

	 private void setViewExtent( double cur_view_extent )
	 {
		 if ( cur_view_extent < getGlobalDuration() ) {
			 viewExtent   = cur_view_extent;
			 if ( viewInit  > getGlobalDuration() - viewExtent ) {
				 viewInit      = getGlobalDuration() - viewExtent;
			 }
		 }
		 else {
			 viewExtent   = getGlobalDuration();
			 viewInit     = 0;
		 }
	 }

	 public Epoch getGlobalMinimum()
	 {
		 return globalMin;
	 }

	 public Epoch getGlobalMaximum()
	 {
		 return globalMax;
	 }
	 
	 public Epoch getViewPositionAdjusted()
	 {
		 return globalMin.add(viewInit);
	 }
	 
	 public Epoch getViewEndAdjusted(){
		 return globalMin.add(viewInit).add(viewExtent);
	 }	 
	 
	 public double getViewPosition()
	 {
		 return viewInit;
	 }
	 
	 public double getViewEnd(){
		 return viewInit + viewExtent;
	 }
	 
	 public double getViewExtent()
	 {
		 return viewExtent;
	 }
	 
	 public void addTimeListener( TimeListener tl )
	 {
		 time_listener_list.add( tl );
	 }

	 public void removeTimeListener( TimeListener tl )
	 {
		 time_listener_list.remove( tl );
	 }	

	 public void fireTimeChanged()
	 {
		 if(! enableFireTimeUpdate)
			 return;
		 for ( TimeListener listener: time_listener_list ) {
				 listener.timeChanged( time_chg_evt );
		 }
	 }

	 public double getTimeZoomFactor()
	 {
		 return NORMAL_ZOOMFACTOR;
	 }

	 public double getTimeZoomFocus()
	 {
		 return viewExtent * 0.5 + viewInit;
	 }

	 /*
        Zoom Level
	  */
	 public double getZoomFaktor()
	 {
		 return getGlobalDuration() / viewExtent;
	 }

	 /*
        Zoom Operations
	  */
	 public void zoomHome()
	 {
		 pushCurrentStateOnZoomStackAndClean();
		 
		 zoom(0, getGlobalDuration());		 
	 }
	 
	 public void zoomHomeWithoutStacking(){
		 zoom(0, getGlobalDuration());
	 }
	 
	 public void clearStacks(){
		 zoom_redo_stack.clear();
		 zoom_undo_stack.clear();
	 }

	 private void pushCurrentStateOnZoomStackAndClean( )
	 {
		 TimeBoundingBox vport_timebox;
		 vport_timebox = new TimeBoundingBox();
		 vport_timebox.setEarliestTime( viewInit );
		 vport_timebox.setLatestFromEarliest( viewExtent );
		 zoom_undo_stack.push( vport_timebox );
		 
		 // remove entry if too many already stacked otherwise unlimited
		 if(zoom_undo_stack.size() > 10)
			 zoom_undo_stack.pollLast();

		 // remove all stack from redo:
		 zoom_redo_stack.clear();
	 }
	 
	 public void zoomIn()
	 {
		 zoomIn(viewInit + viewExtent / NORMAL_ZOOMFACTOR / 2.0);
	 }

	 public void zoomOut(double offsetTime)
	 {
		 this.pushCurrentStateOnZoomStackAndClean();

		 // take special care if we are outside of possible scaling.
		 double extend = viewExtent * NORMAL_ZOOMFACTOR;
		 
		 if( offsetTime < 0) offsetTime = 0;
		 
		 if ( extend + offsetTime > getGlobalDuration() ) extend = getGlobalDuration() - offsetTime;
		 
		 zoom(offsetTime, extend);		
	 }
	 
	 public void zoomIn(double offsetTime)
	 {
		 final double oldtView_extent = viewExtent;
		 final double oldtView_init  = viewInit; 
		 
		 // do not permit a too deep zooming
		 try{
			 this.pushCurrentStateOnZoomStackAndClean();
		 
			 zoom(offsetTime, viewExtent / NORMAL_ZOOMFACTOR);
		 }catch(IllegalStateException e){
			 zoom(oldtView_init, oldtView_extent);
			 zoom_undo_stack.pop();
			 return;
		 }	
	 }
	 
	 
	 public void zoomOut()
	 {
		 double offset = viewInit - viewExtent / NORMAL_ZOOMFACTOR / 2.0;
		 zoomOut(offset);
	 }

	 public void zoomRapidly( double new_tView_init, double new_tView_extent )
	 {	 
		 final double oldtView_extent = viewExtent;
		 final double oldtView_init  = viewInit; 
		 
		 // do not permit a too deep zooming
		 try{
			 this.pushCurrentStateOnZoomStackAndClean();

			 zoom(new_tView_init, new_tView_extent);
		 }catch(IllegalStateException e){
			 zoom(oldtView_init, oldtView_extent);
			 zoom_undo_stack.pop();
			 return;
		 }
	 }
	 
	 public void zoomRapidlyWithoutStacking( double new_tView_init, double new_tView_extent )
	 {	 
		 final double oldtView_extent = viewExtent;
		 final double oldtView_init  = viewInit; 
		 
		 // do not permit a too deep zooming
		 try{
			 zoom(new_tView_init, new_tView_extent);
		 }catch(IllegalStateException e){
			 zoom(oldtView_init, oldtView_extent);			 
			 return;
		 }
	 }

	 /**
	  * Can be used internally, throws a IllegalStateException if a too deep zoom occurred.
	  *  
	  * @param new_tView_init
	  * @param new_tView_extent
	  * @throws IllegalStateException
	  */
	 public void zoomRapidlyInternal( double new_tView_init, double new_tView_extent ) throws IllegalStateException
	 {	 
		 zoom(new_tView_init, new_tView_extent);
	 }
	 
	 /**
	  * Scroll the time, i.e. move the view time if necessary.
	  * @param delta
	  */
	 public void scroll(double delta){
		 if(delta == 0)
			 return;
		 		 
		 double newTinit = viewInit + delta;
		 if(newTinit < 0){
			 if(viewInit == 0){ // already at right spot
				 return;
			 }
			 newTinit = 0;
		 }
		 
		 // check if we can still scroll right
		 final double extendToRight = globalDuration - newTinit;
		 if( extendToRight <= getViewExtent() ){
			 if( viewInit >= (globalDuration - viewExtent) ){
				 // we have already scrolled to the maximum
				 return;
			 }
			 // otherwise scroll to the maximum:
			 newTinit = (globalDuration - viewExtent);
		 }
		 
		 this.setViewPosition( newTinit );		 
		 this.fireTimeChanged();
	 }

	 private void zoom( double new_tView_init, double new_tView_extent ) throws IllegalStateException
	 {
		 if(new_tView_extent == viewExtent && new_tView_init == viewInit)
			 return;
		 
		 if(new_tView_init < 0){
			 new_tView_init = 0; 
		 }
		 
		 if(new_tView_init >= getGlobalDuration()){
			 new_tView_init = 0; 
		 }

		 if(new_tView_extent + new_tView_init > getGlobalDuration()){
			 new_tView_extent = getGlobalDuration() - new_tView_init;
		 }
		 
		 // allow a minimum view extend:
		 final double MINIMUM_VIEW_EXTEND = 0.000001;
		 if (new_tView_extent < MINIMUM_VIEW_EXTEND){
			 new_tView_extent = MINIMUM_VIEW_EXTEND;
			 if (new_tView_extent + new_tView_init > getGlobalDuration()){
				 new_tView_init = getGlobalDuration() - new_tView_extent;
			 }
		 }

		 // disable multiple triggers:		 
		 this.setViewExtent( new_tView_extent );
		 this.setViewPosition( new_tView_init );
		 
		 this.fireTimeChanged();
	 }

	 public void zoomUndo()
	 {
		 if ( ! zoom_undo_stack.isEmpty() ) {
			 TimeBoundingBox vport_timebox;

			 vport_timebox = new TimeBoundingBox();
			 vport_timebox.setEarliestTime( viewInit );
			 vport_timebox.setLatestFromEarliest( viewExtent );
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
			 vport_timebox.setEarliestTime( viewInit );
			 vport_timebox.setLatestFromEarliest( viewExtent );
			 zoom_undo_stack.push(vport_timebox);

			 
			 vport_timebox = zoom_redo_stack.pop();
			 this.zoom( vport_timebox.getEarliestTime(), vport_timebox.getDuration() );
		 }
	 }

	 public boolean isZoomUndoStackEmpty()
	 {
		 return zoom_undo_stack.isEmpty();
	 }

	 public boolean isZoomRedoStackEmpty()
	 {
		 return zoom_redo_stack.empty();
	 }

	 public String toString()
	 {
		 String str_rep = super.toString() + ",  "
		 + "tGlobal_min=" + globalMin + ", "
		 + "tGlobal_max=" + globalMax + ", "
		 + "tView_init=" + viewInit + ", "
		 + "tView_extent=" + viewExtent + ", "
		 ;

		 return getClass().getName() + "{" + str_rep + "}";
	 }

	public void adjustGlobalTime(Epoch globalMinTime, Epoch globalMaxTime) {
		setGlobalMinimum(globalMinTime);
		setGlobalMaximum(globalMaxTime);
		
		viewInit = 0;
		viewExtent = getGlobalDuration();
	}
	
	public void setEnableFireTimeUpdate(boolean enableFireTimeUpdate) {
		this.enableFireTimeUpdate = enableFireTimeUpdate;
	}
}


/** Version Control Information $Id: ModelTime.java 199 2009-04-09 22:47:15Z kunkel $
 * @lastmodified    $Date: 2009-04-10 00:47:15 +0200 (Fr, 10 Apr 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 199 $ 
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

package viewer.zoomable;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.DefaultBoundedRangeModel;

import viewer.common.Debug;
import viewer.common.ModelTime;
import viewer.common.Parameters;

/**
 * Model for the scrollbar.
 */
public class ScrollbarTimeModel extends DefaultBoundedRangeModel implements AdjustmentListener
{
	private static final long serialVersionUID = -7160539618754681624L;

	final private ModelTime modelTime;

	public ScrollbarTimeModel(ModelTime modelTime) {
		this.modelTime = modelTime;
	}	

	// screen properties
	private int iViewWidth;// No. of View pixel per unit time
	
	private int oldValue = 0;
	
	private boolean disableAdjustmentListener = false;

	void updateScrollRange(){		
		super.setRangeProperties( 
				time2pixel( modelTime.getViewPosition() ),
				time2pixel( modelTime.getViewExtent() ), 
				0, 
				time2pixel( modelTime.getGlobalDuration() ),
				super.getValueIsAdjusting() );
		
		oldValue = getValue();
	}
	
	/*
       Set the Number of Pixels in the Viewport window.
       if iView_width is NOT set, pixel coordinates cannot be updated.
	 */
	public void setViewWidth( int width )
	{
		iViewWidth = width;
		
		updateScrollRange();
	}


	public double getViewPixelsPerUnitTime(){
		return iViewWidth / modelTime.getViewExtent();
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

	/**
	 * Return the desired current scrollbar increment
	 */
	public int getScrollbarIncrement(){
		return  time2pixel(modelTime.getViewExtent() * Parameters.TIME_SCROLL_UNIT_RATIO); 
	}

	@Override
	public void adjustmentValueChanged( AdjustmentEvent evt )
	{
		if(disableAdjustmentListener)
			return;
		
		// if no value changed at all:
		if(super.getValue() - oldValue == 0)
			return;
		
		if ( Debug.isActive() ) {
			Debug.println( "ModelTime: AdjustmentValueChanged()'s START: " );
			Debug.println( "adj_evt = " + evt );
		}
		
		modelTime.scroll( pixel2time( super.getValue() - oldValue) );
		
		oldValue = super.getValue();

		// notify all TimeListeners of changes from Adjustment Listener
		//this.fireTimeChanged();
		if ( Debug.isActive() )
			Debug.println( "ModelTime: AdjustmentValueChanged()'s END: " );
	}
	
	public void setDisableAdjustmentListener(boolean disableAdjustmentListener) {
		this.disableAdjustmentListener = disableAdjustmentListener;
	}

	public ModelTime getModelTime() {
		return modelTime;
	}
}

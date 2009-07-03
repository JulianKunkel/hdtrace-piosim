
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

package de.viewer.zoomable;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.DefaultBoundedRangeModel;

import de.viewer.common.Debug;
import de.viewer.common.ModelTime;
import de.viewer.common.Parameters;


/**
 * Model for the time (X-axis) scrollbar. By default the maximum value is Integer.MAX_Value.
 * Internally the value gets converted to time coordinates.
 */
public class ScrollbarTimeModel extends DefaultBoundedRangeModel implements AdjustmentListener
{
	private static final long serialVersionUID = -7160539618754681624L;

	final private ModelTime modelTime;

	public ScrollbarTimeModel(ModelTime modelTime) {
		this.modelTime = modelTime;
		super.setRangeProperties(0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE, super.getValueIsAdjusting());
	}	

	private int oldValue = 0;
	
	private boolean disableAdjustmentListener = false;

	private int convertTimeToScrollbarValue(double time){
		return (int)( time / modelTime.getGlobalDuration() * Integer.MAX_VALUE);
	}
	
	private double convertScrollbarValueToTime(int val){
		return modelTime.getGlobalDuration() / Integer.MAX_VALUE * val;
	}
	
	void updateScrollPosition(){
		int pos = convertTimeToScrollbarValue(modelTime.getViewPosition());
		int extend = convertTimeToScrollbarValue(modelTime.getViewExtent());
		
		if(extend <= 0){
			System.err.println("WARNING: scrollbar extend is getting invalid!" + extend);
		}	
		
		super.setRangeProperties(pos, extend, 0, Integer.MAX_VALUE, super.getValueIsAdjusting());
		
		oldValue = getValue();
	}
	
	/**
	 * Return the desired current scrollbar increment
	 */
	public int getScrollbarIncrement(){
		int scrollbarIncr =  (int) (convertTimeToScrollbarValue(modelTime.getViewExtent()) * Parameters.TIME_SCROLL_UNIT_RATIO);
		if(scrollbarIncr <= 0){
			System.err.println("WARNING: scrollbar incr is getting invalid!" + scrollbarIncr);
		}
		return scrollbarIncr;
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
		
		modelTime.scroll( convertScrollbarValueToTime( super.getValue() - oldValue) );
		
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

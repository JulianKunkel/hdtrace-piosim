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
 * ModelInfo
 */

/*
 *  @author  Frank Panse
 *  @organization PVS University Heidelberg
 */

package viewer.zoomable;

import hdTraceInput.TraceFormatBufferedFileReader;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import viewer.common.Const;
import viewer.common.TimeFormat;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticGroupEntry;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import drawable.Category;


public class ModelInfo 
{

	private static final String         FORMAT = Const.INFOBOX_TIME_FORMAT;
	private static       DecimalFormat  fmt    = null;
	private static       TimeFormat     tfmt   = null;
	private ModelInfoPanel     params_display = null;
	final private TraceFormatBufferedFileReader reader;

	public ModelInfo(TraceFormatBufferedFileReader reader) {
		this.reader = reader;
		if ( fmt == null ) {
			fmt = (DecimalFormat) NumberFormat.getInstance();
			fmt.applyPattern( FORMAT );
		}
		if ( tfmt == null )
			tfmt = new TimeFormat();
	}  

	public void showInfo( StatisticEntry statistic ) {
		reset();		
		
		StatisticGroupEntry groupEntry = statistic.getParentGroupEntry();
		StatisticDescription desc = statistic.getDescription();
		final double time = reader.subtractGlobalMinTimeOffset(groupEntry.getEarliestTime());
		
		this.setStartTime( "" );
		this.setEndTime( "" + time);

		
		this.setDuration( fmt.format( 0 ) );
		final Category cat = reader.getCategory(groupEntry.getGroup(), desc.getName()); 
		this.setCategoryName( desc.getName() );    
		this.setCategoryColor( (Color) cat.getColor() );
		
		if(desc.getUnit() != null)
			this.setInfoString(statistic.getValue().toString() + " " + desc.getUnit());
		else
			this.setInfoString(statistic.getValue().toString());
	}


	public void showInfo( StateTraceEntry state ) {
		reset();
		
		this.setStartTime( "" + reader.subtractGlobalMinTimeOffset(state.getEarliestTime())  );
		this.setEndTime( "" + reader.subtractGlobalMinTimeOffset(state.getLatestTime()) );

		this.setDuration( fmt.format( state.getDurationTimeDouble() ) );
		final Category cat = reader.getCategory(state); 
		this.setCategoryName( cat.getName() );    
		this.setCategoryColor( (Color) cat.getColor() );
	}

	public void showInfo( EventTraceEntry event ) {
		reset();
		
		final double time = reader.subtractGlobalMinTimeOffset(event.getEarliestTime());
		
		this.setStartTime( "" + time  );
		this.setEndTime( "" + time );

		this.setDuration( fmt.format( 0 ) );
		final Category cat = reader.getCategory(event); 
		this.setCategoryName( cat.getName() );    
		this.setCategoryColor( (Color) cat.getColor() );
	}


	public void reset() {
		params_display.reset();    
	}


	public void setTime(String start, String end) {
		params_display.setStartTime(start);
		params_display.setEndTime(end);
	}

	public void setStartTime(String time) {
		params_display.setStartTime(time);
	}

	public void setEndTime(String time) {
		params_display.setEndTime(time);
	}      

	public void setInfoString(String info) {
		params_display.setInfoString(info);
	}      

	public void setCategoryName(String type) {
		params_display.setCategoryName(type);
	}                  

	public void setCategoryColor(Color color) {
		params_display.setCategoryColor(color);
	}        

	public void setDuration(String duration) {
		params_display.setDuration(duration);

	}

	public void setParamDisplay( ModelInfoPanel tl )
	{
		params_display = tl;
	}

	public void removeParamDisplay()
	{
		params_display = null;
	}

	public String toString()
	{
		String str_rep =  "" ;
		return getClass().getName() + "{" + str_rep + "}";
	}
}

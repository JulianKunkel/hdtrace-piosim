
//	Copyright (C) 2008, 2009 Julian M. Kunkel
//	
//	This file is part of PIOsimHD.
//	
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.TraceFormat.statistics;

import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Read values from a statistic file, aka Statistic Group. 
 * 
 * @author Julian M. Kunkel
 *
 */
public class StatisticGroupEntry implements TraceObject{
	
	private final ExternalStatisticsGroup group;
	
	/**
	 * Maps the statistic name to the measured value.
	 */
	private final Object [] values;
	
	private final Epoch timeStamp; 
		
	public StatisticGroupEntry(Object [] values, Epoch timeStamp, ExternalStatisticsGroup group) {
		this.group = group;
		this.values = values;
		this.timeStamp = timeStamp;
	}
	
	public Object[] getValues() {
		return values;
	}
	
	/**
	 * Return a number as double
	 * @param which, the number of the value
	 * @return
	 */
	public double getNumeric(int which){
		if(Number.class.isAssignableFrom(values[which].getClass())){
			return ((Number) values[which]).doubleValue();
		}
		return Double.NaN;
	}
	
	public StatisticEntry createStatisticEntry(int which){
		return new StatisticEntry(values[which], group.getStatisticsOrdered().get(which) , this);
	}
	
	public Epoch getTimeStamp() {
		return timeStamp;
	}
	
	@Override
	final public TraceObjectType getType() {		
		return TraceObjectType.STATISTICGROUPVALUES;
	}
	
	public ExternalStatisticsGroup getGroup() {
		return group;
	}
}

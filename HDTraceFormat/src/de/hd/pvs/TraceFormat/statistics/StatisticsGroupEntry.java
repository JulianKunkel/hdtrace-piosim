
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
  */


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

import de.hd.pvs.TraceFormat.ITracableObject;
import de.hd.pvs.TraceFormat.TracableObjectType;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Contains information for one statistics group and one interval.
 * 
 * @author Julian M. Kunkel
 *
 */
public class StatisticsGroupEntry implements ITracableObject{
	
	/**
	 * The group the entry belongs to.
	 */
	private final StatisticsGroupDescription group;
	
	/**
	 * Array, which maps the statistics group entry to the actual data value.
	 */
	private final Object [] values;
	
	/**
	 * Start time of the interval
	 */
	private final Epoch startTime;
	
	/**
	 * End time of the interval
	 */
	private final Epoch endTime;
	 
	public StatisticsGroupEntry(Object [] values, Epoch startTime, Epoch endTime, StatisticsGroupDescription group) {
		this.group = group;
		this.values = values;
		this.endTime = endTime;
		this.startTime = startTime;
	}
	
	public Object[] getValues() {
		return values;
	}
	
	/**
	 * Return the double value for a given statistics in the group 
	 * @param which, the number of the value
	 * @return
	 */
	public double getNumeric(int which){
		if(Number.class.isAssignableFrom(values[which].getClass())){
			if(((Number) values[which]).doubleValue() == Double.NaN){
				System.out.println("GOD");
			}
			return ((Number) values[which]).doubleValue();
		}
		return Double.NaN;
	}
	
	/**
	 * Create an object for a single statistics contained in this group entry.  
	 */
	public StatisticsEntry createStatisticEntry(int which){
		return new StatisticsEntry(values[which], group.getStatisticsOrdered().get(which) , this);
	}
	
	public Epoch getEarliestTime() {
		return startTime;
	}
	
	@Override
	public Epoch getLatestTime() {
		return endTime;
	}
	
	@Override
	final public TracableObjectType getType() {		
		return TracableObjectType.STATISTICGROUPVALUES;
	}
	
	public StatisticsGroupDescription getGroup() {
		return group;
	}
	
	@Override
	final public Epoch getDurationTime() {
		return getLatestTime().subtract(getEarliestTime());
	}
}

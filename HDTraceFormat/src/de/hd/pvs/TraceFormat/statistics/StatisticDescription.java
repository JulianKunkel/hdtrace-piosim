
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

/**
 * 
 */
package de.hd.pvs.TraceFormat.statistics;


/**
 * Describes a single statistic of a group.
 * @author julian
 */
public class StatisticDescription{
	final StatisticsEntryType type;
	final String name;
	
	final String unit;
	/**
	 * Several statistics of a group might be grouped by function, for instance CPU utilization
	 */
	final String grouping;
	final int multiplier;
	
	final int numberInGroup;
	
	final StatisticsGroupDescription group;
	
	public StatisticDescription(StatisticsGroupDescription group, String name, StatisticsEntryType type, int numberInGroup, String unit, int multiplier, String grouping) {
		this.name = name;
		this.type = type;
		this.multiplier = multiplier;
		this.unit = unit;
		
		if( grouping == null || grouping.length() == 0){
			this.grouping = null;
		}else{
			this.grouping = grouping;
		}
		
		this.numberInGroup = numberInGroup;
		this.group = group;
	}
	
	public String getName() {
		return name;
	}
	
	public StatisticsEntryType getType() {
		return type;
	}
	
	public int getMultiplier() {
		return multiplier;
	}
	
	public String getUnit() {
		return unit;
	}
	
	/**
	 * Retuns true if this object is numeric
	 * @return
	 */
	public boolean isNumeric() {
		return ! type.equals(StatisticsEntryType.STRING);
	}
	
	/**
	 * Return the number/position this statistic has in the group
	 * @return
	 */
	public int getNumberInGroup() {
		return numberInGroup;
	}
	
	public StatisticsGroupDescription getGroup() {
		return group;
	}
	
	public String getGrouping() {
		return grouping;
	}
	
	// two stat descriptions are considered to be equal if the name and group name is equal 
	@Override
	public boolean equals(Object obj) {	
		return ((StatisticDescription) obj).getName().equals(getName()) && ((StatisticDescription) obj).getGroup().getName().equals(getGroup().getName());
	}
}
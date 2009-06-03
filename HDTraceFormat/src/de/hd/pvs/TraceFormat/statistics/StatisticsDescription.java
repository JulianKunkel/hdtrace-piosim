
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
public class StatisticsDescription{

	final String name;

	/**
	 * Type of the statistics
	 */
	final StatisticsEntryType datatype;	

	/**
	 * Unit of the statistics
	 */
	final String unit;

	/**
	 * Several statistics of a group might be grouped by function, for instance CPU utilization
	 */
	final String grouping;

	/**
	 * Group this statistics belongs to
	 */
	final StatisticsGroupDescription group;

	/**
	 * Number / position of the statistics in the group i.e. how many other statistics are before
	 * this one in the group 
	 */
	final int numberInGroup;	

	/**
	 * Create a new statistics in a particular group
	 * @param group The group this description belongs to 
	 * @param name The name of the description (must be unique in a group)
	 * @param datatype The datatype of the statistics entries
	 * @param numberInGroup The number this description has in the group
	 * @param unit The Unit of the statistics
	 * @param grouping The grouping this statistics belongs to, values in a group with the same grouping
	 * 	are considered to contain related information   
	 */
	public StatisticsDescription(StatisticsGroupDescription group, String name, StatisticsEntryType datatype, int numberInGroup, String unit, String grouping) {
		this.name = name;
		this.datatype = datatype;
		this.unit = unit;

		if( grouping == null || grouping.length() == 0){
			this.grouping = "";
		}else{
			this.grouping = grouping;
		}

		this.numberInGroup = numberInGroup;
		this.group = group;
	}

	public String getName() {
		return name;
	}

	public StatisticsEntryType getDatatype() {
		return datatype;
	}

	public String getUnit() {
		return unit;
	}

	/**
	 * Retuns true if this object is numeric
	 * @return
	 */
	public boolean isNumeric() {
		return ! datatype.equals(StatisticsEntryType.STRING);
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

	/**
	 * Get the relation this statistics has to other statistics in the given group.
	 * For instance values grouped by "Network" indicates that these values are related.
	 * Therefore, the grouping provides additional semantics. 
	 * @return
	 */
	public String getGrouping() {
		return grouping;
	}

	// two stat descriptions are considered to be equal if the name and group name is equal 
	@Override
	public boolean equals(Object obj) {	
		return ((StatisticsDescription) obj).getName().equals(getName()) && ((StatisticsDescription) obj).getGroup().getName().equals(getGroup().getName());
	}
}
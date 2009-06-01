
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

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Describes a group of statistics, i.e. a set of statistics which have one timestamp 
 * and exactly one values of all contained statistics per interval.
 *    
 * @author julian
 */
public class StatisticsGroupDescription{

	/**
	 * Describes the format of the Timestamp.
	 */
	StatisticsEntryType timestampDatatype = StatisticsEntryType.EPOCH;

	/**
	 * Multiplies the read value with this value.
	 */
	int timeResolutionMultiplier = 1;

	String timeResolutionMultiplierName = null;
	
	/**
	 * Offset applied to all timestamps
	 */
	Epoch timeAdjustment = Epoch.ZERO;

	/**
	 * Name of this statistic group
	 */
	final String groupName;

	// maps contained statistics to type
	final ArrayList<StatisticsDescription>      statisticOrder;

	public StatisticsGroupDescription(String name) {
		statisticOrder = new ArrayList<StatisticsDescription>();
		this.groupName = name;		
	}

	/**
	 * Add a statistic description, the order defines the order in which it is stored in the binary file.
	 */
	public void addStatistic(StatisticsDescription desc){	
		statisticOrder.add(desc);
	}

	public ArrayList<StatisticsDescription> getStatisticsOrdered() {
		return statisticOrder;
	}
	
	public String getName() {
		return groupName;
	}

	public int getSize(){
		return statisticOrder.size();
	}
	
	public void setTimeResolutionMultiplier(String name) {
		if(name.equals("Mikroseconds")){
			timeResolutionMultiplier = 1000;
		}else if(name.equals("Milliseconds")){
			timeResolutionMultiplier = 1000 * 1000;
		}else if(name == null){
			timeResolutionMultiplier = 1;
		}else{
			throw new IllegalArgumentException("Invalid timestampResulution " + name );
		}
		this.timeResolutionMultiplierName = name;
	}

	public void setTimestampDatatype(StatisticsEntryType timestampDatatype) {
		this.timestampDatatype = timestampDatatype;
	}

	public void setTimeAdjustment(Epoch timeAdjustment) {
		this.timeAdjustment = timeAdjustment;
	}

	public int getTimeResolutionMultiplier() {
		return timeResolutionMultiplier;
	}
	
	public String getTimeResolutionMultiplierName() {
		return timeResolutionMultiplierName;
	}

	public StatisticsEntryType getTimestampDatatype() {
		return timestampDatatype;
	}

	public Epoch getTimeAdjustment() {
		return timeAdjustment;
	}

	/**
	 * Two statistic group descriptions are considered to be equal if the name equals. 
	 */
	@Override
	public boolean equals(Object obj) {
		return groupName.equals(((StatisticsGroupDescription) obj).groupName);
	}
	
	@Override
	public int hashCode() {	
		return groupName.hashCode();
	}
}

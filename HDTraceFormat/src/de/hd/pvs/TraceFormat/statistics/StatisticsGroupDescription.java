
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
import java.util.Collection;
import java.util.HashMap;

import de.hd.pvs.TraceFormat.util.Epoch;

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
	Epoch timeOffset = Epoch.ZERO;

	/**
	 * Name of this statistic group
	 */
	String groupName;

	// maps contained statistics to type
	final HashMap<String, StatisticDescription> statisticTypeMap;
	final ArrayList<StatisticDescription>      statisticOrder;

	public StatisticsGroupDescription() {
		statisticTypeMap = new HashMap<String, StatisticDescription>();
		statisticOrder = new ArrayList<StatisticDescription>();
	}

	public StatisticsGroupDescription(ArrayList<StatisticDescription> statisticOrder,
			HashMap<String, StatisticDescription> statisticTypeMap){
		this.statisticOrder = statisticOrder;
		this.statisticTypeMap = statisticTypeMap;
	}

	/**
	 * Add a statistic description, the order defines the order in which it is stored in the binary file.
	 */
	public void addStatistic(StatisticDescription desc){		
		statisticTypeMap.put(desc.getName(), desc);
		statisticOrder.add(desc);
	}

	public Collection<String> getStatistics() {
		return statisticTypeMap.keySet();
	}

	public StatisticsEntryType getType(String statistic){
		return statisticTypeMap.get(statistic).type;
	}

	public StatisticDescription getStatistic(String stat){
		return statisticTypeMap.get(stat);
	}

	public ArrayList<StatisticDescription> getStatisticsOrdered() {
		return statisticOrder;
	}
	
	public HashMap<String, StatisticDescription> getStatisticTypeMap() {
		return statisticTypeMap;
	}

	public void setName(String name) {
		this.groupName = name;
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

	public void setTimeOffset(Epoch timeOffset) {
		this.timeOffset = timeOffset;
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

	public Epoch getTimeOffset() {
		return timeOffset;
	}

}

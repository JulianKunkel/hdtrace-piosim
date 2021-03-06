
 /** Version Control Information $Id: GlobalStatisticStatsPerGroup.java 325 2009-06-01 15:42:47Z kunkel $
  * @lastmodified    $Date: 2009-06-01 17:42:47 +0200 (Mo, 01. Jun 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 325 $ 
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


package de.topology;

import java.util.HashMap;

import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;

/**
 * Stores global values per statistic group, i.e. the global max value, the max value per file,
 * Std-dev etc.
 * 
 * @author Julian M. Kunkel
 */
public class GlobalStatisticStatsPerGroup {
	
	/**
	 * The group we are storing stats for:
	 */
	final StatisticsGroupDescription group;
	
	/**
	 * Individual statistics of each statistic
	 */
	final HashMap<StatisticsDescription, GlobalStatisticsPerStatistic> statsPerStatistic = new HashMap<StatisticsDescription, GlobalStatisticsPerStatistic>();
	
	/**
	 * Individual statistics of each statistic grouping (if more than one statstic per grouping)
	 * A grouping might contain multiple statistics of this group
	 */
	final HashMap<String, MinMax> groupingStatistic = new HashMap<String, MinMax>();
	
	public GlobalStatisticStatsPerGroup(StatisticsGroupDescription group) {
		this.group = group;
	}
	
	public GlobalStatisticsPerStatistic getStatsForStatistic(StatisticsDescription desc) {
		return statsPerStatistic.get(desc);
	}
	
	public MinMax getStatsForStatisticGrouping(String grouping){
		return groupingStatistic.get(grouping);
	}
	
	public void setStatsForStatistic(StatisticsDescription desc, GlobalStatisticsPerStatistic stats) {
		if(statsPerStatistic.containsKey(desc)){
			throw new IllegalArgumentException("Already contained statistic: " + desc);
		}
		statsPerStatistic.put(desc, stats);
	}
	
	public void setStatsForGrouping(String grouping, MinMax stats) {
		groupingStatistic.put(grouping, stats);
	}
}

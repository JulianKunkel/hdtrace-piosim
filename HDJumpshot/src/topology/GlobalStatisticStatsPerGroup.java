
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
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


package topology;

import java.util.HashMap;

import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;

/**
 * Stores global values per statistic group, i.e. the global max value, the max value per file,
 * Std-dev etc.
 * 
 * @author Julian M. Kunkel
 */
public class GlobalStatisticStatsPerGroup {
	
	/**
	 * Statistics for a single statistic 
	 */
	public static class GlobalStatisticsPerStatistic{
		
		private final StatisticDescription statisticDescription;
		
		private double maxValue = Double.MIN_NORMAL;
		private double minValue = Double.MAX_VALUE;
		
		public GlobalStatisticsPerStatistic(StatisticDescription statisticDescription) {
			this.statisticDescription = statisticDescription;
		}
		
		public void setGlobalMaxValue(double globalMaxValue) {
			this.maxValue = globalMaxValue;
		}

		public void setGlobalMinValue(double globalMinValue) {
			this.minValue = globalMinValue;
		}

		public double getGlobalMaxValue() {
			return maxValue;
		}

		public double getGlobalMinValue() {
			return minValue;
		}
		
		public StatisticDescription getStatisticDescription() {
			return statisticDescription;
		}
	}
	
	/**
	 * The group we are storing stats for:
	 */
	final StatisticsGroupDescription group;
	
	/**
	 * Individual statistics of each statistic
	 */
	final HashMap<String, GlobalStatisticsPerStatistic> statsPerStatistic = new HashMap<String, GlobalStatisticsPerStatistic>();
	
	public GlobalStatisticStatsPerGroup(StatisticsGroupDescription group) {
		this.group = group;
	}
	
	public GlobalStatisticsPerStatistic getStatsForStatistic(String name) {
		return statsPerStatistic.get(name);
	}
	
	public void setStatsForStatistic(String name, GlobalStatisticsPerStatistic stats) {
		statsPerStatistic.put(name, stats);
	}
}

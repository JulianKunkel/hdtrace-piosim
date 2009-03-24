package viewer.topology;

import java.util.HashMap;

import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;

/**
 * Stores global values per statistic group, i.e. the global max value, the max value per file,
 * Std-dev etc.
 * 
 * @author julian
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
	final ExternalStatisticsGroup group;
	
	/**
	 * Individual statistics of each statistic
	 */
	final HashMap<String, GlobalStatisticsPerStatistic> statsPerStatistic = new HashMap<String, GlobalStatisticsPerStatistic>();
	
	public GlobalStatisticStatsPerGroup(ExternalStatisticsGroup group) {
		this.group = group;
	}
	
	public GlobalStatisticsPerStatistic getStatsForStatistic(String name) {
		return statsPerStatistic.get(name);
	}
	
	public void setStatsForStatistic(String name, GlobalStatisticsPerStatistic stats) {
		statsPerStatistic.put(name, stats);
	}
}

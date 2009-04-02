package hdTraceInput;

/**
 * Statistics for a single statistic
 * @author julian
 *
 */
public class StatisticStatistics {
	final double maxValue;
	final double minValue;
	final double averageValue;
	final double stddevValue;
	
	public StatisticStatistics( double maxNumericValue, double minNumericValue, double averageNumericValue, double stddev) {
		this.maxValue = maxNumericValue;
		this.minValue = minNumericValue;
		this.averageValue = averageNumericValue;
		this.stddevValue = stddev;
	}	
	
	public double getAverageValue() {
		return averageValue;
	}
	
	public double getMaxValue() {
		return maxValue;
	}
	
	public double getMinValue() {
		return minValue;
	}
	
	public double getStddevValue() {
		return stddevValue;
	}
}

package hdTraceInput;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Enumeration;

import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticGroupEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Compute statistical information for statistics. 
 * 
 * @author julian
 */
public class StatisticsComputer {
	
	/**
	 * Compute statistics for a particular statistic within a time interval.
	 * @param reader
	 * @param description
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	static public StatisticStatistics computeStatistics(BufferedStatisticFileReader reader, 
			StatisticDescription description, Epoch startTime, Epoch endTime){

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;

		// use bigdecimal to increase accuracy.			
		BigDecimal sum = new BigDecimal(0);			
		BigDecimal integratedSum = new BigDecimal(0);

		int cnt = 0;

		final int groupNumber = description.getNumberInGroup();

		Enumeration<StatisticGroupEntry> entryEnum = reader.enumerateStatistics(startTime, endTime);

		while(entryEnum.hasMoreElements()){
			final StatisticGroupEntry entry = entryEnum.nextElement();
			double value = entry.getNumeric(groupNumber);

			if( value > max ) max = value;
			if( value < min ) min = value;

			sum = sum.add(new BigDecimal(value) );  

			integratedSum = integratedSum.add(
					new BigDecimal(value).multiply(
							entry.getLatestTime().subtract(entry.getEarliestTime()).getBigDecimal())	);
			
			cnt++;
		}

		// second iteration
		BigDecimal stddev = new BigDecimal(0);
		BigDecimal avg = stddev;
		double stddevd = 0;
		
		if (cnt > 0){
			entryEnum = reader.enumerateStatistics(startTime, endTime);

			avg = sum.divide(new BigDecimal(cnt), BigDecimal.ROUND_HALF_EVEN);

			while(entryEnum.hasMoreElements()){
				final StatisticGroupEntry entry = entryEnum.nextElement();
				double value = entry.getNumeric(groupNumber);

				final BigDecimal multiplier = new BigDecimal(value).subtract(avg);
				stddev = stddev.add(multiplier.multiply(multiplier));				
			}
			
			if( cnt > 1){
				stddevd = Math.sqrt(stddev.divide( new BigDecimal (cnt - 1), RoundingMode.HALF_DOWN ).doubleValue());
			}
		}		

		return  new StatisticStatistics(max, min, avg.doubleValue(), stddevd, sum, integratedSum);
	}
}

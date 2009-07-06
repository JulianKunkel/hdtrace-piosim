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

package de.hdTraceInput;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Enumeration;

import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Compute statistical information for statistics. 
 * 
 * @author Julian M. Kunkel
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
	static public StatisticStatistics computeStatistics(BufferedStatisticsFileReader reader, 
			StatisticsDescription description, Epoch startTime, Epoch endTime){

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;

		// use bigdecimal to increase accuracy.			
		BigDecimal sum = new BigDecimal(0);			
		BigDecimal integratedSum = new BigDecimal(0);

		int cnt = 0;

		final int groupNumber = description.getNumberInGroup();

		Enumeration<StatisticsGroupEntry> entryEnum = reader.enumerateStatistics(startTime, endTime);

		while(entryEnum.hasMoreElements()){
			final StatisticsGroupEntry entry = entryEnum.nextElement();
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
				final StatisticsGroupEntry entry = entryEnum.nextElement();
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
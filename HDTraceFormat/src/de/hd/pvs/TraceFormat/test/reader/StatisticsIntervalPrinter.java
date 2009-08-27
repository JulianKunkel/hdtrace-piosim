//	Copyright (C) 2009 Julian M. Kunkel
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

package de.hd.pvs.TraceFormat.test.reader;

import java.math.BigDecimal;

import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsReader;


/**
 * This (test) program reads one statistic file and prints the average, min & max duration of the intervals
 * 
 * @author Julian M. Kunkel
 *
 */
public class StatisticsIntervalPrinter {
	public static void main(String[] args) throws Exception{
		final String filename;
		if(args.length > 0){
			filename = args[0];
		}else{
			filename = "/tmp/test_node01_Performance.stat";
		}
		
		System.out.println("Reader " + filename + " (override by providing a filename as parameter)");
		
		StatisticsReader reader = new StatisticsReader(filename); 
		
		System.out.println("Position in file:" + reader.getFilePosition());
		StatisticsGroupEntry entry = reader.getNextInputEntry();
		final StatisticsGroupDescription group = reader.getGroup();
		
		BigDecimal aggregatedIntervalLength = new BigDecimal(0);
		int valueCount = 0;
		
		double maxDuration = 0;
		double minDuration = Double.MAX_VALUE;
		
		while(entry != null){
			final double duration = entry.getLatestTime().subtract(entry.getEarliestTime()).getDouble();
			
			valueCount++;
			
			aggregatedIntervalLength = aggregatedIntervalLength.add(new BigDecimal(duration));
			
			minDuration = minDuration < duration ? minDuration : duration;
			maxDuration = maxDuration > duration ? maxDuration : duration;
			
			entry = reader.getNextInputEntry();
		}
		
		System.out.println("Total " + valueCount + " entries min/max avg: " + minDuration + "/" + maxDuration + " " + aggregatedIntervalLength.divide(new BigDecimal(valueCount), BigDecimal.ROUND_HALF_UP) );
	}
}

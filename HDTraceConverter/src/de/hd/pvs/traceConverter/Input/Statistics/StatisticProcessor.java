
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

package de.hd.pvs.traceConverter.Input.Statistics;

import java.io.IOException;
import java.util.HashMap;

import de.hd.pvs.TraceFormat.SimpleConsoleLogger;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsReader;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.traceConverter.Input.AbstractTraceProcessor;

/**
 * Processes one group of statistics for exactly one ProcessIdentifier.
 * 
 * @author Julian M. Kunkel
 */
public class StatisticProcessor  extends AbstractTraceProcessor{
	private final StatisticsReader reader;

	private boolean isFinished;

	private StatisticGroupEntry lastRead;
	private long           currentOffset = 0;

	private Epoch nextTimeStamp;

	private StatisticsGroupDescription group;

	private static class StatisticWritten{
		Object lastValue;		

		int    numberOfValues = 0;		
		Number sum = new Integer(0); //Maybe numerical problems for large number of values? TODO

		public StatisticWritten(Object lastValue) {
			this.lastValue = lastValue;
		}
	}

	/**
	 * Record the last read value of a given statistic
	 */
	HashMap<String, StatisticWritten> lastUpdatedStatistic = new HashMap<String, StatisticWritten>();

	private void getNextStatistic() throws Exception{
		if(! isFinished){
			currentOffset = reader.getFilePosition();
			lastRead = reader.getNextInputEntry();
			if(! reader.isFinished()){
				nextTimeStamp = lastRead.getEarliestTime();
			}
		}
	}

	@Override
	public long getFilePosition() throws IOException {
		return currentOffset;
	}

	public StatisticProcessor(StatisticsReader reader) throws Exception{
		this.reader = reader;
	}

	@Override
	public void processEarliestEvent(Epoch now) {
		isFinished = reader.isFinished();

		for(int pos = 0; pos < group.getSize() ; pos ++){
			Object val = lastRead.getValues()[pos];
			final String stat = group.getStatisticsOrdered().get(pos).getName();

			if(getRunParameters().isUpdateStatisticsOnlyIfTheyChangeTooMuch()){
				// check if the statistic changed enough from the last written stamp.

				StatisticWritten lastWritten = lastUpdatedStatistic.get(stat); 

				if(lastWritten != null){
					if(val.equals(lastWritten.lastValue)){
						continue;						
					}else if(Number.class.isAssignableFrom(val.getClass())){
						// check new and old value:
						Number lastNumber = (Number) lastWritten.lastValue;
						Number newNumber = (Number) val;

						Class<Number> cls = (Class<Number>) newNumber.getClass();

						lastWritten.numberOfValues++;

						if(cls.equals(Float.class)){
							lastWritten.sum = new Double(newNumber.doubleValue() + lastWritten.sum.doubleValue());
						}else if(cls.equals(Double.class)){
							lastWritten.sum = new Double(newNumber.doubleValue() + lastWritten.sum.doubleValue());
						}else if(cls.equals(Integer.class)){
							lastWritten.sum = new Long(newNumber.longValue() + lastWritten.sum.longValue());
						}else if(cls.equals(Long.class)){
							lastWritten.sum = new Long(newNumber.longValue() + lastWritten.sum.longValue());
						}

						if( isFinished == false && ((lastNumber.doubleValue() == 0.0 && newNumber.doubleValue() == 0.0) || 
								Math.abs(1.0 - newNumber.doubleValue() / lastNumber.doubleValue()) < 
								getRunParameters().getStatisticModificationUntilUpdate() ) ){
							//System.out.println( "ignoring: " + stat + " " + newNumber + " " + lastNumber + " " + Math.abs(1.0 - newNumber.doubleValue() / lastNumber.doubleValue()));
							// do not write the current value
							continue;
						}

						// write the average instead of the complete value:

						final double averageValue =  (lastWritten.sum.doubleValue() / lastWritten.numberOfValues); 

						if(getRunParameters().isComputeAverageFromStatistics()){
							if(cls.equals(Float.class)){
								val = new Float(averageValue);
							}else if(cls.equals(Double.class)){
								val = new Double(averageValue);
							}else if(cls.equals(Integer.class)){
								val = new Integer((int)  averageValue);
							}else if(cls.equals(Long.class)){
								val = new Long((long) averageValue);
							}else{
								throw new IllegalArgumentException("Type not supported " + cls);
							}
						}

						SimpleConsoleLogger.Debug(now + " "  + getTopology() + " " + stat + " avg: " + averageValue + " sum: " + lastWritten.sum + " count: " + lastWritten.numberOfValues +" lastVal: " + lastWritten.lastValue);						

						// put in current average value as a new "old value"
						lastUpdatedStatistic.put(stat, new StatisticWritten(val));
						getOutputConverter().Statistics(getTopology(), now, stat, group, val );
						continue;

					}
				}
				// put in the first value:
				lastUpdatedStatistic.put(stat, new StatisticWritten(val));
			}		

			// write the value as it is:						
			getOutputConverter().Statistics(getTopology(), now, stat, group, val );

		}

		try{
			getNextStatistic();
		}catch(Exception e){
			throw new IllegalArgumentException("Error in stat group " + group.getName() + " topology " + getTopology() , e);
		}
	}

	@Override
	public void initalize() {
		this.group = reader.getGroup();

		assert(group != null);

		isFinished = reader.isFinished();
		try{
			getNextStatistic();
		}catch(Exception e){
			throw new IllegalArgumentException("Error in stat group " + group.getName() + " topology " + 
					getTopology() , e);
		}

		// register me on the trace converter
		getOutputConverter().addTopology(getTopology());
	}

	@Override
	public Epoch peekEarliestTime() {
		return nextTimeStamp;
	}	

	@Override
	public boolean isFinished() {
		return isFinished;
	}

}

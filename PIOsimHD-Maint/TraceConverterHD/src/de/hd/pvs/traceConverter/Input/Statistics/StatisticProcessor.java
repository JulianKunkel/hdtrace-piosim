package de.hd.pvs.traceConverter.Input.Statistics;

import java.util.HashMap;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.SimpleConsoleLogger;
import de.hd.pvs.traceConverter.Input.AbstractTraceProcessor;
import de.hd.pvs.traceConverter.Input.ProcessIdentifier;
import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup.StatisticType;
import de.hd.pvs.traceConverter.Input.Trace.SaxTraceFileReader;
import de.hd.pvs.traceConverter.Output.TraceOutputConverter;

/**
 * Processes one group of statistics for exactly one ProcessIdentifier.
 * 
 * @author julian
 */
public class StatisticProcessor  extends AbstractTraceProcessor{
	private final StatisticsReader reader;
	private boolean isFinished;
	private StatisticEntry lastRead;

	private ExternalStatisticsGroup group;

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


	public StatisticProcessor(StatisticsReader reader) throws Exception{
		this.reader = reader;
		isFinished = reader.isFinished();
		if(! isFinished){		
			lastRead = reader.getNextStatisticEntry();
		}

	}

	@Override
	public void processEarliestEvent(Epoch now) {
		//process last entry:
		assert(now.equals(lastRead.getTimeStamp()));

		isFinished = reader.isFinished();

		for(String stat: lastRead.getNameResultMap().keySet()){
			Object val = lastRead.getNameResultMap().get(stat);

			//System.out.println(now + " "  + getPID() + " " + now + " stat " + stat + " val: " + val);
			
			if(getRunParameters().isUpdateStatisticsOnlyIfTheyChangeTooMuch()){
				// check if the statistic changed enough from the last written stamp.
				
				StatisticWritten lastWritten = lastUpdatedStatistic.get(stat); 
				if(lastWritten != null){
					if(Number.class.isAssignableFrom(val.getClass())){
						// check new and old value:
						Number lastNumber = (Number) lastWritten.lastValue;
						Number newNumber = (Number) val;
						
						Class<Number> cls = (Class<Number>) newNumber.getClass();

						lastWritten.numberOfValues++;

						if(cls.equals(Float.class)){
							lastWritten.sum = new Double(lastNumber.doubleValue() + lastWritten.sum.doubleValue());
						}else if(cls.equals(Double.class)){
							lastWritten.sum = new Double(lastNumber.doubleValue() + lastWritten.sum.doubleValue());
						}else if(cls.equals(Integer.class)){
							lastWritten.sum = new Long(lastNumber.longValue() + lastWritten.sum.longValue());
						}else if(cls.equals(Long.class)){
							lastWritten.sum = new Long(lastNumber.longValue() + lastWritten.sum.longValue());
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
												
						SimpleConsoleLogger.Debug(now + " "  + getPID() + " " + stat + " avg: " + averageValue + " sum: " + lastWritten.sum + " count: " + lastWritten.numberOfValues +" lastVal: " + lastWritten.lastValue);
						
						lastUpdatedStatistic.put(stat, new StatisticWritten(val));						
						getOutputConverter().Statistics(getPID(), now, group.getName(), stat, group.getType(stat), val );
						continue;

					}else if(val.equals(lastWritten.lastValue)){
						continue;						
					}
				}
				// put in the first value:
				lastUpdatedStatistic.put(stat, new StatisticWritten(val));
			}		
			
			// write the value as it is:						
			getOutputConverter().Statistics(getPID(), now, group.getName(), stat, group.getType(stat), val );

		}

		try{
			if(! isFinished)
				lastRead = reader.getNextStatisticEntry();			
		}catch(Exception e){
			throw new IllegalArgumentException("Error in stat group " + group.getName() + " rank, thread " + 
					getPID().getRank() + "," +getPID().getRank() , e);
		}
	}

	@Override
	public void initalize() {
		this.group = reader.getGroup();

		assert(group != null);

		for(String stat: group.getStatistics()){
			getOutputConverter().addStatistic(getPID().getRank(), getPID().getVthread(), stat, group.getType(stat));
		}
	}

	@Override
	public Epoch peekEarliestTime() {
		return lastRead.getTimeStamp();
	}	

	@Override
	public boolean isFinished() {
		return isFinished;
	}

}

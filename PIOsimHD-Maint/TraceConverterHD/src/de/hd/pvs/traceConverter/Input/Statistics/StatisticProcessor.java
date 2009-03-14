package de.hd.pvs.traceConverter.Input.Statistics;

import java.util.HashMap;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.SimpleConsoleLogger;
import de.hd.pvs.traceConverter.Input.AbstractTraceProcessor;

/**
 * Processes one group of statistics for exactly one ProcessIdentifier.
 * 
 * @author julian
 */
public class StatisticProcessor  extends AbstractTraceProcessor{
	private final StatisticsReader reader;
	private boolean isFinished;
	private StatisticEntry lastRead;
	private Epoch nextTimeStamp;

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

	private void getNextStatistic() throws Exception{
		if(! isFinished){
			lastRead = reader.getNextStatisticEntry();
			if(! reader.isFinished()){
				nextTimeStamp = lastRead.getTimeStamp().add(group.getTimeOffset());
			}
		}
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

			//System.out.println(now + " "  + getPID() + " " + now + " stat " + stat + " val: " + val);

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

						SimpleConsoleLogger.Debug(now + " "  + getPID() + " " + stat + " avg: " + averageValue + " sum: " + lastWritten.sum + " count: " + lastWritten.numberOfValues +" lastVal: " + lastWritten.lastValue);						

						// put in current average value as a new "old value"
						lastUpdatedStatistic.put(stat, new StatisticWritten(val));
						getOutputConverter().Statistics(getPID(), now, stat, group, val );
						continue;

					}
				}
				// put in the first value:
				lastUpdatedStatistic.put(stat, new StatisticWritten(val));
			}		

			// write the value as it is:						
			getOutputConverter().Statistics(getPID(), now, stat, group, val );

		}

		try{
			getNextStatistic();
		}catch(Exception e){
			throw new IllegalArgumentException("Error in stat group " + group.getName() + " rank, thread " + 
					getPID().getRank() + "," +getPID().getRank() , e);
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
			throw new IllegalArgumentException("Error in stat group " + group.getName() + " rank, thread " + 
					getPID().getRank() + "," +getPID().getRank() , e);
		}
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

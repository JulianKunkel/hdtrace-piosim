package de.hd.pvs.traceConverter.Input.Statistics;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.Input.AbstractTraceProcessor;
import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup.StatisticType;
import de.hd.pvs.traceConverter.Input.Trace.SaxTraceFileReader;
import de.hd.pvs.traceConverter.Output.TraceOutputConverter;

public class StatisticProcessor  extends AbstractTraceProcessor{
	final StatisticsReader reader;
	boolean isFinished;
	StatisticEntry lastRead;


	public StatisticProcessor(StatisticsReader reader) throws Exception{
		this.reader = reader;
		isFinished = reader.isFinished();
		if(! isFinished)		
			lastRead = reader.getNextStatisticEntry();		
	}

	// for testing the output
	//static double time = 0.0;
	
	@Override
	public void processEarliestEvent(Epoch now) {
		//process last entry:
		assert(now.equals(lastRead.getTimeStamp()));
		
		for(String stat: lastRead.getNameResultMap().keySet()){
			Object val = lastRead.getNameResultMap().get(stat);
			
			//time++;
			
			getOutputConverter().Statistics(getPID(), now, reader.getGroup().getName(), stat, reader.getGroup().getType(stat), val );
			//getOutputConverter().Statistics(getPID(), new Epoch(time), reader.getGroup().getName(), stat, StatisticType.DOUBLE, new Double(time) );
		}

		try{
			isFinished = reader.isFinished();
			if(! isFinished)
				lastRead = reader.getNextStatisticEntry();			
		}catch(Exception e){
			throw new IllegalArgumentException("Error in stat group " + reader.getGroup().getName() + " rank, thread " + 
					getPID().getRank() + "," +getPID().getRank() , e);
		}
	}

	@Override
	public void initalize() {
		for(String stat: reader.getGroup().getStatistics()){
			getOutputConverter().addStatistic(getPID().getRank(), getPID().getVthread(), stat, reader.getGroup().getType(stat));
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

package de.hd.pvs.traceConverter;

import java.util.ArrayList;
import java.util.PriorityQueue;

import de.hd.pvs.traceConverter.Input.AbstractTraceProcessor;
import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup;
import de.hd.pvs.traceConverter.Input.Statistics.StatisticProcessor;
import de.hd.pvs.traceConverter.Input.Statistics.StatisticsReader;
import de.hd.pvs.traceConverter.Input.Trace.SaxTraceFileReader;
import de.hd.pvs.traceConverter.Input.Trace.StateTraceEntry;
import de.hd.pvs.traceConverter.Input.Trace.TraceProcessor;
import de.hd.pvs.traceConverter.Input.Trace.XMLTraceEntry;
import de.hd.pvs.traceConverter.Input.Trace.XMLTraceEntry.TYPE;
import de.hd.pvs.traceConverter.Output.TraceOutputConverter;

public class HDTraceConverter {

	/**
	 * Start the conversion of the input trace to the given output trace. Therefore, "events"
	 * are processed with the smallest start time first.
	 * 
	 * @param param
	 */
	public void process(RunParameters param) throws Exception{
		// try to instantiate the appropriate output converter:
		TraceOutputConverter outputConverter = instantiateOutputWriter(
				"de.hd.pvs.traceConverter.Output." + 
				param.getOutputFormat() + "." +
				param.getOutputFormat() + "Converter");

		// scan for all the XML files which must be opened during conversion:
		// general description
		ApplicationTraceReader traceReader = new ApplicationTraceReader(param.getInputTraceFile());
		
		// pending events and files to process
		PriorityQueue<AbstractTraceProcessor> pendingReaders = new PriorityQueue<AbstractTraceProcessor>();
		
		// start parsing of the trace files:
		// trace files: rank + thread id are defined in the file name
		ExistingTraceFiles files = traceReader.getTraceFiles();		
		for(int rank=0 ; rank < files.getSize(); rank++){
			for(int thread : files.getExistingThreads(rank)){
				AbstractTraceProcessor processor = new TraceProcessor(new SaxTraceFileReader(files.getFilenameXML(rank, thread)));
				processor.setOutputConverter(outputConverter);
				pendingReaders.add(processor);
				
				// external statistics
				ArrayList<String> statFileList = files.getStatisticsFiles(rank, thread);
				for(String statFile: statFileList){
					System.out.println("Found statistic file for <rank,thread>=" + rank + "," + thread + " group " + statFile);
					
					ExternalStatisticsGroup statGroup = traceReader.getExternalStatisticsDescription(statFile);
					if(statGroup == null){
						throw new IllegalArgumentException("Statistic group " + statGroup + 
								" invalid, <rank,thread>=" + rank + "," + thread);
					}
					
					processor = new StatisticProcessor(new StatisticsReader(files.getFilenameStatistics(rank, thread, statFile), 
							statGroup)); 
					processor.setOutputConverter(outputConverter);
					
					pendingReaders.add(processor);					
				}
			}			
		}
		
		// now start to take the first trace event out of the queue and process:
		while(! pendingReaders.isEmpty()){
			AbstractTraceProcessor reader = pendingReaders.poll();
			
			reader.processEarliestEvent();
			
			if(! reader.isFinished()){
				pendingReaders.add(reader);
			}
		}
	}

	/** 
	 * Simple factory method to instantiate the selected TraceOutput implementation
	 * via reflection 
	 */
	private TraceOutputConverter instantiateOutputWriter(String which){
		Class<TraceOutputConverter> cls;
		try{
			cls = (Class<TraceOutputConverter>) Class.forName(which);
		}catch(ClassNotFoundException e){
			throw new IllegalArgumentException("Output converter for " + which + " not found!");
		}
		TraceOutputConverter converter;
		try{
			converter = cls.newInstance();
		}catch(Exception e){
			throw new IllegalArgumentException("Invalid converter for " + which + "!", e);
		}
		return converter;
	}

}

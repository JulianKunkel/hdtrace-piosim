package de.hd.pvs.traceConverter;

import java.util.ArrayList;
import java.util.PriorityQueue;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.Input.AbstractTraceProcessor;
import de.hd.pvs.traceConverter.Input.ProcessIdentifier;
import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup;
import de.hd.pvs.traceConverter.Input.Statistics.StatisticProcessor;
import de.hd.pvs.traceConverter.Input.Statistics.StatisticsReader;
import de.hd.pvs.traceConverter.Input.Trace.SaxTraceFileReader;
import de.hd.pvs.traceConverter.Input.Trace.TraceProcessor;
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

		// init trace converter to make it ready:
		outputConverter.initializeTrace(param, param.getOutputFilePrefix());

		// start parsing of the trace files:
		// trace files: rank + thread id are defined in the file name
		ExistingTraceFiles files = traceReader.getTraceFiles();		
		for(int rank=0 ; rank < files.getSize(); rank++){
			for(int thread : files.getExistingThreads(rank)){

				ProcessIdentifier pid = new ProcessIdentifier(rank, thread);
				final String traceFile = files.getFilenameXML(rank, thread);
				try{
					TraceProcessor processor = new TraceProcessor(new SaxTraceFileReader(traceFile));

					processor.setOutputConverter(outputConverter);
					processor.setProcessIdentifier(pid);
					processor.setRunParameters(param);

					processor.initalize();

					if(processor.peekEarliestTime() != null){
						pendingReaders.add(processor);
					}else{
						System.out.println("Warning: trace file " + traceFile + " does not contain a single trace entry");
					}
					
				}catch(Exception e){
					SimpleConsoleLogger.Debug("Error in trace file " + traceFile + " " + e.getMessage());
				}

				// external statistics
				ArrayList<String> statFileList = files.getStatisticsFiles(rank, thread);
				for(final String group: statFileList){
					final String statFileName = files.getFilenameStatistics(rank, thread, group);
					
					SimpleConsoleLogger.Debug("Found statistic file for <rank,thread>=" + rank + "," + thread + " group " + group);

					ExternalStatisticsGroup statGroup = traceReader.getExternalStatisticsDescription(group);
					if(statGroup == null){
						throw new IllegalArgumentException("Statistic group " + statGroup + 
								" invalid, <rank,thread>=" + rank + "," + thread);
					}

					StatisticProcessor  processor = new StatisticProcessor(new StatisticsReader(statFileName, 
							statGroup)); 

					processor.setOutputConverter(outputConverter);
					processor.setProcessIdentifier(pid);
					processor.setRunParameters(param);

					processor.initalize();

					if(processor.peekEarliestTime() != null){
						pendingReaders.add(processor);
					}else{
						System.out.println("Warning: statistic file " + statFileName + " does not contain a single statistic, remove the file!");
					}						
				}
			}			
		}

		Epoch now = Epoch.ZERO;
		Epoch old = Epoch.ZERO;


		long eventCount = 0;

		// now start to take the first trace event out of the queue and process:
		while(! pendingReaders.isEmpty()){
			eventCount++;

			AbstractTraceProcessor reader = pendingReaders.poll();

			now = reader.peekEarliestTime();

			if(now.compareTo(old) < 0){
				throw new IllegalArgumentException("Wrong order, negative time " + now + " old " + old);
			}

			reader.processEarliestEvent(now);

			if(! reader.isFinished()){
				pendingReaders.add(reader);
			}

			old = now;

			// print some status on the command line to show that we are still working
			if(eventCount % 10000 == 0){
				System.out.print("-");;
				if(eventCount % 1000000 == 0){
					System.out.println();
				}
			}			

		}

		System.out.println();

		outputConverter.finalizeTrace();

		System.out.println("Completed -> processed " + eventCount + " events");
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

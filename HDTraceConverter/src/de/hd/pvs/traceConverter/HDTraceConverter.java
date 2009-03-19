
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

package de.hd.pvs.traceConverter;

import java.io.File;
import java.util.PriorityQueue;

import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.project.ProjectDescriptionXMLReader;
import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.statistics.StatisticsReader;
import de.hd.pvs.TraceFormat.trace.StAXTraceFileReader;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.traceConverter.Input.AbstractTraceProcessor;
import de.hd.pvs.traceConverter.Input.ProcessIdentifier;
import de.hd.pvs.traceConverter.Input.Statistics.StatisticProcessor;
import de.hd.pvs.traceConverter.Input.Trace.TraceProcessor;
import de.hd.pvs.traceConverter.Output.TraceOutputWriter;
import de.hd.pvs.traceConverter.Output.HDTrace.HDTraceWriter;

public class HDTraceConverter {

	/**
	 * Start the conversion of the input trace to the given output trace. Therefore, "events"
	 * are processed with the smallest start time first.
	 * 
	 * @param param
	 */
	public void process(RunParameters param) throws Exception{
		// try to instantiate the appropriate output converter:
		TraceOutputWriter outputConverter = instantiateOutputWriter(
				"de.hd.pvs.traceConverter.Output." + 
				param.getOutputFormat() + "." +
				param.getOutputFormat() + "Writer");

		// scan for all the XML files which must be opened during conversion:
		// general description
		ProjectDescriptionXMLReader pReader = new ProjectDescriptionXMLReader();
		ProjectDescription projectDescription = new ProjectDescription();
		pReader.readProjectDescription(projectDescription, param.getInputTraceFile());

		// pending events and files to process
		PriorityQueue<AbstractTraceProcessor> pendingReaders = new PriorityQueue<AbstractTraceProcessor>();

		// init trace converter to make it ready:
		outputConverter.initializeTrace(param, param.getOutputFilePrefix());

		// start parsing of the trace files:
		// trace files: rank + thread id are defined in the file name		
		for(int rank=0 ; rank < projectDescription.getProcessCount(); rank++){
			for(int thread = 0; thread < projectDescription.getProcessThreadCount(rank); thread++ ){

				ProcessIdentifier pid = new ProcessIdentifier(rank, thread);
				final String traceFile = projectDescription.getAbsoluteFilenameOfTrace(rank, thread);

				SimpleConsoleLogger.Debug("Checking trace: " + traceFile);
				
				try{
					TraceProcessor processor = new TraceProcessor(new StAXTraceFileReader(traceFile, param.isReadNestedTrace()));

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
					System.err.println("Error in trace file " + traceFile + " " + e.getMessage());
				}

				// external statistics
				for(final String group: projectDescription.getExternalStatisticGroups()){
					final String statFileName = projectDescription.getAbsoluteFilenameOfStatistics(rank, thread, group);

					if (! (new File(statFileName)).canRead() ){
						SimpleConsoleLogger.Debug("Statistic file does not exist " + statFileName);
						continue;
					}
					
					SimpleConsoleLogger.Debug("Found statistic file for <rank,thread>=" + rank + "," + thread + " group " + group);

					ExternalStatisticsGroup statGroup = projectDescription.getExternalStatisticsGroup(group);
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
		
		
		// if it is the HDTrace writer, then write the project description:
		if(outputConverter.getClass().equals(HDTraceWriter.class)){
			((HDTraceWriter) outputConverter).initalizeProjectDescriptionWithOldValues(projectDescription, 
					pReader.getUnparsedChildTags());
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
	private TraceOutputWriter instantiateOutputWriter(String which){
		Class<TraceOutputWriter> cls;
		try{
			cls = (Class<TraceOutputWriter>) Class.forName(which);
		}catch(ClassNotFoundException e){
			throw new IllegalArgumentException("Output converter for " + which + " not found!");
		}
		TraceOutputWriter converter;
		try{
			converter = cls.newInstance();
		}catch(Exception e){
			throw new IllegalArgumentException("Invalid converter for " + which + "!", e);
		}
		return converter;
	}

}

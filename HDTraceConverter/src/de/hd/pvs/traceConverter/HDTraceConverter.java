
/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$ 
 */


//Copyright (C) 2008, 2009 Julian M. Kunkel

//This file is part of PIOsimHD.

//PIOsimHD is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//PIOsimHD is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.traceConverter;

import java.util.PriorityQueue;

import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticSource;
import de.hd.pvs.TraceFormat.statistics.StatisticsReader;
import de.hd.pvs.TraceFormat.topology.TopologyEntry;
import de.hd.pvs.TraceFormat.trace.StAXTraceFileReader;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.traceConverter.Input.AbstractTraceProcessor;
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

		// pending events and files to process
		PriorityQueue<AbstractTraceProcessor> pendingReaders = new PriorityQueue<AbstractTraceProcessor>();

		// init trace converter to make it ready:
		outputConverter.initializeTrace(param, param.getOutputFilePrefix());

		final TraceFormatFileOpener traceReader = new TraceFormatFileOpener(param.getInputTraceFile(), param.isReadNestedTrace(), StatisticsReader.class, StAXTraceFileReader.class);

		final ProjectDescription projectDescription = traceReader.getProjectDescription();

		// if it is the HDTrace writer, then write the project description:
		if(outputConverter.getClass().equals(HDTraceWriter.class)){
			((HDTraceWriter) outputConverter).initalizeProjectDescriptionWithOldValues(
					param.getOutputFilePrefix(),
					projectDescription, 
					traceReader.getProjectDescriptionXMLReader().getUnparsedChildTags());
		}

		// read from topology
		recursivlyInstantiateProcessors(outputConverter,  projectDescription.getTopologyRoot(), param, pendingReaders );

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

	private void recursivlyInstantiateProcessors(TraceOutputWriter outputConverter, TopologyEntry topo, RunParameters param, PriorityQueue<AbstractTraceProcessor> pendingReaders) throws Exception{
		for(StatisticSource ssource: topo.getStatisticSources().values()){
			StatisticsReader reader = (StatisticsReader)  ssource;

			StatisticProcessor processor = new StatisticProcessor(reader);
			processor.setOutputConverter(outputConverter);
			processor.setTopologyEntryResponsibleFor(topo);
			processor.setRunParameters(param);

			processor.initalize();

			if(processor.peekEarliestTime() != null){
				pendingReaders.add(processor);
			}
		}



		StAXTraceFileReader reader = (StAXTraceFileReader) topo.getTraceSource();
		if( reader == null){
			if( topo.isLeaf() ){
				throw new IllegalArgumentException("Trace reader is not initialized for topology: " + topo);
			}
		}else{
			TraceProcessor processor = new TraceProcessor(reader);
			processor.setOutputConverter(outputConverter);
			processor.setTopologyEntryResponsibleFor(topo);
			processor.setRunParameters(param);

			processor.initalize();

			if(processor.peekEarliestTime() != null){
				pendingReaders.add(processor);
			}
		}

		for(TopologyEntry child: topo.getChildElements().values()){
			recursivlyInstantiateProcessors(outputConverter, child, param, pendingReaders);
		}
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

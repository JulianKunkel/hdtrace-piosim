
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

package de.hd.pvs.TraceFormat;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.writer.StatisticWriter;
import de.hd.pvs.TraceFormat.writer.TraceWriter;
import de.hd.pvs.TraceFormat.xml.XMLTag;

/**
 * Exports the XML data to a HDTrace project.
 * 
 * @author Julian M. Kunkel
 * 
 */
public class TraceFormatWriter {
	
	static class PerThreadFiles{
		TraceWriter traceWriter;
		HashMap<ExternalStatisticsGroup, StatisticWriter> registeredStatisticWriter = new HashMap<ExternalStatisticsGroup, StatisticWriter>();
	}

	// map a single process id to the corresponding trace writer.
	final HashMap<Integer, HashMap<Integer, PerThreadFiles>> traceWriterMap = new HashMap<Integer, HashMap<Integer,PerThreadFiles>>();
	
	final ProjectDescription outProject = new ProjectDescription();
	
	LinkedList<XMLTag> unparsedTagsToWrite = null;
	
	public void setUnparsedTagsToWrite(LinkedList<XMLTag> unparsedTagsToWrite){
		this.unparsedTagsToWrite = unparsedTagsToWrite;
	}
	
	public void initializeTrace(String resultFile) {
		outProject.setProjectFilename(resultFile + ".xml");
	}

	public void addTimeline(int process, int thread) {
		final String file = TraceFileNames.getFilenameXML(outProject.getAbsoluteFilesPrefix(), process, thread);
		try {
			HashMap<Integer, PerThreadFiles> threadMap = traceWriterMap.get(process);
			if(threadMap == null){
				threadMap = new HashMap<Integer, PerThreadFiles>();
				traceWriterMap.put(process, threadMap);
			}
			PerThreadFiles files = new PerThreadFiles();
			files.traceWriter =  new TraceWriter(file);
			
			threadMap.put(thread, files);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Statistic file could not be created: " + file);
		}
		if(process >= outProject.getProcessCount() ){
			outProject.setProcessCount(process + 1);
		}
		if(thread >= outProject.getProcessThreadCount(process)){
			outProject.setProcessThreadCount(process, thread+1);
		}
	}
	
	/**
	 * Add a statistic group for output. 
	 * @param group
	 */
	public void addStatisticGroup(ExternalStatisticsGroup group){
		outProject.addExternalStatisticsGroup(group);
	}

	
	public void Event(int process, int thread, Epoch time,
			EventTraceEntry traceEntry) {
		TraceWriter writer = traceWriterMap.get(process).get(thread).traceWriter;
		try {
			writer.Event(time, traceEntry);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not write file for id "
					+ process +"," + thread);
		}
	}

	public void StateEnd(int process, int thread, Epoch time,
			StateTraceEntry traceEntry) {
		TraceWriter writer = traceWriterMap.get(process).get(thread).traceWriter;

		try {
			writer.StateEnd(time, traceEntry);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not write file for "
					+ process + "," + thread);
		}
	}

	public void StateStart(int process, int thread, Epoch time,
			StateTraceEntry traceEntry) {
		TraceWriter writer =  traceWriterMap.get(process).get(thread).traceWriter;

		try {
			writer.StateStart(time, traceEntry);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not write file for "
					+ process + "," + thread);
		}
	}
	
	public void Statistics(int process, int thread, Epoch time, String statistic,
			ExternalStatisticsGroup group, Object value) {
		final HashMap<ExternalStatisticsGroup, StatisticWriter> stats =  traceWriterMap.get(process).get(thread).registeredStatisticWriter;
				
		StatisticWriter outWriter = stats.get(group);

		if (outWriter == null) {			
			final String file = TraceFileNames.getFilenameStatistics(outProject.getAbsoluteFilesPrefix(),
				process, thread, group.getName());
			try {
				// generate a new output writer
				outWriter = new StatisticWriter(file, group);
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Statistic file could not be created: " + file);
			}

			stats.put(group, outWriter);
		}

		try {
			outWriter.writeStatisticEntry(time, statistic, value);
		} catch (IOException e) {
			throw new IllegalArgumentException(
					"Error during write in statistic file", e);
		}
	}

	public void finalizeTrace() throws IOException{
		for (HashMap<Integer, PerThreadFiles> pidMap : traceWriterMap.values()) {
			for (PerThreadFiles files : pidMap.values()) {
				files.traceWriter.finalize();
				
				for (StatisticWriter writer : files.registeredStatisticWriter.values()) {
					writer.finalize();
				}				
			}
		}
		
		final ProjectDescriptionXMLWriter projectWriter = new ProjectDescriptionXMLWriter();		
		projectWriter.writeXMLToProjectFile(outProject, unparsedTagsToWrite);
	}
	
	public ProjectDescription getProjectDescription(){
		return outProject;
	}

}

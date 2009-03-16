
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

package de.hd.pvs.traceConverter.Output.HDTrace;

import java.io.IOException;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.TraceFormatWriter;
import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.traceConverter.RunParameters;
import de.hd.pvs.traceConverter.Input.ProcessIdentifier;
import de.hd.pvs.traceConverter.Output.TraceOutputWriter;

/**
 * Exports the XML data to a HDTrace project.
 * 
 * @author Julian M. Kunkel
 * 
 */
public class HDTraceWriter extends TraceOutputWriter {
	
	TraceFormatWriter writer = new TraceFormatWriter();
	
	public void initalizeProjectDescriptionWithOldValues(ProjectDescription oldDescription, LinkedList<XMLTag> unparsedTagsToWrite){
		writer.setUnparsedTagsToWrite(unparsedTagsToWrite);
		
		ProjectDescription outProject = writer.getProjectDescription();		
		outProject.setDescription(oldDescription.getDescription());
		outProject.setApplicationName(oldDescription.getName());
	}
	
	@Override
	public void initializeTrace(RunParameters parameters, String resultFile) {
		parameters.setProcessAlsoComputeEvents(true);
		writer.initializeTrace(resultFile);
	}
	
	@Override
	public void addTimeline(ProcessIdentifier pid) {
		writer.addTimeline(pid.getProcessNumber(), pid.getThread());
	}
	
	@Override
	public void Event(ProcessIdentifier id, Epoch time,
			EventTraceEntry traceEntry) {
		writer.Event(id.getProcessNumber(), id.getThread(), time, traceEntry);
	}
	
	@Override
	public void finalizeTrace() throws IOException {
		writer.finalizeTrace();
	}
	
	@Override
	public void StateEnd(ProcessIdentifier id, Epoch time,
			StateTraceEntry traceEntry) {
		writer.StateEnd(id.getProcessNumber(), id.getThread(), time, traceEntry);
	}
	
	@Override
	public void StateStart(ProcessIdentifier id, Epoch time,
			StateTraceEntry traceEntry) {
		writer.StateStart(id.getProcessNumber(), id.getThread(), time, traceEntry);
	}
	
	@Override
	public void Statistics(ProcessIdentifier id, Epoch time, String statistic,
			ExternalStatisticsGroup group, Object value) {
		if(writer.getProjectDescription().getExternalStatisticsGroup(group.getName()) == null){
			writer.addStatisticGroup(group);
		}
		
		writer.Statistics(id.getProcessNumber(), id.getThread(), time, statistic, group, value);
	}	
}

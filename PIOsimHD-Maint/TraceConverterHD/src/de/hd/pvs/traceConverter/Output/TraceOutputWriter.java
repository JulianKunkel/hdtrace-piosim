
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

package de.hd.pvs.traceConverter.Output;

import java.io.IOException;

import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.traceConverter.HDTraceConverter;
import de.hd.pvs.traceConverter.RunParameters;
import de.hd.pvs.traceConverter.Input.ProcessIdentifier;

/**
 * An implementation of the TraceOutputWriter decides how to 
 * use the data provided inside the XML trace.
 * The methods of the Converter are called to preserve increasing time in the trace.
 * 
 * @author Julian M. Kunkel
 *
 */
abstract public class TraceOutputWriter {
		
	/**
	 * Initialize the resulting trace output, called by the  {@link HDTraceConverter}
	 * 
	 * @param commandLineArguments Method specific Command Line Arguments.
	 * @param resultFile
	 * @param extStat
	 */
	abstract public void initializeTrace(
			RunParameters parameters,
			String resultFile
			);

	/**
	 * Called by the {@link HDTraceConverter} once all input is processed.
	 */
	abstract public void finalizeTrace() throws IOException;
	
	
	/**
	 * Announce the existence of a rank/thread line for events. Called by the TraceProcessor
	 */
	abstract public void addTimeline(ProcessIdentifier pid);
	
	/**
	 * Add the normal timeline.
	 * @param pid
	 */
	final public void addNormalTimeline(ProcessIdentifier pid){
		addTimeline(pid);
	}

	
	// handle states == default case
	abstract public void StateStart(ProcessIdentifier id, Epoch time, StateTraceEntry traceEntry);
	abstract public void StateEnd(ProcessIdentifier id, Epoch time, StateTraceEntry traceEntry);

	// handle events
	abstract public void Event(ProcessIdentifier id,Epoch time, EventTraceEntry traceEntry);
	
	// handle statistics
	abstract public void Statistics(ProcessIdentifier id, Epoch time, String statistic, ExternalStatisticsGroup group, Object value);
	
}

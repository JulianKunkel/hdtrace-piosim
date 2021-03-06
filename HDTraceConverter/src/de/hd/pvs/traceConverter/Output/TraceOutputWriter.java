
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
  */


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

import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.IEventTraceEntry;
import de.hd.pvs.TraceFormat.trace.IStateTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.traceConverter.HDTraceConverter;
import de.hd.pvs.traceConverter.RunParameters;

/**
 * An implementation of the TraceOutputWriter decides how to use the data provided inside the XML trace.
 * The methods of the converter are called which must preserve increasing time in the trace.
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
		 	) throws IOException;

	/**
	 * Called by the {@link HDTraceConverter} once all input is processed.
	 */
	abstract public void finalizeTrace() throws IOException;
	
	
	/**
	 * Announce the existence of a real topology for which trace or statistic data
	 * will be generated.
	 * Each topology must be initialized before it can be used in subsequent calls.
	 */
	abstract public void initalizeForTopology(TopologyNode newTopologyEntry);
		
	
	/**
	 * Each topology must be initialized before it can be used in subsequent calls.
	 * @param topology
	 * @param time
	 * @param traceEntry
	 */
	// handle states == default case
	abstract public void StateStart(TopologyNode topology, String name, Epoch startTime) throws IOException;
	abstract public void StateEnd(TopologyNode topology, IStateTraceEntry traceEntry) throws IOException;

	// handle events
	abstract public void Event(TopologyNode topology, IEventTraceEntry traceEntry) throws IOException;

	// handle statistics
	abstract public void Statistics(TopologyNode topology, StatisticsGroupEntry entry) throws IOException;	
}

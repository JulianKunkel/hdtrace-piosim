
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

package de.hd.pvs.TraceFormat.trace;

import java.io.IOException;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Write a single trace file and access it with increasing start and end times.
 * The TraceWriter combines the events and states correctly into nested states.
 * Note that all nested elements are kept in memory in this trace writer!
 * 
 * @author Julian M. Kunkel
 * 
 */
public class TraceWriterNestedBuffer extends SimpleTraceWriter{

	/**
	 *  stacks the states and events to produce nested entries.
	 */
	LinkedList<StateTraceEntry> stackedEntries = new LinkedList<StateTraceEntry>();

	/**
	 * Create a new trace writer
	 * @param filename The filename to write output.
	 * @param timeAdjustment Adjusts the time to write by subtracting this value
	 * @throws IOException
	 */
	public TraceWriterNestedBuffer(String filename, Epoch timeAdjustment) throws IOException {
		super(filename, timeAdjustment);
	}

	/**
	 * Write a single event to the trace file.
	 * @param traceEntry
	 * @throws IOException
	 */
	public void writeEvent(EventTraceEntry traceEntry) throws IOException{
		if(stackedEntries.isEmpty()){			
			super.writeEvent(traceEntry);
		}else{
			stackedEntries.peek().addTraceChild(traceEntry);
		}
	}

	/**
	 * Compute the duration for the state end.
	 * 
	 * @param time
	 * @param traceEntry
	 * @throws IOException
	 */
	public void writeStateEnd(StateTraceEntry finEntry) throws IOException{	
		StateTraceEntry traceEntry = stackedEntries.pollFirst();
		
		if(! finEntry.equals(traceEntry)){
			throw new IllegalArgumentException("Expected " + traceEntry + " as state entry!");
		}
		
		if(stackedEntries.isEmpty()){
			writeState(traceEntry);
		}
	}

	/**
	 * Start a state.
	 * @param traceEntry
	 * @throws IOException
	 */
	public void writeStateStart(StateTraceEntry traceEntry) throws IOException {
		if(! stackedEntries.isEmpty()){			
			stackedEntries.peek().addTraceChild(traceEntry);
		}
		stackedEntries.push(traceEntry);
	}
}

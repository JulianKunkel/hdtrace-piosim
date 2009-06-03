
 /** Version Control Information $Id: TraceWriter.java 318 2009-05-30 10:56:40Z kunkel $
  * @lastmodified    $Date: 2009-05-30 12:56:40 +0200 (Sa, 30 Mai 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 318 $ 
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;

/**
 * Write a single trace file and access it with increasing start and end times.
 * The TraceWriter writes the events and states as soon as possible.
 * However, it is required to keep some information about parent states.
 * Therefore, the amount of RAM required depends on the nesting depth. 
 * 
 * @author Julian M. Kunkel
 * 
 */
public class NestedTraceWriter extends SimpleTraceWriter{
	private static class NestedStateData{
		final String name;
		final Epoch startTime;
		public NestedStateData(String name, Epoch startTime) {
			this.name = name;
			this.startTime = startTime;
		}
	}
	
	// stack the states to produce nested entries.
	private LinkedList<NestedStateData> stackedEntries = new LinkedList<NestedStateData>();
	
	private NestedStateData lastOpenedStateTraceEntry = null; 
	
	/**
	 * Create a new trace writer
	 * @param filename The filename to write output.
	 * @param timeAdjustment Adjusts the time to write by subtracting this value
	 * @throws IOException
	 */
	public NestedTraceWriter(String filename, Epoch timeAdjustment) throws IOException {
		super(filename, timeAdjustment);
	}

	private void updateNestedObjectsOnEnter() throws IOException{
		if(lastOpenedStateTraceEntry == null)
			return;
		
		getFile().write("<Nested>\n");
		
		stackedEntries.push(lastOpenedStateTraceEntry);
		lastOpenedStateTraceEntry = null;
	}
	
	public void writeEvent(IEventTraceEntry traceEntry) throws IOException{
		updateNestedObjectsOnEnter();		
	}

	/**
	 * Compute the duration for the state end.
	 * 
	 * @param time
	 * @param traceEntry
	 * @throws IOException
	 */
	public void writeStateEnd(String name, Epoch endTime, HashMap<String, String> attributes, ArrayList<XMLTag> xmldata) throws IOException{
		IStateTraceEntry finEntry;
		if(lastOpenedStateTraceEntry == null){
			if(stackedEntries.size() == 0){
				throw new IllegalArgumentException("State ended, but no corresponding state start found");
			}
			// we have to finish a nested tag
			getFile().write("</Nested>\n");			

			NestedStateData stateData = stackedEntries.pop();
			
			finEntry = new StateTraceEntry(stateData.name, attributes, stateData.startTime, endTime, null, xmldata);					
		}else{
			finEntry = new StateTraceEntry(lastOpenedStateTraceEntry.name, attributes, lastOpenedStateTraceEntry.startTime, endTime, null, xmldata);
			lastOpenedStateTraceEntry = null;
		}
				
		if(name != finEntry.getName()){
			throw new IllegalArgumentException("Start and end trace entry must be the same!");
		}
		writeState(finEntry);
	}

	public void writeStateStart(String name, Epoch startTime) throws IOException {
		updateNestedObjectsOnEnter();
		
		lastOpenedStateTraceEntry = new NestedStateData(name, startTime);
	}
}

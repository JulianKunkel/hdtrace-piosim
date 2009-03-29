
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;

/**
 * Write a single trace file.
 * 
 * @author Julian M. Kunkel
 * 
 */
public class TraceWriter {

	private final FileWriter file;
	private final String filename;
	
	// tag length <program> + </program>, allows to remove empty files
	private final int EMPTY_FILE_LENGTH = 21; 
	
	// stack the states to produce nested entries.
	LinkedList<StateTraceEntry> stackedEntries = new LinkedList<StateTraceEntry>();
	
	StateTraceEntry lastOpenedStateTraceEntry = null; 
	
	public TraceWriter(String filename) throws IOException {
		file = new FileWriter(filename);
		
		file.write("<Program>\n");
	
		this.filename = filename;
	}

	public void finalize() {
		try {
			file.write("</Program>\n");
			file.close();

			// test if the file is empty:
			long size = new RandomAccessFile(filename, "r").length();
			if (size <= EMPTY_FILE_LENGTH){
				// nothing got written, thus remove file
				new File(filename).delete();
			}
		} catch (IOException e) { 
			throw new IllegalArgumentException(e);
		}
	}

	private void writeAttributes(TraceEntry traceEntry) throws IOException{
		writeAttributes(traceEntry.getAttributes()); 
	}

	private void writeAttributes(final HashMap<String, String> attr) throws IOException{
		if(attr == null|| attr.size() == 0)
			return;
		
		for(String key: attr.keySet()){
			file.write(" " + key + "=\"" + attr.get(key) + "\"");
		}
	}

	private void updateNestedObjectsOnEnter() throws IOException{
		if(lastOpenedStateTraceEntry == null)
			return;
		
		file.write("<Nested>\n");
		
		stackedEntries.push(lastOpenedStateTraceEntry);
		lastOpenedStateTraceEntry = null;
	}
	
	public void Event(EventTraceEntry traceEntry) throws IOException{
		updateNestedObjectsOnEnter();
		
		file.write("<Event name=\"" + traceEntry.getName() + "\" time=\"" + 
				traceEntry.getEarliestTime().toNormalizedString() + "\"");

		writeAttributes(traceEntry);
		
		if(traceEntry.getNestedXMLTags() != null && traceEntry.getNestedXMLTags().size() != 0 ){
			file.write(">");
			for(XMLTag nested: traceEntry.getNestedXMLTags()){
				file.write(nested.toString());
			}
			file.write("</Event>\n");
		}else{
			file.write("/>\n");
		}
	}

	/**
	 * Compute the duration for the state end.
	 * 
	 * @param time
	 * @param traceEntry
	 * @throws IOException
	 */
	public void StateEnd(StateTraceEntry finEntry) throws IOException{		
		if(lastOpenedStateTraceEntry != finEntry){
			StateTraceEntry traceEntry;
			if(stackedEntries.size() == 0){
				throw new IllegalArgumentException("State ended, but no corresponding state start found");
			}
			
			if(lastOpenedStateTraceEntry == null){				
				// we have to finish a nested tag
				file.write("</Nested>\n");			
			}
			traceEntry = stackedEntries.pollFirst();
			writeState(finEntry);			
		}else{
			StateTraceEntry traceEntry;
			// last start == end tag => not nested 
			traceEntry = lastOpenedStateTraceEntry;
			lastOpenedStateTraceEntry = null;			
			writeState(traceEntry);
		}
	}
	
	private void writeState(StateTraceEntry traceEntry) throws IOException{
		file.write("<" + traceEntry.getName() + " time=\"" + traceEntry.getEarliestTime().toNormalizedString() + "\" end=\"" 
				+ traceEntry.getLatestTime().toNormalizedString() + "\"");
			writeAttributes(traceEntry);			

			if(traceEntry.getNestedXMLTags() != null && traceEntry.getNestedXMLTags().size() != 0 ){
				file.write(">");
				for(XMLTag nested: traceEntry.getNestedXMLTags()){
					file.write(nested.toString());
				}
				file.write("</" + traceEntry.getName() + ">\n");
			}else{
				file.write("/>\n");
			}		
	}

	public void StateStart(StateTraceEntry traceEntry) throws IOException {
		updateNestedObjectsOnEnter();
		
		lastOpenedStateTraceEntry = traceEntry;
	}
}

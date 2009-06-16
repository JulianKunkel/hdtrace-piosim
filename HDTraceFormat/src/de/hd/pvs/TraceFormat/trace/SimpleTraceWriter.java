
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

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import de.hd.pvs.TraceFormat.TracableObjectType;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;

/**
 * Write a single trace file. The SimpleTraceWriter requires that full StateEntries or EventEntries
 * are written.
 * 
 * @author Julian M. Kunkel
 * 
 */
public class SimpleTraceWriter {
	
	/**
	 * A number of attributes is reserved.
	 */
	static private final HashSet<String> reservedAttributeKeys = new HashSet<String>();

	static{
		reservedAttributeKeys.add("time"); // start time
		reservedAttributeKeys.add("end"); // end time
	}
	
	/**
	 * Output file
	 */
	private final FileWriter file;
	
	/**
	 * time to write = time - timeAdjustment
	 */
	private final Epoch timeAdjustment;
	
	/**
	 * Create a new trace writer
	 * @param filename The filename to write output.
	 * @param timeAdjustment Adjusts the time to write by subtracting this value
	 * @throws IOException
	 */
	public SimpleTraceWriter(String filename, Epoch timeAdjustment) throws IOException {
		file = new FileWriter(filename);
				
		if(timeAdjustment != null){
			file.write("<Program timeAdjustment=\"" + timeAdjustment + "\">\n");
			this.timeAdjustment = timeAdjustment;
		}else{
			file.write("<Program>\n");
			this.timeAdjustment = Epoch.ZERO;
		}
	}

	/**
	 * Close the trace file.
	 */
	public void finalize() {
		try {
			file.write("</Program>\n");
			file.close();
		} catch (IOException e) { 
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * Write the key/value attributes (except reservedAttributes) 
	 * @param attr
	 * @throws IOException
	 */
	private void writeAttributes(final HashMap<String, String> attr) throws IOException{
		if(attr == null|| attr.size() == 0)
			return;
		
		for(String key: attr.keySet()){
			if(! reservedAttributeKeys.contains(key)){
				file.write(" " + key + "=\"" + attr.get(key) + "\"");
			}
		}
	}
	
	/**
	 * Adjust the time by the timeAdjustment.
	 * @param time
	 * @return
	 */
	private String getAdaptedTime(Epoch time){
		return time.subtract(timeAdjustment).toNormalizedString();
	}
	
	/**
	 * Write a single event to the trace file.
	 * @param traceEntry
	 * @throws IOException
	 */
	public void writeEvent(IEventTraceEntry traceEntry) throws IOException{
		file.write("<Event name=\"" + traceEntry.getName() + "\" time=\"" + 
				getAdaptedTime(traceEntry.getEarliestTime()) + "\"");

		writeAttributes(traceEntry.getAttributes());
		
		if(traceEntry.getContainedXMLData() != null && traceEntry.getContainedXMLData().size() != 0 ){
			file.write(">");
			for(XMLTag nested: traceEntry.getContainedXMLData()){
				file.write(nested.toString());
			}
			file.write("</Event>\n");
		}else{
			file.write("/>\n");
		}
	}

	/**
	 * Write the content of the traceEntry. 
	 * 
	 * @param traceEntry
	 * @throws IOException
	 */
	public void writeState(IStateTraceEntry traceEntry) throws IOException{
		if(traceEntry.hasNestedTraceChildren()){
			file.write("<Nested>");
			for(ITraceEntry child: traceEntry.getNestedTraceChildren()){
				if(child.getType() == TracableObjectType.EVENT){
					writeEvent((IEventTraceEntry) child);					
				}else if(child.getType() == TracableObjectType.STATE){
					writeState((IStateTraceEntry) child);
				}else{
					throw new IllegalStateException("Unknown object type: " + child.getType());
				}
			}
			file.write("<Nested/>");
		}
		
		// finally write content:
		file.write("<" + traceEntry.getName() + " time=\"" + 
				getAdaptedTime(traceEntry.getEarliestTime()) + "\" end=\"" 
				+ getAdaptedTime(traceEntry.getLatestTime()) + "\"");
			writeAttributes(traceEntry.getAttributes());			

			if(traceEntry.getContainedXMLData() != null && traceEntry.getContainedXMLData().size() != 0 ){
				file.write(">");
				for(XMLTag nested: traceEntry.getContainedXMLData()){
					file.write(nested.toString());
				}
				file.write("</" + traceEntry.getName() + ">\n");
			}else{
				file.write("/>\n");
			}		
	}
	
	protected FileWriter getFile() {
		return file;
	}
}

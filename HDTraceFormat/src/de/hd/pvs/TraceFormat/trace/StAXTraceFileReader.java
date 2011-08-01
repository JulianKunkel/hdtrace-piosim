
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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Stack;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.sun.xml.internal.stream.XMLInputFactoryImpl;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.TraceFormat.xml.XMLTagCreator;
import de.hd.pvs.TraceFormat.xml.XMLTraceEntryFactory;


/**
 * Reads the XML file on demand with a Sax XML Parser.
 * 
 * @author Julian M. Kunkel
 */
public class StAXTraceFileReader implements TraceSource{

	private XMLStreamReader reader;

	/**
	 * Should nested elements be read.
	 */
	final boolean readNested;

	/**
	 * is the trace file read completely
	 */
	boolean finishedReading = false;
	
	/**
	 * The time adjustment is added to all read timestamps.
	 */
	private Epoch timeAdjustment;
	
	/**
	 * The speed of the processor in MHz
	 */
	private long speedInMHz = 2660; // default value: 2660 MHz

	/**
	 * Current depths of the tag nesting.
	 */
	private int nesting_depth = 0; 
	
	/**
	 * Constructor, start processing of the file.
	 * @param filename
	 */
	public StAXTraceFileReader(String filename, boolean readNested, Epoch timeOffset) throws Exception {
		reader = XMLInputFactoryImpl.newInstance().createXMLStreamReader(new BufferedInputStream(new FileInputStream(filename)));
		timeAdjustment = timeOffset;
		this.readNested = readNested;
	}
	
	public long getProcssorSpeedInMHz() {
		return speedInMHz;
	}

	private String getPosition(){
		return " line: " + reader.getLocation().getLineNumber() + " column: " + reader.getLocation().getColumnNumber(); 
	}
	
	/**
	 * Get new input data if available, may block until new data is available
	 * 
	 * @return
	 */
	public ITraceEntry getNextInputEntry(){
		try{

			/**
			 * Top level nested data:
			 */
			final Stack<XMLTagCreator> stackedData = new Stack<XMLTagCreator>();  
			
			XMLTag nestedElementTag = null;
			
			while(reader.hasNext()){
				final int nextType = reader.next();
				
				switch(nextType){
				case(XMLStreamConstants.CHARACTERS):
					final String str = reader.getText().trim();
					if(str.length() > 0){
						if(stackedData.size() == 0){
							throw new IllegalArgumentException("Invalid text \"" + str + "\" at: " + getPosition());
						}
						
						stackedData.peek().setContainedText(str);
					}
					break;
				case(XMLStreamConstants.START_ELEMENT):{
					final String name =  reader.getName().getLocalPart();
					nesting_depth++;		

					// check if there are nested XMLTrace stats
					XMLTagCreator currentData = new XMLTagCreator();
					currentData.setName(name);
					
					
					// generate attributes
					for( int i = 0; i < reader.getAttributeCount(); i++ ){			
						currentData.addAttribute(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
					}


					if (name.equals("Program") && nesting_depth == 1){
						String tAdj = currentData.getAttribute("timeAdjustment");
						if(tAdj != null){
							timeAdjustment = timeAdjustment.add(Epoch.parseTime(tAdj));
						}
						
						tAdj = currentData.getAttribute("processorSpeedinMHZ");
						if(tAdj != null){
							speedInMHz = Integer.parseInt(tAdj);
						}						
						 
						continue;
					}

					stackedData.push(currentData);
				}
				break;
				case(XMLStreamConstants.END_ELEMENT):{
					final String name =  reader.getName().getLocalPart();

					nesting_depth--;							

					if (name.equals("Program") && nesting_depth == 0){
						return null;
					}
					
					final XMLTagCreator currentData = stackedData.pop();
					final XMLTag newData = currentData.createXMLTag();
					
					assert(newData.getName().equals(name));

					if(nestedElementTag != null){
						// attach nested element to child:						
						currentData.addNestedXMLTag(nestedElementTag);					
						nestedElementTag = null;
					}
					
					if(name.equals("Nested")){
						if(readNested){
							nestedElementTag = newData;
						}
						continue;
					}
					
					if(stackedData.size() == 0){
						ITraceEntry newTraceEntry;
						try{
							newTraceEntry = XMLTraceEntryFactory.manufactureXMLTraceObject(newData, timeAdjustment);
						}catch(IllegalArgumentException e){			
							throw new IllegalArgumentException("Invalid XML object: " + currentData + " at: " + getPosition(), e);
						}

						return newTraceEntry;
					}
					
					stackedData.peek().addNestedXMLTag(newData);					
				}
				break;
				default:
					
				}

			}
		}catch(XMLStreamException e){
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
		return null;
	}
	
	/**
	 * Return the current position in the trace file.
	 * @return
	 */
	public long getFilePosition(){
		return reader.getLocation().getCharacterOffset();
	}

	/**
	 * Close the input stream.
	 * @throws Exception
	 */
	public void close() throws Exception{
		reader.close();
	}
}

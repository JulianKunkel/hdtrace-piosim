
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
import java.util.HashMap;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.sun.xml.internal.stream.XMLInputFactoryImpl;

import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.TraceFormat.xml.XMLTraceEntryFactory;


/**
 * Reads the XML file on demand with a Sax XML Parser.
 * 
 * @author Julian M. Kunkel
 */
public class StAXTraceFileReader{

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
	 * Current depths of the tag nesting.
	 */
	private int nesting_depth = 0; 

	/**
	 * Constructor, start processing of the file.
	 * @param filename
	 */
	public StAXTraceFileReader(String filename, boolean readNested) throws Exception {
		reader = XMLInputFactoryImpl.newInstance().createXMLStreamReader(new BufferedInputStream(new FileInputStream(filename)));
		this.readNested = readNested;
	}

	private String getPosition(){
		return " line: " + reader.getLocation().getLineNumber() + " column: " + reader.getLocation().getColumnNumber(); 
	}
	
	/**
	 * Get new input data if available, may block until new data is available
	 * 
	 * @return
	 */
	public XMLTraceEntry readNextInputEntry(){
		try{

			/**
			 * State/Statistics which are currently build
			 */
			XMLTag currentData = null;

			/**
			 * Top level nested data:
			 */
			XMLTag nestedData = null;

			
			while(reader.hasNext()){
				final int nextType = reader.next();
				
				switch(nextType){
				case(XMLStreamConstants.CHARACTERS):
					final String str = reader.getText().trim();
					if(str.length() > 0){
						if(currentData == null){
							throw new IllegalArgumentException("Invalid text \"" + str + "\" at: " + getPosition());
						}
						
						currentData.setContainedText(str);
					}
					break;
				case(XMLStreamConstants.START_ELEMENT):{
					final String name =  reader.getName().getLocalPart();
					nesting_depth++;		

					if (name.equals("Program") && nesting_depth == 1){
						continue;
					}
					final HashMap<String, String> attributes = new HashMap<String, String>();

					// generate attributes
					for( int i = 0; i < reader.getAttributeCount(); i++ ){			
						attributes.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
					}

					// check if there are nested XMLTrace stats
					XMLTag parent = currentData;
					currentData = new XMLTag(name, attributes, parent);
					
				}
				break;
				case(XMLStreamConstants.END_ELEMENT):{
					final String name =  reader.getName().getLocalPart();

					nesting_depth--;							

					if (name.equals("Program") && nesting_depth == 0){
						return null;
					}

					if(name.equals("Nested")){
						if(! readNested){
							return null;
						}

						nestedData = currentData;				
					}else if(! currentData.isChild()){
						XMLTraceEntry newTraceEntry;
						try{
							newTraceEntry = XMLTraceEntryFactory.manufactureXMLTraceObject(currentData, null, nestedData);
						}catch(IllegalArgumentException e){			
							throw new IllegalArgumentException("Invalid XML object: " + currentData + " at: " + getPosition(), e);
						}

						return newTraceEntry;
					}
					currentData = currentData.getParentTag();
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
	
	public long getFilePosition(){
		return reader.getLocation().getCharacterOffset();
	}
	
	public void close() throws Exception{
		reader.close();
	}
}

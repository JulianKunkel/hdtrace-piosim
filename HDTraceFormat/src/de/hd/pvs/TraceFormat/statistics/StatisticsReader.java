
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

package de.hd.pvs.TraceFormat.statistics;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLReaderToRAM;
import de.hd.pvs.TraceFormat.xml.XMLTag;


/**
 * Read statistics of a single group on demand.
 * 
 * @author Julian M. Kunkel
 *
 */
public class StatisticsReader implements StatisticsSource{
	/**
	 * The input file, null if empty
	 */
	RandomAccessFile file;
	
	/**
	 * The group we are reading.
	 */
	final StatisticsGroupDescription group;
	
	/**
	 * The timestamp on which the last interval ended. Therefore it is the start of the next interval.
	 */
	Epoch lastTimeStamp;
	
	final String filename;
	
	/**
	 * Create a new statistics reader.
	 * 
	 * @param filename
	 * @throws Exception
	 */
	public StatisticsReader(String filename) throws Exception {
		this(filename, null);
	}
	
	/**
	 * Create a new statistics reader, check, if the group name in the file matches the expectations.
	 * @param filename
	 * @param expectedGroup
	 * @throws Exception
	 */
	public StatisticsReader(String filename, String expectedGroup) throws Exception {
		this.file = new RandomAccessFile(filename, "r");
		
		// parse XML header of file:
		final String headerSizeStr = file.readLine();
		final int headerSize = Integer.parseInt(headerSizeStr);
		
		final byte [] headerXML = new byte[headerSize];
		
		file.readFully(headerXML);
		final String str = new String(headerXML); 
		
		if(! str.endsWith("</Statistics>\n")){
			System.err.println("Error: Statistics file is damaged, does not end with Statistics!");
			System.err.println("File: " + filename);
			System.err.println("Content:\"" + str +"\"");
		}
		
		// parse header via DOM
		final XMLReaderToRAM headerReader = new XMLReaderToRAM();
		XMLTag root;
		
		try{
			root = headerReader.convertXMLToXMLTag(str);
		}catch(Exception e){
			System.err.println("XML was: "  + str);
			throw e;
		}
		
		// use the group definition as specified in the XML header of the file
		final XMLTag groupDefinition = root.getFirstNestedXMLTagWithName("Group");

		if(groupDefinition == null ){
			throw new IllegalArgumentException("Did not find group definition in XML " + str);
		}
		
		this.group = parseStatisticGroupInXML(groupDefinition);	

		if(expectedGroup != null && ! group.getName().equals(expectedGroup)){
			throw new IllegalArgumentException("Expected group: " + expectedGroup + " however found group: " + group.getName());
		}
		
		lastTimeStamp = readTimeStamp();
		
		this.filename = filename;
	}
	
	/**
	 * Read a single timestamp in the appropriate format.
	 * @return
	 * @throws IOException
	 */
	private Epoch readTimeStamp() throws IOException{
		Epoch timeStamp;

		// read timestamp:
		switch(group.getTimestampDatatype()){
			case INT32:
				long tstamp = file.readInt() *  (long) group.getTimeResolutionMultiplier();				
				
				timeStamp = new Epoch(tstamp);								
				break;
			case EPOCH:
				int s, us;
				s = file.readInt();
				us = file.readInt();
				
				timeStamp = new Epoch(s, us);
				break;
			default:
				throw new IllegalArgumentException("Unknown timestamp type: " + group.getTimestampDatatype());
		}
		timeStamp = timeStamp.add(group.getTimeAdjustment());
		return timeStamp;
	}
	
	/**
	 * Return the statistics for the next interval.
	 * 
	 * @return null, if there is no further entry!
	 * @throws Exception
	 */
	public StatisticsGroupEntry getNextInputEntry() throws IOException{
		if(isFinished()){
			return null;
		}
		final Epoch timeStamp = readTimeStamp();
		
		final Object [] values = new Object[group.getSize()];
	
		int pos = 0;
		for(StatisticsDescription statDesc: group.getStatisticsOrdered()){
			final String statName = statDesc.getName();
			
			// read data depending on type:
			final StatisticsEntryType type = statDesc.getDatatype();
			Object value;
			switch(type){
			case INT64:
				value = new Long(file.readLong());
				break;
			case INT32:
				value = new Integer(file.readInt());
				break;
			case DOUBLE:
				value = new Double(file.readDouble());
								
				if (Double.isInfinite((Double) value) || Double.isNaN((Double) value)){
					System.err.println("Warning: Value for " + statName + " is " + value  + " at timestamp " + timeStamp + " file: " + filename); 
				}		
				break;
			case FLOAT:
				value = new Float(file.readFloat());		
				if (Float.isInfinite((Float) value) || Float.isNaN((Float) value)){
					System.err.println("Warning: Value for " + statName + " is " + value  + " at timestamp " + timeStamp + " file: " + filename); 
				}
				break;
			case STRING:
				final int length = file.readShort();
				final byte [] buff = new byte[length];
				final int read = file.read(buff, 0, length);
				if(read != length){
					throw new IllegalArgumentException("Read expected " + length + " bytes, but we read " + read + " bytes");				
				}
				value = new String(buff);
				break;
			default:
				throw new IllegalArgumentException("Unknown type: " + type +" in value " + statName);
			}

			values[pos++] = value;
		}
		

		// verify correct ordering of time in statistic trace file:

		if(lastTimeStamp.compareTo(timeStamp) > 0){
			throw new IllegalArgumentException("Time " + timeStamp + " is earlier than last entry time: " + lastTimeStamp);
		}
		
		StatisticsGroupEntry entry = new StatisticsGroupEntry(values, lastTimeStamp, timeStamp, group);
		
		lastTimeStamp = timeStamp;
		
		return entry;
	}

	/**
	 * @return true if there is no further entry which can be read.
	 */
	public boolean isFinished(){
		try{			
			return (file.getFilePointer() == file.length());
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	public StatisticsGroupDescription getGroup() {
		return group;
	}

	public long getFilePosition() throws IOException{
		return file.getFilePointer();
	}
	

	/**
	 * Parse the actual XML header.
	 * @param root
	 * @return
	 */
	private StatisticsGroupDescription parseStatisticGroupInXML(XMLTag root){
		StatisticsGroupDescription stat = new StatisticsGroupDescription(root.getAttribute("name"));
		//System.out.println("Statistics: " + root.getNodeName());

		final String tT = root.getAttribute("timestampDatatype");		
		if(tT != null  && ! tT.isEmpty()){
			StatisticsEntryType type = StatisticsEntryType.valueOf(tT);
			stat.setTimestampDatatype(type);
		}

		final String tR = root.getAttribute("timeResulution");		
		if (tR != null && ! tR.isEmpty()){
			stat.setTimeResolutionMultiplier(tR);
		}

		final String timeAdjustment = root.getAttribute("timeAdjustment");
		if (timeAdjustment != null && ! timeAdjustment.isEmpty()){
			stat.setTimeAdjustment(Epoch.parseTime(timeAdjustment));
		}

		final ArrayList<XMLTag> children = root.getNestedXMLTags();

		// the next number of the statistic group:
		int currentNumberInGroup = 0;
		for(XMLTag child: children){
			StatisticsDescription desc = new StatisticsDescription(stat,
					child.getAttribute("name"), 
					StatisticsEntryType.valueOf( child.getAttribute("type").toUpperCase() ),
					currentNumberInGroup,
					child.getAttribute("unit"),
					 child.getAttribute("grouping"));

			stat.addStatistic(desc);

			currentNumberInGroup++;
		}

		return stat;
	}	
}

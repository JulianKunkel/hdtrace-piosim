
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
 * Read statistics of a single group on demand
 * @author Julian M. Kunkel
 *
 */
public class StatisticsReader implements StatisticsSource{
	final RandomAccessFile file;	
	final StatisticsGroupDescription group;
	
	Epoch lastTimeStamp;
	
	public StatisticsReader(String filename) throws Exception {
		this(filename, null);
	}
	
	public StatisticsReader(String filename, String expectedGroup) throws Exception {
		this.file = new RandomAccessFile(filename, "r");
		
		// parse XML header of file:
		final String headerSizeStr = file.readLine();
		final int headerSize = Integer.parseInt(headerSizeStr);
		
		final byte [] headerXML = new byte[headerSize];
		
		file.readFully(headerXML);
		final String str = new String(headerXML); 
		
		// parse header to DOM
		final XMLReaderToRAM headerReader = new XMLReaderToRAM();
		XMLTag root;
		
		try{
			root = headerReader.convertXMLToXMLTag(str);
		}catch(Exception e){
			System.err.println("XML was: "  + str);
			throw e;
		}
		
		final XMLTag groupDefinition = root.getFirstNestedXMLTagWithName("Group");

		if(groupDefinition == null ){
			throw new IllegalArgumentException("Did not find group definition in XML " + str);
		}
		
		this.group = parseStatisticGroupInXML(groupDefinition);	

		if(expectedGroup != null && ! group.getName().equals(expectedGroup)){
			throw new IllegalArgumentException("Expected group: " + expectedGroup + " however found group: " + group.getName());
		}
		
	}
	
	@Override
	public StatisticGroupEntry getNextInputEntry() throws Exception{
		if(isFinished()){
			return null;
		}
		Epoch timeStamp;
		
		final Object [] values = new Object[group.getSize()];
					
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
		
		// throw away first entry:
		if ( lastTimeStamp == null){
			lastTimeStamp = timeStamp;
		}
		
		int pos = 0;
		for(StatisticDescription statDesc: group.getStatisticsOrdered()){
			final String statName = statDesc.getName();
			
			// read data depending on type:
			final StatisticsEntryType type = statDesc.getType();
			Object value;
			switch(type){
			case INT64:
				value = new Long(file.readLong()) * statDesc.getMultiplier();
				break;
			case INT32:
				value = new Integer(file.readInt())* statDesc.getMultiplier();
				break;
			case DOUBLE:
				value = new Double(file.readDouble())* statDesc.getMultiplier();
								
				break;
			case FLOAT:
				value = new Float(file.readFloat())* statDesc.getMultiplier();			
				
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
		
		StatisticGroupEntry entry = new StatisticGroupEntry(values, lastTimeStamp, timeStamp, group);
		
		lastTimeStamp = timeStamp;
		
		return entry;
	}

	public boolean isFinished(){
		try{
			return file.getFilePointer() == file.length();
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}
	}

	public void close(){
		try{
			file.close();
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
			int multiplier = 1;
			final String multiplierStr = child.getAttribute("multiplier");
			if(multiplierStr != null && multiplierStr.length() > 0){
				multiplier = Integer.parseInt(child.getAttribute("multiplier"));			
			}
			StatisticDescription desc = new StatisticDescription(stat,
					child.getAttribute("name"), 
					StatisticsEntryType.valueOf( child.getAttribute("type").toUpperCase() ),
					currentNumberInGroup,
					child.getAttribute("unit"),
					multiplier, child.getAttribute("grouping"));

			stat.addStatistic(desc);

			currentNumberInGroup++;
		}

		return stat;
	}	
}

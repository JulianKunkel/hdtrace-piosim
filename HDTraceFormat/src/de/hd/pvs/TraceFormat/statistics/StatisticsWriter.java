
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

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Write statistics group values to a statistics file. 
 * Internally it checks the correct write order of the group's statistics and that the time
 * is increasing.
 * 
 * @author Julian M. Kunkel
 *
 */
public class StatisticsWriter {
	/**
	 * The output file stream
	 */
	private final DataOutputStream file;

	/**
	 * The group we are writing
	 */
	private final StatisticsGroupDescription group;

	/**
	 * The time the last entry finished, used to check increasing timestamps.
	 */
	private Epoch lastTimeStamp = null;

	/**
	 * Iterator, to check the correct write order
	 */
	private Iterator<StatisticsDescription> nextExpectedStatisticIter = null;

	/**
	 * Creates a new statistics writer and initializes the statistics file.
	 * @param filename The file to write to.
	 * @param topology The topology this group belongs to.
	 * @param newGroup The group which shall be written
	 * @param startTime The first entry will start that time.
	 * @throws Exception
	 */
	public StatisticsWriter(String filename, TopologyNode topology, 
			StatisticsGroupDescription newGroup, Epoch startTime) throws Exception {
		this.group = newGroup;
		this.file = new DataOutputStream(new FileOutputStream(filename));

		// create XML header
		final StringBuffer xmlHeader = new StringBuffer();
		xmlHeader.append("<Statistics version=\"1\">");
		xmlHeader.append("<Group name=\"" + group.getName() + "\" timestampDatatype=\"" + group.getTimestampDatatype()  + "\" timeAdjustment=\"" +
				group.getTimeAdjustment()  + "\"");
		if(group.getTimeResolutionMultiplierName() != null){
			xmlHeader.append(" timeResulution=\"" + group.getTimeResolutionMultiplierName() + "\"");
		}
		xmlHeader.append(">\n");

		for(StatisticsDescription stat: group.getStatisticsOrdered()){								
			xmlHeader.append("<Statistics name=\"" + stat.getName() + "\"" );

			if(stat.getUnit() != null){
				xmlHeader.append(" unit=\"" + stat.getUnit()  + "\"");
			}

			xmlHeader.append(" type=\"" + stat.getDatatype()  + "\"/>\n");
		}

		xmlHeader.append("</Group>\n");
		xmlHeader.append("</Statistics>\n");		

		// write XML header length and XML header
		file.write( (Integer.toString(xmlHeader.length() ) + "\n").getBytes() );
		file.write(xmlHeader.toString().getBytes());		

		writeStatisticsTimestampInternal(startTime);
	}

	/**
	 * Check if all required values are written.
	 * @return
	 */
	public boolean isStatisticIntervalFinished(){
		return nextExpectedStatisticIter == null || ! nextExpectedStatisticIter.hasNext();
	}

	/**
	 * Write a timestamp in the apropriate format
	 * @param time
	 * @throws IOException
	 */
	private void writeStatisticsTimestampInternal(Epoch time) throws IOException{
		final Epoch realTime = time.subtract(group.getTimeAdjustment());

		// write timestamp:
		switch(group.getTimestampDatatype()){
		case INT32:
			int realVal = (int) (realTime.getDoubleInNS() / group.getTimeResolutionMultiplier());
			file.writeInt( realVal );						
			break;
		case EPOCH:
			file.writeInt(realTime.getSeconds());
			file.writeInt(realTime.getNanoSeconds());
			break;				
		default:
			throw new IllegalArgumentException("Unknown timestamp type: " + group.getTimestampDatatype());
		}

		lastTimeStamp = time;
	}

	/**
	 * Write the timestamp of the next interval.
	 * Can be called ONLY if statistics of the group are written independently.
	 * 
	 * @param time
	 * @throws IOException
	 */
	public void writeStatisticsTimestamp(Epoch time) throws IOException{
		if(lastTimeStamp != null && lastTimeStamp.compareTo(time) > 0){
			throw new IllegalArgumentException("New timestamp is before old timestamp! " + lastTimeStamp + " new: " + time);
		}

		// write timestamp, if necessary
		if(nextExpectedStatisticIter != null && nextExpectedStatisticIter.hasNext() ){
			throw new IllegalArgumentException("Current statistic interval is not finished yet!");
		}

		nextExpectedStatisticIter = group.getStatisticsOrdered().iterator();
		writeStatisticsTimestampInternal(time);
	}

	/**
	 * Write a single statistics value. This function is used in conjunction with writeStatisticsTimestamp.
	 * 
	 * @param statistic
	 * @param value
	 * @throws IOException
	 */
	public void writeStatisticEntry(StatisticsDescription statistic, Object value) throws IOException{
		assert(nextExpectedStatisticIter != null);

		final StatisticsDescription expectedStat = nextExpectedStatisticIter.next();

		if(expectedStat == null || expectedStat != statistic){
			throw new IllegalArgumentException("Expected to get statistics in the correct order! Expected: \"" + expectedStat.getName() + 
					"\", but got \"" + statistic + "\"");
		}

		// write data:
		final StatisticsEntryType type = expectedStat.getDatatype();
		switch(type){
		case INT64:
			file.writeLong((Long) value);
			break;
		case INT32:
			file.writeInt((Integer) value);
			break;
		case DOUBLE:
			file.writeDouble((Double) value);
			break;
		case FLOAT:
			file.writeFloat((Float) value);
			break;
		case STRING:
			final String str = (String) value;
			file.writeShort(str.length());
			file.write(str.getBytes());
			break;
		default:
			throw new IllegalArgumentException("Unknown type: " + type +" in value " + statistic);
		}
	}

	/**
	 * Write a complete statistics group entry.
	 * This function writes the timestamp and all values.
	 * 
	 * @param entry
	 * @throws IOException
	 */
	public void writeStatisticsGroupEntry(StatisticsGroupEntry entry) throws IOException{
		writeStatisticsTimestamp(entry.getLatestTime());
		
		// use output group and NOT input group for checking.
		final ArrayList<StatisticsDescription> descs = group.getStatisticsOrdered();		
		int cur = 0;
		for(Object val: entry.getValues()){
			writeStatisticEntry(descs.get(cur), val);
			cur++;
		}
	}


	/**
	 * Finalize the trace file.
	 */
	public void finalize(){
		try{
			file.close();
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Return the group which is written by the writer.
	 * @return
	 */
	public StatisticsGroupDescription getOutputGroup() {
		return group;
	}
}

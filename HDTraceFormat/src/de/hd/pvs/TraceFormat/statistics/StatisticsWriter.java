
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
import java.util.Iterator;

import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Write a single statistic file 
 * 
 * @author Julian M. Kunkel
 *
 */
public class StatisticsWriter {
	private final DataOutputStream file;	
	private final StatisticsGroupDescription group;

	private Epoch lastTimeStamp = null;

	private Iterator<StatisticDescription> nextExpectedStatisticIter = null;

	public StatisticsWriter(String filename, StatisticsGroupDescription newGroup) throws Exception {
		this.group = newGroup;
		this.file = new DataOutputStream(new FileOutputStream(filename));

		// create XML header
		final StringBuffer xmlHeader = new StringBuffer();
		xmlHeader.append("<Header>");
		xmlHeader.append("<Group name=\"" + group.getName() + "\" timestampDatatype=\"" + group.getTimestampDatatype()  + "\" timeAdjustment=\"" +
				group.getTimeAdjustment()  + "\"");
		if(group.getTimeResolutionMultiplierName() != null){
			xmlHeader.append(" timeResulution=\"" + group.getTimeResolutionMultiplierName() + "\"");
		}
		xmlHeader.append(">\n");
		
		for(StatisticDescription stat: group.getStatisticsOrdered()){								
			xmlHeader.append("<Statistics name=\"" + stat.getName() + "\"" );

			if(stat.getUnit() != null){
				xmlHeader.append(" unit=\"" + stat.getUnit()  + "\"");
			}

			xmlHeader.append(" multiplier=\"" + stat.getMultiplier() + "\" type=\"" + stat.getType()  + "\"/>\n");
		}

		xmlHeader.append("</Group>\n");
		xmlHeader.append("</Header>\n");		
		
		// write XML header length and XML header
		file.write( (Integer.toString(xmlHeader.length() ) + "\n").getBytes() );
		file.write(xmlHeader.toString().getBytes());		
	}

	public void writeStatisticEntry(Epoch time, StatisticDescription statistic, Object value) throws IOException{
		if(lastTimeStamp != null && lastTimeStamp.compareTo(time) > 0){
			throw new IllegalArgumentException("New timestamp is before old timestamp! " + lastTimeStamp + " new: " + time);
		}

		// write timestamp, if necessary
		if(nextExpectedStatisticIter == null || ! nextExpectedStatisticIter.hasNext()){
			nextExpectedStatisticIter = group.getStatisticsOrdered().iterator();
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
		}

		final StatisticDescription expectedStat = nextExpectedStatisticIter.next();

		if(expectedStat == null || expectedStat != statistic){
			throw new IllegalArgumentException("Expected to get statistics in the correct order! Expected: \"" + expectedStat.getName() + 
					"\", but got \"" + statistic + "\"");
		}

		// write data:
		final StatisticsEntryType type = expectedStat.getType();
		switch(type){
		case INT64:
			file.writeLong((Long) value / expectedStat.getMultiplier());
			break;
		case INT32:
			file.writeInt((Integer) value/ expectedStat.getMultiplier());
			break;
		case DOUBLE:
			file.writeDouble((Double) value/ expectedStat.getMultiplier());
			break;
		case FLOAT:
			file.writeFloat((Float) value/ expectedStat.getMultiplier());
			//System.out.println("VAL " + (Float) value/ expectedStat.getMultiplier());

			break;
		case STRING:
			final String str = (String) value;
			file.writeShort(str.length());
			file.write(str.getBytes());
			break;
		default:
			throw new IllegalArgumentException("Unknown type: " + type +" in value " + statistic);
		}


		lastTimeStamp = time;
	}

	public void finalize(){
		try{
			file.close();
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}
	}

	public StatisticsGroupDescription getOutputGroup() {
		return group;
	}
}

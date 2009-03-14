
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

package de.hd.pvs.traceConverter.Output.HDTrace;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticType;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Write a single statistic file 
 * 
 * @author Julian M. Kunkel
 *
 */
public class StatisticWriter {
	private final DataOutputStream file;	
	private final ExternalStatisticsGroup group;
	
	private Epoch lastTimeStamp = null;

	private Iterator<StatisticDescription> nextExpectedStatisticIter = null;
	
	public StatisticWriter(String filename, ExternalStatisticsGroup newGroup) throws Exception {
		this.group = newGroup;
		this.file = new DataOutputStream(new FileOutputStream(filename));
	}

	public void writeStatisticEntry(Epoch time, String statistic, Object value) throws IOException{
		if(lastTimeStamp != null && lastTimeStamp.compareTo(time) > 0){
			throw new IllegalArgumentException("New timestamp is before old timestamp! " + lastTimeStamp + " new: " + time);
		}
				
		// write timestamp, if necessary
		if(nextExpectedStatisticIter == null || ! nextExpectedStatisticIter.hasNext()){
			nextExpectedStatisticIter = group.getStatisticsOrdered().iterator();
			// write timestamp:
			file.writeInt(time.getSeconds());
			file.writeInt(time.getNanoSeconds());
		}
		
		final StatisticDescription expectedStat = nextExpectedStatisticIter.next();
		
		if(expectedStat == null || !expectedStat.getName().equals(statistic)){
			throw new IllegalArgumentException("Expected to get statistics in the correct order! Expected: \"" + expectedStat.getName() + 
					"\", but got \"" + statistic + "\"");
		}
		
		// write data:
		final StatisticType type = expectedStat.getType();
		switch(type){
		case LONG:
			file.writeLong((Long) value);
			break;
		case INT:
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

		
		lastTimeStamp = time;
	}
	
	public void finalize(){
		try{
			file.close();
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	public ExternalStatisticsGroup getOutputGroup() {
		return group;
	}
}

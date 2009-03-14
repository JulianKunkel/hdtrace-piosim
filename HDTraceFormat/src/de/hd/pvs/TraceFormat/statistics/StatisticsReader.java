
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

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import de.hd.pvs.TraceFormat.util.Epoch;


/**
 * Read statistics of a single group on demand
 * @author Julian M. Kunkel
 *
 */
public class StatisticsReader{
	final DataInputStream file;	
	final ExternalStatisticsGroup group;

	public StatisticsReader(String filename, ExternalStatisticsGroup group) throws Exception {
		this.group = group;
		this.file = new DataInputStream(new FileInputStream(filename));
	}
	
	public StatisticEntry getNextStatisticEntry() throws Exception{
		if(isFinished()){
			return null;
		}
		final Epoch timeStamp;
		
		Object [] values = new Object[group.getSize()];
			
		// read timestamp:
		switch(group.getTimestampDatatype()){
			case INT:
				long tstamp = file.readInt() *  (long) group.getTimeResolutionMultiplier();				
				
				timeStamp = new Epoch(tstamp);								
				break;
			case EPOCH:
				timeStamp = new Epoch(file.readInt(), file.readInt());
				break;				
			default:
				throw new IllegalArgumentException("Unknown timestamp type: " + group.getTimestampDatatype());
		}

		int pos = 0;
		for(StatisticDescription statDesc: group.getStatisticsOrdered()){
			final String statName = statDesc.getName();
			
			// read data depending on type:
			final StatisticType type = statDesc.getType();
			Object value;
			switch(type){
			case LONG:
				value = new Long(file.readLong()) * statDesc.getMultiplier();
				break;
			case INT:
				value = new Integer(file.readInt())* statDesc.getMultiplier();
				break;
			case DOUBLE:
				value = new Double(file.readDouble())* statDesc.getMultiplier();
								
				break;
			case FLOAT:
				value = new Float(file.readFloat())* statDesc.getMultiplier();				
				//System.out.println( timeStamp + " " + statName + " " + value);
				
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

		return new StatisticEntry(values, timeStamp);
	}

	public boolean isFinished(){
		try{
			return file.available() <= 0;
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}
	}

	public void finalize(){
		try{
			file.close();
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	public ExternalStatisticsGroup getGroup() {
		return group;
	}

}

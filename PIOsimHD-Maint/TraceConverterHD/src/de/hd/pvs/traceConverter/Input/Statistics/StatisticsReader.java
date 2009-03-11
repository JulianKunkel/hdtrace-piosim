package de.hd.pvs.traceConverter.Input.Statistics;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.ws.util.ByteArrayBuffer;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.Input.AbstractTraceProcessor;
import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup.StatisticType;


/**
 * Read statistics of a single group on demand
 * @author julian
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

		final HashMap<String, Object> nameResultMap = new HashMap<String, Object>();		
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
				final int length = file.readInt();
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

			nameResultMap.put(statName, value);
		}

		return new StatisticEntry(nameResultMap, timeStamp);
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

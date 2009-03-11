package de.hd.pvs.traceConverter.Input.Statistics;

import java.util.HashMap;

import de.hd.pvs.piosim.model.util.Epoch;

/**
 * Read values from a statistic file, aka Statistic Group. 
 * 
 * @author julian
 *
 */
public class StatisticEntry {
	
	/**
	 * Maps the statistic name to the measured value.
	 */
	private final HashMap<String, Object> nameResultMap;
	
	private final Epoch timeStamp; 
		
	public StatisticEntry(HashMap<String, Object> nameResultMap, Epoch timeStamp) {
		this.nameResultMap = nameResultMap;
		this.timeStamp = timeStamp;
	}
	
	public HashMap<String, Object> getNameResultMap() {
		return nameResultMap;
	}
	
	public Epoch getTimeStamp() {
		return timeStamp;
	}
}

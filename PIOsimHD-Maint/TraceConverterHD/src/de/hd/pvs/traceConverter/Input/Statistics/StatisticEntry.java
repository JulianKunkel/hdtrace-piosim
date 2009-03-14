package de.hd.pvs.traceConverter.Input.Statistics;

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
	private final Object [] values;
	
	private final Epoch timeStamp; 
		
	public StatisticEntry(Object [] values, Epoch timeStamp) {
		this.values = values;
		this.timeStamp = timeStamp;
	}
	
	public Object[] getValues() {
		return values;
	}
	
	public Epoch getTimeStamp() {
		return timeStamp;
	}
}

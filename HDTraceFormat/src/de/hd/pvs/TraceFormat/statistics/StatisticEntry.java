package de.hd.pvs.TraceFormat.statistics;

import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * A single read statistic value.
 * 
 * @author julian
 */
public class StatisticEntry implements TraceObject {
	final StatisticGroupEntry parentGroupEntry;
	final StatisticDescription description;	
	
	final Object value;
	
	public StatisticEntry(Object value, StatisticDescription description, StatisticGroupEntry parentGroupEntry) {
			this.value = value;
			this.description = description;
			this.parentGroupEntry = parentGroupEntry;
	}
	
	public Object getValue() {
		return value;
	}
	
	public Number getNumericValue() {
		return (Number) value;
	}
	
	public StatisticDescription getDescription() {
		return description;
	}
	
	public StatisticGroupEntry getParentGroupEntry() {
		return parentGroupEntry;
	}	
	
	@Override
	public TraceObjectType getType() {
		return TraceObjectType.STATISTICENTRY;
	}
	
	@Override
	public Epoch getEarliestTime() {
		return parentGroupEntry.getEarliestTime();
	}	
	
	@Override
	public Epoch getLatestTime() {	
		return parentGroupEntry.getLatestTime();
	}
}

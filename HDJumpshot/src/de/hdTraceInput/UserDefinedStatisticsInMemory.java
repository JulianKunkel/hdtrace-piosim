package de.hdTraceInput;

import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;

/**
 * Represents a user defined statistics function.
 * 
 * @author julian
 *
 */
public class UserDefinedStatisticsInMemory extends BufferedMemoryReader {
	@Override
	public void setEntries(StatisticsGroupEntry[] entries) {
		super.setEntries(entries);
	}
	
	@Override
	public void setGroup(StatisticsGroupDescription group) {
		super.setGroup(group);
	}
	
}

package de.hd.pvs.TraceFormat.statistics;

import de.hd.pvs.TraceFormat.TraceObject;

public interface StatisticSource {
	public TraceObject getNextInputEntry() throws Exception;
}

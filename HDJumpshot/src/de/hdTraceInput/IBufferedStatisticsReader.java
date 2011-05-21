package de.hdTraceInput;

import java.util.Enumeration;

import de.hd.pvs.TraceFormat.ITracableObject;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsSource;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Abstract interface for all buffered / in-memory statistics readers.
 * 
 * @author julian
 */
public interface IBufferedStatisticsReader extends IBufferedReader, StatisticsSource{
	public Enumeration<StatisticsGroupEntry> enumerateStatistics(Epoch startTime, Epoch endTime);
	public int getStatisticPositionAfter(Epoch minEndTime);
	public StatisticsGroupEntry [] getStatEntries();
	public StatisticsGroupDescription getGroup();
	public StatisticStatistics getStatisticsFor(int which);
	public StatisticsGroupEntry getTraceEntryClosestToTime(Epoch dTime);
}

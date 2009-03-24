package hdTraceInput;

import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.statistics.StatisticGroupEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

public interface IBufferedReader {
	
	/**
	 * Minimum time available in the file
	 * @return
	 */
	public Epoch getMinTime();
	
	/**
	 * Maximum time available in the file
	 * @return
	 */
	public Epoch getMaxTime();
	
	/**
	 * Return the trace entry which covers or the one which is closest to this time.
	 * 
	 * @param time
	 * @return
	 */
	public TraceObject getTraceEntryClosestToTime(double dTime);
}

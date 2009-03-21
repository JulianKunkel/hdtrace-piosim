package viewer;

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
}

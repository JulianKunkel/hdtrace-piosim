package de.hd.pvs.TraceFormat;

import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Basic class for all kinds of monitored trace objects
 * @author julian
 *
 */
public interface TraceObject {
	public TraceObjectType getType();
	public Epoch getEarliestTime();
	public Epoch getLatestTime();
}

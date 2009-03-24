package de.hd.pvs.TraceFormat.trace;

import de.hd.pvs.TraceFormat.TraceObject;

public interface TraceSource {
	public TraceObject getNextInputEntry() throws Exception;
}

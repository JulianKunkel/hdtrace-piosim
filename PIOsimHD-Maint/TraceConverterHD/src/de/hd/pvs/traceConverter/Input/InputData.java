package de.hd.pvs.traceConverter.Input;

import de.hd.pvs.traceConverter.SimpleEpoch;

/**
 * Container from data read from the trace file.
 * @author julian
 *
 */
public class InputData {
	/**
	 * when did the event/state etc. occur
	 */
	final SimpleEpoch time;
	
	public InputData(final SimpleEpoch time) {
		this.time = time;
	}
	
	
	public SimpleEpoch getTime() {
		return time;
	}
}

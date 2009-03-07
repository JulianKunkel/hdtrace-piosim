package de.hd.pvs.traceConverter.Input;

import de.hd.pvs.traceConverter.SimpleEpoch;

public class ProcessIdentifier {
	// identify the process and thread/timeline this event occurs:
	int rank;
	int vthread;	
	
	// when did the event/state etc. occur
	SimpleEpoch time;
}

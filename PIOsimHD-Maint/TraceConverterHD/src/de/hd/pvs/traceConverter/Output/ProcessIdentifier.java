package de.hd.pvs.traceConverter.Output;

import de.hd.pvs.piosim.model.util.Epoch;

public class ProcessIdentifier {
	// identify the process and thread/timeline this event occurs:
	int rank;
	int vthread;	
	
	// when did the event/state etc. occur
	Epoch time;
}

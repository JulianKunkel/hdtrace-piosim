package de.hd.pvs.traceConverter.Input;

import de.hd.pvs.piosim.model.util.Epoch;

public class ProcessIdentifier {
	// identify the process and thread/timeline this event occurs:
	private final int rank;
	private final int vthread;	
	
	public ProcessIdentifier(int rank, int vthread) {
		this.rank = rank;
		this.vthread = vthread;
	}
	
	public int getRank() {
		return rank;
	}
	
	public int getVthread() {
		return vthread;
	}
}

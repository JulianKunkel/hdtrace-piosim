package de.hd.pvs.traceConverter.Input;


public class ProcessIdentifier {
	// identify the process and thread/timeline this event occurs:
	private final int rank;
	private final int thread;	
	
	public ProcessIdentifier(int rank, int thread) {
		this.rank = rank;
		this.thread = thread;
	}
	
	public int getRank() {
		return rank;
	}
	
	public int getThread() {
		return thread;
	}
	
	@Override
	public String toString() {
		return ("<" + rank + "," +thread + ">");
	}
}

package hdTraceInput;

import java.util.ArrayList;
import java.util.Enumeration;

import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * walks through a list of XMLTraceEntries between a start and endtime (latest object is one starting before endTime)
 * 
 * @author julian
 */
public class ReaderTraceElementEnumerator implements Enumeration<XMLTraceEntry>{

	protected int currentPos;
	protected XMLTraceEntry currentEntry;
	
	final protected ArrayList<XMLTraceEntry> entries;	
	final protected Epoch endTime;
	boolean hasMoreElements;
	
	private void updateHasMoreElements(){
		hasMoreElements = currentPos >= 0 && currentPos < entries.size() && (entries.get(currentPos).getEarliestTime().compareTo(endTime) < 0);
	}
	
	public ReaderTraceElementEnumerator(BufferedTraceFileReader reader, Epoch startTime, Epoch endTime) {
		this.currentPos = reader.getTraceEntryPositionLaterThan(startTime);
		
		this.entries = reader.getTraceEntries();
		this.endTime = endTime;
	

		updateHasMoreElements();	
		
		if(hasMoreElements)
			currentEntry = entries.get(currentPos++);
	}

	@Override
	public boolean hasMoreElements() {		
		return hasMoreElements;
	}
	
	@Override
	public XMLTraceEntry nextElement() {
		final XMLTraceEntry old = currentEntry;

		updateHasMoreElements();	
		if(hasMoreElements)
			currentEntry = entries.get(currentPos++);
		
		return old;
	}
	
	/**
	 * Return the nesting depth of the next element (does only work for a nested enumerator) 
	 * @return 0 (by default)
	 */
	public int getNestingDepthOfNextElement(){
		return 0;
	}
}

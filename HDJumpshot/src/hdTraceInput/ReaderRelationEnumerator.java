package hdTraceInput;

import java.util.ArrayList;
import java.util.Enumeration;

import de.hd.pvs.TraceFormat.relation.RelationEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Enumerate the relations with an increasing start and end time.
 * 
 * @author julian
 *
 */
public class ReaderRelationEnumerator implements Enumeration<RelationEntry> {	
	private int pos;
	
	protected int lastPos;	
	final protected ArrayList<RelationEntry> entries;
	
	ReaderRelationEnumerator(
			ArrayList<RelationEntry> entries
		) {
		this.entries = entries;
		this.pos = 0;
		this.lastPos = entries.size() - 1;
	}
	
	ReaderRelationEnumerator(
			ArrayList<RelationEntry> entries,
			Epoch startTime,
			Epoch endTime
		) {
		this.entries = entries;
		
		this.pos = ArraySearcher.getPositionEntryOverlappingOrLaterThan(entries, startTime);	
		if(this.pos < 0){
			pos = entries.size();
			this.lastPos = -1;
			return;
		}
		
		final int lastPos = ArraySearcher.getPositionEntryOverlappingOrLaterThan(entries, endTime);
		if(lastPos == -1){
			this.lastPos = entries.size() - 1;
		}else if(entries.get(lastPos).getEarliestTime().compareTo(endTime) >= 0 ){
			this.lastPos = lastPos -1 ;
		}else{
			this.lastPos = lastPos;
		}
	}

	@Override
	public boolean hasMoreElements() {
		return pos <= lastPos;
	}
	
	@Override
	public RelationEntry nextElement() {
		return entries.get(pos++);
	}
}

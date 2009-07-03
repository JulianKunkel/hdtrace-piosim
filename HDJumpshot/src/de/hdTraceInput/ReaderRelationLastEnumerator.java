package de.hdTraceInput;

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.relation.RelationEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * This enumerator expects the entries to be sorted by start time.
 * It returns ALL overlapping relation entries (not only the first)
 * 
 * @author julian
 *
 */
public class ReaderRelationLastEnumerator extends ReaderRelationEnumerator{
	public ReaderRelationLastEnumerator(ArrayList<RelationEntry> entries, Epoch startTime, Epoch endTime) 
	{ 
		super(entries, startTime, endTime);
		if(super.lastPos < 0){
			return;
		}
		
		// try to find the last overlapping state.
		while(super.lastPos < entries.size()){
			if(entries.get(lastPos).getEarliestTime().compareTo(endTime) > 0){
				lastPos--;
				break;
			}
			// earliest time smaller than end time => still overlapping
			lastPos++;
		}
		if(lastPos == entries.size()){
			lastPos--;
		}
	}
}

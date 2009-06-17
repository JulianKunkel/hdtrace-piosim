package hdTraceInput;

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.ITracableObject;
import de.hd.pvs.TraceFormat.relation.RelationEntry;
import de.hd.pvs.TraceFormat.relation.RelationHeader;
import de.hd.pvs.TraceFormat.relation.RelationXMLReader;
import de.hd.pvs.TraceFormat.trace.RelationSource;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Reads a relation file and buffers it in memory, also layouts overlapping entries onto several lines.
 * 
 * @author julian
 */
public class BufferedRelationReader implements IBufferedReader, RelationSource {

	/**
	 * List sorted by end time of buffered entries contained in the file
	 */
	final ArrayList<RelationEntry> entriesEndTimeSorted = new ArrayList<RelationEntry>(20);	
	
	/**
	 * Maps overlapping relations onto a separate line. Therefore, this array is sorted by start AND end time.
	 */
	final ArrayList<ArrayList<RelationEntry>> layoutedEntriesSorted = new ArrayList<ArrayList<RelationEntry>>();
	
	final RelationHeader header;
	
	public BufferedRelationReader(String filename) throws Exception {
		final RelationXMLReader reader = new RelationXMLReader(filename);
		header = reader.getHeader();
		
		// now add all entries
		outer: while(true){
			final RelationEntry entry = reader.getNextEntry();
			if(entry == null){
				break;
			}
			
			entriesEndTimeSorted.add(entry);
			
			// layout the current entry:
			// try to check if it fits on a existing line.
			for(ArrayList<RelationEntry> list: layoutedEntriesSorted){
				// compare entry time with last entry in the list
				if(list.get(list.size() -1).getLatestTime().compareTo(entry.getEarliestTime()) <= 0){
					// it fits into the old timeline.
					list.add(entry);
					continue outer;
				}
			}
			
			// if we reach here it does not fit => create a new line.
			final ArrayList<RelationEntry> list = new ArrayList<RelationEntry>();
			layoutedEntriesSorted.add(list);
			list.add(entry);
		}		
	}
	
	@Override
	public Epoch getMaxTime() {
		return entriesEndTimeSorted.get(entriesEndTimeSorted.size() -1 ).getLatestTime();
	}

	@Override
	public Epoch getMinTime() {
		return entriesEndTimeSorted.get(0).getEarliestTime();
	}

	/**
	 * Return the maximum number of relation entries which are active to a given time.
	 * This is the number of lines needed to render them without overlapping.
	 */
	public int getMaximumConcurrentRelationEntries(){
		return layoutedEntriesSorted.size();
	}
	
	public RelationHeader getHeader() {
		return header;
	}

	@Override
	public ITracableObject getTraceEntryClosestToTime(Epoch time) {
		// pick up one.
		int pos = ArraySearcher.getPositionEntryOverlappingOrLaterThan(entriesEndTimeSorted, time);
		if (pos == -1)
			pos = entriesEndTimeSorted.size() -1;

		return entriesEndTimeSorted.get(pos);
	}
		
	public RelationEntry getTraceEntryClosestToTime(Epoch time, int line) {
		int pos = getPositionAfter(time, line);
		if (pos == -1)
			pos = getEntriesOnLine(line).size() -1;

		return getEntriesOnLine(line).get(pos);
	}
	
	public int getPositionAfter(Epoch minEndTime, int line){
		final ArrayList<RelationEntry> entries = getEntriesOnLine(line);
		return ArraySearcher.getPositionEntryOverlappingOrLaterThan(entries, minEndTime);
	}

	public ReaderRelationEnumerator enumerateRelations(){
		return new ReaderRelationEnumerator(entriesEndTimeSorted);
	}
	
	public ReaderRelationEnumerator enumerateRelations(Epoch startTime, Epoch endTime, int line){
		return new ReaderRelationEnumerator(layoutedEntriesSorted.get(line), startTime, endTime);
	}

	public ArrayList<RelationEntry> getEntriesOnLine(int line) {
		return layoutedEntriesSorted.get(line);
	}
	
	public ArrayList<RelationEntry> getEntries() {
		return entriesEndTimeSorted;
	}
	
	public ITraceElementEnumerator enumerateTraceEntries(
			boolean nested, Epoch startTime, Epoch endTime, int line) {
		final ReaderRelationEnumerator relEnum = enumerateRelations(startTime, endTime, line);
		
		if(! nested)
			return new ReaderRelationTraceElementEnumerator(relEnum, startTime, endTime);
		else
			return new ReaderRelationTraceElementNestedEnumerator(relEnum, startTime, endTime);
	}	
}

package hdTraceInput;

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.TraceObject;
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
	 * Time sorted list of buffered entries contained in the file. 
	 */
	final ArrayList<RelationEntry> entries = new ArrayList<RelationEntry>(20);
	
	/**
	 * Maps overlapping relations onto a separate line.
	 */
	final ArrayList<ArrayList<RelationEntry>> layoutedEntries = new ArrayList<ArrayList<RelationEntry>>();
	
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
			
			entries.add(entry);
			
			// layout the current entry:
			// try to check if it fits on a existing line.
			for(ArrayList<RelationEntry> list: layoutedEntries){
				// compare entry time with last entry in the list
				if(list.get(list.size() -1).getLatestTime().compareTo(entry.getEarliestTime()) <= 0){
					// it fits into the old timeline.
					list.add(entry);
					continue outer;
				}
			}
			
			// if we reach here it does not fit => create a new line.
			ArrayList<RelationEntry> list = new ArrayList<RelationEntry>();
			layoutedEntries.add(list);
			list.add(entry);
		}		
	}
	
	@Override
	public Epoch getMaxTime() {
		return entries.get(entries.size() -1 ).getLatestTime();
	}

	@Override
	public Epoch getMinTime() {
		return entries.get(0).getEarliestTime();
	}

	/**
	 * Return the maximum number of relation entries which are active to a given time.
	 * This is the number of lines needed to render them without overlapping.
	 */
	public int getMaximumConcurrentRelationEntries(){
		return layoutedEntries.size();
	}
	
	public RelationHeader getHeader() {
		return header;
	}

	@Override
	public TraceObject getTraceEntryClosestToTime(Epoch time) {
		throw new IllegalArgumentException("This function does not need to be implemented! Use getTraceEntryClosestToTime(Epoch time, int line)");
	}
		
	public RelationEntry getTraceEntryClosestToTime(Epoch time, int line) {
		int pos = getPositionAfter(time, line);
		if (pos == -1)
			pos = getEntriesOnLine(line).size() -1;

		return getEntriesOnLine(line).get(pos);
	}
	
	public int getPositionAfter(Epoch minEndTime, int line){
		final ArrayList<RelationEntry> entries = getEntriesOnLine(line);
		int min = 0; 
		int max = entries.size() - 1;
		
		while(true){						
			int cur = (min + max) / 2;
			final RelationEntry entry = entries.get(cur);
			
			if(min == max){ // found entry or stopped.
				
				if(entry.getLatestTime().compareTo(minEndTime) < 0 && cur == 0){
					// there was no entry before!
					return -1;
				}

				return cur;
			} 
			// not found => continue bin search:

			if ( entry.getLatestTime().compareTo(minEndTime) >= 0 ){
				max = cur;
			}else{
				min = cur + 1;
			}
		}
	}
	
	public ReaderRelationEnumerator enumerateRelations(){
		return new ReaderRelationEnumerator(entries);
	}
	
	public ReaderRelationEnumerator enumerateRelations(int line){
		return new ReaderRelationEnumerator(layoutedEntries.get(line));
	}

	public ArrayList<RelationEntry> getEntriesOnLine(int line) {
		return layoutedEntries.get(line);
	}
	
	public ArrayList<RelationEntry> getEntries() {
		return entries;
	}
}
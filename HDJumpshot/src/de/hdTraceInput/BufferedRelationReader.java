package de.hdTraceInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

import com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator.HeapNode;

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
	final ArrayList<RelationEntry> entriesStartTimeSorted; 

	/**
	 * Maps overlapping relations onto a separate line. Therefore, this array is sorted by start AND end time.
	 */
	final ArrayList<ArrayList<RelationEntry>> layoutedEntriesSorted = new ArrayList<ArrayList<RelationEntry>>();

	final RelationHeader header;
	
	final String filename;
	final private Epoch additionalTimeOffset;
	
	public BufferedRelationReader(String filename, Epoch additionalTimeOffset) throws Exception {
		this.filename = filename;
		final RelationXMLReader reader = new RelationXMLReader(filename, additionalTimeOffset);
		this.additionalTimeOffset = additionalTimeOffset;
		
		header = reader.getHeader();
		
		while(true){
			final RelationEntry entry = reader.getNextEntry();
			if(entry == null){
				break;
			}

			entriesEndTimeSorted.add(entry);
		}


		entriesStartTimeSorted = new ArrayList<RelationEntry>(entriesEndTimeSorted);
		// sort the list depending on start time:
		Collections.sort(entriesStartTimeSorted, new Comparator<RelationEntry>(){
			@Override
			public int compare(RelationEntry o1, RelationEntry o2) {
				return o1.getEarliestTime().compareTo(o2.getEarliestTime());
			}			
		});

		PriorityQueue<ArrayList<RelationEntry>> earliestFinishedList = new PriorityQueue<ArrayList<RelationEntry>>(10, new Comparator<ArrayList<RelationEntry>>() {
			@Override
			public int compare(ArrayList<RelationEntry> o1,	ArrayList<RelationEntry> o2) {
				final Epoch earliest = o1.get(o1.size() -1).getLatestTime();
				final Epoch earliest2 = o2.get(o2.size() -1).getLatestTime();
		
				return earliest.compareTo(earliest2);
			}
		});
		

		// now add all entries
		for(RelationEntry entry: entriesStartTimeSorted){
			
			// layout the current entry:
			// try to check if it fits on a existing line.
     		// compare entry time with last entry in the list
			if (earliestFinishedList.size() > 0){
				ArrayList<RelationEntry> list = earliestFinishedList.peek();				
				final Epoch latest = list.get(list.size() -1).getLatestTime();
				
				if(latest.compareTo(entry.getEarliestTime()) <= 0){
					// it fits into the old timeline.
					list.add(entry);
					
					earliestFinishedList.poll();
					
					earliestFinishedList.add(list);
					
					continue;
				}
			}
			// if we reach here it does not fit => create a new line.
			final ArrayList<RelationEntry> list = new ArrayList<RelationEntry>();
			list.add(entry);		
						
			layoutedEntriesSorted.add(list);
			earliestFinishedList.add(list);	
			
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

	public ReaderRelationEnumerator enumerateRelations(Epoch startTime, Epoch endTime){
		return new ReaderRelationLastEnumerator(entriesStartTimeSorted, startTime, endTime);
	}

	public ReaderRelationEnumerator enumerateRelations(int line){
		return new ReaderRelationEnumerator(layoutedEntriesSorted.get(line));
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

	public ITraceElementEnumerator enumerateTraceEntries(boolean nested, Epoch startTime, Epoch endTime, int line) {
		final ReaderRelationEnumerator relEnum = enumerateRelations(startTime, endTime, line);

		if(! nested)
			return new ReaderRelationTraceElementEnumerator(relEnum, startTime, endTime);
		else
			return new ReaderRelationTraceElementNestedEnumerator(relEnum, startTime, endTime);
	}	
	
	public String getFilename() {
		return filename;
	}
	
	public Epoch getAdditionalTimeOffset() {
		return additionalTimeOffset;
	}
}

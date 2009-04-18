package arrow;

import hdTraceInput.BufferedTraceFileReader;
import hdTraceInput.TraceFormatBufferedFileReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.topology.TopologyEntry;
import de.hd.pvs.TraceFormat.trace.TraceEntry;

/**
 * Compute arrows for client MPI communication.
 * 
 * @author julian
 */
public class ClientMPICommunicationArrowComputer implements ArrowComputer{
	
	/**
	 * see MPI: Message Envelope for matching
	 */
	private static class MSGMatcher{
		final int rank;
		final int tag;
		final int communicator;
		
		public MSGMatcher(int rank, int tag, int comm) {
			this.rank = rank;
			this.tag = tag;
			this.communicator = comm;
		}
		
		@Override
		public boolean equals(Object obj) {
			final MSGMatcher msg = (MSGMatcher) obj;
			return msg.rank == rank && msg.tag == tag && msg.communicator == communicator;
		}
		
		@Override
		public int hashCode() {			
			return rank * 100 + tag;
		}
	}
	
	private static class PreviousEntry{
		final TopologyEntry topo;
		final TraceEntry    entry;
		
		public PreviousEntry(TopologyEntry topo, TraceEntry entry) {
			this.topo = topo;
			this.entry = entry;
		}
	}
	
	
	@Override
	public ArrowGroup computeArrows(TraceFormatBufferedFileReader reader) {
		final ArrayList<Arrow> arrows = new ArrayList<Arrow>();
		final ArrayList<ArrowCategory> categories = new ArrayList<ArrowCategory>();				
		
		for(int i=0 ; i < reader.getNumberOfFilesLoaded(); i++){
			final HashMap<MSGMatcher, LinkedList<PreviousEntry>> sends = new HashMap<MSGMatcher, LinkedList<PreviousEntry>>();
			final HashMap<MSGMatcher, LinkedList<PreviousEntry>> rcvs = new HashMap<MSGMatcher, LinkedList<PreviousEntry>>();
			
			final TraceFormatFileOpener file = reader.getLoadedFile(i);
			for(final TopologyEntry topology: file.getTopology().getSubTopologies()){
				if(topology.getTraceSource() != null){
					// found one trace file.
					final BufferedTraceFileReader traceReader = (BufferedTraceFileReader) topology.getTraceSource();
					final Enumeration<TraceEntry>  traceEntries = traceReader.enumerateNestedTraceEntry();
					while(traceEntries.hasMoreElements()){
						final TraceEntry entry = traceEntries.nextElement();
						final String name = entry.getName();
						final HashMap<String, String> attributes = entry.getAttributes();
						final String comm = attributes.get("comm");

						if(name.contains("Send")){							
							final String rankStr = attributes.get("toRank");
							final String tagStr = attributes.get("toTag");
							
							if(rankStr != null && tagStr != null && comm != null){
								final MSGMatcher matcher = new MSGMatcher(Integer.parseInt(rankStr), 
										Integer.parseInt(tagStr), Integer.parseInt(comm));
								final LinkedList<PreviousEntry> old = rcvs.get(matcher);
								
								if(old != null){
									final PreviousEntry oldEntry = old.pollLast();
									// matches already
									arrows.add( new Arrow(oldEntry.topo, oldEntry.entry.getEarliestTime(), 
											topology, entry.getEarliestTime(), null) );
									
									if(old.size() == 0){
										rcvs.remove(matcher);
									}
								}else{
									LinkedList<PreviousEntry> prev = sends.get(matcher);
									if(prev == null){
										prev = new LinkedList<PreviousEntry>();
										sends.put(matcher, prev);
									}
									prev.push(new PreviousEntry(topology, entry));
								}
							}
						}
						
						if(name.toLowerCase().contains("recv")){
							final String rankStr = attributes.get("fromRank");
							final String tagStr = attributes.get("fromTag");
							
							if(rankStr != null && tagStr != null && comm != null){
								final MSGMatcher matcher = new MSGMatcher(Integer.parseInt(rankStr), 
										Integer.parseInt(tagStr), Integer.parseInt(comm));
								final LinkedList<PreviousEntry> old = rcvs.get(matcher);
								
								if(old != null){
									final PreviousEntry oldEntry = old.pollLast();
									// matches already
									arrows.add( new Arrow(oldEntry.topo, oldEntry.entry.getEarliestTime(), 
											topology, entry.getEarliestTime(), null) );
									
									if(old.size() == 0){
										rcvs.remove(matcher);
									}
								}else{
									LinkedList<PreviousEntry> prev = rcvs.get(matcher);
									if(prev == null){
										prev = new LinkedList<PreviousEntry>();
										rcvs.put(matcher, prev);
									}
									prev.push(new PreviousEntry(topology, entry));
								}								
							}
						}
					}
				}
			}
		}
		
		// sort the arrows by starting time:
		Collections.sort(arrows, new Comparator<Arrow>(){
			@Override
			public int compare(Arrow o1, Arrow o2) {				
				return o1.getStartTime().compareTo(o2.getStartTime());
			}
		});
				
		return new ArrowGroup("Client MPI Communication", arrows, categories);
	}
}

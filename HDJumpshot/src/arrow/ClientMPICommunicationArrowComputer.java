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

	final ArrowCategory category = new ArrowCategory("MPI communication", null);
	
	@Override
	public ArrowCategory getResponsibleCategory() {		
		return category;
	}
	
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
	
	private void addArrow(ArrayList<Arrow> arrows,
			int rank, 
			HashMap<MSGMatcher, LinkedList<PreviousEntry>> earlyMSGs, 
			HashMap<Integer, HashMap<MSGMatcher, LinkedList<PreviousEntry>>> unmatched,
			TraceEntry entry,
			TopologyEntry topology,
			String targetRankStr, String tagStr, String commStr)
	{
		final int tag = Integer.parseInt(tagStr);
		final int comm = Integer.parseInt(commStr);
		
		final MSGMatcher matcher = new MSGMatcher(rank, tag, comm);
		final int targetRank = Integer.parseInt(targetRankStr);
		
		final HashMap<MSGMatcher, LinkedList<PreviousEntry>> rankMatching = unmatched.get(targetRank);
		LinkedList<PreviousEntry> old = null;
		if(rankMatching != null){
			old = rankMatching.get(matcher);
		}

		if(old != null){
			// found a matching tag.
			final PreviousEntry oldEntry = old.pollLast();
			// matches already
			arrows.add( new Arrow(oldEntry.topo, oldEntry.entry.getEarliestTime(), 
					topology, entry.getEarliestTime(), category) );

			if(old.size() == 0){
				rankMatching.remove(matcher);
			}
		}else{
			// make a new unmatched one.
			final MSGMatcher tmatcher = new MSGMatcher(targetRank, tag, comm);
			LinkedList<PreviousEntry> prev = earlyMSGs.get(tmatcher);
			if(prev == null){
				prev = new LinkedList<PreviousEntry>();
				earlyMSGs.put(tmatcher, prev);
			}
			prev.push(new PreviousEntry(topology, entry));
		}
	}


	@Override
	public ArrowsOrdered computeArrows(TraceFormatBufferedFileReader reader) {
		final ArrayList<Arrow> arrows = new ArrayList<Arrow>();

		for(int i=0 ; i < reader.getNumberOfFilesLoaded(); i++){
			// scan for rank label
			final TraceFormatFileOpener file = reader.getLoadedFile(i);
			int rankDepth = -1;
			for(final String label: file.getTopologyLabels().getLabels()){
				rankDepth++;

				if(label.equals("Rank")){
					break;
				}				
			}

			if(rankDepth == -1){
				// not found
				continue;
			}

			// drill down to the rank topology level with a BFS
			
			final LinkedList<TopologyEntry> rankTopos = file.getTopology().getChildrenOfDepth(rankDepth);

			// maps a sender rank to the msg matcher.
			final HashMap<Integer, HashMap<MSGMatcher, LinkedList<PreviousEntry>>> earlySends = new HashMap<Integer, HashMap<MSGMatcher,LinkedList<PreviousEntry>>>();

			// maps a receiver rank to the msg matcher.
			final HashMap<Integer, HashMap<MSGMatcher, LinkedList<PreviousEntry>>> earlyRcvs = new HashMap<Integer, HashMap<MSGMatcher,LinkedList<PreviousEntry>>>();

			for(final TopologyEntry rankTopo: rankTopos){
				final int rank = Integer.parseInt(rankTopo.getLabel());				
				
				// unmatched sends and receives of the current rank:
				final HashMap<MSGMatcher, LinkedList<PreviousEntry>> earlyRankSends = new HashMap<MSGMatcher, LinkedList<PreviousEntry>>();
				final HashMap<MSGMatcher, LinkedList<PreviousEntry>> earlyRankRcvs = new HashMap<MSGMatcher, LinkedList<PreviousEntry>>();
				
				earlySends.put(rank, earlyRankSends);
				earlyRcvs.put(rank, earlyRankRcvs);
								
				// scan children, i.e. threads 
				for(final TopologyEntry topology: rankTopo.getSubTopologies()){

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
									addArrow(arrows, rank, earlyRankSends, earlyRcvs, entry, topology, rankStr, tagStr, comm);
								}
							}

							if(name.toLowerCase().contains("recv")){
								final String rankStr = attributes.get("fromRank");
								final String tagStr = attributes.get("fromTag");

								if(rankStr != null && tagStr != null && comm != null){
									addArrow(arrows, rank, earlyRankRcvs, earlySends, entry, topology, rankStr, tagStr, comm);								
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

		return new ArrowsOrdered(arrows);
	}
}

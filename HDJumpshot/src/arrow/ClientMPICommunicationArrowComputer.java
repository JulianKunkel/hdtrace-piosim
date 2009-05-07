//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.

package arrow;

import hdTraceInput.BufferedTraceFileReader;
import hdTraceInput.TraceFormatBufferedFileReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;

import viewer.timelines.topologyPlugins.MPIConstants;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.TraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Compute arrows for client MPI communication.
 * 
 * @author Julian M. Kunkel
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
		final TopologyNode topo;
		final TraceEntry    entry;

		public PreviousEntry(TopologyNode topo, TraceEntry entry) {
			this.topo = topo;
			this.entry = entry;
		}
	}
	
	private void addArrow(ArrayList<Arrow> arrows,
			int rank, 
			HashMap<MSGMatcher, LinkedList<PreviousEntry>> earlyMSGs, 
			HashMap<Integer, HashMap<MSGMatcher, LinkedList<PreviousEntry>>> unmatched,
			TraceEntry entry,
			TopologyNode topology,
			String targetRankStr, String tagStr, String commStr,
			boolean useEarliestTime)
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
			final Epoch startTime;
			final Epoch endTime;
			
			if(useEarliestTime ){
				startTime = oldEntry.entry.getEarliestTime();
				endTime = entry.getLatestTime();
			}else{
				startTime = oldEntry.entry.getLatestTime();
				endTime = entry.getEarliestTime();
			}
				
			arrows.add( new Arrow(oldEntry.topo, startTime, 
					topology, endTime, category) );

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
			for(final String label: file.getTopologyLabels().getTypes()){
				rankDepth++;

				if(label.equals(MPIConstants.RANK_TOPOLOGY)){
					break;
				}				
			}

			if(rankDepth == -1){
				// not found
				continue;
			}

			// drill down to the rank topology level with a BFS
			
			final LinkedList<TopologyNode> rankTopos = file.getTopology().getChildrenOfDepth(rankDepth);

			// maps a sender rank to the msg matcher.
			final HashMap<Integer, HashMap<MSGMatcher, LinkedList<PreviousEntry>>> earlySends = new HashMap<Integer, HashMap<MSGMatcher,LinkedList<PreviousEntry>>>();

			// maps a receiver rank to the msg matcher.
			final HashMap<Integer, HashMap<MSGMatcher, LinkedList<PreviousEntry>>> earlyRcvs = new HashMap<Integer, HashMap<MSGMatcher,LinkedList<PreviousEntry>>>();

			for(final TopologyNode rankTopo: rankTopos){
				final int rank = Integer.parseInt(rankTopo.getName());				
				
				// unmatched sends and receives of the current rank:
				final HashMap<MSGMatcher, LinkedList<PreviousEntry>> earlyRankSends = new HashMap<MSGMatcher, LinkedList<PreviousEntry>>();
				final HashMap<MSGMatcher, LinkedList<PreviousEntry>> earlyRankRcvs = new HashMap<MSGMatcher, LinkedList<PreviousEntry>>();
				
				earlySends.put(rank, earlyRankSends);
				earlyRcvs.put(rank, earlyRankRcvs);
								
				// scan children, i.e. threads 
				for(final TopologyNode topology: rankTopo.getSubTopologies()){

					if(topology.getTraceSource() != null){
						// found one trace file.
						final BufferedTraceFileReader traceReader = (BufferedTraceFileReader) topology.getTraceSource();
						final Enumeration<TraceEntry>  traceEntries = traceReader.enumerateNestedTraceEntry();
						while(traceEntries.hasMoreElements()){
							final TraceEntry entry = traceEntries.nextElement();
							final String name = entry.getName();
							final HashMap<String, String> attributes = entry.getAttributes();
							final String comm = attributes.get("cid");

							if(name.contains("Send")){							
								final String rankStr = attributes.get("toRank");
								final String tagStr = attributes.get("toTag");

								if(rankStr != null && tagStr != null && comm != null){
									addArrow(arrows, rank, earlyRankSends, earlyRcvs, entry, topology, rankStr, tagStr, comm, true);
								}
							}

							if(name.toLowerCase().contains("recv")){
								final String rankStr = attributes.get("fromRank");
								final String tagStr = attributes.get("fromTag");

								if(rankStr != null && tagStr != null && comm != null){
									addArrow(arrows, rank, earlyRankRcvs, earlySends, entry, topology, rankStr, tagStr, comm, false);								
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

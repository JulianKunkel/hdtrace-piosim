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

package de.arrow;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.ReservedTopologyNames;
import de.hd.pvs.TraceFormat.SimpleConsoleLogger;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hdTraceInput.BufferedTraceFileReader;
import de.hdTraceInput.TraceFormatBufferedFileReader;
import de.topology.TopologyManager;
import de.topology.TopologyManagerContents;
import de.topology.TopologyTraceTreeNode;
import de.topology.TopologyTreeNode;
import de.viewer.timelines.TimelineType;
import de.viewer.timelines.topologyPlugins.MPIConstants;

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
		final TopologyTreeNode topo;
		final ITraceEntry    entry;

		public PreviousEntry(TopologyTreeNode topo, ITraceEntry entry) {
			this.topo = topo;
			this.entry = entry;
		}
	}

	private void addArrow(ArrayList<Arrow> arrows,
			int rank, 
			HashMap<MSGMatcher, LinkedList<PreviousEntry>> earlyMSGsForMyRank, 
			HashMap<Integer, HashMap<MSGMatcher, LinkedList<PreviousEntry>>> unmatched,	
			ITraceEntry entry,
			TopologyTreeNode topoTreeNode,
			String targetRankStr, String tagStr, String commStr,
			boolean isSend)
	{
		final int tag = Integer.parseInt(tagStr);
		final int comm = Integer.parseInt(commStr);
		
		final MSGMatcher matcher = new MSGMatcher(rank, tag, comm);
		final int communicationPartnerRank = Integer.parseInt(targetRankStr);
		
		final HashMap<MSGMatcher, LinkedList<PreviousEntry>> rankMatching = unmatched.get(communicationPartnerRank);
		LinkedList<PreviousEntry> old = null;
		if(rankMatching != null){
			old = rankMatching.get(matcher);
		}

		if(old != null){
			// found a matching tag.
			final PreviousEntry oldEntry = old.pollLast();
			// matches already
			final Arrow arrow;
			
			if(isSend){
				arrow = new Arrow(topoTreeNode, entry.getEarliestTime(), oldEntry.topo, oldEntry.entry.getLatestTime(), category);
			}else{
				arrow = new Arrow(oldEntry.topo, oldEntry.entry.getEarliestTime(), topoTreeNode, entry.getLatestTime(),category);
			}
			
			arrows.add( arrow );

			if(old.size() == 0){
				rankMatching.remove(matcher);
			}
			
		}else{
			
			// make a new unmatched one.
			final MSGMatcher tmatcher = new MSGMatcher(communicationPartnerRank, tag, comm);
			LinkedList<PreviousEntry> prev = earlyMSGsForMyRank.get(tmatcher);
			if(prev == null){
				prev = new LinkedList<PreviousEntry>();
				earlyMSGsForMyRank.put(tmatcher, prev);
			}
			prev.push(new PreviousEntry(topoTreeNode, entry));
		}
	}

	private static class EarlyMessages{
		final HashMap<Integer, HashMap<MSGMatcher, LinkedList<PreviousEntry>>> earlySends = new HashMap<Integer, HashMap<MSGMatcher,LinkedList<PreviousEntry>>>();
		final HashMap<Integer, HashMap<MSGMatcher, LinkedList<PreviousEntry>>> earlyRcvs = new HashMap<Integer, HashMap<MSGMatcher,LinkedList<PreviousEntry>>>();
	}

	@Override
	public ArrowsOrdered computeArrows(TraceFormatBufferedFileReader reader) {
		final ArrayList<Arrow> arrows = new ArrayList<Arrow>();
		// create a fake topology manager:
		final TopologyManager m = new TopologyManager(reader, null, TopologyManagerContents.TRACE_ONLY);

		// first phase, fill relation manager.
		m.restoreTopology();

		// maps the "file" to the earlySends and Recvs. 
		final HashMap<TopologyTreeNode, EarlyMessages> earlyMessagesGlobal = new HashMap<TopologyTreeNode, EarlyMessages>();

		for( int timeline = 0; timeline < m.getTimelineNumber(); timeline++){			
			final TopologyTreeNode rankNode = m.getTreeNodeForTimeline(timeline);
			if(rankNode == null){
				continue;
			}
			
			final TopologyNode rankTopo = rankNode.getTopology();	
			if(rankTopo == null){
				continue;
			}
			
			// continue only if we are the rank topology.
			if(! rankTopo.getType().equals(MPIConstants.RANK_TOPOLOGY)){
				continue;
			}
			
			// lookup file:
			final TopologyTreeNode fileNode = rankNode.getParentTreeNodeWithTopologyLabel(ReservedTopologyNames.File.toString());
			if(fileNode == null){
				SimpleConsoleLogger.Warning("Topology with reserved name:" + ReservedTopologyNames.File + " not found as parent " + rankNode.getTopology().toRecursiveString());
				continue;
			}						
						
			// maps a sender rank to the msg matcher.
			final EarlyMessages earlyMessages;
			
			if(earlyMessagesGlobal.containsKey(fileNode)){
				earlyMessages = earlyMessagesGlobal.get(fileNode);
			}else{
				earlyMessages = new EarlyMessages();
				earlyMessagesGlobal.put(fileNode, earlyMessages);	
			}
			
			final int rank = Integer.parseInt(rankTopo.getName());				

			// unmatched sends and receives of the current rank:
			final HashMap<MSGMatcher, LinkedList<PreviousEntry>> earlyRankSends;
			final HashMap<MSGMatcher, LinkedList<PreviousEntry>> earlyRankRcvs;

			if(! earlyMessages.earlyRcvs.containsKey(rank)){
				earlyRankSends = new HashMap<MSGMatcher, LinkedList<PreviousEntry>>();
				earlyRankRcvs = new HashMap<MSGMatcher, LinkedList<PreviousEntry>>();

				earlyMessages.earlyRcvs.put(rank, earlyRankRcvs);
				earlyMessages.earlySends.put(rank, earlyRankSends);
			}else{
				earlyRankSends = earlyMessages.earlySends.get(rank);
				earlyRankRcvs = earlyMessages.earlyRcvs.get(rank);
			}
			
			// scan children, i.e. threads
			for(final TopologyTreeNode childTopoNode: rankNode.getTopologyTreeNodeChildren()){
				if( childTopoNode.getType() == TimelineType.TRACE ){
					// found one trace file.
					final BufferedTraceFileReader traceReader = (BufferedTraceFileReader) ((TopologyTraceTreeNode) childTopoNode).getTraceSource();
					
					final Enumeration<ITraceEntry>  traceEntries = traceReader.enumerateNestedTraceEntry();
					while(traceEntries.hasMoreElements()){
						final ITraceEntry entry = traceEntries.nextElement();
						final String name = entry.getName();
						final HashMap<String, String> attributes = entry.getAttributes();
						final String comm = attributes.get("cid");

						if(name.equals("Send") || name.equals("Isend") || name.equals("Sendrecv")){							
							final String rankStr = attributes.get("toRank");
							final String tagStr = attributes.get("toTag");

							if(rankStr != null && tagStr != null && comm != null){
								//System.out.println("Send " + rank + " to " + rankStr  + " tag: " + tagStr + " comm: " + comm + " rcvs: " + earlyRankRcvs.size() + " sends: " + earlyRankSends.size());		

								addArrow(arrows, rank, earlyRankSends, earlyMessages.earlyRcvs, entry, childTopoNode, rankStr, tagStr, comm, true);
							}
						}

						if(name.equals("Recv") || name.equals("Irecv") || name.equals("Sendrecv")){
							final String rankStr = attributes.get("fromRank");
							final String tagStr = attributes.get("fromTag");

							if(rankStr != null && tagStr != null && comm != null){
								//System.out.println("Recvs " + rank + " from " + rankStr  + " tag: " + tagStr + " comm: " + comm + " rcvs: " + earlyRankRcvs.size() + " sends: " + earlyRankSends.size());
								
								addArrow(arrows, rank, earlyRankRcvs, earlyMessages.earlySends,  entry, childTopoNode, rankStr, tagStr, comm, false);								
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

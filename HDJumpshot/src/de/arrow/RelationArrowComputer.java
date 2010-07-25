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

import de.hd.pvs.TraceFormat.relation.RelationEntry;
import de.hdTraceInput.RelationManager;
import de.hdTraceInput.TraceFormatBufferedFileReader;
import de.hdTraceInput.RelationManager.RelationSearchResult;
import de.topology.TopologyManager;
import de.topology.TopologyManagerContents;
import de.topology.TopologyRelationExpandedTreeNode;
import de.topology.TopologyRelationTreeNode;
import de.viewer.timelines.TimelineType;

/**
 * Manufacture arrows for relations
 * 
 * @author Julian M. Kunkel
 */
public class RelationArrowComputer implements ArrowComputer{

	final ArrowCategory category = new ArrowCategory("Relations", null);

	@Override
	public ArrowCategory getResponsibleCategory() {		
		return category;
	}

	@Override
	public ArrowsOrdered computeArrows(TraceFormatBufferedFileReader reader) {
		final ArrayList<Arrow> arrows = new ArrayList<Arrow>();
		final RelationManager relManager = new RelationManager();		

		// create a fake topology manager:
		final TopologyManager m = new TopologyManager(reader, null, TopologyManagerContents.RELATIONS_ONLY);

		// first phase, fill relation manager.
		m.restoreTopology();

		for( int timeline = 0; timeline < m.getTimelineNumber(); timeline++){
			switch (m.getType(timeline)){
			case RELATION:{ 		
				relManager.addFile(m.getRelationReaderForTimeline(timeline), (TopologyRelationTreeNode) m.getTreeNodeForTimeline(timeline));
				break;
			}
			case RELATION_EXPANDED:{
				if( ((TopologyRelationExpandedTreeNode) m.getTreeNodeForTimeline(timeline)).isTheOnlyRelation()){
					relManager.addFile(m.getRelationReaderForTimeline(timeline), (TopologyRelationTreeNode) m.getTreeNodeForTimeline(timeline));
				}
				relManager.addTopologyTreeNode((TopologyRelationExpandedTreeNode) m.getTreeNodeForTimeline(timeline));
				break;
			}			
			}
		}

		for( int timeline = 0; timeline < m.getTimelineNumber(); timeline++){
			if(m.getType(timeline) == TimelineType.RELATION_EXPANDED){				
				final TopologyRelationExpandedTreeNode node = (TopologyRelationExpandedTreeNode) m.getTreeNodeForTimeline(timeline);

				// has a relation, try to create arrows for each entry.
				final Enumeration<RelationEntry> relEntryEnum = node.enumerateEntries(); 
				while(relEntryEnum.hasMoreElements()){
					final RelationEntry relEntry = relEntryEnum.nextElement();
					if(relEntry.getParentToken() == null){
						continue;
					}

					final RelationSearchResult search = relManager.getParentRelationEntry(relEntry);
					if(search == null){
						continue;
					}
					
					arrows.add(new Arrow(
							search.getTopologyRelationExpandedTreeNode(), search.getEntry().getEarliestTime(), search.getEntry(), 
							node, relEntry.getEarliestTime(), relEntry, 
							category));					
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

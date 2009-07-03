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

package de.topology.mappings;

import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hdTraceInput.BufferedRelationReader;
import de.hdTraceInput.BufferedStatisticsFileReader;
import de.hdTraceInput.TraceFormatBufferedFileReader;
import de.topology.TopologyManagerContents;
import de.topology.TopologyRelationExpandedTreeNode;
import de.topology.TopologyRelationTreeNode;
import de.topology.TopologyStatisticTreeNode;
import de.topology.TopologyTreeNode;
import de.viewer.common.SortedJTreeNode;

/**
 * Parent for all topology orders.
 * @author Julian M. Kunkel
 */
abstract public class TopologyTreeMapping {
	abstract public SortedJTreeNode createTopology(TraceFormatBufferedFileReader reader);
	abstract public boolean isAvailable(TraceFormatBufferedFileReader reader);

	private boolean addStatistics = true;
	private boolean addExtendedRelation = true;

	public void setTopologyManagerContents(TopologyManagerContents type){
		this.addStatistics = !(type == TopologyManagerContents.TRACE_ONLY);
		this.addExtendedRelation = (type == TopologyManagerContents.EVERYTHING || type == TopologyManagerContents.RELATIONS_ONLY);
	}

	/**
	 * This function adds for each overlapping line a separate node to the RelationTreeNode 
	 * @param node
	 */
	protected void addRelationTreeNodeChildrenTo(TopologyRelationTreeNode node){
		if(addExtendedRelation){		
			final BufferedRelationReader reader = node.getRelationSource();
			for(int i=0; i < reader.getMaximumConcurrentRelationEntries(); i++){
				TopologyRelationExpandedTreeNode child = new TopologyRelationExpandedTreeNode(i, node.getTopology(), node.getFile());
				node.add(child);
			}
		}
	}

	protected void addTopologyTreeNode(TopologyTreeNode node, SortedJTreeNode parent){
		if(parent != null){
			parent.add(node);
		}
	}

	protected SortedJTreeNode addDummyTreeNode(String name, SortedJTreeNode parent){
		SortedJTreeNode node = new SortedJTreeNode(name);
		parent.add(node);

		return node;
	}


	protected void addStatisticsInTopology(int level, SortedJTreeNode node, TopologyNode topology, TraceFormatFileOpener file){	
		// add statistic nodes:
		for(String group: topology.getStatisticsSources().keySet()){
			BufferedStatisticsFileReader statSource = (BufferedStatisticsFileReader) topology.getStatisticsSource(group);
			final SortedJTreeNode statGroupNode;

			if(statSource.getGroup().getStatisticsOrdered().size() == 1){
				statGroupNode = node;
			}else{			
				statGroupNode = addDummyTreeNode(group, node);
			}


			for(StatisticsDescription statDesc: statSource.getGroup().getStatisticsOrdered()){
				TopologyStatisticTreeNode statNode = new TopologyStatisticTreeNode(statDesc, group, topology, file );

				addTopologyTreeNode(statNode, statGroupNode);
			}
		}
	}

	public boolean isAddStatistics() {
		return addStatistics;
	}	

	/**
	 * Should virtual topologies be created to allow expansion of relations? 
	 * @return
	 */
	public boolean isAddExtendedRelation() {
		return addExtendedRelation;
	}
}

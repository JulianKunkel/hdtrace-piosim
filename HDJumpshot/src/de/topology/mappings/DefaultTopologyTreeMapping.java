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
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hdTraceInput.TraceFormatBufferedFileReader;
import de.topology.TopologyInnerNode;
import de.topology.TopologyRelationTreeNode;
import de.topology.TopologyTraceTreeNode;
import de.topology.TopologyTreeNode;
import de.viewer.common.SortedJTreeNode;

/**
 * Load a default topology, filename => hierarchically print the children 
 */
public class DefaultTopologyTreeMapping extends TopologyTreeMapping{

	public SortedJTreeNode createTopology(TraceFormatBufferedFileReader reader){
		SortedJTreeNode tree_root = new SortedJTreeNode("HDTrace");

		for(int f = 0 ; f < reader.getNumberOfFilesLoaded() ; f++){
			recursivlyAddTopology(1, tree_root, reader.getLoadedFile(f).getTopology(), reader.getLoadedFile(f));
		}

		return tree_root;
	}

	protected void recursivlyAddTopology(int level, SortedJTreeNode parentNode, TopologyNode topology, 
			TraceFormatFileOpener file){		

		final TopologyTreeNode node;
		
		if(topology.getTraceSource() != null){
			node = new TopologyTraceTreeNode(topology.getName(), topology, file);
		}else if (topology.getRelationSource() != null){
			// TODO fix case:  Trace != null AND relation != null
			node = new TopologyRelationTreeNode(topology.getName(), topology, file);
			addRelationTreeNodeChildrenTo((TopologyRelationTreeNode) node);
		}else{
			node = new TopologyInnerNode(topology, file);			
		}
		
		addTopologyTreeNode(node, parentNode);

		if(topology.getChildElements().size() != 0){
			for(TopologyNode child: topology.getChildElements().values()){
				recursivlyAddTopology(level +1, node, child, file);
			}
		}
		if( isAddStatistics() )
			addStatisticsInTopology(level, node, topology, file);
	}

	@Override
	public boolean isAvailable(TraceFormatBufferedFileReader reader) {		
		return true;
	}
}

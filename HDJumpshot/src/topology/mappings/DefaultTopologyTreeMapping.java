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

package topology.mappings;

import hdTraceInput.TraceFormatBufferedFileReader;

import java.util.Collection;

import topology.TopologyInnerNode;
import topology.TopologyTraceTreeNode;
import topology.TopologyTreeNode;
import viewer.common.SortedJTreeNode;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.topology.TopologyEntry;

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

	protected void recursivlyAddTopology(int level, SortedJTreeNode parentNode, TopologyEntry topology, 
			TraceFormatFileOpener file){
		final TopologyTreeNode node = new TopologyInnerNode(topology, file);

		addTopologyTreeNode(node, parentNode);    	

		if(topology.getTraceSource() != null){
			TopologyTreeNode childNode = new TopologyTraceTreeNode("Trace", topology, file);
			addTopologyTreeNode(childNode, node);						
		}

		if(topology.getChildElements().size() != 0){
			// handle leaf level == trace nodes differently:

			Collection<TopologyEntry> children = topology.getChildElements().values();
			boolean leafLevel = children.iterator().next().isLeaf();
			if(leafLevel){
				if(topology.getChildElements().size() == 0)
					// TODO remove this child!
					return;

				final SortedJTreeNode traceParent = addDummyTreeNode("Trace", node);

				for(TopologyEntry child: topology.getChildElements().values()){					
					if (child.getStatisticSources().size() == 0){
						if(child.getTraceSource() != null){
							// only if the file really exists
							TopologyTreeNode childNode = new TopologyTraceTreeNode(child.getLabel(), child, file);
							addTopologyTreeNode(childNode, traceParent);
						}else{
							// TODO remove this child from topology
						}
					}else if(isAddStatistics()){
						// handles statistics on the leaf level:
						final SortedJTreeNode extra = addDummyTreeNode(child.getLabel(), traceParent);

						TopologyTreeNode childNode = new TopologyTraceTreeNode(child.getLabel(), child, file);
						addTopologyTreeNode(childNode, extra);
						addStatisticsInTopology(level, extra, child, file);
					}
				}								
			}else{
				for(TopologyEntry child: topology.getChildElements().values()){
					recursivlyAddTopology(level +1, node, child, file);
				}
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

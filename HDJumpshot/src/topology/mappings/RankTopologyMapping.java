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

import java.util.ArrayList;
import java.util.LinkedList;

import topology.TopologyInnerNode;
import topology.TopologyTraceTreeNode;
import topology.TopologyTreeNode;
import viewer.common.SortedJTreeNode;
import viewer.timelines.topologyPlugins.MPIConstants;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.topology.TopologyNode;

/**
 * Shows ranks first, then threads, levels above are removed.
 * 
 * @author Julian M. Kunkel
 */
public class RankTopologyMapping extends DefaultTopologyTreeMapping{
	@Override
	public SortedJTreeNode createTopology(TraceFormatBufferedFileReader reader) {
		final SortedJTreeNode treeRoot = new SortedJTreeNode("HDTrace");		

		outer: for(int f = 0 ; f < reader.getNumberOfFilesLoaded() ; f++){
			final TraceFormatFileOpener file = reader.getLoadedFile(f);
			final TopologyNode topoRoot = file.getTopology();
			final ArrayList<String> list = file.getTopologyLabels().getLabels();
			
			for(int i=0 ; i < list.size() ; i++){
				if(list.get(i).compareToIgnoreCase(MPIConstants.RANK_TOPOLOGY) == 0){
					
					final TopologyTreeNode fileNode = new TopologyInnerNode(file.getTopology(), file);
					addTopologyTreeNode(fileNode, treeRoot);    	

					if(file.getTopology().getTraceSource() != null){
						TopologyTreeNode childNode = new TopologyTraceTreeNode("Trace", file.getTopology(), file);
						addTopologyTreeNode(childNode, fileNode);			
					}
					
					loadRankTopology(i, fileNode, file);
					continue outer;
				}
			}
			
			recursivlyAddTopology(1, treeRoot, topoRoot, file);
		}

		return treeRoot;	
	}
	
	private void loadRankTopology(int rankPos, SortedJTreeNode parent, TraceFormatFileOpener file)
	{
		// bfs:
		final LinkedList<TopologyNode> rankTopos = file.getTopology().getChildrenOfDepth(rankPos);
		for(TopologyNode topo: rankTopos){
			recursivlyAddTopology(1, parent, topo, file);
		}
	}

	@Override
	public boolean isAvailable(TraceFormatBufferedFileReader reader) {
		// check labels:
		for(int f = 0 ; f < reader.getNumberOfFilesLoaded(); f++){
			final TraceFormatFileOpener file=reader.getLoadedFile(f);
			for(String label: file.getTopologyLabels().getLabels()){
				if(label.compareToIgnoreCase(MPIConstants.RANK_TOPOLOGY) == 0){
					return true;
				}
			}
		}
		return false;
	}
}

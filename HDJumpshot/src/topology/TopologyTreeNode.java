
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
  */

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


package topology;

import java.util.HashMap;

import javax.swing.tree.TreeNode;

import viewer.common.SortedJTreeNode;
import viewer.timelines.TimelineType;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.topology.TopologyNode;

/**
 * 
 * Tree Node object which encapsulates all the information about the file and traces.
 * 
 * @author Julian M. Kunkel
 */
abstract public class TopologyTreeNode extends SortedJTreeNode{
	private static final long serialVersionUID = -5708766035964911422L;
	
	// topology belonging to this node:
	final TopologyNode topology;
	final TraceFormatFileOpener file;	
	
	/**
	 * Topology plugins, on each node of each type only one plugin can be active.
	 */
	final HashMap<Class<? extends ITopologyInputPluginObject>, ITopologyInputPluginObject> plugins = new HashMap<Class<? extends ITopologyInputPluginObject>, ITopologyInputPluginObject>();
	
	abstract public TimelineType getType();
	
	public TopologyTreeNode(TopologyNode topNode,  TraceFormatFileOpener file) {
		this.topology = topNode;
		this.file = file;
		
		// try to apply each topology plugin:
		
	}
	
	public TopologyNode getTopology() {
		return topology;
	}

	public TraceFormatFileOpener getFile() {
		return file;
	}
	
	@Override
	public String toString() {
		return topology.getName();
	}
	
	/**
	 * Search parent nodes to find the topology node which is labeled with
	 * the given text.
	 * @param text
	 * @return
	 */
	public TopologyTreeNode getParentTreeNodeWithTopologyLabel(String text){
		// try to parse communicator:
		TopologyTreeNode cur = this;
		
		// lookup rank up to parent.		
		while(cur != null){			
			if(cur.getTopology().getType().equalsIgnoreCase(text)){
				// found correct node:
				return cur;
			}
			
			TreeNode p = cur.getParent();
			while(p != null && ! TopologyTreeNode.class.isInstance(p)){
				p = p.getParent();
			}
			
			cur = (TopologyTreeNode) p;
		}
		
		return null;
	}
}

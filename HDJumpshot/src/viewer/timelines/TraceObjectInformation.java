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

package viewer.timelines;

import topology.TopologyTreeNode;
import de.hd.pvs.TraceFormat.TraceObject;

public class TraceObjectInformation {
	final TopologyTreeNode node;
	final TraceObject object;
	
	public TraceObjectInformation(TopologyTreeNode topologyTreeNode, TraceObject object) {
		this.node = topologyTreeNode;
		this.object = object;
	}
	
	public TopologyTreeNode getTopologyTreeNode() {
		return node;
	}
	
	public TraceObject getObject() {
		return object;
	}
}

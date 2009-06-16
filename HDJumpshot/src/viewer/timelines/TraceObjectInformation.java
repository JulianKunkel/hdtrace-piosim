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
import de.hd.pvs.TraceFormat.util.Epoch;

public class TraceObjectInformation {
	final TopologyTreeNode node;
	final TraceObject object;	
	final TraceObject rootObject;	// if any	
	final Epoch time;
	
	/**
	 * @param topologyTreeNode
	 * @param rootObject, the object container in which the tracked object is found (or the object itself)
	 * @param childObject
	 * @param time
	 */
	public TraceObjectInformation(TopologyTreeNode topologyTreeNode, TraceObject rootObject, TraceObject childObject, Epoch time) {
		assert(rootObject != null);
		this.node = topologyTreeNode;
		this.object = childObject;
		this.time = time;
		this.rootObject = rootObject;
	}
	
	public TopologyTreeNode getTopologyTreeNode() {
		return node;
	}
	
	public TraceObject getObject() {
		return object;
	}
	
	public TraceObject getRootObject() {
		return rootObject;
	}
	
	public Epoch getTime() {
		return time;
	}
}

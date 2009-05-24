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

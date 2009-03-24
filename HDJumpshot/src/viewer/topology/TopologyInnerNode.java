package viewer.topology;

import viewer.timelines.TimelineType;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.topology.TopologyInternalLevel;

public class TopologyInnerNode extends TopologyTreeNode{
	
	
	public TopologyInnerNode(TopologyInternalLevel topNode,
			TraceFormatFileOpener file, TopologyManager manager) {
		super(topNode, file, manager);
	}

	@Override
	public TimelineType getType() {
		return TimelineType.INNER_NODE;
	}	
}

package viewer.topology;

import viewer.timelines.TimelineType;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.topology.TopologyInternalLevel;
import de.hd.pvs.TraceFormat.topology.TopologyLeafLevel;
import de.hd.pvs.TraceFormat.trace.TraceSource;

public class TopologyTraceTreeNode extends TopologyTreeNode{

	public TopologyTraceTreeNode(TopologyInternalLevel topNode,
			TraceFormatFileOpener file, TopologyManager manager) {
		super(topNode, file, manager);
	}

	public TraceSource getTraceSource(){
		return ((TopologyLeafLevel) getTopology()).getTraceSource();
	}
	
	@Override
	public TimelineType getType() {
		return TimelineType.TRACE;
	}
}

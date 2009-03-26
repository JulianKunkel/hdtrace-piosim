package viewer.topology;

import javax.swing.tree.DefaultMutableTreeNode;

import viewer.timelines.TimelineType;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.topology.TopologyInternalLevel;

/**
 * 
 * Tree Node object which encapsulates all the information about the file and traces.
 * 
 * @author julian
 */
abstract public class TopologyTreeNode extends DefaultMutableTreeNode{
	// topology belonging to this node:
	final TopologyInternalLevel topology;
	final TraceFormatFileOpener file;	
	final TopologyManager       manager;
	
	abstract public TimelineType getType();
	
	public TopologyTreeNode(TopologyInternalLevel topNode,  TraceFormatFileOpener file, TopologyManager manager) {
		this.topology = topNode;
		this.manager = manager;
		this.file = file;
	}
	
	public TopologyInternalLevel getTopology() {
		return topology;
	}

	public TraceFormatFileOpener getFile() {
		return file;
	}
	
	public TopologyManager getManager() {
		return manager;
	}
	
	@Override
	public String toString() {
		return topology.getLabel();
	}
}

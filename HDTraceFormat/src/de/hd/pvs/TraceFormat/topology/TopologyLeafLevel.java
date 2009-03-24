package de.hd.pvs.TraceFormat.topology;

import de.hd.pvs.TraceFormat.trace.TraceSource;

public class TopologyLeafLevel extends TopologyInternalLevel{
	private TraceSource traceSource;
	
	public TopologyLeafLevel(String name, TopologyInternalLevel parent) {
		super(name, parent);
	}
	
	public TraceSource getTraceSource() {
		return traceSource;
	}
	
	public void setTraceSource(TraceSource traceSource) {
		this.traceSource = traceSource;
	}
	
	@Override
	public boolean isLeaf() {
		return true;
	}
}

/**
 * 
 */
package de.hd.pvs.TraceFormat.relation;

import de.hd.pvs.TraceFormat.topology.TopologyNode;

public class RelationToken{
	long id; 
	RelationXMLWriter parent;
	int startedStates = 0;
	
	RelationToken(long id, RelationXMLWriter parent) {
		this.parent = parent;
		this.id = id;
	}
	

	public TopologyNode getTopologyNode() {
		return parent.getTopologyNode();
	}
	
	public RelationXMLWriter getRelationXMLWriter() {
		return parent;
	}
}
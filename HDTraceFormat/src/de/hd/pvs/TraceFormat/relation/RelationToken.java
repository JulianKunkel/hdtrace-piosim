/**
 * 
 */
package de.hd.pvs.TraceFormat.relation;

public class RelationToken{
	long id; 
	RelationXMLWriter parent;
	int startedStates = 0;
	
	RelationToken(long id, RelationXMLWriter parent) {
		this.parent = parent;
		this.id = id;
	}
}
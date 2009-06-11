package de.hd.pvs.TraceFormat.relation.file;


/**
 * An single XML entry as read in the relation file.
 * These simple objects are manufactured and combined to form the real RelationEntry.
 * 
 * @author julian
 *
 */
public abstract class RelationFileEntry {
	public enum Type{
		INITIAL, // initialize a new relation
		RELATE_REMOTE, // relate a remote relation
		RELATE_LOCAL,
		RELATE_PROCESS,
		RELATE_INTERNAL,
		TERMINATE, // terminate a relation
		START_STATE,
		END_STATE
	}
	
	final long relationToken;
	
	abstract public Type getType();
	
	public RelationFileEntry(String token) {
		this.relationToken = Long.parseLong(token);
	}
	
	public final long getRelationID(){
		return relationToken;
	}
}

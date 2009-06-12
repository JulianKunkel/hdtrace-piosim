package de.hd.pvs.TraceFormat.relation.file;

import de.hd.pvs.TraceFormat.util.Epoch;


/**
 * An single XML entry as read in the relation file.
 * These simple objects are manufactured and combined to form the real RelationEntry.
 * 
 * @author julian
 *
 */
public abstract class RelationFileEntry {
	public enum Type{
		CREATE, // initialize a new relation
		TERMINATE, // terminate a relation
		START_STATE,
		END_STATE
	}
	
	final long relationTokenID;
	final Epoch time;	
	
	abstract public Type getType();
	
	public RelationFileEntry(String token, Epoch time) {
		this.relationTokenID = Long.parseLong(token);
		this.time = time;
	}
	
	public final long getRelationTokenID(){
		return relationTokenID;
	}

	public Epoch getTime() {
		return time;
	}	
}

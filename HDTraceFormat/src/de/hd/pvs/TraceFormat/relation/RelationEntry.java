package de.hd.pvs.TraceFormat.relation;

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.trace.IStateTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * An entry as present in the relation file.
 * @author julian
 *
 */
public class RelationEntry implements TraceObject {
	private final Epoch creationTime;
	private final Epoch endTime;
	private final ArrayList<IStateTraceEntry> states;	
	
	/**
	 * The ID of this new token, this ID is unique within the process.
	 */
	private final long tokenID;
	private final RelationHeader header;
	
	private final String parentToken;
	
	public RelationEntry(String parentToken, long tokenID, RelationHeader header, ArrayList<IStateTraceEntry> states,  Epoch creationTime,  Epoch endTime) {
		this.parentToken = parentToken;
		this.states = states;
		
		assert(endTime != null);
		assert(creationTime != null);
		assert(endTime.compareTo(creationTime) >= 0);
		
		this.endTime = endTime;
		this.creationTime = creationTime;
		
		this.header = header;
		this.tokenID = tokenID;
	}
	
	public ArrayList<IStateTraceEntry> getStates() {
		return states;
	}

	@Override
	public Epoch getDurationTime() {
		return endTime.subtract(creationTime);
	}

	@Override
	public Epoch getEarliestTime() {
		return creationTime;
	}

	@Override
	public Epoch getLatestTime() {
		return endTime;
	}

	public RelationHeader getHeader() {
		return header;
	}
	
	public long getTokenID() {
		return tokenID;
	}
	
	/**
	 * Return the unique ID of this relation entry.
	 * @return
	 */
	public String getUniqueEntryID() {
		return header.getUniqueID() + ":" + tokenID;
	}
	
	@Override
	public TraceObjectType getType() {
		return TraceObjectType.RELATION;
	}
	
	@Override
	public String toString() {
		return getUniqueEntryID() + "<\"" + parentToken  + "\" " + getEarliestTime() + "-" + getLatestTime() + ">";
	}
	
	public String getParentToken() {
		return parentToken;
	}

	/**
	 * Return the relatedID per level of the topology.
	 * Similar to BigEndian.
	 * @return
	 */
	public String [] getRelatedIDPerLevel(){
		if(parentToken == null){
			return null;
		}
		return parentToken.split(":");
	}
}

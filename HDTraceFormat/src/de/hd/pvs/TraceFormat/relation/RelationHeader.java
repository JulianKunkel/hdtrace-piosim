package de.hd.pvs.TraceFormat.relation;

import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Represents a single relation in memory
 * 
 * @author julian
 */
public class RelationHeader {
	final String localToken;
	final String hostID;
	final Epoch timeAdjustment;
	
	/**
	 * Valid within the process
	 */
	final int topologyNumber;	
	
	public RelationHeader(String localToken, String hostID, int topologyNumber, Epoch timeAdjustment) {
		assert(localToken != null);
		assert(hostID != null);
		assert(timeAdjustment != null);
		
		this.hostID = hostID;
		this.localToken = localToken;
		this.timeAdjustment = timeAdjustment;
		this.topologyNumber = topologyNumber;
	}
	
	/**
	 * Return a unique identifier for the relation file.
	 * @return
	 */
	public String getUniqueID(){
		return hostID + ":" + localToken + ":" + topologyNumber;
	}
	
	public Epoch getTimeAdjustment() {
		return timeAdjustment;
	}
	
	public String getLocalToken() {
		return localToken;
	}
	
	public int getTopologyNumber() {
		return topologyNumber;
	}
	
	public String getHostID() {
		return hostID;
	}
}

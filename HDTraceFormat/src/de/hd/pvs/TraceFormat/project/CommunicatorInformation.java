/**
 * 
 */
package de.hd.pvs.TraceFormat.project;

public class CommunicatorInformation{
	private final MPICommunicator mpiCommunicator;
	
	private final int cid;
	private final int localId;
	private final int globalId;
	
	public CommunicatorInformation(MPICommunicator communicator, int globalId, int localId, int cid) {
		mpiCommunicator = communicator;
		this.cid = cid;
		this.localId = localId;
		this.globalId = globalId;
	}
	
	public int getCid() {
		return cid;
	}
	
	public int getGlobalId() {
		return globalId;
	}
	
	public int getLocalId() {
		return localId;
	}
	
	public MPICommunicator getMPICommunicator() {
		return mpiCommunicator;
	}
}
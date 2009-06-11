package de.hd.pvs.TraceFormat.relation.file;


public class RelateRemote extends RelateLocal{
	private final String hostID;
	
	public RelateRemote(String token, String relatedToken, String topologyID, String processID, String hostID) {
		super(token, relatedToken,topologyID, processID);
		this.hostID = hostID;
	}
	
	public String getHostID() {
		return hostID;
	}
	
	@Override
	public Type getType() {
		return Type.RELATE_REMOTE;
	}
	
	@Override
	public String getFullTokenID() {
		return hostID + ":" + super.getFullTokenID() ;
	}
}

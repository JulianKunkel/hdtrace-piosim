package de.hd.pvs.TraceFormat.relation.file;


public class RelateProcess extends RelateInternal{
	private final long topologyID;
	
	public RelateProcess(String token, String relatedToken, String topologyID) {
		super(token, relatedToken);
		this.topologyID = Long.parseLong(topologyID);
	}
	
	public long getTopologyID() {
		return topologyID;
	}
	
	@Override
	public Type getType() {
		return Type.RELATE_PROCESS;
	}
	
	@Override
	public String getFullTokenID() {
		return topologyID + ":" + super.getFullTokenID() ;
	}
}

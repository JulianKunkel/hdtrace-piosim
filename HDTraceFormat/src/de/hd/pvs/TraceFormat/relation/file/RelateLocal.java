package de.hd.pvs.TraceFormat.relation.file;


public class RelateLocal extends RelateProcess{
	private final String processID;
	
	public RelateLocal(String token, String relatedToken, String topologyID, String processID) {
		super(token, relatedToken, topologyID);
		this.processID = processID;
	}
	
	public String getProcessID() {
		return processID;
	}
	
	@Override
	public Type getType() {
		return Type.RELATE_LOCAL;
	}
	
	@Override
	public String getFullTokenID() {
		return processID+ ":" + super.getFullTokenID()  ;
	}
}

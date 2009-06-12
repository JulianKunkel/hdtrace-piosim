package de.hd.pvs.TraceFormat.relation.file;

import de.hd.pvs.TraceFormat.util.Epoch;


public class RelationCreate extends RelationFileEntry{
	String parentToken;
	
	public RelationCreate(String parentToken, Epoch time, String relatedToken) {
		super(parentToken, time);
		this.parentToken = relatedToken;
	}

	public String getParentToken() {
		return parentToken;
	}
	
	@Override
	public Type getType() {
		return Type.CREATE;
	}

}

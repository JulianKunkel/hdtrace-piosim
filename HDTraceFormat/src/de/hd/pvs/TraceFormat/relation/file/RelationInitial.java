package de.hd.pvs.TraceFormat.relation.file;


public class RelationInitial extends RelationFileEntry{	
	public RelationInitial(String token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.INITIAL;
	}
}

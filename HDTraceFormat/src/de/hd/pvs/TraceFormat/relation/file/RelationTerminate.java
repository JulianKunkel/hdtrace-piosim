package de.hd.pvs.TraceFormat.relation.file;


public class RelationTerminate extends RelationFileEntry{	
	public RelationTerminate(String token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.TERMINATE;
	}
}

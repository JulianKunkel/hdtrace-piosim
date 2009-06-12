package de.hd.pvs.TraceFormat.relation.file;

import de.hd.pvs.TraceFormat.util.Epoch;


public class RelationTerminate extends RelationFileEntry{	
	public RelationTerminate(String token, Epoch time) {
		super(token, time);
	}
	
	@Override
	public Type getType() {
		return Type.TERMINATE;
	}
}

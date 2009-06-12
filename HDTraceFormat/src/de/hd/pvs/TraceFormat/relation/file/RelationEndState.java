package de.hd.pvs.TraceFormat.relation.file;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;

public class RelationEndState extends RelationStartState{	
	public RelationEndState(String token, Epoch time, XMLTag data) {
		super(token, time, null, data);
	}	
	
	@Override
	public Type getType() {	
		return Type.END_STATE;
	}
}

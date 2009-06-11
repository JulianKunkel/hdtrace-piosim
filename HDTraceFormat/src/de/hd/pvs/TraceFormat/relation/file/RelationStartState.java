package de.hd.pvs.TraceFormat.relation.file;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;

public class RelationStartState extends RelationEndState{
	
	public RelationStartState(XMLTag data, Epoch startTime) {
		super(data, startTime);
	}	
	
	@Override
	public Type getType() {	
		return Type.START_STATE;
	}
}

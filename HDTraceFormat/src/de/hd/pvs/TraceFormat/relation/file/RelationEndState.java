package de.hd.pvs.TraceFormat.relation.file;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;

public class RelationEndState extends RelationFileEntry{
	final XMLTag data;
	final Epoch time;
	
	public RelationEndState(XMLTag data, Epoch time) {
		super(data.getAttribute("t"));
		
		this.time = time;
		this.data = data;		
	}	
	
	@Override
	public Type getType() {	
		return Type.END_STATE;
	}
}

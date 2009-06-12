package de.hd.pvs.TraceFormat.relation.file;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;

public class RelationStartState extends RelationFileEntry{
	final XMLTag data;
	final String name;
	
	public RelationStartState(String token, Epoch time, String name, XMLTag data) {
		super(token, time);
		this.data = data;		
		this.name = name;
	}	

	public String getName(){
		return name;
	}
	
	public XMLTag getData() {
		return data;
	}
	
	@Override
	public Type getType() {	
		return Type.START_STATE;
	}
}

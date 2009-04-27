package de.hd.pvs.TraceFormat.project.datatypes;

import java.util.ArrayList;

/**
 * Basic MPI datatype which can be combined to complex patterns.
 *  
 * @author julian
 */
public class NamedDatatype extends Datatype{
	public static enum NamedDatatypeType{
		LB (0),
		UB (0),
		BYTE (1),
		FLOAT (4),
		DOUBLE (8),
		CHAR(1),
		INT (4),
		INTEGER (4),
		LONG (4),
		LONG_LONG (8)
		;
		
		final int size;
		
		NamedDatatypeType(int size) {
			this.size = size;
		}
		
		public int getSize() {
			return size;
		}
	}
	
	final NamedDatatypeType primitiveType;
	
	public NamedDatatype(NamedDatatypeType type) {
		this.primitiveType = type;
	}
	
	public NamedDatatypeType getPrimitiveType() {
		return primitiveType;
	}
	
	@Override
	public int getExtend() {
		return primitiveType.size;
	}
	
	@Override
	public int getSize() {	
		return primitiveType.size;
	}
	
	@Override
	public DatatypeEnum getType() {
		return DatatypeEnum.NAMED;
	}
	
	@Override
	public ArrayList<Datatype> getChildDataTypes() {
		return new ArrayList<Datatype>();
	}
}

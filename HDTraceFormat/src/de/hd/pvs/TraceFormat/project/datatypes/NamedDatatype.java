//Copyright (C) 2009 Julian M. Kunkel

//This file is part of PIOsimHD.

//PIOsimHD is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//PIOsimHD is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.TraceFormat.project.datatypes;

import java.util.ArrayList;

/**
 * Basic MPI datatype which can be combined to complex patterns.
 *  
 * @author Julian M. Kunkel
 */
public class NamedDatatype extends Datatype{
	
	// TODO: fix, size of some datatypes depends on architecture.
	public static enum NamedDatatypeType{
		LB (0),
		UB (0),
		BYTE (1),
		FLOAT (4),
		DOUBLE (8),
		CHAR(1),
		UNSIGNED_CHAR(1),
		INT (4),
		INTEGER (4),
		LONG (4),
		LONG_LONG (8),
		DOUBLE_PRECISION (8),
		UNIMPLEMENTED(0),
		PACKED(1)
		;

		final int size;

		NamedDatatypeType(int size) {
			this.size = size;
		}

		public int getSize() {
			return size;
		}
	}

	// TODO fix the size of datatypes depending on the machine architecture
	final static public NamedDatatype LB = new NamedDatatype(NamedDatatypeType.LB);
	final static public NamedDatatype UB = new NamedDatatype(NamedDatatypeType.UB);
	final static public NamedDatatype BYTE = new NamedDatatype(NamedDatatypeType.BYTE);
	final static public NamedDatatype FLOAT = new NamedDatatype(NamedDatatypeType.FLOAT);
	final static public NamedDatatype DOUBLE = new NamedDatatype(NamedDatatypeType.DOUBLE);
	final static public NamedDatatype CHAR = new NamedDatatype(NamedDatatypeType.CHAR);
	final static public NamedDatatype PACKED = new NamedDatatype(NamedDatatypeType.PACKED);
	final static public NamedDatatype UNSIGNED_CHAR = new NamedDatatype(NamedDatatypeType.UNSIGNED_CHAR);
	final static public NamedDatatype INTEGER = new NamedDatatype(NamedDatatypeType.INTEGER);
	final static public NamedDatatype LONG_LONG = new NamedDatatype(NamedDatatypeType.LONG_LONG);
	final static public NamedDatatype DOUBLE_PRECISION = new NamedDatatype(NamedDatatypeType.DOUBLE_PRECISION);
	final static public NamedDatatype LONG = new NamedDatatype(NamedDatatypeType.LONG);
	final static public NamedDatatype UNIMPLEMENTED = new NamedDatatype(NamedDatatypeType.UNIMPLEMENTED);

	final NamedDatatypeType primitiveType;

	NamedDatatype(NamedDatatypeType type) {
		this.primitiveType = type;
	}

	public NamedDatatypeType getPrimitiveType() {
		return primitiveType;
	}

	@Override
	public long getExtend() {
		return primitiveType.size;
	}

	@Override
	public long getSize() {	
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

	/**
	 * Return the appropriate datatype for a given string
	 * @param value
	 * @return
	 */
	static public NamedDatatype valueOf(String value){
		NamedDatatypeType type = NamedDatatypeType.valueOf(value);
		switch(type){
		case UB:
			return UB;
		case LB:
			return LB;
		case BYTE:
			return BYTE;
		case CHAR:
			return CHAR;
		case UNSIGNED_CHAR:
			return UNSIGNED_CHAR;
		case DOUBLE:
			return DOUBLE;
		case FLOAT:
			return FLOAT;
		case INT:
			return INTEGER;
		case INTEGER:
			return INTEGER;
		case LONG:
			return LONG;
		case LONG_LONG:
			return LONG_LONG;
		case DOUBLE_PRECISION:
			return DOUBLE_PRECISION;
		case PACKED:
			return PACKED;
		default:
			return null;
		}		
	}
}

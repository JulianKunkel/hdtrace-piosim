//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of PIOsimHD.
//	
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.TraceFormat.project.datatypes;

import java.util.ArrayList;

/**
 * Basic MPI datatype which can be combined to complex patterns.
 *  
 * @author Julian M. Kunkel
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

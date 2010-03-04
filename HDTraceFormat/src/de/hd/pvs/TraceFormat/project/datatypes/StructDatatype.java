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

public class StructDatatype extends Datatype{
	/**
	 * Real size of the struct
	 */
	private int size = 0;
	
	public class StructType{
		final Datatype type;
		final long displacement;
		final int blocklen;
		
		public StructType(Datatype type, long displacement, int blocklen) {
			this.type = type;
			this.displacement = displacement;
			this.blocklen = blocklen;
		}
		
		public Datatype getType() {
			return type;
		}
		
		public long getDisplacement() {
			return displacement;
		}
		
		public int getBlocklen() {
			return blocklen;
		}
		
		public long getEndPos() {
			return displacement + blocklen * type.getExtend();
		}
	}
	
	final ArrayList<StructType> types = new ArrayList<StructType>();
	
	public int getCount(){
		return types.size();
	}
	
	public StructType getType(int which){
		return types.get(which);
	}
	
	public void appendType(Datatype type, long displacement, int blockLen){			
		types.add(new StructType(type, displacement, blockLen));
		
		size += type.getSize() * blockLen;
	}
	
	@Override
	public long getExtend() {
		if(types.size() == 0)
			return 0;
		return types.get(types.size() - 1).getEndPos();
	}
	
	@Override
	public long getSize() {		
		return size;
	}
	
	@Override
	public DatatypeEnum getType() {	
		return DatatypeEnum.STRUCT;
	}
	
	@Override
	public ArrayList<Datatype> getChildDataTypes() {
		final ArrayList<Datatype> children = new ArrayList<Datatype>();
		for(StructType type: types){
			children.add(type.type);
		}
		return children;
	}
}

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

public class VectorDatatype extends Datatype{
	final int count;  
	final int blocklen; 
	final int stride; 
	
	final Datatype previous;
	
	/**
	 * @param previous Datatype
	 * @param count number of blocks
	 * @param blocklen number of elements in each block
	 * @param strideElements number of elements between start of each block (might lead to holes)
	 */
	public VectorDatatype(Datatype previous, int count, int blocklen, int strideElements) {
		this.previous = previous;
		this.count = count;
		this.blocklen = blocklen;
		this.stride = strideElements;
		
		assert(count >= 1);
		assert(blocklen >= 1);
		
		//TODO allow negative values in computation.
		assert(strideElements >= 1);
	}
	
	@Override
	public int getExtend() {
		return previous.getExtend() * stride * blocklen;
	}
	
	@Override
	public int getSize() {
		return previous.getSize() * count * blocklen;
	}
	
	@Override
	public DatatypeEnum getType() {
		return DatatypeEnum.VECTOR;
	}
	
	public int getBlocklen() {
		return blocklen;
	}
	
	public int getCount() {
		return count;
	}
	
	public int getStride() {
		return stride;
	}
	
	public Datatype getPrevious() {
		return previous;
	}
	
	@Override
	public ArrayList<Datatype> getChildDataTypes() {
		final ArrayList<Datatype> children = new ArrayList<Datatype>();
		children.add(previous);
		return children;
	}
}

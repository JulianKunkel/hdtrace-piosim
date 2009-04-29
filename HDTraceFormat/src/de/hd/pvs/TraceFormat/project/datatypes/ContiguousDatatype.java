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

public class ContiguousDatatype extends Datatype{
	final int count;
	
	final Datatype previous;
	
	public ContiguousDatatype(Datatype previous, int count) {
		assert(count >= 1);
		
		this.count = count;
		this.previous = previous;
	}
	
	@Override
	public int getExtend() {
		return previous.getExtend() * count;
	}
	
	@Override
	public int getSize() {
		return previous.getSize() * count;
	}
	
	@Override
	public DatatypeEnum getType() {
		return DatatypeEnum.CONTIGUOUS;
	}
	
	public int getCount() {
		return count;
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

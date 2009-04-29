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
 * Datatype, useful for MPI memory structures and file access patterns.
 * @author Julian M. Kunkel
 */
public abstract class Datatype implements IDatatype{
	/* id of the datatype, used for reference */
	private long tid;
	
	abstract public DatatypeEnum getType();
	
	abstract public int getExtend();
	
	abstract public int getSize();
	
	abstract public ArrayList<Datatype> getChildDataTypes();
	
	final public void setTid(long tid) {
		this.tid = tid;
	}
	
	/**
	 * Return the datatype ID.
	 */
	final public long getTid() {
		return tid;
	}
	
	@Override
	public String toString() {	
		return getType() + "(" + tid + ") " + getSize() + ", " + getExtend();
	}
}

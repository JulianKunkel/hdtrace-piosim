
//	Copyright (C) 2008, 2009 Julian M. Kunkel
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

package de.hd.pvs.traceConverter.Input;


public class ProcessIdentifier {
	// identify the process and thread/timeline this event occurs:
	private final int rank;
	private final int thread;	
	
	public ProcessIdentifier(int rank, int thread) {
		this.rank = rank;
		this.thread = thread;
	}
	
	public int getRank() {
		return rank;
	}
	
	public int getThread() {
		return thread;
	}
	
	@Override
	public String toString() {
		return ("<" + rank + "," +thread + ">");
	}
}

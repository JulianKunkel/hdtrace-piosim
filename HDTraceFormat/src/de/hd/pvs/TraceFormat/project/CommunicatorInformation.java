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

/**
 * 
 */
package de.hd.pvs.TraceFormat.project;

public class CommunicatorInformation{
	private final MPICommunicator mpiCommunicator;
	
	private final int cid;
	private final int localId;
	private final int globalId;
	
	public CommunicatorInformation(MPICommunicator communicator, int globalId, int localId, int cid) {
		mpiCommunicator = communicator;
		this.cid = cid;
		this.localId = localId;
		this.globalId = globalId;
	}
	
	public int getCid() {
		return cid;
	}
	
	public int getGlobalId() {
		return globalId;
	}
	
	public int getLocalId() {
		return localId;
	}
	
	public MPICommunicator getMPICommunicator() {
		return mpiCommunicator;
	}
}
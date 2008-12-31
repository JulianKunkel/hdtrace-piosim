
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

/**
 * 
 */
package de.hd.pvs.piosim.simulator.network.jobs;

/**
 * A very simple NetworkJob which just has a size and no real data needed for the simulation.
 * 
 * @author Julian M. Kunkel
 *
 */
public class NetworkSimpleMessage implements INetworkMessage {
	
	final private long size;
	
	public NetworkSimpleMessage(long size) {
		this.size = size;
	}
	
	@Override
	public long getSize() {
		return size;
	}
	
}

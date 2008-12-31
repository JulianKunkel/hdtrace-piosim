
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
package de.hd.pvs.piosim.simulator.interfaces;

import de.hd.pvs.piosim.simulator.component.NIC.GNIC;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;

/**
 * Interface between NIC and the using component.
 * 
 * @author Julian M. Kunkel
 */
public interface INICToUser {
	/**
	 * Start the transfer of a set of jobs (i.e. multiple Send/Receive operations)
	 * @param jobs All NetworkJobs to work on
	 * @param callback Will be called once the jobs all completed
	 */
	public void initiateTransfer(NetworkJobs jobs);

	/**
	 * return the NIC glue object
	 */
	public GNIC getGNIC();
}

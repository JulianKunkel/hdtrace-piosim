
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$
  */


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

package de.hd.pvs.piosim.model.program;

import de.hd.pvs.TraceFormat.project.MPICommunicator;

/**
 * Implements an MPI communicator. Contains a set of client world ranks which should be used
 * for collective operations and a mapping to the communicator rank.
 * Each rank maintains its own set of communicator ids, which define the Communicator.
 *
 * @author Julian M. Kunkel
 *
 */
public class Communicator extends MPICommunicator{

	/**
	 * Each communicator has a unique ID to identify it.
	 */
	private final int comm_unique_ID;

	/**
	 * The unique number to use for the next communicator.
	 */
	static private int cur_comm_unique_ID = 0;

	/**
	 * the first WORLD rank participating in this communicator
	 */
	private int firstRank = -1;

	public Communicator() {
		//Create a unique ID
		this("comm_" + cur_comm_unique_ID);
	}

	/**
	 * Create a communicator with a given name.
	 * Used to create default communicators.
	 *
	 * @param name
	 */
	public Communicator(String name) {
		super(name);
		comm_unique_ID = cur_comm_unique_ID++;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return comm_unique_ID;
	}

	/**
	 * Get the unique ID of this communicator.
	 * @return
	 */
	public int getIdentity() {
		return comm_unique_ID;
	}

	public int getFirstRank(){
		if(firstRank != -1){
			return firstRank;
		}

		// determine first rank:
		firstRank = Integer.MAX_VALUE;
		for(int rank:getParticipatingRanks()){
			firstRank = firstRank > rank ? rank : firstRank;
		}
		return firstRank;
	}
}

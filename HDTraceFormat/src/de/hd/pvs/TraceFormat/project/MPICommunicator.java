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

package de.hd.pvs.TraceFormat.project;

import java.util.Collection;
import java.util.HashMap;

/**
 * Implements an MPI communicator. Contains a set of client world ranks which should be used 
 * for collective operations and a mapping to the communicator rank.
 * Each rank maintains its own set of communicator ids, which define the Communicator.
 * 
 * @author Julian M. Kunkel
 *
 */
public class MPICommunicator {
	/**
	 * Name of the communicator
	 */
	private String name;
	
	/**
	 * The world participants in this communicator with their corresponding communicator information.
	 */
	private HashMap<Integer, CommunicatorInformation> worldRankMap = new HashMap<Integer, CommunicatorInformation>();	
	
	/**
	 * Map the communicator specific rank to the world rank
	 */
	private HashMap<Integer, CommunicatorInformation> commRankMap = new HashMap<Integer, CommunicatorInformation>();
	
	/**
	 * Return the name of the communicator
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Add a single worldRank to this communicator.
	 * @param rank 
	 * @param internal cid
	 */
	public void addRank(int worldRank, int localRank, int cid){
		CommunicatorInformation commInfo = new CommunicatorInformation(this, worldRank, localRank, cid);
		
		worldRankMap.put(worldRank, commInfo);
		
		commRankMap.put(localRank, commInfo);
	}		
	
	/**
	 * Create a communicator with a given name. 
	 * Used to create default communicators.
	 * 
	 * @param name
	 */
	public MPICommunicator(String name) {
		this.name = name;
	}
	
		
	@Override
	public String toString() {
		return "comm: " + name;
	}
	
	/**
	 * Return the number of participating ranks in this communicator
	 * @return
	 */
	public int getSize(){
		return worldRankMap.size();
	}
	
	public Collection<Integer> getParticipatingRanks() {
		return worldRankMap.keySet();
	}
	
	public Collection<Integer> getLocalRanks() {
		return commRankMap.keySet();
	}
	
	
	public int getWorldRank(int commRank){
		assert(commRank >= 0);
		assert(commRank < getSize());
		return commRankMap.get(commRank).getGlobalId();
	}
	
	public int getLocalRank(int worldRank){
		return worldRankMap.get(worldRank).getLocalId();
	}
	
	/**
	 * Returns a map which maps each rank to its CommunicatorInformation.
	 * @return
	 */
	public HashMap<Integer, CommunicatorInformation> getParticipiants() {
		return worldRankMap;
	}

}

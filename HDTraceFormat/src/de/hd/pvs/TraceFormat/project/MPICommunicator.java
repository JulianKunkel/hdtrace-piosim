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
		worldRankMap.put(worldRank, new CommunicatorInformation(this, worldRank, localRank, cid));
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
	
	/**
	 * Returns a map which maps each rank to its CommunicatorInformation.
	 * @return
	 */
	public HashMap<Integer, CommunicatorInformation> getParticipiants() {
		return worldRankMap;
	}

}

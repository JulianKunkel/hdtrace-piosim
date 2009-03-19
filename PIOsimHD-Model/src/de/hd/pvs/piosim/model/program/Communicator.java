
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

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.piosim.model.interfaces.IXMLReader;

/**
 * Implements an MPI communicator. Contains a set of client world ranks which should be used 
 * for collective operations and a mapping to the communicator rank.
 * Each rank maintains its own set of communicator ids, which define the Communicator.
 * 
 * @author Julian M. Kunkel
 *
 */
public class Communicator implements IXMLReader{
		
	/**
	 * Predefined Communicator for communication with the I/O servers.
	 */
	public static final Communicator IOSERVERS = new Communicator("IO_COMM");
	/**
	 * Predefined Communicator for communication within the simulated "MPI library".
	 */
	public static final Communicator INTERNAL_MPI = new Communicator("InternalMPI");
	
	/**
	 * Each communicator has a unique ID to identify it.
	 */
	private final int comm_unique_ID;
	
	/**
	 * The unique number to use for the next communicator.
	 */
	static private int cur_comm_unique_ID = 0; 
	
	/**
	 * Name of the communicator
	 */
	private String name;
	
	/**
	 * The world participants in this communicator with their corresponding communicator ID.
	 */
	private HashMap<Integer, Integer> participiants = new HashMap<Integer, Integer>(); 
	
	/**
	 * Return the name of the communicator
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Add a single worldRank to this communicator.
	 * @param rank 
	 * @param internal cid
	 */
	public void addRank(int worldRank, int cid){
		participiants.put(worldRank, cid);
	}		
	
	public Communicator() {
		//Create a unique ID
		comm_unique_ID = cur_comm_unique_ID++;
	}
	
	/**
	 * Create a communicator with a given name. 
	 * Used to create default communicators.
	 * 
	 * @param name
	 */
	public Communicator(String name) {
		this();
		this.name = name;
	}
	
	public void readXML(XMLTag xml) throws Exception {
		name =xml.getAttribute("name").toUpperCase(); 

		final LinkedList<XMLTag>  elements = xml.getNestedXMLTagsWithName("Rank");
		
		Iterator<XMLTag> it = elements.iterator();
		
		for(int i=0; i < elements.size(); i++){
			XMLTag tag = it.next();
			
			final String rank = tag.getAttribute("number");
			if (rank == null){
				throw new InvalidParameterException("Invalid XML, no rank number specified !");
			}
			final String cid = tag.getAttribute("cid");
			if (rank == null){
				throw new InvalidParameterException("Invalid XML, no communicator ID specified !");
			}			
			
			try{
				final int ranki = Integer.parseInt(rank);
				final int cidi = Integer.parseInt(cid);
				addRank(ranki, cidi);
				
			}catch(NumberFormatException e){
				throw new InvalidParameterException("Invalid XML, no integer rank or cid specified");
			}	
		}
	}
	
	public void writeXML(StringBuffer sb) {
		
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
		return participiants.size();
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
	
	public Collection<Integer> getParticipatingtRanks() {
		return participiants.keySet();
	}
	
	/**
	 * Returns a map which maps the rank to the CID.
	 * @return
	 */
	public HashMap<Integer, Integer> getParticipiants() {
		return participiants;
	}
}

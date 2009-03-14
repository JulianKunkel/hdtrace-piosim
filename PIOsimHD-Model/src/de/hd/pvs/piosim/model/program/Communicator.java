
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
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;

import de.hd.pvs.TraceFormat.xml.XMLutil;
import de.hd.pvs.piosim.model.interfaces.IXMLReader;

/**
 * Implements an MPI communicator. Contains a set of client world ranks which should be used 
 * for collective operations and a mapping to the communicator rank.
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
	 * The world participants in this communicator. 
	 */
	private int [] participants = new int [0];
	
	/**
	 * This HashMap contains the mapping from World Rank to Communicator Rank.
	 */
	private HashMap<Integer, Integer> worldCommRank = new HashMap<Integer, Integer>(); 
	    
	/**
	 * Return the name of the communicator
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * return the world rank of the communicator rank.
	 * 
	 * @param posInComm
	 * @return
	 */
	public int getWorldRank(int posInComm){
		if (posInComm >= participants.length || posInComm < 0){
			throw new IllegalArgumentException("Invalid rank in communicator: " + posInComm);
		}
		return participants[posInComm];
	}
	
	/**
	 * Add a single worldRank to this communicator.
	 * This is rather inefficient.
	 * @see setWorldRanks
	 * 
	 * @param rank 
	 * @return Rank within the communicator
	 */
	public int addWorldRank(int worldRank){
		int size = participants.length;

		int [] rankMapping = new int [participants.length+1];
		
		for (int i=0; i < size; i++){
			rankMapping[i] = participants[i];
		}
		
		rankMapping[size] = worldRank;
		
		this.setWorldRanks(rankMapping);
		
		return size;
	}
	
	/**
	 * Set the world ranks participating in this communicator.
	 * @param rankMapping
	 */
	public void setWorldRanks(int [] rankMapping){
		if (rankMapping == null){
			throw new IllegalArgumentException("Rank mapping may not be null");
		}

		HashMap<Integer, Integer> worldCommRank = new HashMap<Integer, Integer>();
		
		for (int i=0; i < rankMapping.length; i++){
			if ( worldCommRank.put(rankMapping[i], i) != null){
				throw new IllegalArgumentException("World rank " + rankMapping[i] + " occurs several times");
			}
		}		
		
		/* clone the mapping to ensure that it gets not modified afterwards */
		this.participants = rankMapping.clone();
		this.worldCommRank = worldCommRank;
	}
	
	/**
	 * Return the communicator rank.
	 * @param worldRank
	 * @return
	 */
	public int getCommRank(int worldRank){
		return worldCommRank.get(worldRank);
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
	
	public void readXML(Element xml) throws Exception {
		name = XMLutil.getAttributeText(xml, "name").toUpperCase(); 
			
		// World communicator could be specified but does not need to.
		if (name.compareTo("WORLD") == 0){
			return;
		}
		Element element;
		element = XMLutil.getFirstElementByTag(xml, "ParticipantList");
		ArrayList<Element> elements = XMLutil.getElementsByTag(element, "Rank");
		
		participants = new int [ elements.size() ];
		
		for(int i=0; i < elements.size(); i++){
			String rank = XMLutil.getAttributeText(elements.get(i), "number");
			if (rank == null){
				throw new InvalidParameterException("Invalid XML, no rank specified !");
			}
			try{
				int val = Integer.parseInt(rank);
				participants[i] = val;
			}catch(NumberFormatException e){
				throw new InvalidParameterException("Invalid XML, no integer rank specified");
			}	
		}
		
		// finally, set the ranks:
		setWorldRanks(participants);
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
		return participants.length;
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
	
	/**
	 * Return the mapping from communicator ranks to world ranks.
	 * Note: The array should NEVER be modified outside this class.
	 *  
	 * @return
	 */
	public int [] getParticipantsWorldRank() {
		return participants;
	}
}

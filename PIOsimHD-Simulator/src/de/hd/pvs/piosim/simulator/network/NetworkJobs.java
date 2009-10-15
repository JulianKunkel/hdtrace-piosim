
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

/**
 * 
 */
package de.hd.pvs.piosim.simulator.network;

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;


/**
 * This class bundles a set of messages together. One client can submit a set of network operations
 * and wait until all operations finish.
 * 
 * @author Julian M. Kunkel
 *
 */
public class NetworkJobs {
	
	/** 
	 * describes the environment/state on which the request got submitted.  
	 */
	final private CommandProcessing initialRequestDescription;
	
	/**
	 * When did this set of messages started.
	 */
	final private Epoch          startTime;
	
	/**
	 * List of bundled jobs. Once all of them finish the <code>NetworkJobs</code> are finished.
	 */
	final private ArrayList<InterProcessNetworkJob> jobs = new ArrayList<InterProcessNetworkJob>();
	
	/**
	 * List of responses. Not ordered. The user must match the Receive operation with the Response
	 * by him/her self.
	 */
	final private ArrayList<InterProcessNetworkJob> responses = new ArrayList<InterProcessNetworkJob>();
	
	/** number of currenly pending jobs */
	private int pendingJobs = 0;
	
	/** 
	 * Create a bundle for network jobs.
	 * @param jobDescription
	 * @param startTime
	 */
	public NetworkJobs(CommandProcessing commandDescription) {
		this.initialRequestDescription = commandDescription;
		this.startTime = commandDescription.getStartTime();
	}
	
	final public ArrayList<InterProcessNetworkJob> getNetworkJobs(){
		return jobs;
	}
	
	/**
	 * @return the initialRequestDescription
	 */
	public CommandProcessing getInitialRequestDescription() {
		return initialRequestDescription;
	}
	
	/**
	 * Add a single network job to the list of jobs.
	 * @param job
	 */
	public void addNetworkJob(InterProcessNetworkJob job){
		jobs.add(job);
		pendingJobs++;
	}
	
	/**
	 * @return the time this set of operations got started.
	 */
	public Epoch getStartTime() {
		return startTime;
	}

	/**
	 * Return the responses in the order they got received.
	 * @return the responses
	 */
	public ArrayList<InterProcessNetworkJob> getResponses() {
		return responses;
	}

	/**
	 * Check if this set of operations completed.
	 * @return
	 */
	public boolean isCompleted(){
		assert(pendingJobs >= 0);
		return pendingJobs == 0;
	}
	
	/**
	 * Call if a send job is completed.
	 */
	public void jobCompletedSend(){		
		pendingJobs--;
	}
	
	/**
	 * Called if a receive job is completed.
	 * @param receivedMessage The received job message.
	 */
	public void jobCompletedRecv(InterProcessNetworkJob receivedMessage){
		responses.add(receivedMessage);
		
		pendingJobs--;
	}
	
	public int getSize(){
		return jobs.size();
	}
		
}

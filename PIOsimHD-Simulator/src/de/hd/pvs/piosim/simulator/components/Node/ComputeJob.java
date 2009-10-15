
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
package de.hd.pvs.piosim.simulator.components.Node;


/**
 * Represents a single ComputeJob i.e. a set of instructions processed on the CPU. 
 * It gets run on a node.
 * 
 * @author Julian M. Kunkel
 *
 */
public class ComputeJob implements Comparable<ComputeJob> {
	
	/**
	 * The cycles which must be processed until the job finishes
	 */
	long remainingCycles;
	
	/**
	 * the component which issued this ComputeJob
	 */
	private final ISNodeHostedComponent component;

	/**
	 * A ISNodeHostedComponent can issue a computeJob to run on a node. 
	 * 
	 * @param cycles
	 * @param component
	 */
	public ComputeJob(long cycles, ISNodeHostedComponent component) {
		this.remainingCycles = cycles;
		this.component  = component;
	}
	
	/**
	 * @return the remainingCycles
	 */
	public long getRemainingCycles() {
		return remainingCycles;
	}
	
	/**
	 * Several cycles got processed
	 * @param by how many.
	 */
	public void decreaseRemainingCycles(long by) {
		this.remainingCycles -=by;
	}
	
	/**
	 * Is this job already finished.
	 * @return
	 */
	public boolean isFinished(){
		return remainingCycles == 0;
	}
	
	/**
	 * @return the component which submitted this ComputeJob
	 */
	public ISNodeHostedComponent getComponent() {
		return component;
	}
	
	/**
	 * The less cycles are left the smaller is the ComputeJob.
	 */
	@Override
	public int compareTo(ComputeJob o) {
		return (int) ( this.remainingCycles - o.remainingCycles);
	}
}


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
package de.hd.pvs.piosim.model.components.Node;

import java.util.ArrayList;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.AttributeGetters;
import de.hd.pvs.piosim.model.annotations.ChildComponents;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.components.superclasses.NodeHostedComponent;

/**
 * A node is a component which simulates a complete computer. It contains 
 * network interconnects and might contain one server and a set of programs (Clients) to run on.
 * A node has one or several CPUs working at a given speed.  
 * 
 * @author Julian M. Kunkel
 * 
 * @opt shape node
 * @has 1 - 0..1 de.hd.pvs.piosim.model.components.Server.Server
 * @has 1 - 0..n de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess
 * @has 1 - 1..n NIC
 */
public class Node  extends BasicComponent{	
	/** Number of CPUs in this node */
	@Attribute(xmlName="CPUs")
	@NotNegativeOrZero
	private int cpus = -1;
	
	/** Peak performance of the <code>node</code>. */
	@Attribute
	@NotNegativeOrZero
	private long instructionsPerSecond = -1;
	
	/** Memory of the <code>node</code>. Can be used for Caching. */
	@Attribute
	@NotNegativeOrZero
	private long memorySize = -1;
	
	/** Data transfer speed between two processes in this node */
	@Attribute
	@NotNegativeOrZero
	private long internalDataTransferSpeed = 0;
	
	@ChildComponents
	/** Network cards of the <code>node</code>. */
	ArrayList<NIC> nics = new ArrayList<NIC>();
	
	@ChildComponents
	ArrayList<NodeHostedComponent> hostedComponents = new ArrayList<NodeHostedComponent>();
	
	
	@AttributeGetters
	public long getInstructionsPerSecond() {
		return instructionsPerSecond;
	}
	
	@AttributeGetters
	public long getMemorySize() {
		return memorySize;
	}
	
	/**
	 * @return the internalDataTransferSpeed
	 */
	@AttributeGetters
	public long getInternalDataTransferSpeed() {
		return internalDataTransferSpeed;
	}
	
	/**
	 * @param internalDataTransferSpeed the internalDataTransferSpeed to set
	 */
	public void setInternalDataTransferSpeed(long internalDataTransferSpeed) {
		this.internalDataTransferSpeed = internalDataTransferSpeed;
	}
	
	/**
	 * @param instructionsPerSecond
	 *          the instructionsPerSecond to set
	 */
	public void setInstructionsPerSecond(long instructionsPerSecond) {
		this.instructionsPerSecond = instructionsPerSecond;
	}
	
	/**
	 * @param memorySize The amount of Memory this node should have.
	 */
	public void setMemorySize(long memorySize) {
		this.memorySize = memorySize;
	}
	
	public int getCPUs() {
		return cpus;
	}

	public void setCPUs(int cpus) {
		this.cpus = cpus;
	}

	public ArrayList<NIC> getNICs() {
		return nics;
	}
	
	public ArrayList<NodeHostedComponent> getHostedComponents() {
		return hostedComponents;
	}
	
	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(super.toString() + " MemorySize " + memorySize 
				+ " InstructionsPerSecond " + instructionsPerSecond + "\n");
		for (NIC nic: nics){
			buff.append( "\t " + nic+ "\n");
		}
		
		for(NodeHostedComponent mhc: hostedComponents){
			buff.append( "\t MHC: " + mhc+ "\n");
		}
		
		
		return buff.toString();
	}
	
	
	@Override
	public String getComponentType() {		
		return Node.class.getSimpleName();
	}
}


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

package de.hd.pvs.piosim.simulator.components.Switch;

import java.util.Collection;
import java.util.HashMap;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.Port.Port;
import de.hd.pvs.piosim.model.components.Switch.SimpleSwitch;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.base.SFlowComponent;
import de.hd.pvs.piosim.simulator.base.SNetworkComponent;
import de.hd.pvs.piosim.simulator.components.Node.GNode;
import de.hd.pvs.piosim.simulator.components.Port.GPort;
import de.hd.pvs.piosim.simulator.event.MessagePart;
import de.hd.pvs.piosim.simulator.interfaces.ISNodeHostedComponent;

/**
 * Right now the switch is modeled as a primary controller which first processes all incoming
 * jobs and then delegates them to the necessary output ports.
 */
public class GSwitch  extends SNetworkComponent<SimpleSwitch>{
	
	/**
	 * Routing table, defines the port which must be chosen for a particular node.
	 */
	private HashMap<GNode, GPort> routingTables = new HashMap<GNode, GPort>();
	
	
	@Override
	protected Epoch getMaximumProcessingTime() {
		return new Epoch(getSimulator().getModel().getGlobalSettings().getTransferGranularity() /
				(double) getModelComponent().getTotalBandwidth());
	}
	
	@Override
	protected Epoch getProcessingTime(MessagePart p) {
		return  new Epoch(p.getSize() / (double) getModelComponent().getTotalBandwidth());
	}

	@Override
	protected SFlowComponent getTargetFlowComponent(MessagePart part) {
		return getRoutingEntry( ((ISNodeHostedComponent) part.getNetworkJob().getTargetComponent()	).getAttachedNode());
	}
	
	@Override
	public void setSimulatedModelComponent(SimpleSwitch comp, Simulator sim) throws Exception {
		super.setSimulatedModelComponent(comp, sim);

		/*
		 * create a simulated port for each model port and wire them together 
		 */
		for(Port p: comp.getPorts()){
			GPort gport = (GPort) getSimulator().instantiateSimObjectForModelObj(p);
			
			gport.getIdentifier().setName(comp.getName() + " port " + 
					gport.getIdentifier().getID());
		}		
	}	
	
	/**
	 * Fetch the actual routing table from the neighbor switches and populate yours with attached ports.
	 * This is only possible after the simulated model component was set.
	 * @return the function returns true if the topology changes and false if not.
	 */
	public boolean populateRoutingTable(){
		boolean changes = false;
		SimpleSwitch mswitch = getModelComponent();
		debug("table size: " + routingTables.size());
		
		for(Port p: mswitch.getPorts()){
			
			if(p.getConnectedComponent() == null) {
				continue;
			}
			
			/* maybe we have to set the attached component */
			GPort gPort = (GPort) getSimulator().getSimulatedComponent(p);
			
			assert(gPort != null);
			
			if(gPort.getConnectedComponent() == null){				
				// try to set the connected component of the port.
				
				SFlowComponent sConnectedComp = (SFlowComponent) getSimulator().getSimulatedComponent(p.getConnectedComponent());		
				
				if(sConnectedComp == null){
					throw new IllegalArgumentException("Object not created so far: " + p.getConnectedComponent().getIdentifier());
				}
				
				gPort.setConnectedComponent(sConnectedComp);
				
				// if it is a NIC add the target node in the routing table.
				if ( NIC.class.isAssignableFrom(p.getConnectedComponent().getClass() ) ){
					NIC nic = (NIC) p.getConnectedComponent();
					GNode targetNode = (GNode) getSimulator().getSimulatedComponent( nic.getParentComponent() );
					addRoutingEntry(targetNode, gPort);
				}else if(Port.class.isInstance(p.getConnectedComponent()) ){
					// set parent switch:
					gPort.setConnectedComponent((GSwitch) getSimulator().getSimulatedComponent(p.getConnectedComponent().getParentComponent()));
				}
				
				debugFollowUpLine(gPort.getIdentifier() + " connect to " + sConnectedComp.getModelComponent().getIdentifier());
			}
			
			if ( GSwitch.class.isInstance(gPort.getConnectedComponent() ) ){
				/* now if the connected class is a switch => populate routing table between them */
				boolean ret;
				GSwitch gswitch = (GSwitch) gPort.getConnectedComponent();
				
				ret = populateRoutingTableInternally(this, gswitch, gPort);
			
				if (ret ) 
					changes = true;
				
				debug("populating routing table to " + gswitch.getIdentifier() + " " + ret);
			}
		}
		
		return changes;
	}
	
	/** 
	 * Copy the routing table from the source to the target switch
	 * @return true if the routing table of the target is modified
	 */	
	private boolean populateRoutingTableInternally(GSwitch src, GSwitch tgt, GPort tgtPort ){
		boolean changes = false;
				
		/**
		 * srcPortToTgt
		 * The port we have to choose as routing target on the target for all connected components of the source.
		 */
		
		for(GNode nodes : tgt.routingTables.keySet()) {
			GPort port = src.routingTables.get(nodes);			
			if ( port == null ){
				changes = true;
				src.addRoutingEntry(nodes, tgtPort);
			}
		}
		
		return changes;
	}
	
	private void addRoutingEntry(GNode node, GPort port) {
		routingTables.put(node, port);
	}
	
	private GPort getRoutingEntry(GNode target) {
		GPort port =routingTables.get(target);
		assert(port != null);
		
		return port;
	}
	
	/**
	 * Print the routing table to stdout.
	 */
	public void printRoutingTable() {
		for(GNode comp: routingTables.keySet()){
			try{
				System.out.println(" " + routingTables.get(comp).getIdentifier() + " to " + comp.getIdentifier() + " via " + 
						routingTables.get(comp).getConnectedComponent().getIdentifier() ); 
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Return the list of useful ports, i.e. ports which are connected to some end point.
	 * @return
	 */
	public Collection<GPort> getPortsWithRoutes() {
		return routingTables.values();
	}
	
	
}

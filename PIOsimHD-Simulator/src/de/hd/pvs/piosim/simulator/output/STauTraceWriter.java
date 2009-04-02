
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
package de.hd.pvs.piosim.simulator.output;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.superclasses.ComponentIdentifier;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import edu.uoregon.tau.trace.TraceFactory;
import edu.uoregon.tau.trace.TraceWriter;

/**
 * This class writes a TAU trace file of the simulation results
 * 
 * @author Julian M. Kunkel
 *
 */
public class STauTraceWriter extends STraceWriter {
	
	/**
	 * The tau trace writer.
	 */
	private TraceWriter tauWriter;
	
	
	/**
	 * Inside the trace file each class gets its own group.  
	 */
	private static int nextCompID = 0;
	private HashMap<Class<?>, Integer> tauCompGroupMap = new HashMap<Class<?>, Integer>();
	
	/**
	 * contains all CIDs for which an event got written.
	 */
	private HashSet<ComponentIdentifier> usedCIDs = new HashSet<ComponentIdentifier>();
	
	/**
	 * Category map. Virtual distinguishes between different types of events.
	 */
	private static int nextCatID = 0;
	private HashMap<String, Integer> tauCategoryMap = new HashMap<String,Integer>();
	
	private long getAdaptedTime(Epoch time){
		return (long) (time.getDouble() * 1000 * 1000);
	}
	
	/**
	 * Register a component identifier inside the trace, then the component can be used later for tracing.
	 * 
	 * @param cid
	 */
	@Override
	public void preregister(SPassiveComponent component){
		tauWriter.defThread(component.getIdentifier().getID(), 0, component.getIdentifier().toString());
	}
	
	@Override
	public void startStateInternal(Epoch time, SPassiveComponent comp,
			String eventDesc) 
	{
		ComponentIdentifier cid = comp.getIdentifier();
		
		Integer nodeID = cid.getID();	
		assert(nodeID != null);
		
		Integer categoryID = tauCategoryMap.get(eventDesc);
		if (categoryID == null){
			categoryID = ++nextCatID;
			
			Integer compGroupID = tauCompGroupMap.get(comp.getClass());
			if (compGroupID == null){
				++nextCompID;
				
				tauWriter.defStateGroup(comp.getClass().getSimpleName(), nextCompID);
				
				compGroupID = nextCompID;
				tauCompGroupMap.put(comp.getClass(), compGroupID);
			}
			
			tauWriter.defState(categoryID, eventDesc, compGroupID);
			tauCategoryMap.put(eventDesc, categoryID);
		}
		
		tauWriter.enterState(getAdaptedTime(time),	nodeID, 0, categoryID);
	}
	
	@Override
	public void arrowStartInternal(Epoch time, SPassiveComponent src,
			SPassiveComponent tgt, long messageSize, int messageTag,
			int messageComm) 
	{	
		//endtime == now
		Integer nodeIDStart = src.getIdentifier().getID();
		Integer nodeIDEnd = tgt.getIdentifier().getID();
		
		//System.out.println("S:" + src.getIdentifier() + "-" + tgt.getIdentifier() + " " + getTime() + " " + nodeIDStart + ", " + nodeIDEnd + " :" + messageTag + " " + messageComm);
		
		// Warning do not use values which are bigger than half of integer, otherwise the signed Trace Writer will remove them!!!
		assert(messageComm <= 100000 && messageComm >= 0);
		assert(messageTag >= 0);
		assert(messageSize <= 44576804/2);
		assert(nodeIDStart != null);
		assert(nodeIDEnd != null);
		
		tauWriter.sendMessage(getAdaptedTime(time), nodeIDStart, 0, nodeIDEnd, 
				0, (int) messageSize, messageTag, messageComm);
	}
	
	@Override
	public void arrowEndInternal(Epoch time, SPassiveComponent src,
			SPassiveComponent tgt, long messageSize, int messageTag,
			int messageComm) 
	{
		//endtime == now
		Integer nodeIDStart = src.getIdentifier().getID();
		Integer nodeIDEnd = tgt.getIdentifier().getID();
		
		assert(nodeIDStart != null);
		assert(nodeIDEnd != null);
		
		//System.out.println("E:"+ src.getIdentifier() + "-" + tgt.getIdentifier() + " "  + getTime() + " " + nodeIDStart + ", " + nodeIDEnd + " :" + messageTag + " " + messageComm);
		
		tauWriter.recvMessage(getAdaptedTime(time), nodeIDStart, 0, nodeIDEnd, 
				0, (int)  messageSize, messageTag, messageComm);
	}
	
	@Override
	public void endStateInternal(Epoch time, SPassiveComponent comp,
			String eventDesc)
	{
		ComponentIdentifier cid = comp.getIdentifier();
	
		Integer nodeID = cid.getID();
		Integer categoryID = tauCategoryMap.get(eventDesc);

		assert(nodeID != null);
		assert(categoryID != null);
		

		tauWriter.leaveState(getAdaptedTime(time), nodeID, 0, categoryID);		
		
		addUsedComponent(comp);
	}
	
	private void addUsedComponent(SPassiveComponent comp){
		usedCIDs.add(comp.getIdentifier());
	}

	@Override
	public void eventInternal(Epoch time, SPassiveComponent comp,
			String eventDesc, long userEventValue) 
	{
		ComponentIdentifier cid = comp.getIdentifier();
		
		Integer threadID = cid.getID();
		
		Integer categoryID = tauCategoryMap.get(eventDesc);
		if (categoryID == null){
			categoryID = ++nextCatID;
			
			Integer compGroupID = tauCompGroupMap.get(comp.getClass());
			if (compGroupID == null){
				++nextCompID;
				
				tauWriter.defStateGroup(comp.getClass().getSimpleName(), nextCompID);
				
				compGroupID = nextCompID;
				tauCompGroupMap.put(comp.getClass(), compGroupID);
			}
			
			tauWriter.defUserEvent(categoryID, eventDesc, compGroupID);
			tauCategoryMap.put(eventDesc, categoryID);
		}
		
		tauWriter.eventTrigger(getAdaptedTime(time),	threadID, 0, categoryID, userEventValue);
		
		addUsedComponent(comp);		
	}
	
	/**
	 * Instantiate the trace writer with the given filename (Prefix).
	 */
	public STauTraceWriter(String filename, Simulator sim) {	
		super(filename, sim);
		
		try{
			this.tauWriter = TraceFactory.OpenFileForOutput(
					filename + ".trc",
					filename + ".edf");
		} catch    (Exception e)    {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	
	@Override
	protected void finalizeInternal(Epoch endTime,
			Collection<SPassiveComponent> existingComponents) {
		// finalize logfile
		for(SPassiveComponent component: existingComponents){
			if(usedCIDs.contains(component.getIdentifier())){
				startState(TraceType.ALWAYS, component, component.getClass().getSimpleName());
			}
		}
		final Epoch nendTime = new Epoch(endTime.getDouble() * 1.2);
		for(SPassiveComponent component: existingComponents){
			if(usedCIDs.contains(component.getIdentifier())){
				endState(TraceType.ALWAYS, component, component.getClass().getSimpleName(), nendTime);
			}
		}

		tauWriter.closeTrace();				
	}
}

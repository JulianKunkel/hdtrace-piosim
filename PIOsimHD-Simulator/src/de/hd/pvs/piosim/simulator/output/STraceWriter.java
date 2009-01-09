
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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import de.hd.pvs.piosim.model.components.superclasses.ComponentIdentifier;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.event.MessagePart;
import edu.uoregon.tau.trace.TraceFactory;
import edu.uoregon.tau.trace.TraceWriter;

/**
 * This class writes a TAU trace file and an XML file with statistics
 * 
 * @author Julian M. Kunkel
 *
 */
public class STraceWriter {
	
	/**
	 * Each event belongs to one of this types
	 */
	public enum TraceType {
		ALWAYS,
		CLIENT,
		CLIENT_STEP,
		IOSERVER,
		INTERNAL,
		NOT_INTERNAL
	}
	
	/**
	 * The simulator.
	 */
	final Simulator sim;
	
	/**
	 * The XML file for the statistics.
	 */
	private BufferedWriter xmlstatistics;
	/**
	 * The prefix of all filenames.
	 */
	private String filenamePrefix;
	
	/**
	 * The tau trace writer.
	 */
	private TraceWriter tauWriter;
	
	/**
	 * Return the current simulation time.
	 * @return
	 */
	private long getTime(){
		return (long)(sim.getVirtualTime().getDouble() * 1000 * 1000 ); //wrong SCALING !! 3 x  * 1000
	}
	
	/**
	 * The next jobID which can be used, should be unique. TODO fix for long runs. 
	 */
	private static long jobID = 0; 
	
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
	
	/**
	 * for internal debugging, shows how many arrows (and jobs) started but not yet ended 
	 */
	static int pendingArrows = 0;
	static int pendingStarts = 0;
	
	/**
	 * for debugging only. Counts started jobs per components which are not finished. 
	 */
	private HashMap<SPassiveComponent, HashMap<String, Integer>> startedButNotFinishedEvents = 
		new HashMap<SPassiveComponent, HashMap<String,Integer>>(); 
	
	private boolean logStartOfEvent(SPassiveComponent comp, String category) {
		HashMap<String, Integer> started =  startedButNotFinishedEvents.get(comp);
		if(started == null) {
			started = new HashMap<String, Integer>();
			startedButNotFinishedEvents.put(comp, started);
		}
		
		Integer value = started.get(category);
		
		if( value == null ) {
			started.put(category, 1);
		}else {
			started.put(category, value + 1 );
		}
			
		return true;
	}
	
	private boolean logEndOfEvent(SPassiveComponent comp, String category) {
		HashMap<String, Integer> started =  startedButNotFinishedEvents.get(comp);
		
		Integer value = started.get(category);
		
		started.put(category, value - 1 );
			
		return true;
	}
	
	/**
	 * Register a component identifier inside the trace, then the component can be used later for tracing.
	 * 
	 * @param cid
	 */
	public void preregister(SPassiveComponent component){
		tauWriter.defThread(component.getIdentifier().getID(), 0, component.getIdentifier().toString());
	}
	
	/**
	 * Check if the component is tracable with the current settings. 
	 * 
	 * @param type
	 * @return
	 */
	private boolean isTracableComponent(TraceType type){
		if(! sim.getRunParameters().isTraceEnabled())
			return false;
		
		switch(type) {
			case ALWAYS:
			return true;
			case NOT_INTERNAL:
				return ! sim.getRunParameters().isTraceInternals();
			case CLIENT:
				return true;
			case INTERNAL:
				return sim.getRunParameters().isTraceInternals();
			case CLIENT_STEP:
				return sim.getRunParameters().isTraceClientSteps();
			case IOSERVER:
				return sim.getRunParameters().isTraceServers();
			default:
				assert(false);
		}
		
		return false;
	}
	
	/**
	 * Start a job with a given description.
	 * 
	 * @param cid
	 * @param category
	 * @return jobID (to be used for end)
	 */
	public void start(TraceType type, SPassiveComponent comp, String eventDesc){
		if(! isTracableComponent(type))
			return;
		
		pendingStarts++;
		
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
		
		tauWriter.enterState(getTime(),	nodeID, 0, categoryID);
		
		assert(logStartOfEvent(comp, eventDesc));
		
		jobID++;
	}
	
	/**
	 * Start an arrow from the source to the target. An appropriate arrowEnd must been called.
	 * 
	 * @param src
	 * @param tgt
	 * @param messageSize
	 * @param messageTag
	 * @param messageComm
	 */
	public void arrowStart(TraceType type, SPassiveComponent src, SPassiveComponent tgt, 
			long messageSize, int messageTag, int messageComm){
		if(! isTracableComponent(type))
			return;
		
		assert(tgt != null);
		
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
		
		tauWriter.sendMessage(getTime(), nodeIDStart, 0, nodeIDEnd, 
				0, (int) messageSize, messageTag, messageComm);
		
		pendingArrows++;
	}
	
	/**
	 * End an arrow which got started earlier.
	 * 
	 * @param type
	 * @param src
	 * @param tgt
	 * @param messageSize
	 * @param messageTag
	 * @param messageComm
	 */
	public void arrowEnd(TraceType type, SPassiveComponent src, SPassiveComponent tgt, 
			long messageSize, int messageTag, int messageComm){
		if(! isTracableComponent(type))
			return;
		
		//endtime == now
		Integer nodeIDStart = src.getIdentifier().getID();
		Integer nodeIDEnd = tgt.getIdentifier().getID();
		
		assert(nodeIDStart != null);
		assert(nodeIDEnd != null);
		
		//System.out.println("E:"+ src.getIdentifier() + "-" + tgt.getIdentifier() + " "  + getTime() + " " + nodeIDStart + ", " + nodeIDEnd + " :" + messageTag + " " + messageComm);
		
		tauWriter.recvMessage(getTime(), nodeIDStart, 0, nodeIDEnd, 
				0, (int)  messageSize, messageTag, messageComm);
		
		pendingArrows--;
	}
	
	public void arrowStart(TraceType type, SPassiveComponent src, SPassiveComponent tgt, MessagePart p){
		arrowStart(type, src, tgt, 
				p.getSize(), p.getNetworkJob().getTag(), 
				p.getNetworkJob().getCommunicator().getIdentity());
	}
	
	public void arrowEnd(TraceType type, SPassiveComponent src, SPassiveComponent tgt, MessagePart p){
		arrowEnd(type, src, tgt, 
				p.getSize(), p.getNetworkJob().getTag(), p.getNetworkJob().getCommunicator().getIdentity());
	}
	
	
	/**
	 * End a job which got started with start.
	 * 
	 * @param type
	 * @param comp
	 * @param eventDesc
	 */
	public void end(TraceType type, SPassiveComponent comp, String eventDesc){
		if(! isTracableComponent(type))
			return;
		
		pendingStarts--;
		
		ComponentIdentifier cid = comp.getIdentifier();
		
		Integer nodeID = cid.getID();
		Integer categoryID = tauCategoryMap.get(eventDesc);

		assert(nodeID != null);
		assert(categoryID != null);
		
		assert(logEndOfEvent(comp, eventDesc));

		tauWriter.leaveState(getTime(), nodeID, 0, categoryID);		
		
		addUsedComponent(comp);
	}
	
	private void addUsedComponent(SPassiveComponent comp){
		usedCIDs.add(comp.getIdentifier());
	}
	
	/**
	 * for internal use !
	 * @param type
	 * @param comp
	 * @param eventDesc
	 * @param endTime
	 */
	private void end(TraceType type, SPassiveComponent comp, String eventDesc, long endTime){
		if(! isTracableComponent(type))
			return;
		
		pendingStarts--;
		
		ComponentIdentifier cid = comp.getIdentifier();
		
		Integer nodeID = cid.getID();
		Integer categoryID = tauCategoryMap.get(eventDesc);

		assert(nodeID != null);
		assert(categoryID != null);
		
		assert(logEndOfEvent(comp, eventDesc));

		tauWriter.leaveState(endTime, nodeID, 0, categoryID);		
	}
	
	/**
	 * Start a single event.
	 * @param type
	 * @param comp
	 * @param eventDesc
	 * @param userEventValue
	 */
	public void event(TraceType type, SPassiveComponent comp, String eventDesc, long userEventValue){
		if(! isTracableComponent(type))
			return;
		
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
		
		tauWriter.eventTrigger(getTime(),	threadID, 0, categoryID, userEventValue);
		
		addUsedComponent(comp);
		
		jobID++;		
	}
	
	private void writeInTraceFile(String what){
		try{ 
			xmlstatistics.write(what);
		}catch(IOException e){
			System.err.println("Could not write in XML output file " + filenamePrefix +  ".xml - " + e.getMessage());
		}
	}
	
	/**
	 * Instantiate the trace writer with the given filename (Prefix).
	 */
	public STraceWriter(String filename, Simulator sim) {	
		this.filenamePrefix = filename;
		this.sim = sim;
		try{ 
			FileWriter fstream = new FileWriter(filename + ".xml");
			xmlstatistics = new BufferedWriter(fstream);
		}catch (IOException e){//Catch exception if any
			System.err.println("Could not create Tracefile " + filename + " - " + e.getMessage());
			System.exit(1);
		}
		
		try    {
			this.tauWriter = TraceFactory.OpenFileForOutput(
					filename + ".trc",
					filename + ".edf");
		} catch    (Exception e)    {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * finish the tracing.
	 */
	public void finalize(Collection<SPassiveComponent> existingComponents){
		// finalize logfile
		for(SPassiveComponent component: existingComponents){
			if(usedCIDs.contains(component.getIdentifier())){
				start(TraceType.ALWAYS, component, component.getClass().getSimpleName());
			}
		}
		for(SPassiveComponent component: existingComponents){
			if(usedCIDs.contains(component.getIdentifier())){
				end(TraceType.ALWAYS, component, component.getClass().getSimpleName(), (long) (getTime() * 1.2));
			}
		}
		
		tauWriter.closeTrace();
		
		try{ 
			xmlstatistics.close();
		}catch(IOException e){
			System.err.println("Could not close file: " + e.getMessage());
		}
		
		if (pendingArrows != 0)
			System.err.println("StraceWriter: pending (unfinished arrows) " + pendingArrows);
		if (pendingStarts != 0) {
			System.err.println("StraceWriter: pending (unfinished start states) " + pendingStarts);
			
			for(SPassiveComponent p: startedButNotFinishedEvents.keySet()) {
				boolean missing = false;
				StringBuffer buff = new StringBuffer(); 
				
				buff.append("Key/Values: " + p.getIdentifier() + "\n" );
				
				HashMap<String, Integer> map = startedButNotFinishedEvents.get(p);
				
				for(String category: map.keySet()) {
					int val = map.get(category);
					buff.append(category + " count: " + val  + "\n");
					
					if(val > 0)
						missing = true;
				}
				
				if (missing) {
					System.err.println(buff.toString());
				}				
			}
		}
		
		
	}
}

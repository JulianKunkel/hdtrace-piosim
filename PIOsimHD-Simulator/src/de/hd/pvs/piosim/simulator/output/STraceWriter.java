
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

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.event.MessagePart;

/**
 * This class is a superclass for all TraceWriting classes which can
 * be used with the simulator.
 * 
 * @author Julian M. Kunkel
 *
 */
abstract public class STraceWriter {
	
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
	private final Simulator sim;
	
	private final String filenamePrefix;

	/**
	 * for internal debugging, shows how many arrows (and jobs) started but not yet ended 
	 */
	private static int pendingArrows = 0;
	private static int pendingStarts = 0;
	

	/**
	 * Register a component identifier inside the trace, then the component can be used later for tracing.
	 * 
	 * @param cid
	 */
	abstract public void preregister(SPassiveComponent component);
	
	/**
	 * Return the current simulation time.
	 * @return
	 */
	private Epoch getTimeEpoch(){
		return sim.getVirtualTime();
	}
	
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
	
	abstract protected void startStateInternal(Epoch time, SPassiveComponent comp, String eventDesc);
	
	/**
	 * Start a job with a given description.
	 * 
	 * @param cid
	 * @param category
	 * @return jobID (to be used for end)
	 */
	final public void startState(TraceType type, SPassiveComponent comp, String eventDesc){
		if(! isTracableComponent(type))
			return;
		
		pendingStarts++;
		startStateInternal(getTimeEpoch(), comp, eventDesc);
		
		assert(logStartOfEvent(comp, eventDesc));
	}
	
	abstract protected void arrowStartInternal(Epoch time, SPassiveComponent src, SPassiveComponent tgt, 
			long messageSize, int messageTag, int messageComm);
	
	/**
	 * Start an arrow from the source to the target. An appropriate arrowEnd must been called.
	 * 
	 * @param src
	 * @param tgt
	 * @param messageSize
	 * @param messageTag
	 * @param messageComm
	 */
	final public void arrowStart(TraceType type, SPassiveComponent src, SPassiveComponent tgt, 
			long messageSize, int messageTag, int messageComm){
		if(! isTracableComponent(type))
			return;
		
		assert(tgt != null);
		
		arrowStartInternal(getTimeEpoch(), src, tgt, messageSize, messageTag, messageComm);
		
		pendingArrows++;
	}
	
	abstract protected void arrowEndInternal(Epoch time, SPassiveComponent src, SPassiveComponent tgt, 
			long messageSize, int messageTag, int messageComm);
	
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
	final public void arrowEnd(TraceType type, SPassiveComponent src, SPassiveComponent tgt, 
			long messageSize, int messageTag, int messageComm){
		if(! isTracableComponent(type))
			return;
		
		arrowEndInternal(getTimeEpoch(), src, tgt, messageSize, messageTag, messageComm);
				
		pendingArrows--;
	}
	
	final public void arrowStart(TraceType type, SPassiveComponent src, SPassiveComponent tgt, MessagePart p){
		arrowStart(type, src, tgt, 
				p.getSize(), p.getNetworkJob().getTag(), 
				p.getNetworkJob().getCommunicator().getIdentity());
	}
	
	final public void arrowEnd(TraceType type, SPassiveComponent src, SPassiveComponent tgt, MessagePart p){
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
	final public void endState(TraceType type, SPassiveComponent comp, String eventDesc){
		endState(type,comp, eventDesc, getTimeEpoch());
	}
	
	abstract protected void endStateInternal(Epoch time, SPassiveComponent comp, String eventDesc);
	
	/**
	 * End a job which got started with start.
	 * 
	 * @param type
	 * @param comp
	 * @param eventDesc
	 */
	final public void endState(TraceType type, SPassiveComponent comp, String eventDesc, Epoch endTime){
		if(! isTracableComponent(type))
			return;
		
		pendingStarts--;
		
		assert(logEndOfEvent(comp, eventDesc));
		endStateInternal(endTime, comp, eventDesc);		
	}
	
	abstract protected void eventInternal(Epoch time, SPassiveComponent comp, String eventDesc, long userEventValue);
	
	/**
	 * Start a single event.
	 * @param type
	 * @param comp
	 * @param eventDesc
	 * @param userEventValue
	 */
	final public void event(TraceType type, SPassiveComponent comp, String eventDesc, long userEventValue){
		if(! isTracableComponent(type))
			return;
		
		eventInternal(getTimeEpoch(), comp, eventDesc, userEventValue);
	}
		
	/**
	 * Instantiate the trace writer with the given filename (Prefix).
	 */
	public STraceWriter(String filename, Simulator sim) {	
		this.filenamePrefix = filename;
		this.sim = sim;
	}
	
	abstract protected void finalizeInternal(Epoch endTime, Collection<SPassiveComponent> existingComponents);
	
	/**
	 * finish the tracing.
	 */
	final public void finalize(Collection<SPassiveComponent> existingComponents){
		finalizeInternal(getTimeEpoch(), existingComponents);
		
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
	
	public String getFilenamePrefix() {
		return filenamePrefix;
	}
}

package de.hd.pvs.traceConverter.Output.Tau;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import de.hd.pvs.traceConverter.Output.ProcessIdentifier;
import de.hd.pvs.traceConverter.Output.TraceOutputConverter;
import edu.uoregon.tau.trace.TraceFactory;
import edu.uoregon.tau.trace.TraceWriter;

public class TauConverter extends TraceOutputConverter {

	/**
	 * The tau trace writer.
	 */
	private TraceWriter tauWriter;
	
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
	 * Register a component identifier inside the trace, then the component can be used later for tracing.
	 * 
	 * @param cid
	 */
	private void preregister(int rank, int vthread, String name){
		tauWriter.defThread(rank, vthread, name);
	}
	
	/**
	 * Start a job with a given description.
	 */
	
	public void StateStart(ProcessIdentifier id, String stateName) {
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
	
	/**
	 * End a job which got started with start.
	 * 
	 * @param type
	 * @param comp
	 * @param eventDesc
	 */
	public void end(TraceType type, SPassiveComponent comp, String eventDesc){
		
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
	public void finalizeTrace() {
		// finalize logfile

		tauWriter.closeTrace();
				
		if (pendingArrows != 0)
			System.err.println("StraceWriter: pending (unfinished arrows) " + pendingArrows);
		if (pendingStarts != 0) {
			System.err.println("StraceWriter: pending (unfinished start states) " + pendingStarts);
		}
	}
}

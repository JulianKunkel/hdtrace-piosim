
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

package de.hd.pvs.traceConverter.Output.Tau;

import java.util.HashMap;

import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.statistics.StatisticType;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.traceConverter.RunParameters;
import de.hd.pvs.traceConverter.Input.ProcessIdentifier;
import de.hd.pvs.traceConverter.Output.TraceOutputWriter;
import edu.uoregon.tau.trace.TraceFactory;
import edu.uoregon.tau.trace.TraceWriter;

public class TauWriter extends TraceOutputWriter {
	/**
	 * The tau trace writer.
	 */
	private TraceWriter tauWriter;

	/**
	 * for internal debugging, shows how many arrows (and jobs) started but not yet ended 
	 */
	static int pendingArrows = 0;
	static int pendingStarts = 0;

	/**
	 * Category map. Virtual distinguishes between different types of events.
	 */
	private static int nextCatID = 0;
	private HashMap<String, Integer> tauCategoryMap = new HashMap<String,Integer>();


	private static final int DEFAULT_GROUP_ID = 10;
	private static final int STATISTICS_GROUP_ID = 11;

	@Override
	public void initializeTrace(RunParameters parameters, String resultFile) 
	{
		try{
			this.tauWriter = TraceFactory.OpenFileForOutput(
					resultFile + ".trc",
					resultFile + ".edf");
		} catch    (Exception e)    {
			throw new IllegalArgumentException(e);
		}

		// right now just define one group
		tauWriter.defStateGroup("Normal", DEFAULT_GROUP_ID);
		tauWriter.defStateGroup("TAU", 0);
	}

	@Override
	public void finalizeTrace() {

		tauWriter.closeTrace();

		if (pendingArrows != 0)
			System.err.println("StraceWriter: pending (unfinished arrows) " + pendingArrows);
		if (pendingStarts != 0) {
			System.err.println("StraceWriter: pending (unfinished start states) " + pendingStarts);
		}
	}

	@Override
	public void addTimeline(ProcessIdentifier pid) {
		tauWriter.defThread(pid.getProcessNumber(), pid.getThread(), pid.getProcessNumber() + "_" + pid.getThread());
	}

	@Override
	public void Event(ProcessIdentifier id, Epoch time,
			EventTraceEntry traceEntry) {	
		final String eventName = traceEntry.getName(); 
		Integer categoryID = tauCategoryMap.get(eventName);
		if (categoryID == null){
			categoryID = ++nextCatID;

			tauWriter.defUserEvent(categoryID, eventName, DEFAULT_GROUP_ID);
			tauCategoryMap.put(eventName, categoryID);
		}

		//System.out.println("E " + time + "-" + eventName  + " " + categoryID + " " + id.getRank() + " " + id.getVthread());

		tauWriter.eventTrigger(getTimeMikro(time),	 id.getProcessNumber(), id.getThread(), categoryID, 0);
	}


	@Override
	public void StateEnd(ProcessIdentifier id, Epoch time, StateTraceEntry traceEntry) {		
		pendingStarts--;

		Integer categoryID = tauCategoryMap.get(traceEntry.getName());

		//System.out.println("> " + time + "-" + stateName  + " " + categoryID + " " + id.getRank() + " " + id.getVthread());
		tauWriter.leaveState(getTimeMikro(time), id.getProcessNumber(), id.getThread(), categoryID);	
	}

	@Override
	public void StateStart(ProcessIdentifier id, Epoch time, StateTraceEntry traceEntry) {
		final String stateName = traceEntry.getName();
		pendingStarts++;

		Integer categoryID = tauCategoryMap.get(stateName);
		if (categoryID == null){
			categoryID = ++nextCatID;

			tauWriter.defState(categoryID, stateName, DEFAULT_GROUP_ID);
			tauCategoryMap.put(stateName, categoryID);
		}

		//System.out.println("< " + time + "-" + stateName  + " " + categoryID + " " + id.getRank() + " " + id.getVthread());		
		tauWriter.enterState(getTimeMikro(time), id.getProcessNumber(), id.getThread(), categoryID);		
	}

	@Override
	public void Statistics(ProcessIdentifier id, Epoch time, String name,
			ExternalStatisticsGroup group, Object value) {
		long convertedValue;
		final StatisticType type = group.getType(name);
		
		switch(type){
		case LONG:
			convertedValue = (Long) value;		
			break;
		case INT:
			convertedValue = (Integer) value;
			break;
		case DOUBLE:
			convertedValue = (long)(((double)(Double) value));
			break;
		case FLOAT:
			convertedValue = (long)((float)((Float) value));
			break;
		case STRING:
			// not supported:
			return;
		default:
			throw new IllegalArgumentException("Unknown type: " + type +" in value " + value);
		}
		
		final String eventName = group.getName() + ":" + name;
		
		Integer categoryID = tauCategoryMap.get(eventName);
		if (categoryID == null){
			categoryID = ++nextCatID;

			tauWriter.defUserEvent(categoryID, eventName, STATISTICS_GROUP_ID);
			tauCategoryMap.put(eventName, categoryID);
		}

		//System.out.println("Stat " + time + "-" + eventName  + " " + categoryID + " " + id.getRank() + " " + id.getVthread() + " : " + convertedValue);

		tauWriter.eventTrigger(getTimeMikro(time),	 id.getProcessNumber(), id.getThread(), categoryID, convertedValue);
	}


	private long getTimeMikro(Epoch time){
		return time.getSeconds() * 1000 * 1000 + time.getNanoSeconds() / 1000;
	}

}
package de.hd.pvs.traceConverter.Output.Tau;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.Input.ProcessIdentifier;
import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup;
import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup.StatisticType;
import de.hd.pvs.traceConverter.Output.TraceOutputConverter;
import edu.uoregon.tau.trace.TraceFactory;
import edu.uoregon.tau.trace.TraceWriter;

public class TauConverter extends TraceOutputConverter {
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
	public void initializeTrace(Properties commandLineArguments, String resultFile) 
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
	public void addTimeline(int rank, int timelineThread, String name) {
		tauWriter.defThread(rank, timelineThread, name);
	}


	@Override
	public void addStatistic(int rank, int timeline, String name,
			StatisticType type) {
		// do nothing, maybe later put the statistic on a own rank.
	}

	@Override
	public void Event(ProcessIdentifier id, Epoch time, String eventName) {
		Integer categoryID = tauCategoryMap.get(eventName);
		if (categoryID == null){
			categoryID = ++nextCatID;

			tauWriter.defUserEvent(categoryID, eventName, DEFAULT_GROUP_ID);
			tauCategoryMap.put(eventName, categoryID);
		}

		//System.out.println("E " + time + "-" + eventName  + " " + categoryID + " " + id.getRank() + " " + id.getVthread());

		tauWriter.eventTrigger(getTimeMikro(time),	 id.getRank(), id.getVthread(), categoryID, 0);
	}


	@Override
	public void StateEnd(ProcessIdentifier id, Epoch time, String stateName) {		
		pendingStarts--;

		Integer categoryID = tauCategoryMap.get(stateName);

		//System.out.println("> " + time + "-" + stateName  + " " + categoryID + " " + id.getRank() + " " + id.getVthread());
		tauWriter.leaveState(getTimeMikro(time), id.getRank(), id.getVthread(), categoryID);	
	}

	@Override
	public void StateStart(ProcessIdentifier id, Epoch time, String stateName) {		
		pendingStarts++;

		Integer categoryID = tauCategoryMap.get(stateName);
		if (categoryID == null){
			categoryID = ++nextCatID;

			tauWriter.defState(categoryID, stateName, DEFAULT_GROUP_ID);
			tauCategoryMap.put(stateName, categoryID);
		}

		//System.out.println("< " + time + "-" + stateName  + " " + categoryID + " " + id.getRank() + " " + id.getVthread());		
		tauWriter.enterState(getTimeMikro(time), id.getRank(), id.getVthread(), categoryID);		
	}

	@Override
	public void Statistics(ProcessIdentifier id, Epoch time, String group,
			String name, StatisticType type, Object value) {
		long convertedValue;
		
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
		
		final String eventName = group + ":" + name;
		
		Integer categoryID = tauCategoryMap.get(eventName);
		if (categoryID == null){
			categoryID = ++nextCatID;

			tauWriter.defUserEvent(categoryID, eventName, STATISTICS_GROUP_ID);
			tauCategoryMap.put(eventName, categoryID);
		}

		//System.out.println("Stat " + time + "-" + eventName  + " " + categoryID + " " + id.getRank() + " " + id.getVthread());

		tauWriter.eventTrigger(getTimeMikro(time),	 id.getRank(), id.getVthread(), categoryID, convertedValue);
	}


	private long getTimeMikro(Epoch time){
		return time.getSeconds() * 1000 * 1000 + time.getNanoSeconds() / 1000;
	}

}

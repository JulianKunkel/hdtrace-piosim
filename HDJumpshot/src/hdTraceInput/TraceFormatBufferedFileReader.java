
/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$ 
 */

//Copyright (C) 2009 Julian M. Kunkel

//This file is part of HDJumpshot.

//HDJumpshot is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//HDJumpshot is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.


package hdTraceInput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;

import topology.GlobalStatisticStatsPerGroup;
import topology.GlobalStatisticStatsPerGroup.GlobalStatisticsPerStatistic;
import viewer.legends.LegendTableStatisticModel;
import viewer.legends.LegendTableTraceModel;
import arrow.ArrowManager;
import arrow.ManagedArrowGroup;
import de.hd.pvs.TraceFormat.SimpleConsoleLogger;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticSource;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.TraceEntry;
import de.hd.pvs.TraceFormat.trace.TraceSource;
import de.hd.pvs.TraceFormat.util.Epoch;
import drawable.Category;
import drawable.CategoryEvent;
import drawable.CategoryState;
import drawable.CategoryStatistic;

/**
 * Manages information for different projects.
 * Contains all information about the project, contained files within the project.
 * 
 * @author Julian M. Kunkel
 *
 */
public class TraceFormatBufferedFileReader {

	private Epoch globalMinTime = new Epoch(Integer.MAX_VALUE, 0);
	private Epoch globalMaxTime = new Epoch(Integer.MIN_VALUE, -1);

	final LegendTableTraceModel legendTraceModel = new LegendTableTraceModel();
	final LegendTableStatisticModel legendStatisticModel = new LegendTableStatisticModel();

	final ArrayList<TraceFormatFileOpener> loadedFiles = new ArrayList<TraceFormatFileOpener>();

	// map category names to the category:
	final HashMap<String, CategoryState> categoriesStates = new HashMap<String, CategoryState>();	
	final HashMap<String, CategoryEvent> categoriesEvents = new HashMap<String, CategoryEvent>();
	final HashMap<String, CategoryStatistic> categoriesStatistics = new HashMap<String, CategoryStatistic>();

	final HashMap<StatisticsGroupDescription, GlobalStatisticStatsPerGroup> globalStatStats = new HashMap<StatisticsGroupDescription, GlobalStatisticStatsPerGroup>(); 

	final ArrowManager arrowManager = new ArrowManager(this);

	public TraceFormatBufferedFileReader() {
		legendTraceModel.addCategoryUpdateListener(arrowManager);
	}
	
	public double subtractGlobalMinTimeOffset(Epoch time){
		return time.subtract(globalMinTime).getDouble();
	}


	/**
	 * Update global times:
	 * 
	 * @param reader
	 */
	private void updateMinMaxTime(IBufferedReader reader){
		Epoch minTime = reader.getMinTime();
		Epoch maxTime = reader.getMaxTime();


		if(maxTime.compareTo(globalMaxTime) > 0){
			globalMaxTime = maxTime;
		}

		if(minTime.compareTo(globalMinTime) < 0){
			globalMinTime = minTime;
		}		
	}

	/**
	 * Update Min/Max time if one reader got an earlier/later time
	 * Also set/create global statistics about statistics *g*
	 * @param stats
	 */
	private void setGlobalValuesOnStatistics(Collection<StatisticSource> stats){
		for(StatisticSource statReader: stats){			
			final BufferedStatisticFileReader reader = ((BufferedStatisticFileReader) statReader);
			updateMinMaxTime(reader);

			final StatisticsGroupDescription group = reader.getGroup();

			GlobalStatisticStatsPerGroup globalStats = globalStatStats.get(group);
			
			if (globalStats == null){
				globalStats = new GlobalStatisticStatsPerGroup(group);
				globalStatStats.put(group, globalStats);				
			}
			
			// for each member, update global statistic information TODO put into a file
			int groupNumber = -1;
			for(StatisticDescription statDesc: group.getStatisticsOrdered()){
				groupNumber++;

				final String stat = statDesc.getName();
				GlobalStatisticsPerStatistic statsPerStatistic = globalStats.getStatsForStatistic(stat);				

				if (statsPerStatistic == null){
					// no such statistic exists so far.
					statsPerStatistic = new GlobalStatisticsPerStatistic(statDesc);
					globalStats.setStatsForStatistic(stat, statsPerStatistic);					
				}

				// compute statistic if possible:
				if(statDesc.isNumeric()){
					// update globals:
					double globalMaxValue = statsPerStatistic.getGlobalMaxValue();
					double globalMinValue = statsPerStatistic.getGlobalMinValue();
					
					final StatisticStatistics statsForFile = reader.getStatisticsFor(groupNumber);

					if( statsForFile.getMaxValue() > globalMaxValue ) globalMaxValue =  statsForFile.getMaxValue();
					if( statsForFile.getMinValue() < globalMinValue ) globalMinValue =  statsForFile.getMinValue();

					statsPerStatistic.setGlobalMaxValue(globalMaxValue);
					statsPerStatistic.setGlobalMinValue(globalMinValue);

					//System.out.println(stat + " " + globalMaxValue +"  min " + globalMinValue + " file-max: " + reader.getMaxNumericValue(groupNumber));
				}				
			}
		}
	}

	// TODO read from category index file.
	private void updateVisibleCategories(BufferedTraceFileReader reader){
		Enumeration<TraceEntry> enu = reader.enumerateNestedTraceEntry(); //reader.enumerateTraceEntry(true, new Epoch(-1),new Epoch(300000000));
		
		while(enu.hasMoreElements()){
			
			final TraceEntry entry = enu.nextElement();
			final String catName = entry.getName();		
						
			if(entry.getType() == TraceObjectType.STATE){		
				if(! categoriesStates.containsKey(catName)){
					categoriesStates.put( catName, new CategoryState(catName, null));
				}
				
			}if(entry.getType() == TraceObjectType.EVENT){
				if(! categoriesEvents.containsKey(catName))
					categoriesEvents.put( catName, new CategoryEvent(catName, null));
			}
		}	
	}


	/**
	 * Add statistic categories in the file if not already part of the category.
	 * @param fileOpener
	 */
	private void updateStatisticCategories(TraceFormatFileOpener fileOpener){
		for(StatisticsGroupDescription group: fileOpener.getProjectDescription().getExternalStatisticGroups()){
			for(StatisticDescription desc: group.getStatisticsOrdered()){
				final String name = group.getName() + ":" + desc.getName(); 
				if(!categoriesStatistics.containsKey(name)){
					categoriesStatistics.put(name, new CategoryStatistic(desc, null));
				}
			}
		}        	
	}

	/**
	 * Reads a trace file.
	 * 
	 * @param projectFileName
	 * @throws Exception
	 */	
	public void loadAdditionalFile(String projectFileName) throws Exception{
		TraceFormatFileOpener fileOpener = new TraceFormatFileOpener( projectFileName, true, 
				BufferedStatisticFileReader.class, BufferedTraceFileReader.class );

		updateStatisticCategories(fileOpener);

		// determine global min/maxtime
		TopologyNode rootTopology = fileOpener.getTopology();

		for(TopologyNode topology: rootTopology.getSubTopologies()){
			setGlobalValuesOnStatistics(topology.getStatisticSources().values());

			final TraceSource traceSource = topology.getTraceSource();
			if(traceSource != null){
				updateMinMaxTime((IBufferedReader) traceSource);
				updateVisibleCategories((BufferedTraceFileReader) traceSource);
			}
		}

		SimpleConsoleLogger.Debug("Global Min/Max time: " + getGlobalMinTime() + "/" + getGlobalMaxTime());

		loadedFiles.add(fileOpener);


		// update legends:
		legendTraceModel.clearCategories();
		legendStatisticModel.clearCategories();

		for(Category cat: getCategoriesEvents().values()){
			legendTraceModel.addCategory(cat);
		}
		for(Category cat: getCategoriesStates().values()){
			legendTraceModel.addCategory(cat);		
		}
		for(ManagedArrowGroup group: arrowManager.getManagedGroups()){
			legendTraceModel.addCategory(group.getCategory());
		}
		
		for(Category cat: getCategoriesStatistics().values()){
			legendStatisticModel.addCategory(cat);
		}

		legendTraceModel.commitModel();
		legendStatisticModel.commitModel();
	}

	public int getNumberOfFilesLoaded(){
		return loadedFiles.size();
	}

	public TraceFormatFileOpener getLoadedFile(int fileNumber) {
		return loadedFiles.get(fileNumber);
	}

	public String getProjectFilename(int fileNumber){
		return loadedFiles.get(fileNumber).getProjectDescription().getProjectFilename();
	}

	public String getCombinedProjectFilename(){
		String name = "";
		for(TraceFormatFileOpener tf:loadedFiles){
			name= name + tf.getProjectDescription().getProjectFilename() + " ";
		}
		return name;
	}

	public Epoch getGlobalMaxTime() {
		return globalMaxTime;
	}

	public Epoch getGlobalMinTime() {
		return globalMinTime;
	}

	public HashMap<String, CategoryEvent> getCategoriesEvents() {
		return categoriesEvents;
	}

	public HashMap<String, CategoryState> getCategoriesStates() {
		return categoriesStates;
	}

	public HashMap<String, CategoryStatistic> getCategoriesStatistics() {
		return categoriesStatistics;
	}

	public CategoryEvent getCategory(EventTraceEntry entry){
		return categoriesEvents.get(entry.getName());
	}

	public CategoryState getCategory(StateTraceEntry entry){
		return categoriesStates.get(entry.getName());
	}

	public Category getCategory(TraceObject object){
		switch(object.getType()){
		case EVENT:
			return getCategory((EventTraceEntry) object);			
		case STATISTICGROUPVALUES:
			return null;
		case STATE:
			return getCategory((StateTraceEntry) object);
		case STATISTICENTRY:
			StatisticEntry entry = (StatisticEntry) object;
			return getCategory(entry.getParentGroupEntry().getGroup(), entry.getDescription().getName());
		}
		return null;	
	}

	public CategoryStatistic getCategory(StatisticsGroupDescription group, String statistic){
		return categoriesStatistics.get(group.getName() + ":" + statistic);
	}

	public Collection<String> getGroupNames(int fileNumber){
		return loadedFiles.get(fileNumber).getProjectDescription().getExternalStatisticGroupNames();
	}

	public GlobalStatisticStatsPerGroup getGlobalStatStats(StatisticsGroupDescription group) {
		return globalStatStats.get(group);
	}

	public LegendTableTraceModel getLegendTraceModel() {
		return legendTraceModel;
	}
	
	public LegendTableStatisticModel getLegendStatisticModel() {
		return legendStatisticModel;
	}	

	public ArrowManager getArrowManager() {
		return arrowManager;
	}
}

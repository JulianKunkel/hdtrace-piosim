
/** Version Control Information $Id: TraceFormatBufferedFileReader.java 469 2009-07-01 13:27:24Z kunkel $
 * @lastmodified    $Date: 2009-07-01 15:27:24 +0200 (Mi, 01. Jul 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 469 $ 
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


package de.hdTraceInput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;

import org.xml.sax.SAXParseException;

import de.arrow.ArrowManager;
import de.arrow.ManagedArrowGroup;
import de.drawable.Category;
import de.drawable.CategoryEvent;
import de.drawable.CategoryState;
import de.drawable.CategoryStatistic;
import de.hd.pvs.TraceFormat.ITracableObject;
import de.hd.pvs.TraceFormat.SimpleConsoleLogger;
import de.hd.pvs.TraceFormat.TracableObjectType;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.relation.RelationEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsSource;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.IEventTraceEntry;
import de.hd.pvs.TraceFormat.trace.IStateTraceEntry;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.trace.RelationSource;
import de.hd.pvs.TraceFormat.trace.TraceSource;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.topology.GlobalStatisticStatsPerGroup;
import de.topology.GlobalStatisticsPerStatistic;
import de.topology.MinMax;
import de.viewer.legends.LegendTableStatisticModel;
import de.viewer.legends.LegendTableTraceModel;

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
	final HashMap<StatisticsDescription, CategoryStatistic> categoriesStatistics = new HashMap<StatisticsDescription, CategoryStatistic>();

	final HashMap<StatisticsGroupDescription, GlobalStatisticStatsPerGroup> globalStatStats = new HashMap<StatisticsGroupDescription, GlobalStatisticStatsPerGroup>(); 

	final ArrowManager arrowManager = new ArrowManager(this);
	
	final ArrayList<FileLoadedListener> fileLoadListener = new ArrayList<FileLoadedListener>();

	public TraceFormatBufferedFileReader() {
		legendTraceModel.addCategoryUpdateListener(arrowManager);
	}

	public Epoch subtractGlobalMinTimeOffset(Epoch time){
		return time.subtract(globalMinTime);
	}


	/**
	 * Update global times:
	 * 
	 * @param reader
	 */
	private void updateMinMaxTime(IBufferedReader reader){
		final Epoch minTime = reader.getMinTime();
		final Epoch maxTime = reader.getMaxTime();
		
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
	public void setGlobalValuesOnStatistics(Collection<StatisticsSource> stats){
		for(StatisticsSource statReader: stats){			
			final IBufferedStatisticsReader reader = ((IBufferedStatisticsReader) statReader);
			updateMinMaxTime(reader);

			final StatisticsGroupDescription group = reader.getGroup();

			GlobalStatisticStatsPerGroup globalStats = globalStatStats.get(group);

			if (globalStats == null){
				globalStats = new GlobalStatisticStatsPerGroup(group);
				globalStatStats.put(group, globalStats);				
			}

			// for each member, update global statistic information TODO put into a file
			int groupNumber = -1;
			for(StatisticsDescription statDesc: group.getStatisticsOrdered()){
				groupNumber++;

				GlobalStatisticsPerStatistic statsPerStatistic = globalStats.getStatsForStatistic(statDesc);				

				if (statsPerStatistic == null){
					// no such statistic exists so far.
					statsPerStatistic = new GlobalStatisticsPerStatistic(statDesc);
					globalStats.setStatsForStatistic(statDesc, statsPerStatistic);					
				}

				// compute statistic if possible:
				if(statDesc.isNumeric()){
					// update globals:					
					final StatisticStatistics statsForFile = reader.getStatisticsFor(groupNumber);

					statsPerStatistic.updateMaxValue(statsForFile.getMaxValue() );
					statsPerStatistic.updateMinValue(statsForFile.getMinValue() );

					// modify grouping:
					final String grouping = statDesc.getGrouping(); 
					if(grouping != null){
						MinMax statistics  = globalStats.getStatsForStatisticGrouping(statDesc.getGrouping());
						if(statistics == null){
							statistics = new MinMax();
							globalStats.setStatsForGrouping(statDesc.getGrouping(), statistics);
						}

						// update min/max if necessary:		
						statistics.updateMaxValue(statsForFile.getMaxValue());
						statistics.updateMinValue(statsForFile.getMinValue());
					}

					//System.out.println(stat + " " + globalMaxValue +"  min " + globalMinValue + " file-max: " + reader.getMaxNumericValue(groupNumber));
				}				
			}
		}
	}
	
	private void updateVisibleCategoryFor(ITraceEntry entry){
		final String catName = entry.getName();		

		if(entry.getType() == TracableObjectType.STATE){		
			if(! categoriesStates.containsKey(catName)){
				categoriesStates.put( catName, new CategoryState(catName, null));
			}

		}if(entry.getType() == TracableObjectType.EVENT){
			if(! categoriesEvents.containsKey(catName))
				categoriesEvents.put( catName, new CategoryEvent(catName, null));
		}
	}

	// TODO read from category index file.
	private void updateVisibleCategories(BufferedTraceFileReader reader){
		Enumeration<ITraceEntry> enu = reader.enumerateNestedTraceEntry(); //reader.enumerateTraceEntry(true, new Epoch(-1),new Epoch(300000000));

		while(enu.hasMoreElements()){

			final ITraceEntry entry = enu.nextElement();
			updateVisibleCategoryFor(entry);
		}	
	}

	// TODO read from category index file.
	private void updateVisibleCategories(BufferedRelationReader reader){
		Enumeration<RelationEntry> enu = reader.enumerateRelations(); //reader.enumerateTraceEntry(true, new Epoch(-1),new Epoch(300000000));

		while(enu.hasMoreElements()){
			final RelationEntry entry = enu.nextElement();

			for(IStateTraceEntry state: entry.getStates()){
				final Enumeration<ITraceEntry> childEnum = state.childForwardEnumeration();

				updateVisibleCategoryFor(state);
				
				while(childEnum.hasMoreElements()){
					final ITraceEntry child = childEnum.nextElement();
					updateVisibleCategoryFor(child);
				}
			}
		}
	}	

	/**
	 * Add statistic categories in the file if not already part of the category.
	 * @param fileOpener
	 */
	private void updateStatisticCategories(TopologyNode rootNode){
		// walk through the complete topology and check each statistic
		for(TopologyNode topo: rootNode.getSubTopologies()){
			for(StatisticsSource statSource: topo.getStatisticsSources().values()) {
				
				final StatisticsGroupDescription group = ((IBufferedStatisticsReader) statSource).getGroup();
				addCategories(group);
			}
		}
	}
	
	public void addCategories(StatisticsGroupDescription group){
		
		for(StatisticsDescription desc: group.getStatisticsOrdered()){										 
			if(!categoriesStatistics.containsKey(desc)){
				categoriesStatistics.put(desc, new CategoryStatistic(desc, null));
			}
		}
	}
	
	public void addFileLoadListener(FileLoadedListener listener){
		fileLoadListener.add(listener);
	}
	
	public void removeFileLoadListener(FileLoadedListener listener){
		fileLoadListener.remove(listener);
	}	

	/**
	 * Reads a trace file.
	 * 
	 * @param projectFileName
	 * @throws Exception
	 */	
	public void loadAdditionalFile(String projectFileName) throws Exception{
		TraceFormatFileOpener fileOpener ;
		try{
			fileOpener = new TraceFormatFileOpener( projectFileName, 
				true, 
				BufferedStatisticsFileReader.class, 
				BufferedTraceFileReader.class, 
				BufferedRelationReader.class );
		}catch(IOException e){
			throw new IllegalArgumentException("Error while reading file: " + projectFileName );
		}

		updateStatisticCategories(fileOpener.getTopology());

		// determine global min/maxtime
		TopologyNode rootTopology = fileOpener.getTopology();

		// update global values & categories
		for(TopologyNode topology: rootTopology.getSubTopologies()){
			setGlobalValuesOnStatistics(topology.getStatisticsSources().values());

			final TraceSource traceSource = topology.getTraceSource();
			if(traceSource != null){
				updateMinMaxTime((IBufferedReader) traceSource);
				updateVisibleCategories((BufferedTraceFileReader) traceSource);				
			}

			// manage relations
			final RelationSource relationSource = topology.getRelationSource();
			if(relationSource != null){
				updateMinMaxTime((IBufferedReader) relationSource);
				updateVisibleCategories((BufferedRelationReader) relationSource);
			}
		}

		SimpleConsoleLogger.Debug("Global Min/Max time: " + getGlobalMinTime() + "/" + getGlobalMaxTime());

		loadedFiles.add(fileOpener);

		releadTopologyAndCategories();
	}
	
	public void releadTopologyAndCategories(){

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

		for(FileLoadedListener listener: fileLoadListener){
			listener.additionalFileLoaded();
		}
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

	public HashMap<StatisticsDescription, CategoryStatistic> getCategoriesStatistics() {
		return categoriesStatistics;
	}

	public CategoryEvent getCategory(IEventTraceEntry entry){
		return categoriesEvents.get(entry.getName());
	}

	public CategoryState getCategory(IStateTraceEntry entry){
		return categoriesStates.get(entry.getName());
	}

	public Category getCategory(ITracableObject object){
		switch(object.getType()){
		case EVENT:
			return getCategory((IEventTraceEntry) object);			
		case STATISTICGROUPVALUES:
			return null;
		case STATE:
			return getCategory((IStateTraceEntry) object);
		case STATISTICENTRY:
			StatisticsEntry entry = (StatisticsEntry) object;
			return getCategory(entry.getDescription());
		}
		return null;	
	}

	public CategoryStatistic getCategory(StatisticsDescription statistic){
		return categoriesStatistics.get(statistic);
	}

	public Collection<String> getGroupNames(int fileNumber){
		return loadedFiles.get(fileNumber).getProjectDescription().getStatisticsGroupNames();
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

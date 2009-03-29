
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
import java.util.HashMap;

import topology.GlobalStatisticStatsPerGroup;
import topology.GlobalStatisticStatsPerGroup.GlobalStatisticsPerStatistic;
import viewer.legends.LegendTableModel;
import de.hd.pvs.TraceFormat.SimpleConsoleLogger;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticSource;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.topology.TopologyEntry;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.TraceSource;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import drawable.Category;
import drawable.CategoryEvent;
import drawable.CategoryState;
import drawable.CategoryStatistic;
import drawable.ColorAlpha;

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

	final LegendTableModel legendModel = new LegendTableModel();

	final ArrayList<TraceFormatFileOpener> loadedFiles = new ArrayList<TraceFormatFileOpener>();

	// map category names to the category:
	HashMap<String, Category> categoriesStates = new HashMap<String, Category>();	
	HashMap<String, Category> categoriesEvents = new HashMap<String, Category>();
	HashMap<String, Category> categoriesStatistics = new HashMap<String, Category>();

	HashMap<StatisticsGroupDescription, GlobalStatisticStatsPerGroup> globalStatStats = new HashMap<StatisticsGroupDescription, GlobalStatisticStatsPerGroup>(); 


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

			final GlobalStatisticStatsPerGroup globalStats = new GlobalStatisticStatsPerGroup(group);
			globalStatStats.put(group, globalStats);
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

					double fileMaxValue = Double.MIN_VALUE;
					double fileMinValue = Double.MAX_VALUE;

					// check file:
					for(StatisticGroupEntry entry: reader.statEntries){
						double value = entry.getNumeric(groupNumber);

						if( value > fileMaxValue ) fileMaxValue = value;
						if( value < fileMinValue ) fileMinValue = value;
					}

					// update globals:
					double globalMaxValue = statsPerStatistic.getGlobalMaxValue();
					double globalMinValue = statsPerStatistic.getGlobalMinValue(); 

					if( fileMaxValue > globalMaxValue ) globalMaxValue = fileMaxValue;
					if( fileMinValue < globalMinValue ) globalMinValue = fileMinValue;

					statsPerStatistic.setGlobalMaxValue(globalMaxValue);
					statsPerStatistic.setGlobalMinValue(globalMinValue);

					//System.out.println(stat + " " + globalMaxValue +"  min " + globalMinValue + " file-min: " + fileMinValue + " max: " + fileMaxValue);
				}				
			}
		}
	}

	// TODO read from category index file.
	private void updateVisibleCategories(BufferedTraceFileReader reader){
		for(XMLTraceEntry entry: reader.getTraceEntries()){
			createTraceObjectCategory(entry);
		}	
	}

	private void addTraceObjectCategoryIfNecessary(XMLTraceEntry entry){
		final String catName = entry.getName();

		if(entry.getType() == TraceObjectType.STATE){		
			if(! categoriesStates.containsKey(catName))
				categoriesStates.put( catName, new CategoryState(catName, new ColorAlpha(20,00,0)));
		}if(entry.getType() == TraceObjectType.EVENT){
			if(! categoriesEvents.containsKey(catName))
				categoriesEvents.put( catName, new CategoryEvent(catName, new ColorAlpha(20,00,0)));
		}
	}

	private void createTraceObjectCategory(XMLTraceEntry entry){
		if(entry.getType() == TraceObjectType.STATE){
			StateTraceEntry state = (StateTraceEntry) entry;
			if(state.hasNestedTraceChildren()){
				for(XMLTraceEntry child: state.getNestedTraceChildren()){
					addTraceObjectCategoryIfNecessary(child);
				}
			}
		}
		addTraceObjectCategoryIfNecessary(entry);
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
					categoriesStatistics.put(name, new CategoryStatistic(name, new ColorAlpha(0,0,200)));
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
		TraceFormatFileOpener fileOpener = new TraceFormatFileOpener( projectFileName, true, BufferedStatisticFileReader.class, BufferedTraceFileReader.class );

		updateStatisticCategories(fileOpener);

		// determine global min/maxtime
		TopologyEntry rootTopology = fileOpener.getTopology();

		for(TopologyEntry topology: rootTopology.getSubTopologies()){
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
		legendModel.clearCategories();

		for(Category cat: getCategoriesEvents().values())
			legendModel.addCategory(cat);
		for(Category cat: getCategoriesStates().values())
			legendModel.addCategory(cat);
		for(Category cat: getCategoriesStatistics().values())
			legendModel.addCategory(cat);

		legendModel.commitModel();		
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

	public HashMap<String, Category> getCategoriesEvents() {
		return categoriesEvents;
	}

	public HashMap<String, Category> getCategoriesStates() {
		return categoriesStates;
	}

	public HashMap<String, Category> getCategoriesStatistics() {
		return categoriesStatistics;
	}

	public Category getCategory(EventTraceEntry entry){
		return categoriesEvents.get(entry.getName());
	}

	public Category getCategory(StateTraceEntry entry){
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

	public Category getCategory(StatisticsGroupDescription group, String statistic){
		return categoriesStatistics.get(group.getName() + ":" + statistic);
	}

	public Collection<String> getGroupNames(int fileNumber){
		return loadedFiles.get(fileNumber).getProjectDescription().getExternalStatisticGroupNames();
	}

	public GlobalStatisticStatsPerGroup getGlobalStatStats(StatisticsGroupDescription group) {
		return globalStatStats.get(group);
	}

	public LegendTableModel getLegendModel() {
		return legendModel;
	}
}

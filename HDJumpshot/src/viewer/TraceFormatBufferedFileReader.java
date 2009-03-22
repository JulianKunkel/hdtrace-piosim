package viewer;

import java.util.Collection;
import java.util.HashMap;

import viewer.GlobalStatisticStatsPerGroup.GlobalStatisticsPerStatistic;

import base.drawable.Category;
import base.drawable.ColorAlpha;
import base.drawable.Topology;
import de.hd.pvs.TraceFormat.SimpleConsoleLogger;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsReader;
import de.hd.pvs.TraceFormat.topology.HostnamePerProjectContainer;
import de.hd.pvs.TraceFormat.topology.RanksPerHostnameTraceContainer;
import de.hd.pvs.TraceFormat.topology.ThreadsPerRankTraceContainer;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Contains all information about the project, contained files within the project.
 * 
 * @author julian
 *
 */
public class TraceFormatBufferedFileReader {

	private Epoch globalMinTime = new Epoch(Integer.MAX_VALUE, 0);
	private Epoch globalMaxTime = new Epoch(Integer.MIN_VALUE, -1);

	final TraceFormatFileOpener fileOpener;

	// map category names to the category:
	HashMap<String, Category> categoriesStates = new HashMap<String, Category>();	
	HashMap<String, Category> categoriesEvents = new HashMap<String, Category>();
	HashMap<String, Category> categoriesStatistics = new HashMap<String, Category>();
	
		HashMap<ExternalStatisticsGroup, GlobalStatisticStatsPerGroup> globalStatStats = new HashMap<ExternalStatisticsGroup, GlobalStatisticStatsPerGroup>(); 

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
	private void setGlobalValuesOnStatistics(HashMap<String, StatisticsReader> stats){
		for(StatisticsReader statReader: stats.values()){			
			final BufferedStatisticFileReader reader = ((BufferedStatisticFileReader) statReader);
			updateMinMaxTime(reader);
			
			final ExternalStatisticsGroup group = reader.getGroup();
			
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
					for(StatisticEntry entry: reader.statEntries){
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
				categoriesStates.put( catName, new Category(catName, Topology.STATE, new ColorAlpha(20,00,0)));
		}if(entry.getType() == TraceObjectType.EVENT){
			if(! categoriesEvents.containsKey(catName))
				categoriesEvents.put( catName, new Category(catName, Topology.EVENT, new ColorAlpha(20,00,0)));
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
    
    private void createStatisticCategories(){
        for(String catName: fileOpener.getProjectDescription().getExternalStatisticGroups()){
        	categoriesStatistics.put(catName, new Category(catName, Topology.STATISTIC, new ColorAlpha(0,0,200)));
        }        	
    }
	
	/**
	 * Reads a trace file.
	 * 
	 * @param projectFileName
	 * @throws Exception
	 */
	public TraceFormatBufferedFileReader(String projectFileName) throws Exception{
		fileOpener = new TraceFormatFileOpener( projectFileName, true, BufferedStatisticFileReader.class, BufferedTraceFileReader.class );

		createStatisticCategories();
		
		// determine global min/maxtime
		for (String host: fileOpener.getHostnameProcessMap().keySet()){
			final HostnamePerProjectContainer hostInfo = fileOpener.getHostnameProcessMap().get(host);
			setGlobalValuesOnStatistics(hostInfo.getStatisticReaders());

			for (Integer rank: hostInfo.getTraceFilesPerRank().keySet()){
				final RanksPerHostnameTraceContainer ranksInfo = hostInfo.getTraceFilesPerRank().get(rank);
				setGlobalValuesOnStatistics(ranksInfo.getStatisticReaders());

				for(Integer thread: ranksInfo.getFilesPerThread().keySet()){
					final ThreadsPerRankTraceContainer threadInfo = ranksInfo.getFilesPerThread().get(thread);

					setGlobalValuesOnStatistics(threadInfo.getStatisticReaders());

					updateMinMaxTime((IBufferedReader) threadInfo.getTraceFileReader());

					updateVisibleCategories((BufferedTraceFileReader) threadInfo.getTraceFileReader());
				}
				// TODO read from category index file.
			}
		}

		SimpleConsoleLogger.Debug("Global Min/Max time: " + getGlobalMinTime() + "/" + getGlobalMaxTime());
	}

	public TraceFormatFileOpener getFileOpener() {
		return fileOpener;
	}

	public String getProjectFilename(){
		return fileOpener.getProjectDescription().getProjectFilename();
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
	
	public Category getCategory(StatisticEntry entry, String statistic){
		return categoriesStatistics.get(entry.getGroup().getName());
	}
	
	public Collection<String> getGroupNames(){
		return fileOpener.getProjectDescription().getExternalStatisticGroups();
	}
	
	public GlobalStatisticStatsPerGroup getGlobalStatStats(ExternalStatisticsGroup group) {
		return globalStatStats.get(group);
	}
	
	
}

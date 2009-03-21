package viewer;

import java.util.Collection;
import java.util.HashMap;

import base.drawable.Category;
import base.drawable.ColorAlpha;
import base.drawable.Topology;
import de.hd.pvs.TraceFormat.SimpleConsoleLogger;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.statistics.StatisticsReader;
import de.hd.pvs.TraceFormat.topology.HostnamePerProjectContainer;
import de.hd.pvs.TraceFormat.topology.RanksPerHostnameTraceContainer;
import de.hd.pvs.TraceFormat.topology.ThreadsPerRankTraceContainer;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry.TYPE;
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
	 * @param stats
	 */
	private void updateMinMaxTimeStat(HashMap<String, StatisticsReader> stats){
		for(String group: stats.keySet()){
			IBufferedReader buff = ((IBufferedReader) stats.get(group));
			updateMinMaxTime(buff);
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
		
		if(entry.getType() == TYPE.STATE){		
			if(! categoriesStates.containsKey(catName))
				categoriesStates.put( catName, new Category(catName, Topology.STATE, new ColorAlpha(20,00,0), 100));
		}if(entry.getType() == TYPE.EVENT){
			if(! categoriesEvents.containsKey(catName))
				categoriesEvents.put( catName, new Category(catName, Topology.EVENT, new ColorAlpha(20,00,0), 100));
		}
	}

    private void createTraceObjectCategory(XMLTraceEntry entry){
		if(entry.getType() == XMLTraceEntry.TYPE.STATE){
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
        	categoriesStatistics.put(catName, new Category(catName, Topology.STATISTIC, new ColorAlpha(0,0,200), 100));
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
			updateMinMaxTimeStat(hostInfo.getStatisticReaders());

			for (Integer rank: hostInfo.getTraceFilesPerRank().keySet()){
				final RanksPerHostnameTraceContainer ranksInfo = hostInfo.getTraceFilesPerRank().get(rank);
				updateMinMaxTimeStat(ranksInfo.getStatisticReaders());

				for(Integer thread: ranksInfo.getFilesPerThread().keySet()){
					final ThreadsPerRankTraceContainer threadInfo = ranksInfo.getFilesPerThread().get(thread);

					updateMinMaxTimeStat(threadInfo.getStatisticReaders());

					updateMinMaxTime((IBufferedReader) threadInfo.getTraceFileReader());

					updateVisibleCategories((BufferedTraceFileReader) threadInfo.getTraceFileReader());
				}
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
	
	public Collection<String> getGroupNames(){
		return fileOpener.getProjectDescription().getExternalStatisticGroups();
	}
	
}

package de.hd.pvs.TraceFormat;

import java.io.File;
import java.util.HashMap;

import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.project.ProjectDescriptionXMLReader;
import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.statistics.StatisticsReader;
import de.hd.pvs.TraceFormat.topology.HostnamePerProjectContainer;
import de.hd.pvs.TraceFormat.topology.RanksPerHostnameTraceContainer;
import de.hd.pvs.TraceFormat.topology.ThreadsPerRankTraceContainer;
import de.hd.pvs.TraceFormat.trace.StAXTraceFileReader;

/**
 * This class opens all files belonging to a particular project.
 * 
 * @author julian
 *
 */
public class TraceFormatFileOpener {
	final ProjectDescription projectDescription;
	final ProjectDescriptionXMLReader projectDescrReader;

	final HashMap<String, HostnamePerProjectContainer> hostnameProcessMap = new HashMap<String, HostnamePerProjectContainer>();

	public ProjectDescription getProjectDescription() {
		return projectDescription;
	}

	public ProjectDescriptionXMLReader getProjectDescriptionXMLReader() {
		return projectDescrReader;
	}

	public TraceFormatFileOpener(String filename, boolean readNested, Class<? extends StatisticsReader> statCls, Class<? extends StAXTraceFileReader> traceCls) throws Exception{
		// scan for all the XML files which must be opened during conversion:
		// general description
		projectDescrReader = new ProjectDescriptionXMLReader();
		projectDescription = new ProjectDescription();
		projectDescrReader.readProjectDescription(projectDescription, filename);

		// start parsing of the trace files:
		// trace files: rank + thread id are defined in the file name

		// TODO do HOSTNAMES
		String hostname="localhost";
		HostnamePerProjectContainer hostRanks = new HostnamePerProjectContainer(hostname);

		hostnameProcessMap.put(hostname, hostRanks);


		for(int rank=0 ; rank < projectDescription.getProcessCount(); rank++){
			final RanksPerHostnameTraceContainer  rankFiles = new RanksPerHostnameTraceContainer(rank);

			hostRanks.setRank(rank, rankFiles);

			for(int thread = 0; thread < projectDescription.getProcessThreadCount(rank); thread++ ){
				final String traceFile = projectDescription.getAbsoluteFilenameOfTrace(rank, thread);
				final StAXTraceFileReader staxReader = traceCls.getConstructor(new Class<?>[]{String.class, boolean.class}).newInstance(new Object[]{traceFile, readNested}); 

				final ThreadsPerRankTraceContainer  threadFiles = new ThreadsPerRankTraceContainer(thread, staxReader);
				rankFiles.setThread(thread, threadFiles);				

				SimpleConsoleLogger.Debug("Checking trace: " + traceFile);

				// external statistics
				for(final String group: projectDescription.getExternalStatisticGroups()){
					final String statFileName = projectDescription.getAbsoluteFilenameOfStatistics(rank, thread, group);

					if (! (new File(statFileName)).canRead() ){
						SimpleConsoleLogger.Debug("Statistic file does not exist " + statFileName);
						continue;
					}

					SimpleConsoleLogger.Debug("Found statistic file for <rank,thread>=" + rank + "," + thread + " group " + group);

					ExternalStatisticsGroup statGroup = projectDescription.getExternalStatisticsGroup(group);
					if(statGroup == null){
						throw new IllegalArgumentException("Statistic group " + statGroup + 
								" invalid, <rank,thread>=" + rank + "," + thread);
					}

					final StatisticsReader statReader = statCls.getConstructor(new Class<?>[]{String.class, ExternalStatisticsGroup.class}).newInstance(new Object[]{statFileName, statGroup});

					threadFiles.setStatisticReader(statGroup.getName(), statReader);
				}
			}			
		}
	}

	public HashMap<String, HostnamePerProjectContainer> getHostnameProcessMap() {
		return hostnameProcessMap;
	}	
}

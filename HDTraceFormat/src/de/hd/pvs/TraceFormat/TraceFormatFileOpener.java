package de.hd.pvs.TraceFormat;

import java.io.File;

import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.project.ProjectDescriptionXMLReader;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticSource;
import de.hd.pvs.TraceFormat.topology.TopologyInternalLevel;
import de.hd.pvs.TraceFormat.topology.TopologyLabels;
import de.hd.pvs.TraceFormat.topology.TopologyLeafLevel;
import de.hd.pvs.TraceFormat.trace.TraceSource;

/**
 * This class opens all files belonging to a particular project.
 * 
 * @author julian
 *
 */
public class TraceFormatFileOpener {
	final ProjectDescription projectDescription;
	final ProjectDescriptionXMLReader projectDescrReader;
	
	public ProjectDescription getProjectDescription() {
		return projectDescription;
	}

	public ProjectDescriptionXMLReader getProjectDescriptionXMLReader() {
		return projectDescrReader;
	}
	
	// check if the file exists and is readable or not.
	private boolean fileExists(String filename){
		File file =  new File(filename);
		
		if(file.exists() && ! file.canRead()){
			throw new IllegalArgumentException("File " + filename + " exists but is not readable");
		}
		
		return file.exists();
	}
	
	private void recursivlyCreateTraceSources(TopologyInternalLevel currentTopo, Class<? extends StatisticSource> statCls, Class<? extends TraceSource> traceCls, boolean readNested) throws Exception{		
		final String filePath = projectDescription.getParentDir() + "/"; 
		
		// load statistics:
		for(StatisticsGroupDescription group: projectDescription.getExternalStatisticGroups()){
			String filename = filePath + currentTopo.getStatisticFileName(group.getName());
			if( fileExists(filename) ){
				// read it
				SimpleConsoleLogger.Debug("Stat file exists: " + filename);				
				
				final StatisticSource statReader = statCls.getConstructor(new Class<?>[]{String.class, StatisticsGroupDescription.class}).newInstance(new Object[]{filename, group});
				currentTopo.setStatisticReader(group.getName(), statReader);
			}
		}
		
		if( currentTopo.isLeaf() ){
			final TopologyLeafLevel leaf = (TopologyLeafLevel) currentTopo;
			final String traceFile = filePath + currentTopo.getTraceFileName();
			
			SimpleConsoleLogger.Debug("Checking trace: " + traceFile);
			
			if( ! fileExists(traceFile) ){
				throw new IllegalArgumentException("Trace file does not exist: " + traceFile);
			}
			TraceSource staxReader = traceCls.getConstructor(new Class<?>[]{String.class, boolean.class}).newInstance(new Object[]{traceFile, readNested});			
			
			leaf.setTraceSource(staxReader);
		}else{
			assert(currentTopo.getChildElements().size() > 0);
			
			for(TopologyInternalLevel child: currentTopo.getChildElements().values()){
				recursivlyCreateTraceSources(child, statCls, traceCls, readNested);
			}
		}

	}

	public TraceFormatFileOpener(String filename, boolean readNested, Class<? extends StatisticSource> statCls, Class<? extends TraceSource> traceCls) throws Exception{
		// scan for all the XML files which must be opened during conversion:
		// general description
		projectDescrReader = new ProjectDescriptionXMLReader();
		projectDescription = new ProjectDescription();
		projectDescrReader.readProjectDescription(projectDescription, filename);

		// start parsing of the trace files:
		// trace files: rank + thread id are defined in the file name
		
		TopologyInternalLevel root = projectDescription.getTopologyRoot();

		recursivlyCreateTraceSources(root, statCls, traceCls, readNested );
	}	
	
	public TopologyInternalLevel getTopology(){
		return projectDescription.getTopologyRoot();
	}
	
	public TopologyLabels getTopologyLabels(){
		return projectDescription.getTopologyLabels();
	}
}

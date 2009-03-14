package de.hd.pvs.traceConverter.Output.HDTrace;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.FileNames;
import de.hd.pvs.traceConverter.Input.ProcessIdentifier;
import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup;
import de.hd.pvs.traceConverter.Input.Statistics.StatisticType;
import de.hd.pvs.traceConverter.Input.Trace.EventTraceEntry;
import de.hd.pvs.traceConverter.Input.Trace.StateTraceEntry;
import de.hd.pvs.traceConverter.Output.TraceOutputConverter;

/**
 * Exports the XML data to a HDTrace project.
 * 
 * @author julian
 *
 */
public class HDTraceConverter  extends TraceOutputConverter{
	
	private String filePrefixPath;

	
	// map a single statistic group to a output writer
	HashMap<ExternalStatisticsGroup, HashMap<ProcessIdentifier, StatisticWriter>> statGroupWriterMap = new HashMap<ExternalStatisticsGroup, HashMap<ProcessIdentifier,StatisticWriter>>(); 
	
	@Override
	public void addTimeline(int rank, int thread, String name) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void Event(ProcessIdentifier id, Epoch time,
			EventTraceEntry traceEntry) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void initializeTrace(
			Properties commandLineArguments,
			String resultFile) {
		filePrefixPath = resultFile;
	}
	
	@Override
	public void StateEnd(ProcessIdentifier id, Epoch time,
			StateTraceEntry traceEntry) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void StateStart(ProcessIdentifier id, Epoch time,
			StateTraceEntry traceEntry) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void Statistics(ProcessIdentifier id, Epoch time, String statistic,
		ExternalStatisticsGroup group, Object value) {
		
		HashMap<ProcessIdentifier, StatisticWriter>  pidMap = statGroupWriterMap.get(group);
		if(pidMap == null){
			pidMap = new HashMap<ProcessIdentifier, StatisticWriter>();
			statGroupWriterMap.put(group, pidMap);
		}
		
		StatisticWriter outWriter = pidMap.get(id); 
		ExternalStatisticsGroup newGroupDef;
		
		// create a new group to write the definition, if it does not yet exist.
		if(outWriter == null){
			newGroupDef = 	new ExternalStatisticsGroup(group.getStatisticsOrdered(), group.getStatisticTypeMap());
			newGroupDef.setName(group.getName());
			//newGroup.setTimeOffset(timeOffset) 0.0 by default
			newGroupDef.setTimeResolutionMultiplier(1);
			// by default, write into epoch:
			newGroupDef.setTimestampDatatype(StatisticType.EPOCH);
			
			final String file = FileNames.getFilenameStatistics(filePrefixPath, id.getRank(), id.getVthread(), group.getName());
			try{
				// generate a new output writer
				outWriter = new StatisticWriter(file, newGroupDef);
			}catch(Exception e){
				throw new IllegalArgumentException("Statistic file could not be created: " + file);
			}
			
			pidMap.put(id, outWriter);
		}
		
		try{
			outWriter.writeStatisticEntry(time, statistic, value);
		}catch(IOException e){
			throw new IllegalArgumentException("Error during write in statistic file", e);
		}
	}
	

	
	@Override
	public void finalizeTrace() {
		for(HashMap<ProcessIdentifier, StatisticWriter> pidMap: statGroupWriterMap.values()){
			for(StatisticWriter writer: pidMap.values()){
				writer.finalize();
			}
		}
	}

}

package de.hd.pvs.traceConverter.Output.HDTrace;

import java.util.HashMap;
import java.util.Properties;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.FileNames;
import de.hd.pvs.traceConverter.Input.ProcessIdentifier;
import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup;
import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup.StatisticType;
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
	HashMap<ExternalStatisticsGroup, StatisticWriter> statGroupWriterMap = new HashMap<ExternalStatisticsGroup, StatisticWriter>(); 
	
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
		
		StatisticWriter outWriter = statGroupWriterMap.get(group.getName());
		ExternalStatisticsGroup newGroupDef;
		
		// create a new group to write the definition, if it does not yet exist.
		if(outWriter == null){
			newGroupDef = 	new ExternalStatisticsGroup();
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
			
			statGroupWriterMap.put(group, outWriter);
		}
		
		// check if the statistic is already existing:
		if(ou)
		
		outWriter.writeStatisticEntry(time, statistic, value);		
	}
	

	
	@Override
	public void finalizeTrace() {
		for(StatisticWriter writer: statGroupWriterMap.values()){
			writer.finalize();
		}
	}

}

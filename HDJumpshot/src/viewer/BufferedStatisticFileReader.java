package viewer;

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.statistics.StatisticEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsReader;
import de.hd.pvs.TraceFormat.util.Epoch;

public class BufferedStatisticFileReader extends StatisticsReader implements IBufferedReader{

	private Epoch minTime;
	private Epoch maxTime;
	
	ArrayList<StatisticEntry> statEntries = new ArrayList<StatisticEntry>();
	
	public BufferedStatisticFileReader(String filename, ExternalStatisticsGroup group) throws Exception{
		super(filename, group);
		
		StatisticEntry current = readNextInputEntry();
		
		minTime = current.getTimeStamp();
		
		while(current != null){
			statEntries.add(current);

			current = readNextInputEntry();
		}
		
		maxTime = statEntries.get(statEntries.size()-1).getTimeStamp();
	}
	
	public ArrayList<StatisticEntry> getStatEntries() {
		return statEntries;
	}

	public Epoch getMinTime() {
		return minTime;
	}
	
	public Epoch getMaxTime() {
		return maxTime;
	}
}

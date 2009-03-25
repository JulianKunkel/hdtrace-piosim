package hdTraceInput;

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.statistics.StatisticGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsReader;
import de.hd.pvs.TraceFormat.util.Epoch;

public class BufferedStatisticFileReader extends StatisticsReader implements IBufferedReader{

	private Epoch minTime;
	private Epoch maxTime;
	
	ArrayList<StatisticGroupEntry> statEntries = new ArrayList<StatisticGroupEntry>();
	
	public BufferedStatisticFileReader(String filename, ExternalStatisticsGroup group) throws Exception{
		super(filename, group);
		
		StatisticGroupEntry current = getNextInputEntry();
		
		minTime = current.getTimeStamp();
		
		while(current != null){
			statEntries.add(current);

			current = getNextInputEntry();
		}
		
		maxTime = statEntries.get(statEntries.size()-1).getTimeStamp();
	}
	
	public StatisticGroupEntry getTraceEntryClosestToTime(Epoch dTime){
		int min = 0; 
		int max = statEntries.size() - 1;
		
		while(true){
			int cur = (min + max) / 2;
			StatisticGroupEntry entry = statEntries.get(cur);
			
			if(min == max){ // found entry or stopped.
				return entry;
			} 
			// not found => continue bin search:
			
			if ( entry.getTimeStamp().compareTo(dTime) >= 0 ){
				max = cur;
			}else{
				min = cur + 1;
			}
		}
	}

	
	public ArrayList<StatisticGroupEntry> getStatEntries() {
		return statEntries;
	}

	public Epoch getMinTime() {
		return minTime;
	}
	
	public Epoch getMaxTime() {
		return maxTime;
	}
}

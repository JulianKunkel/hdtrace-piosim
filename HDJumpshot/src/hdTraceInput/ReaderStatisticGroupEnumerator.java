package hdTraceInput;

import java.util.ArrayList;
import java.util.Enumeration;

import de.hd.pvs.TraceFormat.statistics.StatisticGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.util.Epoch;

public class ReaderStatisticGroupEnumerator implements Enumeration<StatisticGroupEntry> {

	int currentPos;
	final ArrayList<StatisticGroupEntry> entries;
	final Epoch endTime;

	StatisticGroupEntry current;
	
	// read one more as required, i.e. to cover length of statistic
	boolean isFinalOne = false;

	public ReaderStatisticGroupEnumerator(BufferedStatisticFileReader reader, StatisticsGroupDescription group, Epoch startTime, Epoch endTime) {		
		entries = reader.getStatEntries();
		currentPos = reader.getStatisticPositionAfter(startTime) ;

		this.endTime = endTime;
		
		if(currentPos < 0){
			currentPos = entries.size() - 1;
		}
		current = entries.get(currentPos);
		currentPos++;
		
		if(current.getEarliestTime().compareTo(endTime) > 0){
			if(currentPos <= entries.size()){
				isFinalOne = true;
			}else{
				current = null;
			}
		}		
	}

	@Override
	public boolean hasMoreElements() {
		return current != null;
	}

	@Override
	public StatisticGroupEntry nextElement() {
		StatisticGroupEntry old = current;
	
		if(currentPos < entries.size()){
			current = entries.get(currentPos);
			
			currentPos++;
			
			if(isFinalOne){
				current = null;
			}else if(current.getEarliestTime().compareTo(endTime) > 0){
				isFinalOne = true;
			}
		}else{
			current = null;
		}

		return old;
	}


}

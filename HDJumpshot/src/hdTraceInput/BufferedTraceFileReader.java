package hdTraceInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import base.drawable.DrawObjects;

import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.trace.StAXTraceFileReader;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

public class BufferedTraceFileReader extends StAXTraceFileReader implements IBufferedReader {

	private Epoch minTime;
	private Epoch maxTime;

	ArrayList<XMLTraceEntry> traceEntries = new ArrayList<XMLTraceEntry>();

	public BufferedTraceFileReader(String filename, boolean nested) throws Exception {
		super(filename, nested);

		XMLTraceEntry current = getNextInputEntry();

		minTime = current.getTimeStamp();

		while(current != null){
			traceEntries.add(current);

			current = getNextInputEntry();
		}

		maxTime = traceEntries.get(traceEntries.size()-1).getTimeStamp();
	}

	public ArrayList<XMLTraceEntry> getTraceEntries() {
		return traceEntries;
	}

	public Epoch getMinTime() {
		return minTime;
	}

	public Epoch getMaxTime() {
		return maxTime;
	}

	
	/**
	 * Return the XMLTrace entry which is covered or closest to this time.
	 * 
	 * @param time
	 * @return
	 */
	public XMLTraceEntry getTraceEntryClosestToTime(double dTime){	
		int min = 0; 
		int max = traceEntries.size() - 1;
		
		while(true){
			int cur = (min + max) / 2;
			XMLTraceEntry entry = traceEntries.get(cur);
			
			if(min == max){ // found entry or stopped.
				XMLTraceEntry best = entry;
				double bestDistance = DrawObjects.getTimeDistance(dTime, entry);
								
				// check entries left and right.
				if( min != 0){
					XMLTraceEntry left = traceEntries.get(cur -1);
					double leftDistance = DrawObjects.getTimeDistance(dTime, left);
					
					if(leftDistance < bestDistance){
						bestDistance = leftDistance;
						best = left;
					}
				}
				
				if(max != traceEntries.size() -1){
					// check right
					XMLTraceEntry right = traceEntries.get(cur + 1);
					double rightDistance = DrawObjects.getTimeDistance(dTime, right);
					
					if(rightDistance < bestDistance){
						bestDistance = rightDistance;
						best = right;
					}
				}

				return best;
			} 
			// not found => continue bin search:
			
			if ( entry.getTimeStamp().getDouble() >= dTime ){
				max = cur;
			}else{
				min = cur + 1;
			}
		}
	}
}

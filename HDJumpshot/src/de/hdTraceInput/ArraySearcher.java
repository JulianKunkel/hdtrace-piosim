package de.hdTraceInput;

import java.util.ArrayList;

import de.drawable.DrawObjects;
import de.hd.pvs.TraceFormat.ITracableObject;
import de.hd.pvs.TraceFormat.util.Epoch;

public class ArraySearcher {
	
	static public int getPositionEntryOverlappingOrLaterThan(ArrayList list, Epoch laterThanTime){
		int min = 0; 
		int max = list.size() - 1;
		
		if(max < 0){
			return -1;
		}
		
		while(true){
			final int cur = (min + max) / 2;
			final ITracableObject entry = (ITracableObject) list.get(cur);
			
			if(min == max){ // found entry or stopped.
				final int ret = entry.getLatestTime().compareTo(laterThanTime);
				if( ret > 0 ){
					return cur;				
				}else if (ret == 0){
					if(cur +1 == list.size())
						return -1;
					return cur + 1;
				}else{				
					return -1;
				}
			} 
			// not found => continue bin search:
			
			if ( entry.getLatestTime().compareTo(laterThanTime) >= 0){
				max = cur;
			}else{
				min = cur + 1;
			}
		}
	}
		

	static public int getPositionEntryClosestToTime(ArrayList list, Epoch dTime){
		int min = 0; 
		int max = list.size() - 1;
		
		if(max < 0){
			return -1;
		}
		
		while(true){
			int cur = (min + max) / 2;
			ITracableObject entry = (ITracableObject) list.get(cur);
			
			if(min == max){ // found entry or stopped.
				int best = cur;
				double bestDistance = DrawObjects.getTimeDistance(dTime, entry);
								
				// check entries left and right.
				if( min != 0){
					ITracableObject left = (ITracableObject) list.get(cur -1);
					double leftDistance = DrawObjects.getTimeDistance(dTime, left);
					
					if(leftDistance < bestDistance){
						bestDistance = leftDistance;
						best = cur-1;
					}
				}
				
				if(max != list.size() -1){
					// check right
					ITracableObject right = (ITracableObject) list.get(cur + 1);
					double rightDistance = DrawObjects.getTimeDistance(dTime, right);
					
					if(rightDistance < bestDistance){
						bestDistance = rightDistance;
						best = cur+1;
					}
				}

				return best;
			} 
			// not found => continue bin search:
			
			if ( entry.getEarliestTime().compareTo(dTime) >= 0){
				max = cur;
			}else{
				min = cur + 1;
			}
		}
	}
	
	
}

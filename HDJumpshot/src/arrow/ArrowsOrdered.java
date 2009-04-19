package arrow;

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * A group which might contain a time sorted list of arrows.
 * 
 * @author julian
 */
public class ArrowsOrdered {
	/**
	 * Sorted in an increasing start time order.
	 */
	final ArrayList<Arrow> sortedArrows;
	
	public ArrowsOrdered(ArrayList<Arrow> sortedArrows) {
		this.sortedArrows = sortedArrows;
	}
	
	public ArrayList<Arrow> getSortedArrows() {
		return sortedArrows;
	}
	
	/**
	 * Does a binary search on the sortedArrows to find the first arrow later than the given time
	 * returns -1 if no such arrow exists.
	 */
	int searchArrowPositionWithLargerEndTimeThan(Epoch time){
		int min = 0; 
		int max = sortedArrows.size() - 1;
		
		if(max == -1)
			return -1;
		
		if(sortedArrows.get(max).getEndTime().compareTo(time) < 0 )
			return -1;
		
		while(true){
			int cur = (min + max) / 2;
			Arrow entry = sortedArrows.get(cur);
			
			if(min == max){ // found entry or stopped.
				return cur;
			} 
			
			// not found => continue bin search:			
			if ( entry.getEndTime().compareTo(time) >= 0){
				max = cur;
			}else{
				min = cur + 1;
			}
		}
	}
}

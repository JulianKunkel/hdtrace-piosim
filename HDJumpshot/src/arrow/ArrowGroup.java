package arrow;

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * A group which might contain a set of arrow categories and related arrows.
 * A group serves a purpose for instance MPI individual communication or 
 * client/server I/O communication. 
 * 
 * @author julian
 */
public class ArrowGroup {
	final String name;
	final ArrayList<ArrowCategory> categories;
	
	/**
	 * Sorted in an increasing start time order.
	 */
	final ArrayList<Arrow> sortedArrows;
	
	public ArrowGroup(String name, ArrayList<Arrow> sortedArrows, ArrayList<ArrowCategory> categories) {
		this.name = name;
		this.sortedArrows = sortedArrows;
		this.categories = categories;
	}
	
	public ArrayList<ArrowCategory> getCategories() {
		return categories;
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<Arrow> getSortedArrows() {
		return sortedArrows;
	}
	
	/**
	 * Does a binary search on the sortedArrows to find the first arrow later than the given time
	 * returns -1 if no such arrow exists.
	 */
	int searchArrowPositionWithLargerTimeThan(Epoch time){
		int min = 0; 
		int max = sortedArrows.size() - 1;
		
		if(max == -1)
			return -1;
		
		if(sortedArrows.get(max).getStartTime().compareTo(time) < 0 )
			return -1;
		
		while(true){
			int cur = (min + max) / 2;
			Arrow entry = sortedArrows.get(cur);
			
			if(min == max){ // found entry or stopped.
				return cur;
			} 
			
			// not found => continue bin search:			
			if ( entry.getStartTime().compareTo(time) >= 0){
				max = cur;
			}else{
				min = cur + 1;
			}
		}
	}
}

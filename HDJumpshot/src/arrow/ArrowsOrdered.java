//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.

package arrow;

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * A group which might contain a time sorted list of arrows.
 * 
 * @author Julian M. Kunkel
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

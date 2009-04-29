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
import java.util.Enumeration;
import java.util.Iterator;

import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Iterates through all existing arrows between a start and a end time which are visible.
 * Right now walks through all first group arrows, then next group.
 * 
 * @author Julian M. Kunkel
 */
public class VisibleArrowEnumerator implements Enumeration<Arrow>{
	final Iterator<ManagedArrowGroup> groupIter;
	
	final Epoch startTime;
	final Epoch endTime;

	ArrowsOrdered currentGroup = null;
	int        currentGroupPos = 0;
	Arrow      currentArrow = null;	

	private void loadNextGroup(){
		while(groupIter.hasNext()){
			final ManagedArrowGroup currentComputeState = groupIter.next();	
			currentGroup = currentComputeState.getArrowsOrdered();
			
			if(! currentComputeState.getCategory().isVisible() || ! currentComputeState.isComputed()){
				continue;
			}
			
			currentGroupPos = currentGroup.searchArrowPositionWithLargerEndTimeThan(startTime);
			if(currentGroupPos >= 0){
				// check time
				currentArrow = currentGroup.getSortedArrows().get(currentGroupPos);
				if(currentArrow.getStartTime().compareTo(endTime) < 0 ){
					// found one.
					return;
				}
			}
		}
		currentGroup = null;		
		currentArrow = null;
	}
	
	private void loadNextArrow(){
		final ArrayList<Arrow> arrows = currentGroup.getSortedArrows();
		if(currentGroupPos >= arrows.size()){
			// search next group
			loadNextGroup();
			return;
		}		
		this.currentArrow = arrows.get(currentGroupPos);
		
		currentGroupPos++;
		
		if(currentArrow.getStartTime().compareTo(endTime) >= 0 ){
			// no match, i.e. load next group.
			loadNextGroup();
		}
	}

	VisibleArrowEnumerator(Iterator<ManagedArrowGroup> groupIter, Epoch startTime, Epoch endTime) {
		this.groupIter = groupIter;
		this.endTime = endTime;
		this.startTime = startTime;
		
		loadNextGroup();
	}

	@Override
	public boolean hasMoreElements() {
		return currentArrow != null;
	}

	@Override
	public Arrow nextElement() {
		final Arrow last = currentArrow;
		loadNextArrow();
		return last;
	}

}

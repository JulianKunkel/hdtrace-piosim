
/** Version Control Information $Id: ReaderTraceElementEnumerator.java 342 2009-06-03 08:45:02Z kunkel $
 * @lastmodified    $Date: 2009-06-03 10:45:02 +0200 (Mi, 03. Jun 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 342 $ 
 */

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


package hdTraceInput;

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.trace.IStateTraceEntry;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * Walks through the trace entries contained in the relations between a start and endtime 
 * (latest object is one starting before endTime)
 * 
 * @author Julian M. Kunkel
 */
public class ReaderRelationTraceElementEnumerator implements ITraceElementEnumerator{

	// relation enumerator, tracks only internals.
	protected ReaderRelationEnumerator relationEnum; 
	
	// states in the relation, must be set to null once an invalid one is read (i.e. time > endtime).
	protected ArrayList<IStateTraceEntry> stateEntries = null;
	
	// position of the state in the relation
	protected int statePos;	

	// next element to extract
	protected ITraceEntry nextElement = null;

	final protected Epoch endTime;

	private void updateNextElement(){
		// depends on the inner state
		
		nextElement = null;
		
		// no state to walk to, jump to next relation
		if(stateEntries == null){
			statePos = 0;
			if(! relationEnum.hasMoreElements()){
				return;
			}
			
			stateEntries = relationEnum.nextElement().getStates();
		}
		
		// check for empty ones.
		while(relationEnum.hasMoreElements() && stateEntries.size() == 0){
			stateEntries = relationEnum.nextElement().getStates();
		}
		
		if(stateEntries == null){
			return;
		}
		
		// now we have to check the contained states, we know there is at least one state.
		nextElement = stateEntries.get(statePos++);
		
		if(statePos >= stateEntries.size()){
			stateEntries = null;
		}
		
		if( nextElement.getEarliestTime().compareTo(endTime) >= 0 ){
			// we finished.
			stateEntries = null;
			nextElement = null;
			return;
		}		
	}

	ReaderRelationTraceElementEnumerator(ReaderRelationEnumerator relationEnum, Epoch startTime, Epoch endTime) {
		this.relationEnum = relationEnum;
		this.endTime = endTime;
		
		if(! relationEnum.hasMoreElements()){
			this.stateEntries = null;
			return;
		}
		
		this.stateEntries = relationEnum.nextElement().getStates();
		
		// scan for start trace element:
		this.statePos = ArraySearcher.getPositionEntryOverlappingOrLaterThan(stateEntries, startTime);
		if(statePos < 0 || statePos >= stateEntries.size()){
			// change to the next one.
			
			if(! relationEnum.hasMoreElements()){
				this.stateEntries = null;
				return;
			}
			
			this.stateEntries = relationEnum.nextElement().getStates();
		} 
		
		updateNextElement();
	}

	ReaderRelationTraceElementEnumerator(ReaderRelationEnumerator relationEnum){
		this(relationEnum, new Epoch(Integer.MIN_VALUE, 0), new Epoch(Integer.MAX_VALUE, Integer.MAX_VALUE));
	}

	@Override
	public ITraceEntry nextElement() {
		ITraceEntry old = nextElement;
		updateNextElement();
		return old;	
	}

	@Override
	public ITraceEntry peekNextElement(){
		return nextElement;
	}

	@Override
	public boolean hasMoreElements() {
		return nextElement != null;
	}

	@Override
	public int getNestingDepthOfNextElement(){
		return 0;
	}
}

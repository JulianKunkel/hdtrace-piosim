
/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$ 
 */


//Copyright (C) 2008, 2009 Julian M. Kunkel

//This file is part of PIOsimHD.

//PIOsimHD is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//PIOsimHD is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.traceConverter.Input.Trace;


import java.io.IOException;
import java.util.Stack;

import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StAXTraceFileReader;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.traceConverter.Input.AbstractTraceProcessor;

/**
 * Reads data from a XML trace and triggers the appropriate Start/Stop Event/State calls. 
 * 
 * @author Julian M. Kunkel
 *
 */
public class TraceProcessor extends AbstractTraceProcessor{
	final StAXTraceFileReader reader;

	private Stack<StateTraceEntry> nestedStates = new Stack<StateTraceEntry>();
	private TraceObject currentTraceObject = null;
	private long          currentTraceEntryOffset = 0;

	/**
	 * If the currentTraceEntry is a State, does it start now, or end?
	 */
	private boolean stateStarts = true;
	private Epoch   eventTime;

	@Override
	public long getFilePosition() throws IOException {	
		return currentTraceEntryOffset;
	}

	public TraceProcessor(final StAXTraceFileReader reader) {
		this.reader = reader;
		readNextTraceEntryIfNecessary();
	}

	@Override
	public void initalize() {
		// register me on the trace converter, right now use just one timeline for our events.
		getOutputConverter().initalizeForTopology(getTopologyEntryResponsibleFor());
	}

	private void readNextTraceEntryIfNecessary(){		
		//System.out.println(currentTraceObject);

		if(currentTraceObject != null){
			// if it is a child, pick the next object:
			//System.out.println("Parent: " + currentTraceEntry.getName());

			if(stateStarts == true && currentTraceObject.getType() == TraceObjectType.STATE){
				StateTraceEntry state = (StateTraceEntry) currentTraceObject;

				if(state.hasNestedTraceChildren()){
					nestedStates.push(state);
					currentTraceObject = state.getNestedTraceChildren().pollFirst();
					eventTime = currentTraceObject.getEarliestTime();		

					stateStarts = true;
					return;
				}else{
					stateStarts = false;
					eventTime = state.getLatestTime();			
					return;
				}
			}			

			// now a state end is reached or an event.
			if(nestedStates.size() > 0){
				StateTraceEntry state = nestedStates.peek();

				if(state.hasNestedTraceChildren()){
					currentTraceObject = state.getNestedTraceChildren().pollFirst();
					eventTime = currentTraceObject.getEarliestTime();		

					stateStarts = true;
				}else{
					// finish State now
					currentTraceObject = nestedStates.pop();

					stateStarts = false;
					eventTime = state.getLatestTime();			
				}
				return;
			}

			// normal event end => read new...
		}

		stateStarts = true;
		currentTraceEntryOffset = reader.getFilePosition();

		currentTraceObject = reader.getNextInputEntry();
		if(currentTraceObject == null)
			return;

		eventTime = currentTraceObject.getEarliestTime();	
	}

	@Override
	public void processEarliestEvent(Epoch now) {		
		//System.out.println(eventTime.getFullDigitString() + " " + currentTraceObject + " processing " + " t " + currentTraceObject.getEarliestTime());

		if(currentTraceObject.getType() == TraceObjectType.EVENT){
			getOutputConverter().Event(getTopologyEntryResponsibleFor(), (EventTraceEntry) currentTraceObject);

		}else if(currentTraceObject.getType() == TraceObjectType.STATE){			
			StateTraceEntry state = (StateTraceEntry) currentTraceObject;

			if(stateStarts){
				getOutputConverter().StateStart(getTopologyEntryResponsibleFor(), state);
			}else{
				state.setEndTime(now);
				getOutputConverter().StateEnd(getTopologyEntryResponsibleFor(), state);
			}
		}		

		readNextTraceEntryIfNecessary();
	}

	@Override
	public Epoch peekEarliestTime() {		
		return eventTime;
	}

	@Override
	public boolean isFinished() {
		return currentTraceObject == null;
	}

}

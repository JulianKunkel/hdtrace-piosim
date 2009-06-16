
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

import de.hd.pvs.TraceFormat.ITracableObject;
import de.hd.pvs.TraceFormat.TracableObjectType;
import de.hd.pvs.TraceFormat.trace.IEventTraceEntry;
import de.hd.pvs.TraceFormat.trace.IStateTraceEntry;
import de.hd.pvs.TraceFormat.trace.StAXTraceFileReader;
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

	private static class StackElements{
		final IStateTraceEntry state;
		int curPos = 0;
		
		public StackElements(IStateTraceEntry element) {
			this.state = element;
		}
		
		public boolean hasNext(){
			return state.hasNestedTraceChildren() && curPos < state.getNestedTraceChildren().size();
		}
	}
	
	private Stack<StackElements> nestedStates = new Stack<StackElements>();
	private ITracableObject currentTraceObject = null;
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
		
		if(currentTraceObject != null){
			if(stateStarts == true && currentTraceObject.getType() == TracableObjectType.STATE){
				IStateTraceEntry state = (IStateTraceEntry) currentTraceObject;

				if(state.hasNestedTraceChildren()){
					final StackElements element = new StackElements(state);
					nestedStates.push( element );					
					eventTime = currentTraceObject.getEarliestTime();
					currentTraceObject = state.getNestedTraceChildren().get(element.curPos++);
					
					stateStarts = true;
					return;
				}else{
					stateStarts = false;
					eventTime = state.getLatestTime();			
					return;
				}
			}			

			// if it is a child, pick the next object			
			// now a state end is reached or an event.
			if(nestedStates.size() > 0){
				final StackElements element = nestedStates.peek();				
				
				if(element.hasNext()){
					currentTraceObject = element.state.getNestedTraceChildren().get(element.curPos++);
					eventTime = currentTraceObject.getEarliestTime();		

					stateStarts = true;
				}else{
					// finish State now
					currentTraceObject = nestedStates.pop().state;

					stateStarts = false;
					eventTime = currentTraceObject.getLatestTime();			
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
	public void processEarliestEvent(Epoch now) throws IOException{		
		//System.out.println(eventTime.getFullDigitString() + " " + currentTraceObject + " processing " + " t " + currentTraceObject.getEarliestTime());

		if(currentTraceObject.getType() == TracableObjectType.EVENT){
			getOutputConverter().Event(getTopologyEntryResponsibleFor(), (IEventTraceEntry) currentTraceObject);

		}else if(currentTraceObject.getType() == TracableObjectType.STATE){			
			IStateTraceEntry state = (IStateTraceEntry) currentTraceObject;

			if(stateStarts){
				getOutputConverter().StateStart(getTopologyEntryResponsibleFor(), state.getName(), state.getEarliestTime());
			}else{				
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

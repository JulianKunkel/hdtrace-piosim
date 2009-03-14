
//	Copyright (C) 2008, 2009 Julian M. Kunkel
//	
//	This file is part of PIOsimHD.
//	
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.traceConverter.Input.Trace;


import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.Input.AbstractTraceProcessor;
import de.hd.pvs.traceConverter.Input.Trace.XMLTraceEntry.TYPE;

/**
 * Reads data from a XML trace and triggers the appropriate Start/Stop Event/State calls. 
 * 
 * @author Julian M. Kunkel
 *
 */
public class TraceProcessor extends AbstractTraceProcessor{
	final SaxTraceFileReader reader;
	
	private XMLTraceEntry currentTraceEntry = null;	
	/**
	 * If the currentTraceEntry is a State, does it start now, or end?
	 */
	private boolean stateStart = true;
	private Epoch   eventTime;
	
	public TraceProcessor(final SaxTraceFileReader reader) {
		this.reader = reader;
		readNextTraceEntryIfNecessary();
	}
	
	@Override
	public void initalize() {
		// register me on the trace converter, right now use just one timeline for our events.
		getOutputConverter().addNormalTimeline(getPID());
	}
	
	private void readNextTraceEntryIfNecessary(){		
		//if(currentTraceEntry != null)
			//System.out.println(currentTraceEntry);
		
		// if it is a child, pick the next object:
		if(currentTraceEntry != null && currentTraceEntry.isTraceChild()){
			//System.out.println("Parent: " + currentTraceEntry.getName());
			
			if(currentTraceEntry.getType() == TYPE.STATE){
				StateTraceEntry state = (StateTraceEntry) currentTraceEntry;
				if(state.hasNestedTrace()){
					currentTraceEntry = state.getNestedTraceChildren().pollFirst();
					eventTime = currentTraceEntry.getTime();		

					stateStart = true;
					return;
				}
			}			
			
			currentTraceEntry = currentTraceEntry.getParentTraceData();
			// are there further children?
			StateTraceEntry state = (StateTraceEntry) currentTraceEntry;
			
			if(state.hasNestedTrace()){
				currentTraceEntry = state.getNestedTraceChildren().pollFirst();
				eventTime = currentTraceEntry.getTime();		
				
				stateStart = true;
			}else{
				// finish State now
				stateStart = false;
				eventTime = state.getTime().add(state.getDuration());			
			}
			
			return;
		}
		
		currentTraceEntry = reader.getNextInputData();
		if(currentTraceEntry == null)
			return;
		eventTime = currentTraceEntry.getTime();		
	}
		
	@Override
	public void processEarliestEvent(Epoch now) {		
		//System.out.println(eventTime.getFullDigitString() + " " + stateStart + " processing " + currentTraceEntry.getName() + " t " + currentTraceEntry.getTime());
		
		if(currentTraceEntry.getType() == TYPE.EVENT){
			getOutputConverter().Event(getPID(), now, (EventTraceEntry) currentTraceEntry);
			
			readNextTraceEntryIfNecessary();
		}else if(currentTraceEntry.getType() == TYPE.STATE){			
			StateTraceEntry state = (StateTraceEntry) currentTraceEntry;
			final String name = currentTraceEntry.getName();
			
			if(stateStart){
				if(getRunParameters().isProcessAlsoComputeEvents() || ! name.equals("Compute"))
					getOutputConverter().StateStart(getPID(), now, state);
				
				if(state.hasNestedTrace()){
					currentTraceEntry = state.getNestedTraceChildren().pollFirst();
					eventTime = currentTraceEntry.getTime();					
					stateStart = true;
				}else{
					stateStart = false;				
					eventTime = state.getTime().add(state.getDuration());
				}
			}else{
				if(getRunParameters().isProcessAlsoComputeEvents() || ! name.equals("Compute"))
					getOutputConverter().StateEnd(getPID(), now, state);
				
				stateStart = true;
				readNextTraceEntryIfNecessary();
			}
		}		
	}
	
	@Override
	public Epoch peekEarliestTime() {		
		return eventTime;
	}
	
	@Override
	public boolean isFinished() {
		return currentTraceEntry == null;
	}
	
}

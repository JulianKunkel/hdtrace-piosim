package de.hd.pvs.traceConverter.Input.Trace;


import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.traceConverter.Input.AbstractTraceProcessor;
import de.hd.pvs.traceConverter.Input.Trace.XMLTraceEntry.TYPE;

/**
 * Reads data from a XML trace and triggers the appropriate Start/Stop Event/State calls. 
 * 
 * @author julian
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

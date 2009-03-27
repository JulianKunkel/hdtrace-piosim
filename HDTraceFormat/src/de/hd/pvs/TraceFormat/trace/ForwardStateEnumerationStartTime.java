package de.hd.pvs.TraceFormat.trace;

import de.hd.pvs.TraceFormat.util.Epoch;


/**
 * Enumerate the children in correct time order, start with an initial time.
 * 
 * @author julian
 */
class ForwardStateEnumerationStartTime extends ForwardStateEnumeration{
	
	XMLTraceEntry current = null;
		
	public ForwardStateEnumerationStartTime(StateTraceEntry owner, Epoch startTime) {
		super(owner);
		while(super.hasMoreElements){
			current = super.nextElement();
			if (current.getEarliestTime().compareTo(startTime) >= 0){ // TODO could be optimized with bin tree algorithm.
				return;
			}
		}
	}
	
	@Override
	public XMLTraceEntry nextElement() {
		XMLTraceEntry old = current;
		
		if(super.hasMoreElements){
			current = super.nextElement();
		}else{
			current = null;
		}
		
		return old;
	}
	
	@Override
	public boolean hasMoreElements() {		
		return current != null;
	}
}
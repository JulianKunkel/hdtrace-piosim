/**
 * 
 */
package de.hd.pvs.TraceFormat.trace;

import java.util.Iterator;

class BackwardStateEnumeration extends ForwardStateEnumeration{
	public BackwardStateEnumeration(StateTraceEntry owner) {
		super(owner);
	}
	
	@Override
	protected Iterator<XMLTraceEntry> iterator(StateTraceEntry state) {		
		return state.getNestedTraceChildren().descendingIterator();
	}
}
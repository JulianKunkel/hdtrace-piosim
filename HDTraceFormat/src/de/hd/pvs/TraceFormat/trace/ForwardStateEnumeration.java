package de.hd.pvs.TraceFormat.trace;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Stack;

import de.hd.pvs.TraceFormat.TraceObjectType;

/**
 * Enumerate the children in correct time order
 * @author julian
 */
public class ForwardStateEnumeration implements Enumeration<XMLTraceEntry>{
	Stack<StateTraceEntry> nestedChildren = new Stack<StateTraceEntry>();
	Stack<Iterator<XMLTraceEntry>> nestedIterator = new Stack<Iterator<XMLTraceEntry>>();

	protected boolean hasMoreElements;

	public ForwardStateEnumeration(StateTraceEntry owner) {			
		pushOnStackIfPossible(owner);
		hasMoreElements = (nestedIterator.size() != 0);			
	}

	private void pushOnStackIfPossible(StateTraceEntry entry){
		if(entry.hasNestedTraceChildren()){
			nestedChildren.push(entry);
			nestedIterator.push( iterator(entry) );
		}			
	}
	
	protected Iterator<XMLTraceEntry> iterator(StateTraceEntry entry){
		return entry.getNestedTraceChildren().iterator();
	}

	@Override
	public boolean hasMoreElements() {
		return hasMoreElements;
	}

	@Override
	public XMLTraceEntry nextElement() {
		Iterator<XMLTraceEntry> iter = nestedIterator.peek();

		XMLTraceEntry obj = iter.next();

		if(obj.getType() == TraceObjectType.STATE){
			// we have to do a DFS, therefore go into this one:
			StateTraceEntry state = (StateTraceEntry) obj;
			pushOnStackIfPossible(state);
		}

		// update iter value:
		iter = nestedIterator.peek();			
		while(! iter.hasNext()){
			// pop from stack
			nestedIterator.pop();
			nestedChildren.pop();
			if(nestedChildren.size() == 0){
				hasMoreElements = false;
				break;
			}

			iter = nestedIterator.peek();				
		}

		return obj;
	}

	
	public int getNestingDepthOfNextElement() {
		return nestedChildren.size();
	}
}
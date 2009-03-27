package hdTraceInput;

import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.trace.ForwardStateEnumeration;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * walks through a list of XMLTraceEntries between a start and endtime and also through nested elements.
 * 
 * @author julian
 */
public class ReaderTraceElementNestedEnumerator extends ReaderTraceElementEnumerator {

	protected ForwardStateEnumeration stateChildEnumeration = null;

	public ReaderTraceElementNestedEnumerator(BufferedTraceFileReader reader, Epoch startTime, Epoch endTime) {
		super(reader, startTime, endTime);
		if (currentEntry == null || currentPos == 0)
			return;

		// scan if the state before has nested elements later than the given time.
		final XMLTraceEntry before = entries.get(currentPos -1 );

		if(before.getLatestTime().compareTo(startTime) <= 0){
			// then there might be no overlapping of the potential previous state.
			return;
		}

		if(before.getType() == TraceObjectType.STATE){
			// now its a state
			final StateTraceEntry state = (StateTraceEntry) before;
			if(! state.hasNestedTraceChildren())
				return;
			stateChildEnumeration = state.childForwardEnumeration(startTime);
			if(! stateChildEnumeration.hasMoreElements()){
				stateChildEnumeration = null;
			}else{
				// now we have found an event in the state directly before this one.
				currentPos--;
			}
		}
	}

	@Override
	public XMLTraceEntry nextElement() {
		if( stateChildEnumeration == null){ 
			final XMLTraceEntry current = super.nextElement();
			
			if(current.getType() == TraceObjectType.STATE){
				final StateTraceEntry state = (StateTraceEntry) current;
				if(! state.hasNestedTraceChildren())
					return current;
				
				stateChildEnumeration = state.childForwardEnumeration();
				return current;
			}
			
			return current;
		}
		
		// now stateChildEnumeration != null
		final XMLTraceEntry current = stateChildEnumeration.nextElement();
		
		if(! stateChildEnumeration.hasMoreElements()){
			stateChildEnumeration = null;
		}
		
		return current;
	}

	@Override
	public boolean hasMoreElements() {
		return stateChildEnumeration != null || super.hasMoreElements();
	}
	
	@Override
	public int getNestingDepthOfNextElement() {
		if (stateChildEnumeration == null)
			return 0;
		return stateChildEnumeration.getNestingDepthOfNextElement();
	}
}

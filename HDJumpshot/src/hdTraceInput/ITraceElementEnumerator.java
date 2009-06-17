package hdTraceInput;

import java.util.Enumeration;

import de.hd.pvs.TraceFormat.trace.ITraceEntry;

public interface ITraceElementEnumerator extends Enumeration<ITraceEntry>{
	
	public ITraceEntry peekNextElement();
	
	/**
	 * Return the nesting depth of the next element.
	 */
	public int getNestingDepthOfNextElement();
}

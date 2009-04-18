package arrow;

import hdTraceInput.TraceFormatBufferedFileReader;

/**
 * Abstract interface providing methods to compute a group of arrows with an dedicated task. 
 * 
 * @author julian
 */
public interface ArrowComputer {
	public ArrowGroup computeArrows(TraceFormatBufferedFileReader reader); 	
}

package arrow;

import hdTraceInput.TraceFormatBufferedFileReader;

/**
 * Abstract interface providing methods to compute a group of arrows with an dedicated task. 
 * 
 * @author julian
 */
public interface ArrowComputer {
	public ArrowsOrdered computeArrows(TraceFormatBufferedFileReader reader);
	public ArrowCategory getResponsibleCategory();
	
	/**
	 * Could be implemented to hide categories which are not applicable to the current reader.
	 */
	// public boolean isUseful(TraceFormatBufferedFileReader reader); 
}

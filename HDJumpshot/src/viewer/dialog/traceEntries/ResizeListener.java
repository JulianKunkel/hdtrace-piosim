package viewer.dialog.traceEntries;

import java.util.EventListener;

/**
 * Called when the Datatype View changed
 * @author julian
 */
public interface ResizeListener extends EventListener{
	/**
	 * Called when the datatype layout got changed.
	 */
	public void layoutRefreshed();
}

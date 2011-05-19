package de.hdTraceInput;

import java.util.EventListener;

import de.hd.pvs.TraceFormat.TraceFormatFileOpener;

public interface FileLoadedListener extends EventListener{
	/**
	 * This method is invoked if another file gets loaded.
	 */
	public void additionalFileLoaded();
}

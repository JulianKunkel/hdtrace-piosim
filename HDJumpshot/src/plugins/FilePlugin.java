package plugins;

import de.hd.pvs.TraceFormat.TraceFormatFileOpener;

/**
 * A plugin which can / or cannot activated on a given file.
 * 
 * @author julian
 */
abstract public class FilePlugin{
	/**
	 * A plugin might be activated only if it is applicable for a file.
	 * returns false if it can't be activated
	 * 
	 * If the plugin is already loaded for a first file the plugin must manage the
	 * activation for the second file as well i.e. it might need to update its inner states. 
	 */
	abstract public boolean tryToActivate(TraceFormatFileOpener file);
	
	final public Class<? extends FilePlugin> getPluginType(){
		return this.getClass();
	}
}

package plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import de.hd.pvs.TraceFormat.TraceFormatFileOpener;


/**
 * A plugin handler could be used in any component to manage plugins.
 * 
 * @author julian
 *
 */
public class PluginHandler<pluginType extends FilePlugin> {
	private final HashMap<Class<? extends FilePlugin>, pluginType> activePlugins = new HashMap<Class<? extends FilePlugin>, pluginType>();
	
	public void tryToActivatePlugins(List<Class<pluginType>> pluginsToTest, TraceFormatFileOpener file){
		for(Class<pluginType> type: pluginsToTest){
			final pluginType instance;
			
			if(activePlugins.containsKey(type)){
				// use existing instance to reactivate
				activePlugins.get(type).tryToActivate(file);
				continue;
			}
			
			try{
				instance = type.newInstance();
			}catch(Exception e){
				System.err.println("Error on loading plugin " + type.getCanonicalName());
				e.printStackTrace();
				continue;
			}
			
			if(instance.tryToActivate(file)){
				activePlugins.put(instance.getClass(), instance);
			}			
		}
	}
	
	public Collection<pluginType> getActivePlugins() {
		return activePlugins.values();
	}
	
	public pluginType getPlugin(Class<pluginType> type){
		return activePlugins.get(type);
	}
	
	public boolean isPluginActive(Class<pluginType> type){
		return activePlugins.containsKey(type);
	}
}

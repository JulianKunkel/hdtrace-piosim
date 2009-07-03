//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.

package de.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import de.hd.pvs.TraceFormat.TraceFormatFileOpener;


/**
 * A plugin handler could be used in any component to manage plugins.
 * 
 * @author Julian M. Kunkel
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

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

package plugins;

import de.hd.pvs.TraceFormat.TraceFormatFileOpener;

/**
 * A plugin which can / or cannot activated on a given file.
 * 
 * @author Julian M. Kunkel
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

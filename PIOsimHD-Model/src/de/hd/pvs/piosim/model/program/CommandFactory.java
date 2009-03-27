
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
  */


//	Copyright (C) 2008, 2009 Julian M. Kunkel
//	
//	This file is part of PIOsimHD.
//	
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

/**
 * 
 */
package de.hd.pvs.piosim.model.program;

import java.lang.reflect.Constructor;

import de.hd.pvs.piosim.model.program.commands.superclasses.Command;


/**
 * Factory which uses reflection to instantiate a command object of a given type. 
 * 
 * @author Julian M. Kunkel
 */
public class CommandFactory {
	/**
	 * Create a new Command of the given type. 
	 * 
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public Command createCommand(String type) throws Exception {
		/* dynamic reference via reflection */
		String className = "de.hd.pvs.piosim.model.program.commands." +  type.substring(0, 1).toUpperCase() + type.substring(1);
		
		try {
			Class<Command> cls = (Class<Command>) Class.forName( className);
			if (cls == null){
				throw new IllegalArgumentException("Model object not found: " + className);
			}
			Constructor<Command> ct = cls.getConstructor();
			return ct.newInstance();
		}
		catch (ClassCastException e) {
			throw new Exception("Invalid Command: " + type + " " + e.getMessage());
		}
	}

}


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
import java.util.HashMap;
import java.util.HashSet;

import de.hd.pvs.piosim.model.program.commands.NoOperation;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;


/**
 * Factory which uses reflection to instantiate a command object of a given type.
 *
 * @author Julian M. Kunkel
 */
public class CommandFactory {
	// TODO move to external file:
	final private static HashMap<String, String> commandClassMapping = new HashMap<String, String>();
	final private static HashSet<String> availableCommands = new HashSet<String>();


	static{
		commandClassMapping.put("File_open", "Fileopen");
	}

	static{
		availableCommands.add("Allgather");
		availableCommands.add("Allgather");
		availableCommands.add("Allreduce");
		availableCommands.add("Barrier");
		availableCommands.add("Bcast");
		availableCommands.add("Compute");
		availableCommands.add("Fileopen");
		availableCommands.add("Filereadall");
		availableCommands.add("Fileread");
		availableCommands.add("Filewriteall");
		availableCommands.add("Filewrite");
		availableCommands.add("Gather");
		availableCommands.add("NoOperation");
		availableCommands.add("Receive");
		availableCommands.add("Reduce");
		availableCommands.add("Send");
		availableCommands.add("Sendrecv");
		availableCommands.add("Sync");
		availableCommands.add("Wait");
	}

	/**
	 * Create a new Command of the given type.
	 *
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public Command createCommand(String type) {
		type = type.replaceAll("_", "");
		if(! availableCommands.contains(type)){
			return new NoOperation();
		}

		/* dynamic reference via reflection */
		final String className = "de.hd.pvs.piosim.model.program.commands." +  type;

		try {
			Class<Command> cls = (Class<Command>) Class.forName( className);
			if (cls == null){
				throw new IllegalArgumentException("Model object not found: " + className);
			}
			Constructor<Command> ct = cls.getConstructor();
			return ct.newInstance();
		}
		catch(Exception e){
			throw new IllegalArgumentException("Invalid Command: " + type + " " + e.getMessage());
		}
	}

}

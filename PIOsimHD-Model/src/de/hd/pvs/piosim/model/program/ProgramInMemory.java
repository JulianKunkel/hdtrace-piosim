
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

package de.hd.pvs.piosim.model.program;

import java.util.ArrayList;

import de.hd.pvs.piosim.model.program.commands.superclasses.Command;

/**
 * The full program is always in memory.
 *
 * @author Julian M. Kunkel
 */
public class ProgramInMemory extends Program {

	/**
	 * Sequence of commands to run.
	 */
	private final ArrayList<Command> commands = new ArrayList<Command>();

	/**
	 * The next command number to be read.
	 */
	private int currentCommandPosition = 0;

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (Command c : commands) {
			b.append(c + "\n");
		}
		return b.toString();
	}

	/**
	 * Return the number of commands in this program
	 * @return
	 */
	public int getSize() {
		return commands.size();
	}

	/**
	 * Return the list of commands to run.
	 * @return
	 */
	public ArrayList<Command> getCommands() {
		return commands;
	}

	public void addCommand(Command comm){
		commands.add(comm);
	}

	@Override
	public Command getNextCommand() {
		if(currentCommandPosition < commands.size())
			return commands.get(currentCommandPosition++);
		else
			return null;
	}

	@Override
	public boolean isFinished() {
		return currentCommandPosition == commands.size();
	}

	@Override
	public void restartWithFirstCommand() {
		currentCommandPosition = 0;
	}

	@Override
	public void setFilename(String filename) {
		//TODO does nothing
	}
}

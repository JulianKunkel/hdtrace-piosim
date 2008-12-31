
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

import java.util.ArrayList;

import de.hd.pvs.piosim.model.program.commands.superclasses.Command;

/**
 * Contains the sequential program trace for a single client process.
 * Several cooperating programs are an application.
 *  
 * @author Julian M. Kunkel
 * 
 */
public class Program {
	
	/**
	 * Sequence of commands to run.
	 */
	private final ArrayList<Command> commands = new ArrayList<Command>();
	
	/**
	 * The parent Application.
	 */
	private Application parentApplication = null;
	
	/**
	 * The rank of this program inside an application.
	 */
	private int rank;


	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (Command c : commands) {
			b.append(c + "\n");
		}
		return b.toString();
	}

	/**
	 * Return the parent application.
	 * @return
	 */
	public Application getApplication() {
		return parentApplication;
	}

	/**
	 * Create a new Program.
	 */
	public Program(Application parentApplication, int rank) {
		this.parentApplication = parentApplication;
		this.rank = rank;
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
	
	/**
	 * @return the program rank in the application
	 */
	public int getRank() {
		return rank;
	}
	
}

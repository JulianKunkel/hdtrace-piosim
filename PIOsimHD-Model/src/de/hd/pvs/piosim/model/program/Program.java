
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

import de.hd.pvs.piosim.model.program.commands.superclasses.Command;


/**
 * Contains the sequential program trace for a single client process.
 * Several cooperating programs are an application.
 *  
 * @author Julian M. Kunkel
 * 
 */
abstract public class Program {
	
	/**
	 * The parent Application.
	 */
	private Application parentApplication = null;
	
	/**
	 * The rank of this program inside an application.
	 */
	private int rank;
	
	/**
	 * The thread number within the rank.
	 */
	private int thread;
	
	/**
	 * Is the program processed completely
	 */
	abstract public boolean isFinished();
	
	/**
	 * Read the next command.
	 * @return
	 */
	abstract public Command getNextCommand();
	
	/**
	 * Allow to read all commands again => rewind to the first command
	 */
	abstract public void restartWithFirstCommand();

	/**
	 * Return the parent application.
	 * @return
	 */
	final public Application getApplication() {
		return parentApplication;
	}

	/**
	 * Create a new Program.
	 */
	public void setApplication(Application parentApplication, int rank, int thread) {
		this.parentApplication = parentApplication;
		this.rank = rank;
		this.thread = thread;
	}
	
	/**
	 * @return the program rank in the application
	 */
	final public int getRank() {
		return rank;
	}
	
	final public int getThread() {
		return thread;
	}
	
}

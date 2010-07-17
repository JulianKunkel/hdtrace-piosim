
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

package de.hd.pvs.piosim.simulator.event;

import de.hd.pvs.piosim.model.inputOutput.MPIFile;


/**
 * Describes a single I/O operation.
 *
 * @author Julian M. Kunkel
 *
 */
public class IOJob<UserData extends Object> implements EventData {
	static public enum IOOperation{
		READ,
		WRITE,
		FLUSH
	}

	final private long size;
	final private long offset;

	final private IOOperation type;

	final private UserData userData;

	final private MPIFile file;


	public IOJob(MPIFile file, UserData userData, long size, long offset, IOOperation operation) {
		this.userData = userData;
		this.size = size;
		this.offset = offset;

		this.type = operation;
		this.file = file;
	}

	public IOJob(IOJob<UserData> oldJob){
		this.userData = oldJob.userData;
		this.size = oldJob.size;
		this.offset = oldJob.offset;

		this.type = oldJob.type;
		this.file = oldJob.file;
	}


	public long getOffset() {
		return offset;
	}

	public long getSize() {
		return size;
	}

	public IOOperation getType() {
		return type;
	}

	public UserData getUserData() {
		return userData;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "<" + userData + "> " + getType() + "  <offset, size> = " + getOffset() + "," +  getSize() + ">";
	}

	public MPIFile getFile() {
		return file;
	}
}

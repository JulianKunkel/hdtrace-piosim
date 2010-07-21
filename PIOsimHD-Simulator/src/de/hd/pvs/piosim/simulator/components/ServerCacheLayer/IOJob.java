
 /** Version Control Information $Id: IOJob.java 781 2010-07-18 10:51:59Z kunkel $
  * @lastmodified    $Date: 2010-07-18 12:51:59 +0200 (So, 18. Jul 2010) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 781 $
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

package de.hd.pvs.piosim.simulator.components.ServerCacheLayer;

import de.hd.pvs.piosim.model.inputOutput.FileMetadata;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IOOperationData.IOOperationType;
import de.hd.pvs.piosim.simulator.event.EventData;


/**
 * Describes a single I/O operation.
 *
 * @author Julian M. Kunkel
 *
 */
public class IOJob<UserData extends Object, IOData extends IOOperationData> implements IOJobContainer, EventData {
	/**
	 * Operation data is used within the I/O layer to perform the specified operation
	 */
	final private IOData operationData;
	/**
	 * The operation to perform inside the I/O layer.
	 */
	final private IOOperationType operationType;

	/**
	 * The userData will not be used by the I/O layer.
	 */
	final private UserData userData;

	/**
	 * The file we work on
	 */
	final private FileMetadata file;


	public IOJob(FileMetadata file, UserData userData, IOOperationType operationType, IOData operationData) {
		this.userData = userData;
		this.file = file;

		this.operationData = operationData;
		this.operationType = operationType;
	}

	public IOData getOperationData() {
		return operationData;
	}

	public IOOperationType getOperationType() {
		return operationType;
	}

	public UserData getUserData() {
		return userData;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "<" + userData + "> " + operationType + " " + operationData;
	}

	public FileMetadata getFile() {
		return file;
	}

	@Override
	public int getNumberOfJobs() {
		return 1;
	}
}

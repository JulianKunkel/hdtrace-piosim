
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

package de.hd.pvs.piosim.simulator.components.IOSubsystem;

import de.hd.pvs.piosim.model.components.IOSubsystem.IOSubsystem;
import de.hd.pvs.piosim.simulator.base.ISPassiveComponent;
import de.hd.pvs.piosim.simulator.event.IOJob;
import de.hd.pvs.piosim.simulator.interfaces.IIOSubsystemCaller;

public interface IGIOSubsystem<Type extends IOSubsystem>
	extends ISPassiveComponent<Type>
{
	/**
	 * Set the I/O callback which gets invoked once an I/O operation finishes.
	 * @param callback
	 */
	public void setIOCallback(IIOSubsystemCaller callback);

	/**
	 * Start a new I/O job.
	 */
	public void startNewIO(IOJob job);


	/**
	 * return the number of pending jobs
	 * @return
	 */
	public int getNumberOfBlockedJobs();
}

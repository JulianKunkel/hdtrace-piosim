
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

package de.hd.pvs.piosim.model.components.IOSubsystem;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;

/**
 * Basic class for IOSubsystems.
 *
 * @author Julian M. Kunkel
 */
abstract public class IOSubsystem extends BasicComponent<Server> {

	@Attribute
	@NotNegativeOrZero
	/**
	 * The maximum number of requests which shall be scheduled on this component.
	 */
	private long maxConcurrentRequests = 1;

	/**
	 * @param maxConcurrentRequests the maxConcurrentRequests to set
	 */
	final public void setMaxConcurrentRequests(long maxConcurrentRequests) {
		this.maxConcurrentRequests = maxConcurrentRequests;
	}

	/**
	 * @return the maxConcurrentRequests
	 */
	final public long getMaxConcurrentRequests() {
		return maxConcurrentRequests;
	}

	public final String getObjectType() {
		return IOSubsystem.class.getSimpleName();
	}
}

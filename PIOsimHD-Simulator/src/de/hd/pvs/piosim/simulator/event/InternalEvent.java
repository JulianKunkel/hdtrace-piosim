
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

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.base.SBasicComponent;

/**
 * An internal event is an event a component submits to itself to start internal processing in
 * the future.
 *
 * Note: this class has a natural ordering that is
 * inconsistent with equals.
 *
 * @author Julian M. Kunkel
 *
 */
public class InternalEvent
implements Comparable<InternalEvent>
{

	/**
	 * The time this event can be started i.e. processed by the target component.
	 */
	private final Epoch earlistStartTime;

	/**
	 * The component which will start the event.
	 */
	private final SBasicComponent targetComponent;


	public InternalEvent(SBasicComponent targetComponent, Epoch earlistStartTime){
		this.earlistStartTime = earlistStartTime;
		this.targetComponent = targetComponent;
	}


	/**
	 * Return the earliest time the event is registered on the component and
	 * available for processing.
	 * @return
	 */
	public Epoch getEarliestStartTime(){
		return earlistStartTime;
	}

	public SBasicComponent getTargetComponent() {
		return targetComponent;
	}


	/**
	 * Events shall be sorted by the time they can be processed.
	 */
	@Override
	public int compareTo(InternalEvent o) {
		if(this.equals(o)){
			return 0;
		}

		return getEarliestStartTime().compareTo(o.getEarliestStartTime());

		// by using the hashCode it leads to different order depending on the memory address of the object.
		// therefore don't use it !
		//if (ret == 0){
		//	return (this.hashCode() < o.hashCode()) ? -1 : +1;
		//}
	}

	@Override
	public String toString() {
		return "IEvent " + getEarliestStartTime() + targetComponent;
	}
}

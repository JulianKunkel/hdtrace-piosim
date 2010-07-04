
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

package de.hd.pvs.piosim.simulator.base;

import java.util.LinkedList;

import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.event.EventData;

/**
 * A blocking component schedules only one "job" after another.
 * A job is defined by a start and end event.
 * If the component is busy it will queue up new events.
 * The earliest event in the queue gets scheduled, once the component gets ready again.
 *
 * @author Julian M. Kunkel
 */
abstract public class SSequentialBlockingComponent<Type extends IBasicComponent, EventDataType extends EventData>
	extends SSchedulableBlockingComponent<Type, EventDataType>
{
	/**
	 * The actual queue of pending events (contains blocked events)
	 */
	private LinkedList<Event<EventDataType>> waitingEvents = new LinkedList<Event<EventDataType>>();

	@Override
	final protected void addNewEvent(Event<EventDataType> job) {
		waitingEvents.add(job);
	}

	@Override
	protected Event<EventDataType> getNextPendingAndSchedulableEvent() {
		Event<EventDataType> ev = waitingEvents.poll();
		assert(ev != null);
		return ev;
	}

	@Override
	final public int getNumberOfBlockedJobs() {
		return waitingEvents.size();
	}


	/**
	 * Print the queue of the pending events (for debugging)
	 */
	final public void printWaitingEvents(){
			System.out.println("waiting events for " + this.getIdentifier());
			for (Event e : waitingEvents) {
				System.out.println("    : \"" + e + " inc: " + e.getEarliestStartTime());
			}
	}

	@Override
	public void simulationFinished() {
		super.simulationFinished();
		if(waitingEvents.size() > 0){
			System.err.println("ERROR: pending events on component " + this.getIdentifier() + " BlockedJobs: " + this.getNumberOfBlockedJobs());
			printWaitingEvents();
		}
	}
}

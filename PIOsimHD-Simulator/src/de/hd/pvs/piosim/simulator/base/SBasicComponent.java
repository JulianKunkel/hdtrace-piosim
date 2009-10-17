
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

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.event.InternalEvent;

/**
 * A BasicComponent can get events submitted by other components.
 * The simulator chooses the component with the next event and runs its methods to execute that
 * event.
 *
 * @author Julian M. Kunkel
 *
 * @param <ModelComp> The model component which should be assigned to this simulation object.
 * @param <EventType> If the component only accepts one event type it should be specified in this
 * 		template variable.
 */
public abstract class SBasicComponent<ModelComp extends IBasicComponent>
  extends SPassiveComponent<ModelComp>
{
	/**
	 * Remembers the next internal event at which we should wake up.
	 */
	InternalEvent nextWakeUpEvent = null;

	/**
	 * The simulator calls this function to allow the component to actually run its next event.
	 * This means the particular component must have an event which must be scheduled next.
	 */
	abstract public void processEvent(Event event, Epoch time);

	/**
	 * Process an event the component started for itself
	 * @param event
	 */
	abstract public void processInternalEvent(InternalEvent event, Epoch time);


	/**
	 * Try to start pending operations on the component at a given epoch.
	 * Implements some kind of timer when this component should be woken up.
	 *
	 * @param when is added to the simulation current time
	 */
	final protected void setNewWakeupTimerInFuture(Epoch futureOffset){
		setNewWakeupTimerAbsolute(futureOffset.add(getSimulator().getVirtualTime()));
	}

	/**
	 * Try to start pending operations on the component at a given epoch.
	 * Implements some kind of timer when this component should be woken up.
	 *
	 * @param when is the absolute time
	 */
	final protected void setNewWakeupTimerAbsolute(Epoch when){
		//System.out.println("Wakeup timer SET " + this.getIdentifier() +" " + when );

		debug("when: " + when);

		getSimulator().submitNewEvent(new InternalEvent(this, when));
	}

	// update the wakeup timer to the current time
	final protected void setNewWakeupTimeNow(){
		setNewWakeupTimerAbsolute(getSimulator().getVirtualTime());
	}

	/**
	 * Similar to startTimedWakeup this function sets a timer in the future when the component
	 * should wake up. However, it tries to remove old timers from the simulator. (i.e. at most
	 * one timer should be pending).
	 *
	 * @param futureOffset is added to the simulation current time
	 */
	final protected void updateWakeupTimerInFuture(Epoch futureOffset){
		updateWakeupTimer(futureOffset.add(getSimulator().getVirtualTime()));

	}

	/**
	 * Similar to startTimedWakeup this function sets a timer in the future when the component
	 * should wake up. However, it tries to remove old timers from the simulator. (i.e. at most
	 * one timer should be pending).
	 *
	 * @param when
	 */
	final protected void updateWakeupTimer(Epoch when){
		getSimulator().deleteFutureEvent(nextWakeUpEvent);
		nextWakeUpEvent = new InternalEvent(this, when);
		getSimulator().submitNewEvent(nextWakeUpEvent);
	}

	@Override
	public String toString() {
		return super.toString() + "<" + getIdentifier() +">" ;
	}
}


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

import de.hd.pvs.TraceFormat.relation.RelationToken;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.base.ISBasicComponent;
import de.hd.pvs.piosim.simulator.base.ISPassiveComponent;

/**
 * An event is a single occurrence of some work information submitted to a particular component.
 * Typically, it leads to an observable change of the component state.
 * An event can happen in the future.
 *
 * @author Julian M. Kunkel
 *
 * @param <EventType>
 */
final public class Event<EventType extends EventData>
extends InternalEvent
{

	/**
	 * The source of the event.
	 */
	final ISPassiveComponent issueingComponent;

	/**
	 * Information about the event i.e. data contained.
	 */
	final EventType event;

	/**
	 * Track the path of the events i.e. which event caused this event.
	 */
	final RelationToken relationToken;

	/**
	 * Return information about the event.
	 * @return
	 */
	public EventType getEventData() {
		return event;
	}

	/**
	 * This constructor creates a  new event.
	 */
	public Event(ISPassiveComponent issueingComponent, ISBasicComponent targetComponent,
			Epoch earlistStartTime, EventType eventData, RelationToken previousToken){
		super(targetComponent, earlistStartTime);
		this.issueingComponent = issueingComponent;
		this.event = eventData;
		assert(issueingComponent != null);
		assert(earlistStartTime != null);

		relationToken = previousToken;
	}

	public RelationToken getRelationToken() {
		return relationToken;
	}

	/**
	 * Return the source of this event i.e. the component which submitted the event.
	 * @return
	 */
	public ISPassiveComponent getIssuingComponent(){
		return issueingComponent;
	}


	@Override
	public String toString() {
		return " Event: " + getTargetComponent() +  " data class: " + event.getClass().getSimpleName() +
		" eStartTime: " + getEarliestStartTime() + " Data: " + event;
	}
}

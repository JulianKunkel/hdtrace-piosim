
 /** Version Control Information $Id: SFlowComponent.java 611 2009-07-27 12:14:42Z kunkel $
  * @lastmodified    $Date: 2009-07-27 14:14:42 +0200 (Mo, 27. Jul 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 611 $
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

/**
 *
 */
package de.hd.pvs.piosim.simulator.base;

import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.network.MessagePart;
import de.hd.pvs.piosim.simulator.output.STraceWriter.TraceType;

/**
 * A SFlowComponent does never split or delete a single Event,
 * instead it only passes the event with the SAME data to another Component.
 * Internally, a SFlowComponent tries to submit a limited number of jobs to the next component.
 * However, enough to keep the source and the drain busy (i.e. optimal).
 * This is similar to a data flow and looks more realistic than having an unlimited receive buffer.
 *
 * For instance this is useful for network devices.
 *
 * @author Julian M. Kunkel
 *
 */
abstract public class SNetworkComponent<Type extends IBasicComponent>
	extends SSequentialBlockingComponent<Type, MessagePart> implements ISNetworkComponent<Type>
{
	/**
	 * This class describes the state of the connection to a particular endpoint.
	 * All jobs which should be transfered to the same endpoint get an instance of this class assigned.
	 *
	 * @author Julian M. Kunkel
	 */
	static private class ConcurrentEvents {
		/**
		 * The jobs which are blocked right now.
		 */
		final LinkedList<Event> blockedJobs = new LinkedList<Event>();

		/**
		 * The sum of the latency of all jobs which are processed in the following component.
		 */
		Epoch              usedLatency = Epoch.ZERO;

		/**
		 * Number of jobs which are currently processed or pending in the next component.
		 */
		int               jobsInTransit = 0;

		/**
		 * Is transfer to this endpoint blocked? This is the case if too many events are under way.
		 */
		boolean           blocked = false;

		@Override
		public String toString() {
			return " currentlyPendingLat:" + usedLatency + " blockedEvents: " + blockedJobs.size();
		}

		boolean isBlocked(){
			return blocked;
		}

		void block(){
			this.blocked = true;
		}

		void unblock(){
			this.blocked = false;
		}
	};

	/**
	 * for each target endpoint the state of the events is observed.
	 */
	private HashMap<INetworkExit, ConcurrentEvents > concurrentEvents =
		new HashMap<INetworkExit, ConcurrentEvents >();

	/**
	 * The maximum time a job can require in this component.
	 * It is needed to approximate the number of jobs which must be submitted to the target.
	 */
	private Epoch maxTimeToTransferAJobToTheNextComponent = null;

	/**
	 * Determine the maximum latency a job can take.
	 * @return
	 */
	private Epoch getMaxTimeToTransferAJobToTheNextComponent() {
		if(maxTimeToTransferAJobToTheNextComponent == null){
			maxTimeToTransferAJobToTheNextComponent = getProcessingLatency().add( getMaximumProcessingTime() );
		}

		return maxTimeToTransferAJobToTheNextComponent;
	}

	/**
	 * Lookup the component the particular job must be delivered.
	 * @param event
	 * @return
	 */
	abstract public ISNetworkComponent getTargetFlowComponent(MessagePart event);

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#flowJobTransferred(de.hd.pvs.piosim.simulator.network.MessagePart, de.hd.pvs.TraceFormat.util.Epoch)
	 */
	public void messagePartTransmitted(MessagePart part, Epoch endTime){

	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#eventDestroyed(de.hd.pvs.piosim.simulator.network.MessagePart, de.hd.pvs.TraceFormat.util.Epoch)
	 */
	public void messagePartDestroyed(MessagePart part, Epoch endTime){

	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#isDirectlyControlledByBlockUnblock()
	 */
	public boolean isDirectlyControlledByBlockUnblock(){
		return false;
	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#getProcessingTime(de.hd.pvs.piosim.simulator.network.MessagePart)
	 */
	abstract public Epoch getProcessingTime(MessagePart part);

	@Override
	final protected Epoch getProcessingTimeOfScheduledJob(MessagePart eventData) {
		return getProcessingTime(eventData);
	};

	/**
	 * In case an event of a request was blocked the component only continues
	 * if it is notified to activate the Request or the target advises to continue transferring jobs.
	 * This can be done by this call.
	 * By calling this method, the "first" blocked job is enabled in this component for further
	 * processing.
	 */
	private final void continueProcessingOfFlow(
			INetworkExit target,
			MessagePart part,
			Epoch startTime,
			ISNetworkComponent targetFlowComponent)
	{
		getSimulator().getTraceWriter().event(TraceType.INTERNAL, this, "ContinueFlow", 1);
		ConcurrentEvents pendingEvents = concurrentEvents.get(target);

		debug("criterion: " + target);

		assert(isDirectlyControlledByBlockUnblock() == false);
		assert(pendingEvents != null);
		pendingEvents.usedLatency = pendingEvents.usedLatency.subtract(getProcessingTime(part));
		pendingEvents.jobsInTransit--;

		if ( pendingEvents.blockedJobs.size() == 0 ){
			debugFollowUpLine(" EMPTY: criterion: " + target);
			return;
		}

		/* activate an object only if the pendingLatency is smaller than the ProcessingLatency */

		if( pendingEvents.usedLatency.compareTo( getMaxTimeToTransferAJobToTheNextComponent()) >= 0 ){
			debugFollowUpLine("Pending latency is still too high");
			return;
		}

		getSimulator().getTraceWriter().event(TraceType.INTERNAL, this, "ContinueFlowSucc", 0);

		Event<MessagePart> firstEvent = pendingEvents.blockedJobs.poll();

		/*
		 * it is imperative to add the event with the smallest incoming time, but this happens implicitly right now
		 */
		addNewEvent(firstEvent);
		if(getState() == State.READY){
			// reactivate the component
			timerEvent(getSimulator().getVirtualTime());
		}

		debugFollowUpLine(  "sucessfullyReActivatedEvent for: " + firstEvent.getIssuingComponent().getIdentifier() +  " " +  firstEvent);
	}

	/**
	 * Check if there are jobs blocked for the particular target.
	 * @param target
	 * @return true if this component is blocked.
	 */
	private boolean isBlockedByEndpoint(INetworkExit target){
		ConcurrentEvents pendingEvents = concurrentEvents.get(target);

		if(pendingEvents == null) return false;

		if(isDirectlyControlledByBlockUnblock())
			return pendingEvents.isBlocked();

		Epoch maxTime = getMaxTimeToTransferAJobToTheNextComponent();

		int ret = pendingEvents.usedLatency.compareTo( maxTime);

		debug(" return: " + ret + " " + pendingEvents + " MAXTIME " + maxTime + " pUSEDLAT: " +  pendingEvents.usedLatency);

		return ret > 0 || (ret == 0 && maxTime.compareTo(Epoch.ZERO) > 0) ;
	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#getNextPendingAndSchedulableEvent()
	 */
	@Override
	final public Event<MessagePart> getNextPendingAndSchedulableEvent() {
		while(getNumberOfBlockedJobs() > 0){
			Event<MessagePart> event = super.getNextPendingAndSchedulableEvent();

			INetworkExit target = event.getEventData().getMessageTarget();

			ConcurrentEvents pendingEvents = concurrentEvents.get(target);
			if (pendingEvents == null){
				pendingEvents = new ConcurrentEvents();
				concurrentEvents.put(target, pendingEvents);
			}

			debug(  "pending events:" + pendingEvents + " " +  event.getEventData());

			assert(getProcessingLatency() !=  null);

			/*
			 * Block if the pending processed jobs time is bigger than the latency of this component.
			 */
			//System.out.println(getIdentifier().getName() + " " + pendingEvents.availableRuntimeToUse +" " +  jobRunTime);

			// check if the flow to the target is blocked, then this event should not be scheduled at all.
			if( isBlockedByEndpoint(target) ){
				getSimulator().getTraceWriter().event(TraceType.INTERNAL, this, "Block", 1);

				pendingEvents.blockedJobs.add(event);

				continue;
			}

			if(! isDirectlyControlledByBlockUnblock()){
				Epoch jobRunTime = getProcessingTime(event.getEventData());

				// manage flow control here:
				pendingEvents.usedLatency = pendingEvents.usedLatency.add(jobRunTime);
				pendingEvents.jobsInTransit++;
			}

			debugFollowUpLine("pending runtime: " + pendingEvents.usedLatency);

			return event;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#jobStarted(de.hd.pvs.piosim.simulator.event.Event, de.hd.pvs.TraceFormat.util.Epoch)
	 */
	@Override
	final public void jobStarted(Event<MessagePart> event, Epoch startTime) {
		debug( "issuing comp: " +  event.getIssuingComponent().getIdentifier());

		ISPassiveComponent source =  event.getIssuingComponent();

		getSimulator().getTraceWriter().startState(TraceType.INTERNAL, this, buildTraceEntry(event.getEventData()));

		/**
		 * special case for NIC which generates new events.
		 */
		if(SNetworkComponent.class.isInstance( event.getIssuingComponent() ) && this != source
				&& ! ((ISNetworkComponent) source).isDirectlyControlledByBlockUnblock() ){
			// manage flow control here:
			INetworkExit target = event.getEventData().getMessageTarget();
			((SNetworkComponent) source).continueProcessingOfFlow(
					event.getEventData().getMessageTarget(), event.getEventData(), startTime, this );
		}

		if(isDirectlyControlledByBlockUnblock()){
			// then submit exactly one other job to the same target as long as it is not blocked.

			ConcurrentEvents pendingEvents = concurrentEvents.get(event.getEventData().getMessageTarget());
			if(pendingEvents.blockedJobs.size() != 0){
				Event firstEvent = pendingEvents.blockedJobs.poll();
				addNewEvent(firstEvent);
			}
		}
	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#timerEvent(de.hd.pvs.TraceFormat.util.Epoch)
	 */
	@Override
	public void timerEvent(Epoch wakeUpTime) {
		// gets called if a component is idle and gets reactivated, to reactivate the component
		// a new timerEvent is submitted.
		startNextPendingEventIfPossible(wakeUpTime);
	}

	private String buildTraceEntry(MessagePart part){
		return "msg_" + part.getMessageSource().getIdentifier().getID() + "_" + part.getMessageTarget().getIdentifier().getID();
	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#jobCompleted(de.hd.pvs.piosim.simulator.event.Event, de.hd.pvs.TraceFormat.util.Epoch)
	 */
	@Override
	final public void jobCompleted(Event<MessagePart> event, Epoch endTime) {
		debug( " event " + event);

		getSimulator().getTraceWriter().endState(TraceType.INTERNAL, this, buildTraceEntry(event.getEventData()));

		final ISNetworkComponent targetComponent = getTargetFlowComponent(event.getEventData());

		if(targetComponent == null){
			messagePartDestroyed(event.getEventData(), endTime);
			return;
		}

		messagePartTransmitted(event.getEventData(), endTime);

		final Event newEvent = new Event(this,
				targetComponent,
				endTime.add(getProcessingLatency()),
				event.getEventData());

		getSimulator().submitNewEvent( newEvent);
	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#blockFlow(de.hd.pvs.piosim.model.networkTopology.INetworkExit)
	 */
	public void blockFlow(INetworkExit target){
		ConcurrentEvents pendingEvents = concurrentEvents.get(target);
		assert(pendingEvents.blocked == false);
		assert(isDirectlyControlledByBlockUnblock() == true);
		pendingEvents.blocked = true;
	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#unblockFlow(de.hd.pvs.piosim.model.networkTopology.INetworkExit)
	 */
	public void unblockFlow(INetworkExit target){
		ConcurrentEvents pendingEvents = concurrentEvents.get(target);
		assert(pendingEvents.blocked == true);
		pendingEvents.blocked = false;

		if(getState() == State.READY){
			if(pendingEvents.blockedJobs.size() != 0){
				Event firstEvent = pendingEvents.blockedJobs.poll();
				addNewEvent(firstEvent);

				timerEvent(getSimulator().getVirtualTime());
			}
		}
	}
}

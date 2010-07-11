
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
extends SSchedulableBlockingComponent<Type, MessagePart> implements ISNetworkComponent<Type>
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
		final LinkedList<Event> pendingJobs = new LinkedList<Event>();

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
		boolean           blockedByLatency = false;

		/**
		 * True if the user decided to block it
		 */
		boolean           blockedManually = false;

		/**
		 * True if this exit is contained in eventsPerExit.
		 */
		boolean           isScheduled = false;

		@Override
		public String toString() {
			return " currentlyPendingLat:" + usedLatency + " blockedEvents: " + pendingJobs.size();
		}
	};

	/**
	 * for each target endpoint the state of the events is observed.
	 */
	private HashMap<INetworkExit, ConcurrentEvents > eventsPerExit =
		new HashMap<INetworkExit, ConcurrentEvents >();

	/**
	 * Contains all exits for which we have operations.
	 */
	private LinkedList<INetworkExit> pendingExits = new LinkedList<INetworkExit>();

	/**
	 * The total number of jobs.
	 */
	private int numberOfPendingJobs = 0;

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

	@Override
	final protected void addNewEvent(Event<MessagePart> job) {
		INetworkExit target = job.getEventData().getMessageTarget();

		ConcurrentEvents pendingEvents = eventsPerExit.get(target);
		if (pendingEvents == null){
			pendingEvents = new ConcurrentEvents();
			eventsPerExit.put(target, pendingEvents);
		}

		pendingEvents.pendingJobs.add(job);

		numberOfPendingJobs++;

		//System.out.println( this.getIdentifier() + " addNewEvent " + " " + job.getEventData() );

		// should we activate the particular exit?
		if(     ! pendingEvents.isScheduled
				&& ! pendingEvents.blockedManually
				&& !pendingEvents.blockedByLatency )
		{
			// now schedule the exit:
			pendingEvents.isScheduled = true;
			pendingExits.add(target);
			reactivateBlockingComponentNow();
		}

	}

	@Override
	final public int getNumberOfBlockedJobs() {
		return numberOfPendingJobs;
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

	/**
	 * The target got a message part from us, now it starts to process the message part.
	 * @param part
	 * @param endTime
	 */
	public void messagePartReceivedAndStartedProcessing(ISNetworkComponent target, MessagePart part, Epoch time){

	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#eventDestroyed(de.hd.pvs.piosim.simulator.network.MessagePart, de.hd.pvs.TraceFormat.util.Epoch)
	 */
	public void messagePartDestroyed(MessagePart part, Epoch endTime){

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
	protected void continueProcessingOfFlow(
			INetworkExit target,
			MessagePart part,
			Epoch startTime,
			ISNetworkComponent targetFlowComponent)
	{
		final ConcurrentEvents pendingEvents = eventsPerExit.get(target);

		debug("criterion: " + target);

		//System.out.println( this.getIdentifier() + " continueProcessingOfFlow " + " target " + target.getIdentifier() +  " " + part.getMessage());

		assert(pendingEvents.jobsInTransit > 0);

		pendingEvents.usedLatency = pendingEvents.usedLatency.subtract(getProcessingTime(part));
		pendingEvents.jobsInTransit--;

		if ( ! pendingEvents.blockedByLatency ){
			debugFollowUpLine(" Target is not blocked: " + target);
			return;
		}

		/* activate an object only if the pendingLatency is smaller than the ProcessingLatency */

		if( pendingEvents.jobsInTransit > 0 && pendingEvents.usedLatency.compareTo( getMaxTimeToTransferAJobToTheNextComponent()) >= 0 ){
			debugFollowUpLine("Pending latency is still too high");
			return;
		}

		//getSimulator().getTraceWriter().event(TraceType.INTERNAL, this, "ContinueFlowSucc", 0);

		pendingEvents.blockedByLatency = false;

		if( pendingEvents.isScheduled
				|| pendingEvents.pendingJobs.isEmpty()
				|| pendingEvents.blockedManually ){
			// if more operations are in transit, then the reactivation already happened in job_complete.
			return;
		}

		//System.out.println(" re-activating.");

		// add to the exit
		pendingExits.add(target);
		pendingEvents.isScheduled = true;

		reactivateBlockingComponentNow();
	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#getNextPendingAndSchedulableEvent()
	 */
	@Override
	final public Event<MessagePart> getNextPendingAndSchedulableEvent() {
		//System.out.println("getNextPendingAndSchedulableEvent " + this.getIdentifier());
		while(! pendingExits.isEmpty() ){
			final INetworkExit target = pendingExits.poll();

			//System.out.println(" checking " + target.getIdentifier());

			final ConcurrentEvents pendingEvents = eventsPerExit.get(target);

			assert(pendingEvents.pendingJobs.size() > 0);
			assert(pendingEvents.isScheduled == true);
			assert(pendingEvents.blockedByLatency == false);

			pendingEvents.isScheduled = false;

			// remove blocked events:
			if(pendingEvents.blockedManually){
				continue;
			}

			// dispatch event:
			final Event<MessagePart> event = pendingEvents.pendingJobs.poll();

			numberOfPendingJobs--;

			debug(  "pending events:" + pendingEvents + " " +  event.getEventData());

			/*
			 * Block if the pending processed jobs time is bigger than the latency of this component.
			 */
			//System.out.println( getIdentifier() + " getNextPendingAndSchedulableEvent");

			Epoch jobRunTime = getProcessingTime(event.getEventData());

			// manage flow control here:
			pendingEvents.usedLatency = pendingEvents.usedLatency.add(jobRunTime);
			pendingEvents.jobsInTransit++;

			if( pendingEvents.usedLatency.compareTo(getMaxTimeToTransferAJobToTheNextComponent()) >= 0 ){
				// now we have enough components in flight => block
				pendingEvents.blockedByLatency = true;
				//getSimulator().getTraceWriter().event(TraceType.INTERNAL, this, "Block", 1);
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
		debug( this.getIdentifier() + " issuing comp: " +  event.getIssuingComponent().getIdentifier());

		getSimulator().getTraceWriter().startState(TraceType.INTERNAL, this, buildTraceEntry(event.getEventData()));

		/**
		 * special case for NIC which generates new events.
		 */
		if(SNetworkComponent.class.isInstance( event.getIssuingComponent() ))
		{
			final ISPassiveComponent source =  event.getIssuingComponent();
			final MessagePart part = event.getEventData();

			SNetworkComponent ssource = (SNetworkComponent) source;
			INetworkExit target = part.getMessageTarget();

			//System.out.println( this.getIdentifier() + " jobStarted issued by " + source.getIdentifier() );

			if(source != this){
				// special case, we created the new message
				// manage flow control here:
				ssource.continueProcessingOfFlow( part.getMessageTarget(), part , startTime, this );
			}

			ssource.messagePartReceivedAndStartedProcessing(this, part, startTime);
		}
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

		//System.out.println( this.getIdentifier() +  " jobCompleted " + " " + event.getEventData() );

		// now reactivate exit if we are not blocked.

		final MessagePart part = event.getEventData();
		final INetworkExit target = part.getMessageTarget();
		final ConcurrentEvents pendingEvents = eventsPerExit.get(target);

		if(     ! pendingEvents.isScheduled
				&& ! pendingEvents.blockedByLatency
				&& ! pendingEvents.blockedManually
				&& ! pendingEvents.pendingJobs.isEmpty())
		{
			pendingExits.add(target);
			pendingEvents.isScheduled = true;
		}

		// now reschedule

		messagePartTransmitted(part, endTime);

		if(part.getMessageTarget() == getModelComponent()){
			// we are the target
			// prevent endless loops in routing algorithm.
			messagePartDestroyed(part, endTime);
			continueProcessingOfFlow(part.getMessageTarget(), part, endTime, this);
			return;
		}

		// now issue a new event on the target.

		final ISNetworkComponent targetComponent = getTargetFlowComponent(part);

		if( targetComponent == null ){
			messagePartDestroyed(part, endTime);
			continueProcessingOfFlow(part.getMessageTarget(), part, endTime, this);
			return;
		}

		// transmit the packet to the target after it arrives (latency).
		final Event newEvent = new Event(this,
				targetComponent,
				endTime.add(getProcessingLatency()),
				event.getEventData());

		getSimulator().submitNewEvent( newEvent);
	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#blockFlow(de.hd.pvs.piosim.model.networkTopology.INetworkExit)
	 */
	public void blockFlowManually(INetworkExit target){
		//System.out.println(this.getIdentifier() + " block flow " + target);

		ConcurrentEvents pendingEvents = eventsPerExit.get(target);

		if (pendingEvents == null){
			pendingEvents = new ConcurrentEvents();
			eventsPerExit.put(target, pendingEvents);
		}

		assert(pendingEvents.blockedManually == false);
		pendingEvents.blockedManually = true;
	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#unblockFlow(de.hd.pvs.piosim.model.networkTopology.INetworkExit)
	 */
	public void unblockFlowManually(INetworkExit target){
		//System.out.println(this.getIdentifier() + " unblock flow " + target);

		ConcurrentEvents pendingEvents = eventsPerExit.get(target);
		assert(pendingEvents.blockedManually == true);

		pendingEvents.blockedManually = false;

		if(  pendingEvents.isScheduled
				|| pendingEvents.blockedByLatency
				|| pendingEvents.pendingJobs.size() == 0)
		{
			return;
		}

		pendingEvents.isScheduled = true;
		// schedule the exit:
		pendingExits.add(target);

		// now reactivate component if possible:
		reactivateBlockingComponentNow();
	}

	@Override
	public void simulationFinished() {
		super.simulationFinished();
		if(getNumberOfBlockedJobs() > 0){
			System.err.println("ERROR: pending events on component " + this.getIdentifier() + " BlockedJobs: " + getNumberOfBlockedJobs());
			printWaitingEvents();

			getSimulator().errorDuringProcessing();
		}
	}

	/**
	 * Print the queue of the pending events (for debugging)
	 */
	final public void printWaitingEvents(){
		System.out.println("waiting exits for " + this.getIdentifier());
		for (INetworkExit exit: eventsPerExit.keySet()) {
			final ConcurrentEvents c = eventsPerExit.get(exit);
			if( c.pendingJobs.isEmpty() && c.jobsInTransit == 0){
				continue;
			}

			System.out.println("\t exit:" + exit.getIdentifier() + " jobsInTransit: " + c.jobsInTransit + " sched: " + c.isScheduled + " blockManually:" + c.blockedManually + " blockedlatency: "+ c.blockedByLatency);

			for(Event event: c.pendingJobs){
				System.out.println("\t\t" + event + " inc: " + event.getEarliestStartTime());
			}
		}
	}

}

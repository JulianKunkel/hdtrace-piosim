
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

import de.hd.pvs.TraceFormat.relation.RelationToken;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.network.MessagePart;
import de.hd.pvs.piosim.simulator.output.STraceWriter;
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
		 * True if this exit is contained in eventsPerExit.
		 */
		boolean           isScheduled = false;

		@Override
		public String toString() {
			return " currentlyPendingLat:" + usedLatency + " blockedEvents: " + pendingJobs.size();
		}
	};


	/**
	 * True if the user decided to block it
	 */
	boolean           blockedManually = false;


	static private class NetworkPair{
		final INetworkEntry     entry;
		final INetworkExit 		exit;

		public NetworkPair(INetworkEntry     entry, INetworkExit 		exit) {
			this.entry = entry;
			this.exit = exit;
		}

		@Override
		public int hashCode() {
			return entry.hashCode() + exit.hashCode();
		}
		@Override
		public boolean equals(Object arg0) {
			NetworkPair n = (NetworkPair) arg0;
			return n.entry == this.entry && n.exit == this.exit;
		}
	};

	/**
	 * for each target endpoint the state of the events is observed.
	 */
	private HashMap<NetworkPair, ConcurrentEvents > eventsPerStartNExit = new HashMap<NetworkPair, ConcurrentEvents >();

	/**
	 * Contains all exits for which we have operations.
	 */
	private LinkedList<NetworkPair> pendingPairs = new LinkedList<NetworkPair>();

	/**
	 * The total number of jobs.
	 */
	private int numberOfPendingJobs = 0;

	private RelationToken jobToken;

	/**
	 * The maximum time a job can require in this component.
	 * It is needed to approximate the number of jobs which must be submitted to the target.
	 */
	private Epoch maxTimeToTransferAJobToTheNextComponent = null;

    /* (non-Javadoc)
     * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#getProcessingTime(de.hd.pvs.piosim.simulator.network.MessagePart)
     */
    abstract public Epoch getProcessingTime(MessagePart part);

    @Override
    final protected Epoch getProcessingTimeOfScheduledJob(MessagePart eventData) {
            return eventData.getLastProcessingTime();
    };

	/**
	 * Determine the maximum latency a job can take.
	 * @return
	 */
	private Epoch getMaxTimeToTransferAJobToTheNextComponent() {
		if(maxTimeToTransferAJobToTheNextComponent == null){
			maxTimeToTransferAJobToTheNextComponent = getMaximumProcessingLatency().add( getMaximumProcessingTime() );
		}

		return maxTimeToTransferAJobToTheNextComponent;
	}

	@Override
	final protected void addNewEvent(Event<MessagePart> job) {

		NetworkPair pair = new NetworkPair(job.getEventData().getMessageSource(), job.getEventData().getMessageTarget());

		ConcurrentEvents pendingEvents = eventsPerStartNExit.get(pair);
		if (pendingEvents == null){
			pendingEvents = new ConcurrentEvents();
			eventsPerStartNExit.put(pair, pendingEvents);
		}

		pendingEvents.pendingJobs.add(job);

		numberOfPendingJobs++;

		//System.out.println( this.getIdentifier() + " addNewEvent " + " " + job.getEventData() );

		// should we activate the particular exit?
		if(     ! pendingEvents.isScheduled
				&& !pendingEvents.blockedByLatency )
		{
			// now schedule the exit:
			pendingEvents.isScheduled = true;
			pendingPairs.add(pair);
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

	/**
	 * In case an event of a request was blocked the component only continues
	 * if it is notified to activate the Request or the target advises to continue transferring jobs.
	 * This can be done by this call.
	 * By calling this method, the "first" blocked job is enabled in this component for further
	 * processing.
	 */
	protected void continueProcessingOfFlow(
			NetworkPair pairs,
			MessagePart part,
			Epoch lastProcessingTime,
			Epoch startTime,
			ISNetworkComponent targetFlowComponent)
	{
		final ConcurrentEvents pendingEvents = eventsPerStartNExit.get(pairs);

		if (pendingEvents == null){
			return;
		}

//		debug("criterion: " + target);

		//System.out.println( this.getIdentifier() + " continueProcessingOfFlow " + " target " + target.getIdentifier() +  " " + part.getMessage());

		assert(pendingEvents.jobsInTransit > 0);

		pendingEvents.usedLatency = pendingEvents.usedLatency.subtract(lastProcessingTime);
		pendingEvents.jobsInTransit--;

		if ( ! pendingEvents.blockedByLatency ){
//			debugFollowUpLine(" Target is not blocked: " + target);
			return;
		}

		/* activate an object only if the pendingLatency is smaller than the ProcessingLatency */

		if( pendingEvents.jobsInTransit > 0 && pendingEvents.usedLatency.compareTo( getMaxTimeToTransferAJobToTheNextComponent()) >= 0 ){
//			debugFollowUpLine("Pending latency is still too high");
			return;
		}

		//getSimulator().getTraceWriter().event(TraceType.INTERNAL, this, "ContinueFlowSucc", 0);

		pendingEvents.blockedByLatency = false;


		if(pendingEvents.pendingJobs.isEmpty() && pendingEvents.jobsInTransit == 0){
			eventsPerStartNExit.remove(pairs);
			return;
		}

		if( pendingEvents.isScheduled ||  pendingEvents.pendingJobs.isEmpty() ){
			// if more operations are in transit, then the reactivation already happened in job_complete.
			return;
		}

		//System.out.println(" re-activating.");

		// add to the exit
		pendingPairs.add(pairs);
		pendingEvents.isScheduled = true;

		reactivateBlockingComponentNow();
	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#getNextPendingAndSchedulableEvent()
	 */
	@Override
	final public Event<MessagePart> getNextPendingAndSchedulableEvent() {
		// remove blocked events:
		if(blockedManually){
			return null;
		}


		//System.out.println("getNextPendingAndSchedulableEvent " + this.getIdentifier());
		while(! pendingPairs.isEmpty() ){
			final NetworkPair target = pendingPairs.poll();

			//System.out.println(" checking " + target.getIdentifier());

			final ConcurrentEvents pendingEvents = eventsPerStartNExit.get(target);

			assert(pendingEvents.pendingJobs.size() > 0);
			assert(pendingEvents.isScheduled == true);
			assert(pendingEvents.blockedByLatency == false);

			pendingEvents.isScheduled = false;

			// remove blocked events:
			//if(blockedManually){
			//	continue;
			//}

			// dispatch event:
			final Event<MessagePart> event = pendingEvents.pendingJobs.poll();

			numberOfPendingJobs--;

//			debug(  "pending events:" + pendingEvents + " " +  event.getEventData());

			/*
			 * Block if the pending processed jobs time is bigger than the latency of this component.
			 */
			//System.out.println( getIdentifier() + " getNextPendingAndSchedulableEvent");

			final MessagePart part = event.getEventData();
			final Epoch lastProcessingTime = part.getLastProcessingTime();

			Epoch jobRunTime = getProcessingTime(part);
			Epoch latency = getProcessingLatency(part);
			event.getEventData().updateCurrentState(this, jobRunTime, latency);


			// manage flow control here:
			pendingEvents.usedLatency = pendingEvents.usedLatency.add(jobRunTime);
			pendingEvents.jobsInTransit++;

			if( pendingEvents.usedLatency.compareTo(getMaxTimeToTransferAJobToTheNextComponent()) >= 0 ){
				// now we have enough components in flight => block
				pendingEvents.blockedByLatency = true;
			}

////			debugFollowUpLine("pending runtime: " + pendingEvents.usedLatency);


			/**
			 * special case for NIC which generates new events.
			 */
			if(SNetworkComponent.class.isInstance( event.getIssuingComponent() ))
			{
				final Epoch startTime = getSimulator().getVirtualTime();
				final ISPassiveComponent source =  event.getIssuingComponent();

				SNetworkComponent ssource = (SNetworkComponent) source;

				//System.out.println( this.getIdentifier() + " jobStarted issued by " + source.getIdentifier() );

				if(source != this){
					// special case, we created the new message
					// manage flow control here:
					ssource.continueProcessingOfFlow( target , part, lastProcessingTime, startTime, this );
				}

				ssource.messagePartReceivedAndStartedProcessing(this, part, startTime);
			}
			return event;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#jobStarted(de.hd.pvs.piosim.simulator.event.Event, de.hd.pvs.TraceFormat.util.Epoch)
	 */
	@Override
	final public void jobStarted(Event<MessagePart> event, Epoch startTime) {
//		debug( this.getIdentifier() + " issuing comp: " +  event.getIssuingComponent().getIdentifier());

		final STraceWriter tw = getSimulator().getTraceWriter();
		if(tw.isTracableComponent(TraceType.INTERNAL)){
			if(event.getRelationToken() != null){
				jobToken = tw.relRelateProcessLocalToken(TraceType.INTERNAL, this, event.getRelationToken());
			}else{
				jobToken = tw.relCreateTopLevelRelation(TraceType.INTERNAL, this);
			}
			tw.relStartState(TraceType.INTERNAL, jobToken, buildTraceEntry(event.getEventData()));
		}else{
			jobToken = event.getRelationToken();
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
//		debug( " event " + event);

		final STraceWriter tw = getSimulator().getTraceWriter();
		final String [] attr = new String[4];
		attr[0] = "size";
		attr[1] = "" + event.getEventData().getPayloadSize();
		attr[2] = "offset";
		attr[3] = "" + event.getEventData().getPosition();

		tw.relEndState(TraceType.INTERNAL, jobToken, "", attr);
		tw.relDestroy(TraceType.INTERNAL, jobToken);

		//System.out.println( this.getIdentifier() +  " jobCompleted " + " " + event.getEventData() );

		// now reactivate exit if we are not blocked.

		final MessagePart part = event.getEventData();
		final NetworkPair target = new NetworkPair(part.getMessageSource(), part.getMessageTarget());
		{
			final ConcurrentEvents pendingEvents = eventsPerStartNExit.get(target);

			if( pendingEvents != null && ! pendingEvents.isScheduled
					&& ! pendingEvents.blockedByLatency
					&& ! pendingEvents.pendingJobs.isEmpty())
			{
				pendingPairs.add(target);
				pendingEvents.isScheduled = true;
			}
		}

		// now reschedule

		messagePartTransmitted(part, endTime);

		if(part.getMessageTarget() == getModelComponent()){
			// we are the target
			// prevent endless loops in routing algorithm.
			messagePartDestroyed(part, endTime);
			continueProcessingOfFlow(target, part, part.getLastProcessingTime(), endTime, this);
			return;
		}

		// now issue a new event on the target.

		final ISNetworkComponent targetComponent = getTargetFlowComponent(part);

		if( targetComponent == null ){
			messagePartDestroyed(part, endTime);
			continueProcessingOfFlow(target, part, part.getLastProcessingTime(), endTime, this);

			return;
		}

		// transmit the packet to the target after it arrives (latency).
		final Event newEvent = new Event(this,targetComponent,	endTime.add(part.getLastLatency()),	event.getEventData(), jobToken);

		getSimulator().submitNewEvent( newEvent);
	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#blockFlow(de.hd.pvs.piosim.model.networkTopology.INetworkExit)
	 */
	public void blockFlowManually(){
		//System.out.println(this.getIdentifier() + " block flow " + target);

		assert(blockedManually == false);
		blockedManually = true;
	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.base.ISNetworkComponent#unblockFlow(de.hd.pvs.piosim.model.networkTopology.INetworkExit)
	 */
	public void unblockFlowManually(){
		//System.out.println(this.getIdentifier() + " unblock flow " + target);

		assert(blockedManually == true);

		blockedManually = false;

		if ( pendingPairs.size() > 0){
			// now reactivate component if possible:
			reactivateBlockingComponentNow();
		}
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
	}

}


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

/**
 *
 */
package de.hd.pvs.piosim.simulator.base;

import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.event.FlowEvent;
import de.hd.pvs.piosim.simulator.interfaces.ISNodeHostedComponent;
import de.hd.pvs.piosim.simulator.output.STraceWriter.TraceType;

/** 
 * A SFlowComponent does never split or delete a single Event, 
 * instead it only passes the event with the SAME data to another Component.
 * Internally, a SFlowComponent tries to submit a limited number of jobs to the next component.
 * However, enough to keep the source and the drain busy (i.e. optimal). 
 * This is similar to a data flow and looks more realistic than having an unlimited receive buffer.
 * It is assumed the component delivers ALL events ~= Jobs with the same "endpoint" over the same path.
 * 
 * TODO: fix this issue, otherwise it breaks. 
 *  
 * For instance this is useful for network devices. 
 * 
 * @author Julian M. Kunkel
 *
 */
abstract public class SFlowComponent
<Type extends BasicComponent, EventDataType extends FlowEvent> 
extends SSequentialBlockingComponent<Type, EventDataType>
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
	private HashMap<ISNodeHostedComponent, ConcurrentEvents > concurrentEvents = 
		new HashMap<ISNodeHostedComponent, ConcurrentEvents >();

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
	abstract protected SFlowComponent getTargetFlowComponent(EventDataType event);
	
	/**
	 * Gets called if a job got transferred to the right target.
	 * @param eventData
	 */
	protected void flowJobTransferred(EventDataType eventData, Epoch endTime){
		
	}
	
	/**
	 * Gets called if an event is destroyed i.e. has no target.
	 * @param eventData
	 */
	protected void eventDestroyed(EventDataType eventData, Epoch endTime){
		
	}

	/**
	 * The component can control the Flow either by sending at most a number of jobs with a runtime 
	 * of the latency + processing time OR by directly blocking and unblocking the Flow.
	 */
	protected boolean isDirectlyControlledByBlockUnblock(){
		return false;
	}

	/**
	 * The time a job takes to arrive on the component it gets submitted, i.e. transfer time between 
	 * cable and receiver. 
	 * @return
	 */
	protected Epoch getProcessingLatency(){
		return Epoch.ZERO;
	}

	/**
	 * The maximum time a job needs to get processed.
	 * @return
	 */
	protected Epoch getMaximumProcessingTime(){
		return Epoch.ZERO;
	}

	/**
	 * Return the processing time.
	 * Must be invariant for subsequent calls to this method.
	 * @param eventData
	 * @return
	 */
	abstract protected Epoch getProcessingTime(EventDataType eventData);

	final protected Epoch getProcessingTimeOfScheduledJobAndChangeInternalStates(EventDataType eventData) {
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
			ISNodeHostedComponent target, 
			EventDataType eventData,
			Epoch startTime, 
			SFlowComponent targetFlowComponent) 
	{
		getSimulator().getTraceWriter().event(TraceType.INTERNAL, this, "ContinueFlow", 1);
		ConcurrentEvents pendingEvents = concurrentEvents.get(target);

		debug("criterion: " + target);

		assert(isDirectlyControlledByBlockUnblock() == false);
		assert(pendingEvents != null);
		pendingEvents.usedLatency = pendingEvents.usedLatency.subtract(getProcessingTime(eventData));
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

		Event<EventDataType> firstEvent = pendingEvents.blockedJobs.poll();

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
	private boolean isBlockedByEndpoint(ISNodeHostedComponent target){
		ConcurrentEvents pendingEvents = concurrentEvents.get(target);

		if(pendingEvents == null) return false;

		if(isDirectlyControlledByBlockUnblock()) return pendingEvents.isBlocked();

		Epoch maxTime = getMaxTimeToTransferAJobToTheNextComponent();

		int ret = pendingEvents.usedLatency.compareTo( maxTime);

		debug(" return: " + ret + " " + pendingEvents + " MAXTIME " + maxTime + " pUSEDLAT: " +  pendingEvents.usedLatency);

		return ret > 0 || (ret == 0 && maxTime.compareTo(Epoch.ZERO) > 0) ;
	}

	@Override
	final protected Event<EventDataType> getNextPendingAndSchedulableEvent() {
		while(getNumberOfBlockedJobs() > 0){
			Event<EventDataType> event = super.getNextPendingAndSchedulableEvent();

			ISNodeHostedComponent target = event.getEventData().getFinalTarget();

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

	@Override
	final protected void jobStarted(Event<EventDataType> event, Epoch startTime) {
		debug( "issuing comp: " +  event.getIssuingComponent().getIdentifier());

		SPassiveComponent source =  event.getIssuingComponent();

		getSimulator().getTraceWriter().startState(TraceType.INTERNAL, this, event.getEventData().getClass().getSimpleName());
		
		/**
		 * special case for NIC which generates new events.
		 */
		if(SFlowComponent.class.isInstance( event.getIssuingComponent() ) && this != source  
				&& ! ((SFlowComponent) source).isDirectlyControlledByBlockUnblock() ){
			// manage flow control here:
			ISNodeHostedComponent target = event.getEventData().getFinalTarget();			
			((SFlowComponent) source).continueProcessingOfFlow(
					event.getEventData().getFinalTarget(), event.getEventData(), startTime, this );
		}		

		if(isDirectlyControlledByBlockUnblock()){
			// then submit exactly one other job to the same target as long as it is not blocked.

			ConcurrentEvents pendingEvents = concurrentEvents.get(event.getEventData().getFinalTarget());
			if(pendingEvents.blockedJobs.size() != 0){
				Event firstEvent = pendingEvents.blockedJobs.poll();
				addNewEvent(firstEvent);
			}
		}
	}

	@Override
	public void timerEvent(Epoch wakeUpTime) {
		// gets called if a component is idle and gets reactived, to reactivate the component
		// a new timerEvent is submitted.
		startNextPendingEventIfPossible(wakeUpTime);
	}

	@Override
	final protected void jobCompleted(Event<EventDataType> event, Epoch endTime) {		
		debug( " event " + event);
		
		getSimulator().getTraceWriter().endState(TraceType.INTERNAL, this, event.getEventData().getClass().getSimpleName());
		
		SFlowComponent targetComponent = getTargetFlowComponent(event.getEventData());
		
		if(targetComponent == null){
			eventDestroyed(event.getEventData(), endTime);
			return;
		}
		
		flowJobTransferred(event.getEventData(), endTime);
		
		Event newEvent = new Event(this, 
				targetComponent,
				endTime.add(getProcessingLatency()), 
				event.getEventData());
		
		getSimulator().submitNewEvent( newEvent);
	}

	/**
	 * Block the data flow to a specific target, the <code>unblockFlow</code> is used to restart it.
	 * @param target
	 */
	public void blockFlow(ISNodeHostedComponent target){
		ConcurrentEvents pendingEvents = concurrentEvents.get(target);
		assert(pendingEvents.blocked == false);
		assert(isDirectlyControlledByBlockUnblock() == true);
		pendingEvents.blocked = true;
	}

	/**
	 * Unblock a flow which was blocked with blockFlow to a specific target
	 * @param target
	 */
	public void unblockFlow(ISNodeHostedComponent target){		
		ConcurrentEvents pendingEvents = concurrentEvents.get(target);
		assert(pendingEvents.blocked == true);
		pendingEvents.blocked = false;

		if(getState() == State.READY){
			if(pendingEvents.blockedJobs.size() != 0){
				Event firstEvent = pendingEvents.blockedJobs.poll();
				addNewEvent(firstEvent);
			}
		}
	}
}

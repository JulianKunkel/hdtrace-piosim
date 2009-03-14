
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
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.event.EventData;
import de.hd.pvs.piosim.simulator.event.InternalEvent;

/**
 * This class allows to define a specific data structure to hold the pending events.
 * A blocking component schedules only one "job" after another.
 * A job is defined by a start and end event. 
 * If the component is busy it will queue up new events.
 * 
 * The derived component can decide the scheduling order of the pending events. 
 * 
 * @author Julian M. Kunkel
 *
 * @param <Type>
 * @param <EventDataType>
 */
abstract public class SSchedulableBlockingComponent
<Type extends BasicComponent, EventDataType extends EventData>  
extends SBasicComponent<Type> 
{
	/** Internal states, the busy component processes a job right now */
	static public enum State {
		BUSY, READY
	}

	/**
	 * the internal State of the component, is it busy or ready right now.
	 */
	private State state = State.READY;
	
	/**
	 * Which event gets serviced right now
	 */
	private Event<EventDataType> runningJob = null;
	
	/**
	 * The last serviced events start-time and expected end-time 
	 */
	private Epoch lastRunningEventEndTime = Epoch.ZERO;
	
	/**
	 * How long will it take to process this particular event
	 */
	abstract protected Epoch getProcessingTimeOfScheduledJobAndChangeInternalStates(EventDataType eventData);
		
	/**
	 * This function gets called if a job completed.
	 */
	abstract protected void jobCompleted(Event<EventDataType> event, Epoch endTime);
	
	/**
	 * This function gets called if a job is started to be serviced.
	 * It should be overridden if needed. 
	 */
	abstract protected void jobStarted(Event<EventDataType> event, Epoch startTime);
		
	/**
	 * Remove the next pending/queued up event. This method gets called when the component gets
	 * idle. Before a job gets serviced, this function should check if any event can be serviced 
	 * right now. If there is none then a timer event should be submitted to ensure that the 
	 * events get started.
	 * 
	 * @return
	 */
	abstract protected Event<EventDataType> getNextPendingAndSchedulableEvent(); 
	
	/**
	 * Add a new event to the queue
	 * @param job
	 */
	abstract protected void addNewEvent(Event<EventDataType> job);

	/**
	 * Print the queue of the pending events (for debugging)
	 */
	abstract public void printWaitingEvents();
	
	/**
	 * gets called when a timer event forces the component to wake up.
	 */
	public void timerEvent(Epoch wakeUpTime){
		
	}

	
	/**
	 * return the number of pending jobs
	 * @return
	 */
	abstract public int getNumberOfBlockedJobs();		
	
	/**
	 * Submit a new event to this component, the event will get queued up and will be scheduled 
	 * when needed.
	 * Required for a job to be scheduled:
	 * - Current simulated time is at least the incoming time of the job.
	 * - There is no job which has a earlier incoming time.
	 */
	@Override
	final public void processEvent(Event event, Epoch time) {
		addNewEvent(event);
				
		startNextPendingEventIfPossible(time);
	}
	
	/**
	 * Try to start a pending event.
	 */
	protected void startNextPendingEventIfPossible(Epoch curTime){		
		if(state == State.BUSY){
			return;
		}
				
		if(isFinished())
			return;
		
		Event<EventDataType> event =  getNextPendingAndSchedulableEvent();
		
		if(event == null){
			// no event found, maybe all the pending once are stalled.
			return;
		}

		Epoch processingTime = getProcessingTimeOfScheduledJobAndChangeInternalStates(event.getEventData());
		
		/* component is busy right now */
		state = State.BUSY;
		this.runningJob = event;
		
		this.lastRunningEventEndTime = curTime.add(processingTime);
		
		/* notify derived component that a job started */
		jobStarted( event, curTime );
		
		// wake up the component when the job finishes
		setNewWakeupTimer(lastRunningEventEndTime);
		
		debug(" earliestEventTime: " + event.getEarliestStartTime() +  " from " + curTime + " busy up to:" + lastRunningEventEndTime);
	}
	
	/**
	 * These events are used to signal completion of a job.
	 */
	@Override
	final public void processInternalEvent(InternalEvent event, Epoch time) {
		debug(state + " " +" end: " + lastRunningEventEndTime + " starting " + runningJob);		

		if(state == State.BUSY && time.equals(lastRunningEventEndTime)){
			// finish the pending event.
			
			this.state = State.READY;
			jobCompleted(this.runningJob, time);
			
			// try to start another event.
			startNextPendingEventIfPossible(time);
						
			return;
		}

		// internal trigger
		timerEvent(time);
	}
	
	/**
	 * Which state (Busy or Idle) does this component have right now.
	 * 
	 * @return The current state of this component.
	 */
	final public State getState() {
		return state;
	}	
	
	final boolean isFinished(){
		return getNumberOfBlockedJobs() == 0;
	}
}

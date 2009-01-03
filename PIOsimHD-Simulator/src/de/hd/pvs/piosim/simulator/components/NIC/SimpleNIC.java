
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
package de.hd.pvs.piosim.simulator.components.NIC;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.components.FakeBasicComponent;
import de.hd.pvs.piosim.model.components.Connection.Connection;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.Port.Port;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.base.SFlowComponent;
import de.hd.pvs.piosim.simulator.base.SNetworkComponent;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.event.Message;
import de.hd.pvs.piosim.simulator.event.MessagePart;
import de.hd.pvs.piosim.simulator.interfaces.ISNodeHostedComponent;
import de.hd.pvs.piosim.simulator.network.NetworkJobType;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.SingleNetworkJob;
import de.hd.pvs.piosim.simulator.network.jobs.INetworkMessage;
import de.hd.pvs.piosim.simulator.output.STraceWriter.TraceType;

/**
 * Simulates a Network Interface Card.
 * 
 * A NIC is split into NIC upload (TX - realized by subclasses) and NIC download (RX) 
 * (directly incorporated in the main class).
 * 
 * @author Julian M. Kunkel
 */
public class SimpleNIC extends GNIC<NIC>{
	/** matches via Tag suffice, this hashMap contains Sends which are performed before a client put a Receive on it */	
	private HashMap<MSGMatchingCriterion, LinkedList<SingleNetworkJob>> mapReceivedMsgsBeforePost = new 
	HashMap<MSGMatchingCriterion, LinkedList<SingleNetworkJob>>();
	
	/** Posted receives before the matching message got received */
	private HashMap<MSGMatchingCriterion, SingleNetworkJob> mapPostedJobsBeforeReceive =
		new HashMap<MSGMatchingCriterion, SingleNetworkJob>();
	
	/** Posted any source messages but with a communicator and tag */
	private HashMap<Communicator, HashMap<Integer, SingleNetworkJob >> mapPostedAnySourceWithTag = 
		new HashMap<Communicator, HashMap<Integer,SingleNetworkJob>>();
	
	/**
	 * The upload path of the NIC.
	 */
	private GNICUpload upload = new GNICUpload();
	
	/**
	 * This class uploads data from the NIC to the target.
	 * 
	 * @author Julian M. Kunkel
	 */
	public class GNICUpload extends SNetworkComponent<FakeBasicComponent>{
		
		/**
		 * gets populated automatically
		 */
		private SNetworkComponent connectedComponent = null;
		
		private SNetworkComponent getConnectedComponent(){
			if (connectedComponent != null){
				return connectedComponent;
			}
			
			// if it is a port, instead of fetching it fetch the Switch.
			BasicComponent component = getModelNIC().getConnectedComponent();
			if (Port.class.isInstance(component)) {
				component = ((Port) component).getParentComponent();
			}
			
			connectedComponent = (SNetworkComponent) getSimulator().getSimulatedComponent(component);

			return connectedComponent;
		}
		
		@Override
		protected SFlowComponent getTargetFlowComponent(MessagePart event) {			
			if(getAttachedNode().isNodeLocal(event.getNetworkJob().getTargetComponent().getIdentifier())){
				// upload directly to the local maschine.
				return null; 
			}else{
				return getConnectedComponent();
			}
		}
		
		@Override
		protected Epoch getMaximumProcessingTime() {
			double minBW = getAttachedNode().getModelComponent().getInternalDataTransferSpeed();
			double newBW =  getModelNIC().getConnection().getBandwidth();			
			
			if(newBW < minBW){
				minBW = newBW; 
			}
						
			return new Epoch(getSimulator().getModel().getGlobalSettings().getTransferGranularity() / minBW);
		}
		
		/* (non-Javadoc)
		 * @see de.hd.pvs.piosim.simulator.base.SBlockingComponent#getProcessingTime(de.hd.pvs.piosim.simulator.event.Event)
		 */
		@Override
		protected Epoch getProcessingTime(MessagePart eventData) {
			MessagePart msgPart = (MessagePart) eventData;


			if(getAttachedNode().isNodeLocal(msgPart.getNetworkJob().getTargetComponent().getIdentifier())){
				// local transfer is done with full speed.
				return new Epoch(msgPart.getSize() / (double) getAttachedNode().getModelComponent().getInternalDataTransferSpeed());
			}	

			/* depends on model component */
			Connection connection = getModelNIC().getConnection();
			/* the latency is NOT added here ! */ 
			return new Epoch(msgPart.getSize() / (double) connection.getBandwidth());		
		}

		@Override
		protected Epoch getProcessingLatency() {
			return getModelNIC().getConnection().getLatency();
		}
		
		@Override
		protected void eventDestroyed(MessagePart eventData, Epoch endTime) {
			// gets called only when data shall be uploaded directly to the local node.
			getSimulator().submitNewEvent(new Event<MessagePart>(this, getGNIC(), endTime, eventData) );
			
			flowJobTransferred(eventData, endTime);
		}
		
		@Override
		protected void flowJobTransferred(MessagePart eventData, Epoch endTime) {
			MessagePart newMsgPart = eventData.getMessage().createNextMessagePart(getSimulator().getModel().getGlobalSettings().getTransferGranularity());
			ISNodeHostedComponent hostedSrc = eventData.getNetworkJob().getSourceComponent();

			/* if this message is part of a Flow receive then use the callback to notify upload completion */
			if(eventData.getNetworkJob().isPartialSendCallbackActive()){
				/* always confirm reception, for flow receives */
				hostedSrc.sendMsgPartCB(getGNIC(), eventData, endTime);
			}				

			if(newMsgPart == null){
				//if( p.isLastPart() ){
				/* all data is sent, either Flow or Message completed => SingleNetworkJob completed */
				//	hostedSrc.sendCB(p.getMessage().getNetworkJob(), mEndTime);
				/* this should only happen if this is an incomplete send msg */
				//}
				NetworkJobs jobs = eventData.getNetworkJob().getParentNetworkJobs();

				if (jobs != null){	

					jobs.jobCompletedSend();

					if(jobs.isCompleted()){
						hostedSrc.jobsCompletedCB(jobs, endTime);
					}
				}
			}else{
				/* create a new event to upload */
				Event<MessagePart> partEvent = new Event<MessagePart>( this, this, endTime, newMsgPart);
				addNewEvent(partEvent);
			}
		}
		
	} /************************ END GNIC UPLOAD ***********************************/
	
	/**
	 * Data gets uploaded to the local node.
	 */
	protected SFlowComponent getTargetFlowComponent(MessagePart event) {
			return null;
	};
	
	private GNIC getGNIC(){
		return this;
	}
	
	/**
	 * Return the NIC.
	 * Needed to allow access for the upload component.
	 * @return
	 */
	private NIC getModelNIC(){
		return getModelComponent();
	}
	
	/**
	 * Append some new data to a message i.e. new data gets available for a message and can be
	 * transferred.
	 * 
	 * @param message
	 * @param count
	 */
	public void appendAvailableDataToIncompleteSend(Message message, long count){
		assert(count > 0);
		
		SingleNetworkJob job = message.getNetworkJob();
		
		if(message.isAllAvailableDataSplitIntoParts() && 
				(message.isAllMessageDataAvailable() || getSimulator().getModel().getGlobalSettings().getTransferGranularity() <= count)){
			
			/* i.e. transfer of this message got stalled right now */
			
			message.appendAvailableDataToSend(count);
			
			MessagePart newPart = message.createNextMessagePart(getSimulator().getModel().getGlobalSettings().getTransferGranularity());
			
			Epoch time = getSimulator().getVirtualTime();

			/* restart the data transfer of this message */
			
			Event<MessagePart> event = new Event<MessagePart>(this, upload, time ,newPart);

			getSimulator().submitNewEvent(event);			
		}else{ // not stalled right now.
			message.appendAvailableDataToSend(count);
		}
	}
	
	@Override
	public void setSimulatedModelComponent(NIC comp, Simulator sim)  throws Exception {
		super.setSimulatedModelComponent(comp, sim);
		/*
		 * Set the simulated model component to the same as the NICs
		 * this shall never happen !!!
		 */
		FakeBasicComponent c = new FakeBasicComponent(comp.getIdentifier().getName()+ " upload", sim.getModel());
		this.upload.setSimulatedModelComponent(	c , sim		);
	}
	
	@Override
	protected Epoch getProcessingTime(MessagePart eventData) {
		return Epoch.ZERO;
	}

	/**
	 * This method gets invoked if a new MessagePart is transferred from a network component to this NIC.
	 */
	@Override
	protected void eventDestroyed(MessagePart p, Epoch endTime) {
		debug("received " + p);
		
		p.getMessage().receivePart(p);
		
		//getSimulator().getTraceWriter().arrowEnd( TraceType.INTERNAL, event.getIssuingComponent(), 
		//		p.getNetworkJob().getTargetComponent().getSimulatorObject(), p);
		
		ISNodeHostedComponent hostedTgt = p.getNetworkJob().getTargetComponent();
		
		/* if this message is part of a Flow receive then use the callback to notify reception */
		if(p.getMessage().getNetworkJob().isPartialRecvCallbackActive()){
			/* always confirm reception, for flow receives */
			hostedTgt.recvMsgPartCB(this, p, endTime);
		}
		
		if (p.getMessage().isReceivedCompletely()){
			/* if this job was expected we have it in the queue */
			SingleNetworkJob rjob = p.getMessage().getNetworkJob();
			Message msg = p.getMessage();

			/* remove it first, because a new read msg with the same matching crit could be initiated */
			SingleNetworkJob expectedJob =  mapPostedJobsBeforeReceive.remove( new MSGMatchingCriterion(rjob));
			
		//	System.out.println("NIC receive: " + rjob + " " + rjob.getMatchingCriterion());
	//		for(SingleNetworkJob exJob: mapPostedJobsBeforeReceive.values()) {
//				System.out.println("Pending : " + exJob + " " + exJob.getMatchingCriterion());
			//}
			//System.out.println("expected Job: " +  expectedJob);
			
			// TODO match also ANY_TAG MSGS??			
			
			if(expectedJob == null){
				HashMap<Integer, SingleNetworkJob> tagMap = mapPostedAnySourceWithTag.get(rjob.getCommunicator());
				
				if(tagMap != null) {
					expectedJob = tagMap.remove(rjob.getTag());
				}
				
				if(expectedJob != null){
					// matching an any-source post
					completeReceive(expectedJob, rjob);
					return;
				}else{
					MSGMatchingCriterion crit = new MSGMatchingCriterion(rjob);
					LinkedList<SingleNetworkJob> list = mapReceivedMsgsBeforePost.get(crit);
					if( list == null){
						list = new LinkedList<SingleNetworkJob>();
						mapReceivedMsgsBeforePost.put(crit, list);
					}
					list.add(rjob);
					return;
				}
			}else{
				completeReceive(expectedJob, rjob);
				return;
			}
		}else{
			getSimulator().getTraceWriter().event(TraceType.INTERNAL, p.getNetworkJob().getTargetComponent().getSimulatorObject(), "MSG-Part", p.getSize());
		}
	}
	
	/**
	 * A message got received completely.
	 * 
	 * @param job The job which got finished
	 * @param response The response (for a receive).
	 */
	private void completeReceive(SingleNetworkJob job, SingleNetworkJob response){
		assert(response != null);
		Epoch time = getSimulator().getVirtualTime();
		
		if(job.getParentNetworkJobs() == null){
			// trigger single message callback
			
			job.getTargetComponent().receiveCB(job, response, time);
		}else{
			// check if complete job is finished.
			NetworkJobs jobs = job.getParentNetworkJobs();
			
			debug("" + job + " rsp " +  response);
			assert(response.getSourceComponent() != null);
			
			getSimulator().getTraceWriter().arrowEnd(TraceType.ALWAYS, 
					response.getSourceComponent().getSimulatorObject(), 
					job.getTargetComponent().getSimulatorObject(), 
					response.getJobData().getSize(), 
					response.getTag(), 
					response.getCommunicator().getIdentity());
			
			jobs.jobCompletedRecv(response);
			if(jobs.isCompleted()){
				jobs.getInitialRequestDescription().getInvokingComponent().jobsCompletedCB(jobs, time);
			}
		}
	}
	
	/**
	 * Start a new network job.
	 * 
	 * The method will delegate the job either to the upload queue or register the job on the 
	 * download queue
	 */
	public void submitNewNetworkJob(SingleNetworkJob job){
		/* submit any new Events with the current time */
		INetworkMessage data = job.getJobData();
		
		//System.out.println(" start:" +  
		//		job.getJobOperation() + " src: " + job.getSourceComponent() + " tgt: " +  job.getTargetComponent().getIdentifier());
		
		if (job.getJobOperation() == NetworkJobType.SEND){
			Epoch time = getSimulator().getVirtualTime();
			
			MessagePart msgP = null;
			/* start to split message into parts */
			Message msg;
			
			if(job.isPartialSendCallbackActive()){
				/* now the data to sent is not available right now */
				msg = new Message(data.getSize(), job, 0, this);
				
				/* initial callback */
				
				job.getSourceComponent().sendMsgPartCB(this, new MessagePart(msg, 0,0), time);
			}else{			
				msg = new Message(data.getSize(), job, this);
			}
			msgP = msg.createNextMessagePart(getSimulator().getModel().getGlobalSettings().getTransferGranularity());
			
			if(job.isPartialSendCallbackActive() && msgP == null){
				return;
			}
			assert (msgP != null); /* does not make any sense to send an empty message */
			
			assert(job.getTag() >= 0);
			assert(job.getTargetComponent() != null);
			assert(job.getSourceComponent() != this);
			assert(job.getSourceComponent() != null);
			
			
			getSimulator().getTraceWriter().arrowStart(TraceType.ALWAYS, 
					job.getSourceComponent().getSimulatorObject(), 
					job.getTargetComponent().getSimulatorObject(), 
					msgP);
						
			Event<MessagePart> event = new Event(this, upload, time,  msgP);
			getSimulator().submitNewEvent(event);
			
			return;			
		}else{ /*  Receive */
			MSGMatchingCriterion crit = new MSGMatchingCriterion(job);
			
			//if(data.getSize() <= 0){
			/* size is unknown until data is received completely */
			//}
			
			// check if required job is already received
			
			/* signal completion of early receives, this might lead to an immediately completion of the request */
			if (mapReceivedMsgsBeforePost.size() != 0){ /* there are pending early requests */
				
				if(job.getSourceComponent() == null) {
					// search for a msg with the corresponding tag. AnySource is specified
					for(LinkedList<SingleNetworkJob> pendingJobsWithCrit: mapReceivedMsgsBeforePost.values()) {
						Iterator<SingleNetworkJob> it = pendingJobsWithCrit.iterator();
						while (it.hasNext()) {
							SingleNetworkJob pJob = it.next();
							
							if(//job.getTag() < 0 || // any source any tag ...  
									pJob.getTag() == job.getTag()) {
								// match TODO maybe we match a different object...
								it.remove();
								completeReceive(job, pJob);
								
								if(pendingJobsWithCrit.size() == 0){
									mapReceivedMsgsBeforePost.remove(crit);
								}
								return;
							}
						}
					}
					
				}else { // not any source					
					LinkedList<SingleNetworkJob> list = mapReceivedMsgsBeforePost.get(crit);
					
					if( list != null ){					
						completeReceive(job, list.poll());
						
						if(list.size() == 0){
							mapReceivedMsgsBeforePost.remove(crit);
						}
						return;
					}
				}
			}
			
			//System.out.println("OUTER " + job.getMatchingCriterion());
			
			if( job.getSourceComponent() != null ){
				SingleNetworkJob existingJob = mapPostedJobsBeforeReceive.put( crit,	job);
				if (existingJob != null){
					// might happen due to non-blocking and wrong user programs
					new IllegalArgumentException("Invalid receive job! " + job.getTag() + " with comm " + 
							job.getCommunicator().getName() +" already used!\n" +
					"Probably your application is wrong and uses non-blocking calls with the same comm and tag");
				}
			}else{
				HashMap<Integer, SingleNetworkJob>  tagMap =  mapPostedAnySourceWithTag.get(job.getCommunicator());
				
				if(tagMap == null) {
					tagMap = new HashMap<Integer, SingleNetworkJob>();
					mapPostedAnySourceWithTag.put(job.getCommunicator(), tagMap);
				}
				
				SingleNetworkJob existingJob = tagMap.put( job.getTag() , job);
				if (existingJob != null){
					// might happen due to non-blocking and wrong user programs
					new IllegalArgumentException("Invalid any source job! " + job.getTag() + " with comm " + 
							job.getCommunicator().getName() +" already used!\n" +
					"Probably your application is wrong and uses non-blocking calls with the same comm and tag");
				}
			}
		} // end if receive
	}
	
	public GNICUpload getUpload() {
		return upload;
	}
	
	@Override
	public void simulationFinished() {		
		if(false){		
		StringBuffer buf = new StringBuffer();		
		
		for(SingleNetworkJob job: mapPostedJobsBeforeReceive.values()) {
			buf.append("  unmatched receive:" + job + "\n");
		}
		
		for(LinkedList<SingleNetworkJob> list: mapReceivedMsgsBeforePost.values()) {
			for (SingleNetworkJob job: list) {
				buf.append("  unmatched message received: " + job + "\n");
			}
		}
		
		if(buf.length() != 0){
			System.out.println("GNIC " + getIdentifier() + " has pending operations:");
			System.out.println(buf.toString());
		}
		}
	}
}

package de.hd.pvs.piosim.simulator.components.NIC;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.simulator.base.ISNetworkComponent;
import de.hd.pvs.piosim.simulator.components.NetworkNode.AGNetworkNode;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkEntry;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkExit;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.network.Message;
import de.hd.pvs.piosim.simulator.network.MessagePart;

public class GProcessNetworkInterface
extends  AGNetworkNode<NIC>
implements IProcessNetworkInterface, IGNetworkEntry, IGNetworkExit
{
	/**
	 * Store received but not announced jobs. Empty lists are not contained in the map to conserve space.
	 */
	private HashMap<MessageMatchingCriterion, LinkedList<Message>> earlyRecvsMap = new HashMap<MessageMatchingCriterion, LinkedList<Message>>();

	/**
	 * Early receives of messages which are later received via wildcards are handled by iterating through the earlyRecvsMap, thus the messages could be reordered when they are
	 * received, an advantage is the faster handling of normal matching.
	 */

	/**
	 * Store all announced recvs. Empty lists are not contained in the map to conserve space.
	 */
	private HashMap<MessageMatchingCriterion, LinkedList<InterProcessNetworkJob>> announcedRecvMap = new HashMap<MessageMatchingCriterion, LinkedList<InterProcessNetworkJob>>();


	/**
	 * All announced recvs which contain ANY_SOURCE or ANY_TAG wildcards.
	 */
	final private LinkedList<InterProcessNetworkJob>  announcedAnySourceOrTagRecv = new LinkedList<InterProcessNetworkJob>();



	/**
	 * Once a new receive is started which pairs to another announced receive, then it get put into this map:
	 */
	private HashMap<Message, InterProcessNetworkJob> startedRecvMap = new HashMap<Message, InterProcessNetworkJob>();

	private void callRecvCallbacksIfNececssary(MessagePart part, InterProcessNetworkJob remoteJob, InterProcessNetworkJob announcedJob, Epoch time){
		assert(remoteJob != null);
		assert(announcedJob != null);

		// check if the partial recv is active.
		announcedJob.getCallbacks().messagePartReceivedCB(part, remoteJob, announcedJob, time);

		// call target completed callback if msg is completed
		final Message msg = part.getMessage();
		callRecvCallback(msg, remoteJob, announcedJob, time);
	}

	private void callRecvCallback(Message msg, InterProcessNetworkJob remoteJob, InterProcessNetworkJob announcedJob, Epoch time){
		if(msg.isReceivedCompletely()){
			startedRecvMap.remove(msg);
			announcedJob.getCallbacks().recvCompletedCB(remoteJob, announcedJob, time);
		}
	}

	@Override
	public void messagePartDestroyed(MessagePart part, Epoch endTime) {
		//System.out.println(this.getIdentifier() + " messagePartDestroyed ");

		// called upon recv of the message.
		assert(part.getMessageTarget() == this.getModelComponent());
		final Message<InterProcessNetworkJob> msg = part.getMessage();
		final InterProcessNetworkJob remoteJob = msg.getContainedUserData();

		// data arrived
		msg.receivePart(part);

		//System.out.println(" for " + remoteJob.getMatchingCriterion().getTargetComponent() + " from " + remoteJob.getMatchingCriterion().getSourceComponent() + " size: " +  part.getSize() + " remaining "  +  msg.getRemainingBytesToReceive());

		// check if we have already matched the messages:
		if(startedRecvMap.containsKey(part.getMessage())){
			final InterProcessNetworkJob announcedJob = startedRecvMap.get(part.getMessage());

			if(announcedJob != null){
				// it is null if an unexpected recv.
				callRecvCallbacksIfNececssary(part, remoteJob, announcedJob, endTime);
			}
		}else{
			// first packet received => try to match messages
			final MessageMatchingCriterion crit = remoteJob.getMatchingCriterion();

			// the matching job if applicable
			InterProcessNetworkJob announcedJob = null;


			// check first, if this is an expected message with exact tag / source matching
			final LinkedList<InterProcessNetworkJob> announcedRecvsForCriterion = announcedRecvMap.get(crit);
			if( announcedRecvsForCriterion != null ){
				// we found a matching job
				announcedJob = announcedRecvsForCriterion.poll();

				// remove empty lists.
				if(announcedRecvsForCriterion.size() == 0){
					announcedRecvMap.remove(remoteJob.getMatchingCriterion());
				}
			}

			// is this message an expected message which matches by using wildcards?
			if (announcedJob == null && announcedAnySourceOrTagRecv.size() > 0){
				// there are pending wildcard operations

				final Iterator<InterProcessNetworkJob> iter = announcedAnySourceOrTagRecv.iterator();

				while(iter.hasNext()){
					InterProcessNetworkJob job = iter.next();
					if(job.getMatchingCriterion().matchesAnySourceOrTagWith(crit)){
						// it matches the pending wildcard operation!
						announcedJob = job;
						iter.remove();
						break;
					}
					iter.next();
				}
			}



			if ( announcedJob != null ){
				// we found a matching message => assign it.

				if(! msg.isReceivedCompletely()){
					// optimization, ignore completed msgs.
					startedRecvMap.put(msg, announcedJob);
				}

				callRecvCallbacksIfNececssary(part, remoteJob, announcedJob, endTime);
			}else{
				// uh oh, unexpected recv.
				if(! msg.isReceivedCompletely()){
					// optimization, ignore completed msgs.
					startedRecvMap.put(msg, null);
				}

				// register early recv.
				LinkedList<Message> earlyRcvs = earlyRecvsMap.get(crit);
				if(earlyRcvs == null){
					earlyRcvs = new LinkedList<Message>();
					earlyRecvsMap.put(crit, earlyRcvs);
				}

				earlyRcvs.add(msg);

				return;
			}
		}
	}

	@Override
	public void appendAvailableDataToIncompleteSend(Message msg, long count, Epoch startTime) {
		assert(count > 0);

		/**
		 * Several cases:
		 * - message is now completed => enforce creation of remaining bytes
		 * - message is not completed:
		 * -- is more than transfer granularity in the buffer => send transfer granularity bytes
		 * -- otherwise wait for new data
		 */
		msg.appendAvailableDataToSend(count);

		final long remainingBytes = msg.getRemainingBytesToSend();
		final long transferGranularity = getSimulator().getModel().getGlobalSettings().getTransferGranularity();

		MessagePart newPart = null;

		if(remainingBytes >= transferGranularity){
			/* restart the data transfer of this message */
			newPart = msg.createNextMessagePart(transferGranularity);
		}else if(msg.isAllMessageDataAvailable()){
			newPart = msg.createNextMessagePart(remainingBytes);
		}

		if(newPart != null){
			Event<MessagePart> event = new Event<MessagePart>(this, this, startTime, newPart, msg.getRelationToken());

			getSimulator().submitNewEvent(event);
			return;
		}

		// wait for more data
	}

	@Override
	public void initiateInterProcessReceive(InterProcessNetworkJob job, Epoch time) {
		assert(job.getJobOperation() == InterProcessNetworkJobType.RECEIVE);

		final MessageMatchingCriterion crit = job.getMatchingCriterion();


		assert(crit != null);

		//System.out.println(this.getIdentifier() + " RECV initiate" + job);

		// any source any tag match:
		if(crit.getSourceComponent() == MessageMatchingCriterion.ANY_SOURCE || crit.getTag() == MessageMatchingCriterion.ANY_TAG){

			// check if some data has been received already.
			// This is rather inefficient.
			for(MessageMatchingCriterion cur: earlyRecvsMap.keySet()){
				if( crit.matchesAnySourceOrTagWith(cur) ){
					// matched either any source or any tag
					final LinkedList<Message> earlyRcvs = earlyRecvsMap.get(cur);

					final Message<InterProcessNetworkJob> msg = earlyRcvs.poll();

					if(earlyRcvs.size() == 0){
						earlyRecvsMap.remove(cur);
					}

					startedRecvMap.put(msg, job);
					callRecvCallback(msg, msg.getContainedUserData(), job, time);

					// terminated.
					return;
				}
			}

			// early receive
			announcedAnySourceOrTagRecv.add(job);
			return;
		}

		// check if the message is already pending.
		final LinkedList<Message> earlyRcvs = earlyRecvsMap.get(crit);

		if(earlyRcvs == null){
			// no pending data.
			LinkedList<InterProcessNetworkJob> pendingJobs = announcedRecvMap.get(crit);
			if(pendingJobs == null){
				// add a new list
				pendingJobs = new LinkedList<InterProcessNetworkJob>();
				announcedRecvMap.put(crit, pendingJobs);
			}

			pendingJobs.add(job);
		}else{
			// we already have a pending message => match.
			final Message<InterProcessNetworkJob> msg = earlyRcvs.poll();

			if(earlyRcvs.size() == 0){
				earlyRecvsMap.remove(crit);
			}

			startedRecvMap.put(msg, job);
			callRecvCallback(msg, msg.getContainedUserData(), job, time);
		}
	}

	@Override
	public void initiateInterProcessSend(InterProcessNetworkJob job, Epoch startTime) {
		final Message<InterProcessNetworkJob> msg = new Message<InterProcessNetworkJob>(
				job.getSize(),
				job,
				this.getModelComponent(),
				job.getMatchingCriterion().getTargetComponent().getNetworkInterface(),
				job.getRelationToken()
		);
		initiateInterProcessSend(msg, startTime);
	}

	@Override
	public void initiateInterProcessSend(Message<? extends InterProcessNetworkJob> msg, Epoch startTime) {
		InterProcessNetworkJob job = msg.getContainedUserData();
		assert(job.getJobOperation() == InterProcessNetworkJobType.SEND);

		//System.out.println(this.getIdentifier() + " Send initiate" + job);

		assert(job.getSize() > 0);

		if(job.getJobData().getSize() == 0){
			throw new IllegalArgumentException("Data size is 0.");
		}

		submitNewMessage(msg, startTime);
	}

	@Override
	public void blockFurtherDataReceives() {
		super.blockFlowManually(this.getModelComponent());
	}


	@Override
	public void unblockFurtherDataReceives() {
		super.unblockFlowManually(this.getModelComponent());
	}

	// Entry code:

	@Override
	public void submitNewMessage(Message msg, Epoch startTime) {
		assert(msg.getSize() > 0);
		final MessagePart part = msg.createNextMessagePart(getSimulator().getModel().getGlobalSettings().getTransferGranularity());
		if(part == null){
			/* does not make any sense to send an empty message, it will be appended later */
			return;
		}

		final Event<MessagePart> event = new Event(this, this, startTime,  part, msg.getRelationToken());
		getSimulator().submitNewEvent(event);
	}

	@Override
	public void messagePartReceivedAndStartedProcessing(ISNetworkComponent target, MessagePart part, Epoch time) {
		//System.out.println(sourceComponent.getIdentifier() + " - " + target.getIdentifier() + " announced " + time + " " + this.getIdentifier() + " partial transmitted remaining: " + part.getMessage().getRemainingBytesToSend() );

		if(target == this){
			final Message<InterProcessNetworkJob> msg = part.getMessage();
			final InterProcessNetworkJob job = msg.getContainedUserData();

			// partial send?
			job.getCallbacks().messagePartSendCB(part, job, time);

			if(msg.getRemainingBytesToSend() == 0){
				// all data is send => call callback.
				job.getCallbacks().sendCompletedCB(job, time);
			}else{
				final MessagePart newMsgPart = part.getMessage().createNextMessagePart(getSimulator().getModel().getGlobalSettings().getTransferGranularity());
				if(newMsgPart != null){
					/* create a new event to upload data */
					Event<MessagePart> partEvent = new Event<MessagePart>( this, this, time, newMsgPart, part.getMessage().getRelationToken());
					getSimulator().submitNewEvent(partEvent);
				}
			}
		}
		// we are the target
	}

	@Override
	public ISNetworkComponent getTargetFlowComponent(MessagePart part) {
		if(part.getMessage().getMessageSource() == part.getMessage().getMessageTarget()){
			return this;
		}

		return routing.getTargetRouteForMessage(this.getModelComponent(), part);
	}


	@Override
	public Epoch getMaximumProcessingTime() {
		return new Epoch(((double) getSimulator().getModel().getGlobalSettings().getTransferGranularity()) / getModelComponent().getTotalBandwidth());	}

	@Override
	public Epoch getProcessingLatency() {
		return Epoch.ZERO;
	}

	@Override
	public Epoch getProcessingTime(MessagePart part) {
		return new Epoch(((double) part.getSize()) / getModelComponent().getTotalBandwidth());
	}

	@Override
	public void simulationFinished() {
		super.simulationFinished();
		if(earlyRecvsMap.size() != 0 || announcedRecvMap.size() != 0){
			getSimulator().errorDuringProcessing();

			System.err.println("Warning: Receives on NIC: " + this.getIdentifier());
			System.err.println("\tEearly recvs");
			for(LinkedList<Message> msgList: earlyRecvsMap.values()){
				for(Message msg: msgList){
					System.out.println("\t\t" + msg);
				}
			}
			System.err.println("\tAnnounced, but pending recvs");

			for(LinkedList<InterProcessNetworkJob> msgList:announcedRecvMap.values()){
				for(InterProcessNetworkJob job: msgList){
					System.out.println("\t\t" + job);
				}
			}
		}
	}

}

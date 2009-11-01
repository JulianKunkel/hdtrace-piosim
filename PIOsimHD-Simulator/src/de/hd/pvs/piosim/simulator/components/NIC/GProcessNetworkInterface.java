package de.hd.pvs.piosim.simulator.components.NIC;

import java.util.HashMap;
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
	 * Store all announced recvs. Empty lists are not contained in the map to conserve space.
	 */
	private HashMap<MessageMatchingCriterion, LinkedList<InterProcessNetworkJob>> announcedRecvMap = new HashMap<MessageMatchingCriterion, LinkedList<InterProcessNetworkJob>>();

	/**
	 * TODO: work with any number of any source recvs
	 */
	private InterProcessNetworkJob  anySourceRecv = null;

	/**
	 * Once a new recv is started which pairs to another announced recv, then it get put into this map:
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
			final LinkedList<InterProcessNetworkJob> announcedRecvsForCriterion = announcedRecvMap.get(crit);

			// check if any source is enabled
			if(announcedRecvsForCriterion == null && anySourceRecv == null){

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

			// we found a matching message => assign it.
			final InterProcessNetworkJob announcedJob;

			if(announcedRecvsForCriterion == null){
				// any recv.
				announcedJob = anySourceRecv;
				anySourceRecv = null;
			}else{
				announcedJob = announcedRecvsForCriterion.poll();

				// remove empty lists.
				if(announcedRecvsForCriterion.size() == 0){
					announcedRecvMap.remove(remoteJob.getMatchingCriterion());
				}
			}

			if(! msg.isReceivedCompletely()){
				// optimization, ignore completed msgs.
				startedRecvMap.put(msg, announcedJob);
			}

			callRecvCallbacksIfNececssary(part, remoteJob, announcedJob, endTime);
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
			Epoch time = getSimulator().getVirtualTime();
			Event<MessagePart> event = new Event<MessagePart>(this, this, time ,newPart);

			getSimulator().submitNewEvent(event);
			return;
		}

		// wait for more data
	}

	@Override
	public void initiateInterProcessReceive(InterProcessNetworkJob job, Epoch time) {
		assert(job.getJobOperation() == InterProcessNetworkJobType.RECEIVE);

		assert(job.getMatchingCriterion() != null);

		//System.out.println(this.getIdentifier() + " RECV initiate" + job);

		// any source any tag match ? TODO fixme.
		if(job.getMatchingCriterion().getSourceComponent() == null){
			assert(earlyRecvsMap.size() == 0);
			assert(anySourceRecv == null);
			anySourceRecv = job;

			return;
		}

		// check if the message is already pending.
		final MessageMatchingCriterion crit = job.getMatchingCriterion();
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
				job.getMatchingCriterion().getTargetComponent().getNetworkInterface()
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
		super.blockFlow(this.getModelComponent());
	}


	@Override
	public void unblockFurtherDataReceives() {
		super.unblockFlow(this.getModelComponent());
	}

	// Entry code:

	@Override
	public void submitNewMessage(Message msg, Epoch startTime) {
		assert(msg.getSize() > 0);
		final MessagePart msgP = msg.createNextMessagePart(getSimulator().getModel().getGlobalSettings().getTransferGranularity());
		if(msgP == null){
			/* does not make any sense to send an empty message, it will be appended later */
			return;
		}

		final Event<MessagePart> event = new Event(this, this, startTime,  msgP);
		getSimulator().submitNewEvent(event);
	}

	@Override
	public void messagePartTransmitted(MessagePart part, Epoch endTime) {
		//System.out.println(this.getIdentifier() + " messagePartTransmitted " + part.getSize());

		if(part.getMessageSource() == this.getModelComponent()){
			final MessagePart newMsgPart = part.getMessage().createNextMessagePart(getSimulator().getModel().getGlobalSettings().getTransferGranularity());

			final Message<InterProcessNetworkJob> msg = part.getMessage();

			final InterProcessNetworkJob job = msg.getContainedUserData();

			// partial send?
			job.getCallbacks().messagePartSendCB(part, job, endTime);

			if(newMsgPart != null){
				/* create a new event to upload data */
				Event<MessagePart> partEvent = new Event<MessagePart>( this, this, endTime, newMsgPart);
				addNewEvent(partEvent);
			}else{
				assert(msg.getRemainingBytesToSend() == 0);
				// all data is send => call callback.
				job.getCallbacks().sendCompletedCB(job, endTime);
			}
		}
		// we are the target
	}

	@Override
	public ISNetworkComponent getTargetFlowComponent(MessagePart part) {
		return routing.getTargetRouteForMessage(this.getModelComponent(), part);
	}

	@Override
	public Epoch getProcessingTime(MessagePart part) {
		return Epoch.ZERO;
	}

	@Override
	public Epoch getMaximumProcessingTime() {
		return Epoch.ZERO;
	}

	@Override
	public Epoch getProcessingLatency() {
		return Epoch.ZERO;
	}

	@Override
	public boolean isDirectlyControlledByBlockUnblock() {
		return true;
	}
}

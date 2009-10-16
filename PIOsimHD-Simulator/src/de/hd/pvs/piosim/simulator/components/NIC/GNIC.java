package de.hd.pvs.piosim.simulator.components.NIC;

import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.simulator.components.NetworkNode.GStoreForwardNode;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkEntry;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkExit;
import de.hd.pvs.piosim.simulator.components.Node.ISNodeHostedComponent;
import de.hd.pvs.piosim.simulator.network.Message;
import de.hd.pvs.piosim.simulator.network.MessagePart;

/**
 * The GNIC must not be any intermediate node, i.e. the GNIC shall never forward network data.
 *
 * @author julian
 *
 */
public class GNIC
extends  GStoreForwardNode<NIC>
implements IGNetworkExit, IGNetworkEntry, INetworkRessource
{

	boolean acceptNetworkData = true;

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

	/**
	 * Pending msgs to send.
	 */
	HashMap<INetworkExit, LinkedList<Message>> ongoingSendsMap = new HashMap<INetworkExit, LinkedList<Message>>();

	@Override
	protected void messageTransferCompletedEvent(MessagePart part) {
		//System.out.println(this.getIdentifier() + " messageTransferCompletedEvent ");

		if(part.getMessageSource() == this.getModelComponent()){
			sendMsgPartCB(part);
		}
	}

	@Override
	public void messagePartReceived(MessagePart part) {
		final Message<InterProcessNetworkJob> msg = part.getMessage();
		final InterProcessNetworkJob remoteJob = msg.getContainedUserData();

		// data arrived
		msg.receivePart(part);

		// check if we have already matched the messages:
		if(startedRecvMap.containsKey(part.getMessage())){
			final InterProcessNetworkJob announcedJob = startedRecvMap.get(part.getMessage());

			if(announcedJob != null){
				// it is null if an unexpected recv.
				callRecvCallbacksIfNececssary(part, remoteJob, announcedJob);
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

			if(anySourceRecv == null){
				announcedJob = announcedRecvsForCriterion.poll();

				// remove empty lists.
				if(announcedRecvsForCriterion.size() == 0){
					announcedRecvMap.remove(remoteJob.getMatchingCriterion());
				}

			}else{
				announcedJob = anySourceRecv;
				anySourceRecv = null;
			}



			if(! msg.isReceivedCompletely()){
				// optimization, ignore completed msgs.
				startedRecvMap.put(msg, announcedJob);
			}

			callRecvCallbacksIfNececssary(part, remoteJob, announcedJob);
		}
	}

	private void callRecvCallbacksIfNececssary(MessagePart part, InterProcessNetworkJob remoteJob, InterProcessNetworkJob announcedJob){
		assert(remoteJob != null);
		assert(announcedJob != null);
		final ISNodeHostedComponent host = announcedJob.getMatchingCriterion().getTargetComponent();

		// check if the partial recv is active.
		if(announcedJob.isPartialCallbackActive()){
			host.messagePartReceivedCB(part, remoteJob, announcedJob, getSimulator().getVirtualTime());
		}

		// call target completed callback if msg is completed
		final Message msg = part.getMessage();
		callRecvCallback(msg, remoteJob, announcedJob);
	}

	private void callRecvCallback(Message msg, InterProcessNetworkJob remoteJob, InterProcessNetworkJob announcedJob){
		final ISNodeHostedComponent host = announcedJob.getMatchingCriterion().getTargetComponent();

		if(msg.isReceivedCompletely()){
			startedRecvMap.remove(msg);
			host.recvCompletedCB(remoteJob, announcedJob, getSimulator().getVirtualTime());
		}
	}

	//// SENDING PARTS

	@Override
	public boolean announceSubmissionOf(MessagePart part) {
		final INetworkExit exit = part.getMessageTarget();
		// check if we are the final target.
		if(exit == this.getModelComponent()){
			//System.out.println(" announceSubmissionOf");

			return mayIReceiveAMessagePart(part);
		}

		// check if we want to send to the same exit from the local node
		final LinkedList<Message> msgs = ongoingSendsMap.get(exit);
		if(part.getMessageSource() != this.getModelComponent()
				&& msgs != null && msgs.size() > 0)
		{
			// if we get data, then we must be the target.
			assert(part.getMessageTarget() == this.getModelComponent());

			return true;
		}

		return super.announceSubmissionOf(part);
	}

	@Override
	public void submitMessagePart(MessagePart part) {
		final INetworkExit exit = part.getMessageTarget();
		// check if we are the final target.
		if(exit == this.getModelComponent()){
			// announce that the packet will not be routed again (for now).
			routing.messagePartRemoved(part);

			messagePartReceived(part);

			return;
		}else{
			// otherwise we must be the source of the message
			assert(part.getMessageSource() == this.getModelComponent());
		}

		super.submitMessagePart(part);
	}

	@Override
	public void initiateInterProcessReceive(InterProcessNetworkJob job) {
		assert(job.getJobOperation() == InterProcessNetworkJobType.RECEIVE);

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
			if(job.isPartialCallbackActive()){
				throw new IllegalArgumentException("Invalid state - Partial recv is active, however an early rcv happend.");
			}

			final Message<InterProcessNetworkJob> msg = earlyRcvs.poll();

			if(earlyRcvs.size() == 0){
				earlyRecvsMap.remove(crit);
			}

			startedRecvMap.put(msg, job);

			callRecvCallback(msg, msg.getContainedUserData(), job);
		}

	}

	@Override
	public Message<InterProcessNetworkJob> initiateInterProcessSend(InterProcessNetworkJob job) {
		assert(job.getJobOperation() == InterProcessNetworkJobType.SEND);

		if(job.getJobData().getSize() == 0){
			throw new IllegalArgumentException("Data size is 0.");
		}

		final Message<InterProcessNetworkJob> msg = new Message<InterProcessNetworkJob>(
				job.getSize(),
				job,
				this.getModelComponent(),
				job.getMatchingCriterion().getTargetComponent().getNetworkInterface().getModelComponent()
		);

		if(! job.isDataAvailable()){
			msg.setAvailableDataPosition(0);
		}

		submitNewMessage(msg);

		return msg;
	}

	// Network interface

	public void blockFurtherDataReceives(){
		assert(acceptNetworkData);

		acceptNetworkData = false;

		blockPushForExit(this.getModelComponent());
	}

	public void unblockFurtherDataReceives(){
		assert(! acceptNetworkData);

		acceptNetworkData = true;

		unblockExit(this.getModelComponent());
	}

	@Override
	public boolean mayIReceiveAMessagePart(MessagePart part) {
		return acceptNetworkData;
	}

	@Override
	public void simulationFinished() {
		super.simulationFinished();

		//System.out.println("Rcvd data: " + rcvdData);

		for(INetworkExit exit: ongoingSendsMap.keySet()){
			LinkedList<Message> pending = ongoingSendsMap.get(exit);
			for(Message msg : pending){
				System.out.println("Warning: pending msgs: " + msg);
			}
		}
	}

	private void tryToContinueSendFromMessage(Message msg){
		//System.out.println(" " + getIdentifier() + " tryToContinueSendFromMessage ");

		assert(msg.getMessageSource() == this.getModelComponent());

		final long networkGranularity = getSimulator().getModel().getGlobalSettings().getTransferGranularity();

		// we will start to re-send a data packet if message data is all available
		// or network granularity is reached
		if (msg.isAllMessageDataAvailable() || msg.getRemainingBytesToSend() >= networkGranularity){
			final MessagePart msgPart = msg.createNextMessagePart(networkGranularity);
			if(announceSubmissionOf(msgPart)){
				submitMessagePart(msgPart);
			}else{
				msg.undoCreationOfMessagePart(msgPart);
			}

			// check if there is more data to send right now, if so add the pending msg.

			if(msg.getRemainingBytesToSend() > 0){
				final INetworkExit exit = msg.getMessageTarget();
				final LinkedList<Message> pendingMsgs = ongoingSendsMap.get(exit);

				pendingMsgs.add(msg);
			}
		}
	}

	@Override
	public void sendMsgPartCB(MessagePart part) {
		// add round robin mechanism for blocked data.
		final Message<InterProcessNetworkJob> msg = part.getMessage();
		final INetworkExit exit = msg.getMessageTarget();

		final InterProcessNetworkJob job = msg.getContainedUserData();
		final ISNodeHostedComponent sender =job.getMatchingCriterion().getSourceComponent();

		// partial send?
		if(msg.getContainedUserData().isPartialCallbackActive()){
			sender.messagePartSendCB(part, job, getSimulator().getVirtualTime());
		}

		if(msg.getRemainingBytesToSend() == 0){
			// all data is send => call callback.
			sender.sendCompletedCB(job, getSimulator().getVirtualTime());
		}

		final LinkedList<Message> pendingMsgs = ongoingSendsMap.get(exit);

		if(pendingMsgs.size() > 0){
			tryToContinueSendFromMessage(pendingMsgs.poll());
		}
	}

	@Override
	public void appendAvailableDataToIncompleteSend(Message msg, long count) {
		msg.appendAvailableDataToSend(count);

		// check if we have to reactivate active send, if this is not true then
		// not all data is send right now => data transfer will be automatically reactivated by sendMsgPartCB
		if(msg.getRemainingBytesToSend() == count){
			tryToContinueSendFromMessage(msg);
		}
	}


	@Override
	public void submitNewMessage(Message msg) {
		// source node shall be this node
		assert(msg.getMessageSource() == this.getModelComponent());

		// messages of size 0 might get lost in the next code.
		assert(msg.getSize() > 0);

		final INetworkExit exit = msg.getMessageTarget();
		LinkedList<Message> pendingMsgs = ongoingSendsMap.get(exit);
		if(pendingMsgs == null){
			pendingMsgs = new LinkedList<Message>();
			ongoingSendsMap.put(exit, pendingMsgs);
		}

		if(pendingMsgs.size() > 0){
			pendingMsgs.add(msg);
		}else{
			tryToContinueSendFromMessage(msg);
		}
	}
}

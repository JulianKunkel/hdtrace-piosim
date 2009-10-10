package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network;

import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardForwardNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.simulator.components.NetworkNode.GStoreUndForwardNode;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkEntry;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkEntryCallbacks;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkExit;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkExitCallbacks;
import de.hd.pvs.piosim.simulator.network.Message;
import de.hd.pvs.piosim.simulator.network.MessagePart;

public class GStoreAndForwardExitNode extends GStoreUndForwardNode<StoreForwardForwardNode>
implements IGNetworkExit, IGNetworkEntry
{
	IGNetworkExitCallbacks networkExitI;

	HashMap<INetworkExit, LinkedList<Message>> pendingMsgsMap = new HashMap<INetworkExit, LinkedList<Message>>();

	private long rcvdData = 0;

	public long getRcvdData() {
		return rcvdData;
	}

	@Override
	public void setNetworkExitImplementor(IGNetworkExitCallbacks networkExitI) {
		// TODO Auto-generated method stub

	}


	@Override
	public void setNetworkEntryImplementor(
			IGNetworkEntryCallbacks networkEntryImplementor) {

	}

	@Override
	public void submitMessagePart(MessagePart part) {
		final INetworkExit exit = part.getMessageTarget();
		// check if we are the final target.
		if(exit == this.getModelComponent()){
			//System.out.println("+ " + this.getIdentifier() + " recveived data: " + part.getSize() + " at " + getSimulator().getVirtualTime());
			rcvdData += part.getSize();
			return;
		}

		super.submitMessagePart(part);
	}

	@Override
	public boolean announceSubmissionOf(MessagePart part) {
		final INetworkExit exit = part.getMessageTarget();
		// check if we are the final target.
		if(exit == this.getModelComponent()){
			//System.out.println(" announceSubmissionOf");

			return true;
		}
		return super.announceSubmissionOf(part);
	}

	@Override
	public void sendMsgPartCB(MessagePart part) {
		// add round robin mechanism for blocked data.
		final Message msg = part.getMessage();
		final INetworkExit exit = msg.getMessageTarget();
		final LinkedList<Message> pendingMsgs = pendingMsgsMap.get(exit);

		if(pendingMsgs.size() > 0){
			tryToContinueSendFromMessage(pendingMsgs.poll());
		}
	}

	@Override
	protected void messageTransferCompletedEvent(MessagePart part) {
		//System.out.println(this.getIdentifier() + " messageTransferCompletedEvent ");

		if(part.getMessageSource() == this.getModelComponent()){
			sendMsgPartCB(part);
		}
	}

	@Override
	public void simulationFinished() {
		super.simulationFinished();

		//System.out.println("Rcvd data: " + rcvdData);
	}

	private void tryToContinueSendFromMessage(Message msg){
		//System.out.println(" " + getIdentifier() + " tryToContinueSendFromMessage ");

		assert(msg.getMessageSource() == this.getModelComponent());
		if(msg.getRemainingBytesToSend() == 0){
			return;
		}

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
				final LinkedList<Message> pendingMsgs = pendingMsgsMap.get(exit);

				pendingMsgs.add(msg);
			}
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
		LinkedList<Message> pendingMsgs = pendingMsgsMap.get(exit);
		if(pendingMsgs == null){
			pendingMsgs = new LinkedList<Message>();
			pendingMsgsMap.put(exit, pendingMsgs);
		}

		if(pendingMsgs.size() > 0){
			pendingMsgs.add(msg);
		}else{
			tryToContinueSendFromMessage(msg);
		}
	}
}

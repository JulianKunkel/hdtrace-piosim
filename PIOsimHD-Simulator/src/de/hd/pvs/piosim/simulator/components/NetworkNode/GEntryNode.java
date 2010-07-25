package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;
import de.hd.pvs.piosim.simulator.base.ISNetworkComponent;
import de.hd.pvs.piosim.simulator.event.Event;
import de.hd.pvs.piosim.simulator.network.Message;
import de.hd.pvs.piosim.simulator.network.MessagePart;

public class GEntryNode extends AGNetworkNode<NetworkNode>
	implements IGNetworkEntry
{

	@Override
	public void appendAvailableDataToIncompleteSend(Message message, long count, Epoch startTime) {
		assert(count > 0);

		/**
		 * Several cases:
		 * - message is now completed => enforce creation of remaining bytes
		 * - message is not completed:
		 * -- is more than transfer granularity in the buffer => send transfer granularity bytes
		 * -- otherwise wait for new data
		 */
		message.appendAvailableDataToSend(count);

		final long remainingBytes = message.getRemainingBytesToSend();
		final long transferGranularity = getSimulator().getModel().getGlobalSettings().getTransferGranularity();

		MessagePart newPart = null;

		if(remainingBytes >= transferGranularity){
			/* restart the data transfer of this message */
			newPart = message.createNextMessagePart(transferGranularity);
		}else if(message.isAllMessageDataAvailable()){
			newPart = message.createNextMessagePart(remainingBytes);
		}

		if(newPart != null){
			Epoch time = getSimulator().getVirtualTime();
			Event<MessagePart> event = new Event<MessagePart>(this, this, time ,newPart, message.getRelationToken());

			getSimulator().submitNewEvent(event);
			return;
		}

		// wait for more data
	}

	@Override
	public void submitNewMessage(Message msg, Epoch startTime) {
		Epoch time = getSimulator().getVirtualTime();

		MessagePart msgP = null;
		msgP = msg.createNextMessagePart(getSimulator().getModel().getGlobalSettings().getTransferGranularity());
		assert (msgP != null); /* does not make any sense to send an empty message */

		final Event<MessagePart> event = new Event(this, this, time,  msgP, msg.getRelationToken());
		getSimulator().submitNewEvent(event);
	}

	@Override
	public void messagePartTransmitted(MessagePart part, Epoch endTime) {
		MessagePart newMsgPart = part.getMessage().createNextMessagePart(getSimulator().getModel().getGlobalSettings().getTransferGranularity());

		if(newMsgPart != null){
			/* create a new event to upload */
			Event<MessagePart> partEvent = new Event<MessagePart>( this, this, endTime, newMsgPart, part.getMessage().getRelationToken());
			addNewEvent(partEvent);
		}
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
}

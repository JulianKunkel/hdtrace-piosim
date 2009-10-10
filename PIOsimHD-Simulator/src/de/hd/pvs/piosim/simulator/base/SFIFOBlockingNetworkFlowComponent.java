package de.hd.pvs.piosim.simulator.base;

import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.networkTopology.INetworkFlowComponent;
import de.hd.pvs.piosim.simulator.network.MessagePart;


/**
 * determines if the outgoing edge is blocked implicitly by checking
 * pendindPackets size.
 * The network node receiving data by this edge can block data reception if it has no
 * buffer left.
 */
abstract public class SFIFOBlockingNetworkFlowComponent<ModelType extends INetworkFlowComponent>
	extends SBlockingNetworkFlowComponent<ModelType>
{

	/**
	 * The targets which are scheduled to receive data from this edge.
	 * The first exit is the one which might be scheduled right now.
	 */
	private LinkedList<INetworkExit> fifoExitQueue = new LinkedList<INetworkExit>();

	/**
	 * Multiple message parts can be pending for a given exit
	 */
	private HashMap<INetworkExit, LinkedList<MessagePart>> pendingMessageParts = new HashMap<INetworkExit, LinkedList<MessagePart>>();

	@Override
	final protected void addNetworkPart(MessagePart part) {
		final INetworkExit exit = part.getMessageTarget();

		LinkedList<MessagePart> pendingMessages = this.pendingMessageParts.get(exit);

		if(pendingMessages == null){
			pendingMessages = new LinkedList<MessagePart>();
			this.pendingMessageParts.put(exit, pendingMessages);
		}

		// startup transfer to exit if necessary
		if(pendingMessages.isEmpty()){
			fifoExitQueue.add(exit);
		}

		pendingMessages.add(part);
	}

	@Override
	protected MessagePart pollScheduledNetworkPart() {
		INetworkExit exit = fifoExitQueue.poll();
		LinkedList<MessagePart> queue = pendingMessageParts.get(exit);

		if(queue.size() > 1){
			// more events are pending => put it back into the list at the END to ensure interleaving of packets
			// if it is now blocked then we will determine it at the next wakeup event
			fifoExitQueue.add(exit);
		}

		return queue.poll();
	}

	@Override
	protected MessagePart peekScheduledNetworkPart() {
		return pendingMessageParts.get(fifoExitQueue.peek()).peek();
	}

	@Override
	final protected boolean isEmpty() {
		return fifoExitQueue.isEmpty();
	}

	@Override
	final protected void blockPushForExit(INetworkExit exit) {
		assert(exit == fifoExitQueue.peek());
		fifoExitQueue.poll();
	}


	@Override
	final protected void unblockBlockedExit(INetworkExit exit) {
		// shall be unblocked only if it is blocked right now
		assert(! fifoExitQueue.contains(exit));
		fifoExitQueue.add(exit);
	}
}

package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;
import de.hd.pvs.piosim.simulator.base.ISNetworkComponent;
import de.hd.pvs.piosim.simulator.network.MessagePart;

public class GExitNode	extends AGNetworkNode<NetworkNode>
	implements IGNetworkExit
{
	private long rcvdData = 0;

	public long getRcvdData() {
		return rcvdData;
	}

	@Override
	public void messagePartDestroyed(MessagePart part, Epoch endTime) {
		//System.out.println("+ " + this.getIdentifier() + " recveived data: " + part.getSize() + " at " + getSimulator().getVirtualTime());
		rcvdData += part.getPayloadSize();
	}

	@Override
	public Epoch getProcessingTime(MessagePart part) {
		return Epoch.ZERO;
	}

	@Override
	public ISNetworkComponent getTargetFlowComponent(MessagePart event) {
		return null;
	}

	@Override
	public Epoch getMaximumProcessingTime() {
		return Epoch.ZERO;
	}

	@Override
	public Epoch getProcessingLatency(MessagePart part) {
		return Epoch.ZERO;
	}
}

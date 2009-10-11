package de.hd.pvs.piosim.simulator.components.NetworkEdge;

import de.hd.pvs.piosim.model.components.NetworkEdge.CutThroughNetworkEdge;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.simulator.base.IGNetworkFlowComponent;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkNode;
import de.hd.pvs.piosim.simulator.network.MessagePart;

public class GCutThroughNetworkEdge<ModelType extends CutThroughNetworkEdge>
	extends SPassiveComponent<ModelType>
	implements IGNetworkEdge<ModelType>
{
	private IGNetworkNode targetNode;

	final public void setTargetNode(IGNetworkNode targetNode) {
		assert(targetNode != null);

		this.targetNode = targetNode;
	}

	final public IGNetworkNode getTargetNode() {
		assert(targetNode != null);

		return targetNode;
	}

	@Override
	public boolean announceSubmissionOf(MessagePart part) {
		return targetNode.announceSubmissionOf(part);
	}

	@Override
	public void submitMessagePart(MessagePart part) {
		targetNode.submitMessagePart(part);
	}

	@Override
	public void unblockExit(INetworkExit exit) {
		throw new IllegalArgumentException("shall never be called, target components must do flow control");
	}

	@Override
	public void rememberBlockedDataPushFrom(
			IGNetworkFlowComponent src,
			INetworkExit exit)
	{
		targetNode.rememberBlockedDataPushFrom(src, exit);
	}


}

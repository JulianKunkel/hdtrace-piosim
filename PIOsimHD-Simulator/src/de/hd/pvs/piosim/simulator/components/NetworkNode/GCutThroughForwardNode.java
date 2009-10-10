package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.piosim.model.components.NetworkNode.CutThroughForwardNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.components.NetworkEdge.IGNetworkEdge;
import de.hd.pvs.piosim.simulator.network.MessagePart;
import de.hd.pvs.piosim.simulator.network.routing.IPaketTopologyRouting;

public class GCutThroughForwardNode<ModelType extends CutThroughForwardNode>
	extends SPassiveComponent<ModelType>
	implements IGNetworkNode<ModelType>
{
	protected IPaketTopologyRouting routing;

	@Override
	public void setPaketRouting(IPaketTopologyRouting routing) {
		this.routing = routing;
	}

	public IPaketTopologyRouting getRouting() {
		return routing;
	}

	@Override
	public boolean announceSubmissionOf(MessagePart part) {
		//if(part.getMessageTarget() == this.getModelComponent()){
		//	final IGNetworkExit exit = ((IGNetworkExit) this);
		//	return exit.mayIReceiveAMessagePart(part);
		//}

		// check if target edge might accept the message part
		IGNetworkEdge edge = routing.getTargetRouteForMessage(getModelComponent(), part);
		assert(edge != null);
		return edge.announceSubmissionOf(part);
	}

	@Override
	public void submitMessagePart(MessagePart part) {
		if(part.getMessageTarget() == this.getModelComponent()){
			final IGNetworkExit exit = ((IGNetworkExit) this);
			exit.messagePartReceived(part);
			return;
		}

		IGNetworkEdge edge = routing.getTargetRouteForMessage(getModelComponent(), part);
		assert(edge != null);
		assert(edge.getTargetNode().announceSubmissionOf(part));
		edge.submitMessagePart(part);

		if(part.getMessageSource() == this.getModelComponent()){
			// invoke message callback
			((IGNetworkEntry) this).sendMsgPartCB(part);
		}
	}

	@Override
	public void unblockExit(INetworkExit exit) {
		throw new IllegalArgumentException("Cut through switches never shall be blocked or unblocked");
	}

}

package de.hd.pvs.piosim.simulator.components.NetworkNode;

import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.components.NetworkNode.CutThroughForwardNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.simulator.base.IGNetworkFlowComponent;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.components.NetworkEdge.IGNetworkEdge;
import de.hd.pvs.piosim.simulator.network.MessagePart;
import de.hd.pvs.piosim.simulator.network.routing.IPaketTopologyRouting;

public class GCutThroughForwardNodeExplizitBlocking<ModelType extends CutThroughForwardNode>
	extends SPassiveComponent<ModelType>
	implements IGNetworkNode<ModelType>
{
	protected IPaketTopologyRouting routing;

	/**
	 * Remember the blocked sources
	 */
	protected HashMap<INetworkExit, LinkedList<IGNetworkFlowComponent>> mapBlockedExits = new HashMap<INetworkExit, LinkedList<IGNetworkFlowComponent>>();

	private IGNetworkEdge lastEdgeForTransmission;

	@Override
	public void setPaketRouting(IPaketTopologyRouting routing) {
		this.routing = routing;
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

		lastEdgeForTransmission = edge;
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

		final INetworkExit exit = part.getMessageTarget();
		final LinkedList<IGNetworkFlowComponent> blockedOnes = mapBlockedExits.get(exit);
		if(blockedOnes != null && ! blockedOnes.isEmpty()){
			// wakeup another pending one.
			blockedOnes.pop().unblockExit(exit);
		}
	}

	@Override
	public void unblockExit(INetworkExit exit) {
		final LinkedList<IGNetworkFlowComponent> blockedOnes = mapBlockedExits.get(exit);
		blockedOnes.pop().unblockExit(exit);
	}

	@Override
	public void rememberBlockedDataPushFrom(
			IGNetworkFlowComponent src,
			INetworkExit exit)
	{
		LinkedList<IGNetworkFlowComponent> blockedOnes = mapBlockedExits.get(exit);
		if(blockedOnes == null){
			blockedOnes = new LinkedList<IGNetworkFlowComponent>();
			mapBlockedExits.put(exit, blockedOnes);
		}

		// next target is stored by announceSubmissionOf
		blockedOnes.add(src);

		if(blockedOnes.isEmpty()){
			lastEdgeForTransmission.rememberBlockedDataPushFrom(this, exit);
		}

	}

}

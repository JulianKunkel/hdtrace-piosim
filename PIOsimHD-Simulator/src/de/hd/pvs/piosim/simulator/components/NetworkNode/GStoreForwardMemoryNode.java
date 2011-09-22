package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardMemoryNode;
import de.hd.pvs.piosim.simulator.components.NetworkEdge.AGNetworkEdge;
import de.hd.pvs.piosim.simulator.network.MessagePart;

/**
 * Simulates network contention on a single host, when two processes on one host transfers data between each other bandwidth is halfed,
 * as both, read and write costs some memory bandwidth.
 *
 * @author julian
 */
public class GStoreForwardMemoryNode extends GStoreForwardNode<StoreForwardMemoryNode>
{
	@Override
	public Epoch getMaximumProcessingTime() {
		long local = getModelComponent().getLocalBandwidth();
		long total = getModelComponent().getTotalBandwidth();
		long max = local > total ? total: local;

		return new Epoch(((double) getSimulator().getModel().getGlobalSettings().getTransferGranularity()) / max);
	}

	@Override
	public Epoch getProcessingTime(MessagePart part) {
		// use local bandwidth, if the source and targets are directly connected to this node.
		AGNetworkNode node = (AGNetworkNode) getSimulator().getSimulatedComponent( part.getMessageSource() );

		// TODO: this code is not nice, a cleanup would be good

		if (((AGNetworkEdge) node.getTargetFlowComponent(part)).getTargetNode() == this &&
				((AGNetworkEdge) getTargetFlowComponent(part)).getTargetNode() == getSimulator().getSimulatedComponent(part.getMessageTarget()) ){

			return new Epoch(((double) part.getSize()) / getModelComponent().getLocalBandwidth());
		}else{
			return new Epoch(((double) part.getSize()) / getModelComponent().getTotalBandwidth());
		}
	}
}

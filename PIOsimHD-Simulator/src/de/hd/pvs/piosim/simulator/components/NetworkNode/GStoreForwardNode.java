package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardNode;
import de.hd.pvs.piosim.simulator.base.ISNetworkComponent;
import de.hd.pvs.piosim.simulator.network.MessagePart;

/**
 * Simulates a unidirectional bus-system i.e. a single upstream channel which is shared among all outgoing routes.
 *
 * @author julian
 */
public class GStoreForwardNode<ModelType extends StoreForwardNode>
	extends AGNetworkNode<ModelType>
{
	@Override
	public ISNetworkComponent getTargetFlowComponent(MessagePart part) {
		return routing.getTargetRouteForMessage(this.getModelComponent(), part);
	}

	@Override
	public Epoch getMaximumProcessingTime() {
		return new Epoch(((double) getSimulator().getModel().getGlobalSettings().getTransferGranularity()) / getModelComponent().getTotalBandwidth());
	}

	@Override
	public Epoch getProcessingLatency() {
		return Epoch.ZERO;
	}

	@Override
	public Epoch getProcessingTime(MessagePart part) {
		return new Epoch(((double) part.getSize()) / getModelComponent().getTotalBandwidth());
	}
}

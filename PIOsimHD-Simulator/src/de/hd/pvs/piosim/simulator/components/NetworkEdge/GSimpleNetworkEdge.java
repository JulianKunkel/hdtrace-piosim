package de.hd.pvs.piosim.simulator.components.NetworkEdge;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NetworkEdge.SimpleNetworkEdge;
import de.hd.pvs.piosim.simulator.base.ISNetworkComponent;
import de.hd.pvs.piosim.simulator.network.MessagePart;

public class GSimpleNetworkEdge extends AGNetworkEdge<SimpleNetworkEdge> {
	@Override
	public Epoch getProcessingTime(MessagePart part) {
		return computeTransportTime(part.getSize());
	}

	@Override
	public ISNetworkComponent getTargetFlowComponent(MessagePart part) {
		return getTargetNode();
	}

	/**
	 * How long does it take to transport a message of a given size over the edge.
	 * @param size
	 * @return
	 */
	private Epoch computeTransportTime(long size){
		return new Epoch(((double) size) / getModelComponent().getBandwidth());
	}

	@Override
	public Epoch getMaximumProcessingTime() {
		return computeTransportTime(getSimulator().getModel().getGlobalSettings().getTransferGranularity());
	}

	@Override
	public Epoch getProcessingLatency() {
		return getModelComponent().getLatency();
	}
}

package de.hd.pvs.piosim.simulator.components.NetworkEdge;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NetworkEdge.SimpleNetworkEdge;
import de.hd.pvs.piosim.simulator.base.IGNetworkFlowComponent;
import de.hd.pvs.piosim.simulator.network.MessagePart;

public class GSimpleNetworkEdge extends AGNetworkEdge<SimpleNetworkEdge> {

	/**
	 * Method is called upon initialization of the component
	 *
	 * @return the maximum time a message part needs to get transported to the end of this edge.
	 */
	@Override
	protected Epoch getMaximumTransportDurationForAMessagePart(){
		// latency + max msg part size / bandwidth
		return computeTransportTime(getSimulator().getModel().getGlobalSettings().getTransferGranularity());
	}

	@Override
	protected Epoch computeTransportTime(MessagePart part){
		return computeTransportTime(part.getSize());
	}

	@Override
	protected IGNetworkFlowComponent getTargetComponent(MessagePart part) {

		return getTargetNode();
	}

	/**
	 * How long does it take to transport a message of a given size over the edge.
	 * @param size
	 * @return
	 */
	private Epoch computeTransportTime(long size){
		return getModelComponent().getLatency().add(((double) size) / getModelComponent().getBandwidth());
	}

}

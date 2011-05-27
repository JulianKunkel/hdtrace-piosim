package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardNode;
import de.hd.pvs.piosim.simulator.network.MessagePart;

/**
 * Simulates network contention on a single host, when two processes on one host transfers data between each other bandwidth is halfed,
 * as both, read and write costs some memory bandwidth.
 *
 * @author julian
 */
public class GStoreForwardLocalNode<ModelType extends StoreForwardNode> extends GStoreForwardNode<ModelType>
{
	@Override
	public Epoch getMaximumProcessingTime() {
		return new Epoch(((double) getSimulator().getModel().getGlobalSettings().getTransferGranularity()) / getModelComponent().getTotalBandwidth() * 2);
	}

	@Override
	public Epoch getProcessingTime(MessagePart part) {
		// half the bandwidth when the source and target node are within the same node
		if( ((NIC) part.getMessageSource()).getParentComponent().getParentComponent()  != ((NIC) part.getMessageTarget()).getParentComponent().getParentComponent()){
			return new Epoch(((double) part.getSize()) / getModelComponent().getTotalBandwidth());
		}else{
			return new Epoch(((double) part.getSize()) / getModelComponent().getTotalBandwidth() * 2);
		}
	}
}

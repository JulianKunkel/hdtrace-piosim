package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.piosim.simulator.event.Message;
import de.hd.pvs.piosim.simulator.network.SingleNetworkJob;

public class GNetworkNodeEntry implements INetworkEntryInterface, IGNetworkNode{
	@Override
	public void appendAvailableDataToIncompleteSend(Message message, long count) {

	}

	@Override
	public void submitNewNetworkJob(SingleNetworkJob job) {

	}
}

package de.hd.pvs.piosim.simulator.components.NIC;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.network.MessagePart;

/**
 * Provides stubs for the NIC callbacks.
 * @author julian
 *
 */
public class InterProcessNetworkJobCallbackAdaptor implements IInterProcessNetworkJobCallback {

	@Override
	public void messagePartMatchesAnnounced(InterProcessNetworkJob remoteJob,
			InterProcessNetworkJob announcedJob, Epoch endTime) {
	}

	@Override
	public void messagePartReceivedCB(MessagePart part, InterProcessNetworkJob remoteJob,
			InterProcessNetworkJob announcedJob, Epoch endTime) {
	}

	@Override
	public void messagePartSendCB(MessagePart part, InterProcessNetworkJob myJob, Epoch endTime) {
	}

	@Override
	public void recvCompletedCB(InterProcessNetworkJob remoteJob, InterProcessNetworkJob announcedJob, Epoch endTime) {
	}

	@Override
	public void sendCompletedCB(InterProcessNetworkJob myJob, Epoch endTime) {
	}

}

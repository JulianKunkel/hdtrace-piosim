package de.hd.pvs.piosim.simulator.components.NIC;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.network.MessagePart;

public interface IInterProcessNetworkJobCallback {
	/**
	 * This callback is executed once the NetworkJobs completed.
	 * @param jobs
	 * @param endTime
	 */
	public void recvCompletedCB(InterProcessNetworkJob remoteJob, InterProcessNetworkJob announcedJob, Epoch endTime);

	public void sendCompletedCB(InterProcessNetworkJob myJob, Epoch endTime);

	public void messagePartSendCB(MessagePart part, InterProcessNetworkJob myJob, Epoch endTime);

	public void messagePartReceivedCB(MessagePart part, InterProcessNetworkJob remoteJob, InterProcessNetworkJob announcedJob, Epoch endTime);

	/**
	 * This callback is invoked, when the first packet is received which matches an expected message.
	 * @param remoteJob
	 * @param announcedJob
	 * @param endTime
	 */
	public void messagePartMatchesAnnounced(InterProcessNetworkJob remoteJob, InterProcessNetworkJob announcedJob, Epoch endTime);
}

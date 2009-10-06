package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.piosim.simulator.event.Message;
import de.hd.pvs.piosim.simulator.network.SingleNetworkJob;

public interface INetworkEntryInterface {

	/**
	 * Append some new data to a message i.e. new data gets available for a message and can be
	 * transferred.
	 *
	 * @param message
	 * @param count
	 */
	public void appendAvailableDataToIncompleteSend(Message message, long count);

	/**
	 * Start a new network job.
	 *
	 * The method will delegate the job either to the upload queue or register the job on the
	 * download queue
	 */
	public void submitNewNetworkJob(SingleNetworkJob job);

	public void setNetworkEntryImplementor(INetworkEntryInterfaceCallbacks networkEntryImplementor);
}

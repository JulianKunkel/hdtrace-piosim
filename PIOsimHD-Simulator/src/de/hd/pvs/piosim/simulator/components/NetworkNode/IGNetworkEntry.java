package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.piosim.simulator.network.Message;

public interface IGNetworkEntry {

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
	public void submitNewMessage(Message msg);

	public void setNetworkEntryImplementor(IGNetworkEntryCallbacks networkEntryImplementor);
}

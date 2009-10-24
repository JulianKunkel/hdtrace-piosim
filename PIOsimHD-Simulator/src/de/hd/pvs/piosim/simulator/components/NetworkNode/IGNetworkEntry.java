package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.network.Message;
import de.hd.pvs.piosim.simulator.network.MessagePart;

public interface IGNetworkEntry {

	/**
	 * Append some new data to a message i.e. new data gets available for a message and can be
	 * transferred.
	 *
	 * @param message
	 * @param count
	 */
	public void appendAvailableDataToIncompleteSend(Message message, long count, Epoch startTime);

	/**
	 * Start a new network job.
	 *
	 * The method will delegate the job either to the upload queue or register the job on the
	 * download queue
	 */
	public void submitNewMessage(Message msg, Epoch startTime);

	/**
	 * This function is called if a single Message part is successfully send.
	 * Then the next message part can be created.
	 */
	public void messagePartTransmitted(MessagePart part, Epoch endTime);
}

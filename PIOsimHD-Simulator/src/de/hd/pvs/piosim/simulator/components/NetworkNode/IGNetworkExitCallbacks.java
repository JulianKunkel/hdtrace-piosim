package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.components.NetworkEdge.GBusExit;
import de.hd.pvs.piosim.simulator.event.MessagePart;
import de.hd.pvs.piosim.simulator.network.SingleNetworkJob;


/**
 * Interface which must be implemented by any component which wants to receive data from
 * the bus.
 *
 * @author julian
 */
public interface IGNetworkExitCallbacks {

	/**
	 * This function is called if a single Network job completed
	 *
	 * @param job  The job which finished
	 * @param response The response (if any) for Receive Jobs.
	 * @param endTime The time when the job finished
	 */
	public void receiveCB(SingleNetworkJob job,  SingleNetworkJob response,  Epoch endTime);

	/**
	 * This function gets called if a single Message part is received from the Network Cable and
	 * isShouldPartialRecv() of the senders' Message is active.
	 *
	 * @param part
	 * @param endTime
	 */
	public void recvMsgPartCB(MessagePart part, Epoch endTime);

}

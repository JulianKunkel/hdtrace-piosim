package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.network.Message;
import de.hd.pvs.piosim.simulator.network.MessagePart;


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
	 * @param endTime The time when the job finished
	 */
	public void receiveCB(Message msg,  Epoch endTime);

	/**
	 * This function gets called if a single Message part is received from the Network Cable
	 *
	 * @param part
	 * @param endTime
	 */
	public void recvMsgPartCB(MessagePart part, Epoch endTime);

}

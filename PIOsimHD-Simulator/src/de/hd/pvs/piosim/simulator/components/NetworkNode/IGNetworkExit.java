package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.network.MessagePart;


public interface IGNetworkExit {
	/**
	 * This function gets called if a single Message part is received from the Network Cable
	 *
	 * @param part
	 * @param endTime
	 */
	public void messagePartDestroyed(MessagePart part, Epoch endTime);
}

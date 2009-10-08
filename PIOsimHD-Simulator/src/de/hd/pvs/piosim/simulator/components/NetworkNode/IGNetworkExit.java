package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.piosim.simulator.network.MessagePart;

public interface IGNetworkExit {
	public void setNetworkExitImplementor(IGNetworkExitCallbacks networkExitI);

	/**
	 * This function gets called if a single Message part is received from the Network Cable
	 *
	 * @param part
	 * @param endTime
	 */
	public void messagePartReceived(MessagePart part);

	public boolean mayIReceiveAMessagePart(MessagePart part);
}

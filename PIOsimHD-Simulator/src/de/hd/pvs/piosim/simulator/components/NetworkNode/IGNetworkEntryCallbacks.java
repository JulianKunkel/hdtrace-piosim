package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.network.MessagePart;

public interface IGNetworkEntryCallbacks {

	/**
	 * This function is called if a single Message part is given to the Network cable
	 */
	public void sendMsgPartCB(IGNetworkEntry entry, MessagePart part, Epoch endTime);
}

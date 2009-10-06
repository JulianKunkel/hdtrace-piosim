package de.hd.pvs.piosim.simulator.components.NetworkNode;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.event.MessagePart;

public interface INetworkEntryInterfaceCallbacks {

	/**
	 * This function is called if a single Message part is given to the Network Cable and
	 * isPartialSend() of the Job is active.
	 */
	public void sendMsgPartCB(INetworkEntryInterface entry, MessagePart part, Epoch endTime);
}

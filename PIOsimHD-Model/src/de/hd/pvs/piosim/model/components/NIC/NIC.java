package de.hd.pvs.piosim.model.components.NIC;

import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;

public class NIC
	extends StoreForwardNode
	implements INetworkEntry, INetworkExit
{
	public String getObjectType() {
		return NIC.class.getSimpleName();
	}
}

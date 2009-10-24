package de.hd.pvs.piosim.model.components.NIC;

import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;

/**
 * The NIC must not be any intermediate node, i.e. the GNIC shall never forward network data.
 *
 * Typically:
 *  Wiring is as follows: WORLD -> GNIC -> EXITS
 *  Outgoing wiring is as follows: Entry -> GNIC -> WORLD
 *  For internal transfer, the routing is: Entry -> GNIC -> Exit
 *  Therefore, the NIC (memory) bandwidth is used.
 *
 * @author julian
 */
public class NIC
	extends StoreForwardNode
	implements INetworkEntry, INetworkExit
{
	public String getObjectType() {
		return NIC.class.getSimpleName();
	}
}

package de.hd.pvs.piosim.model.components.NIC;

import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;

/**
 *  An analytical model to compute time for completion.
 *  The arrival of a message is determined without taking any congestion into account.
 * @author julian
 */
public class NICAnalytical
	extends NIC
	implements INetworkEntry, INetworkExit
{
}

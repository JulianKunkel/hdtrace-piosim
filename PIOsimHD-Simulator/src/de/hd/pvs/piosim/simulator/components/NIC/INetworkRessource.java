package de.hd.pvs.piosim.simulator.components.NIC;

import de.hd.pvs.piosim.model.components.NIC.NIC;



/**
 * Allows node hosted components to use node resources like CPU and network.
 *
 * @author julian
 *
 */
public interface INetworkRessource{
	/**
	 * Transfer data to another process or
	 * announce receive of data from another node.
	 * The NodeHostedComponent callback is called once all data is send.
	 */
	public void addInterProcessTransfer(InterProcessNetworkJob job);

	public NIC getModelComponent();
}

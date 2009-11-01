package de.hd.pvs.piosim.simulator.components.NIC;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.simulator.network.Message;

/**
 * Allows node hosted components to use node resources like CPU and network.
 *
 * @author julian
 *
 */
public interface IProcessNetworkInterface
{
	public NIC getModelComponent();

	/**
	 * Transfer data to another process or
	 * announce receive of data from another node.
	 * The NodeHostedComponent callback is called once all data is send.
	 */
	public void initiateInterProcessReceive(InterProcessNetworkJob job, Epoch time);

	public void initiateInterProcessSend(Message<? extends InterProcessNetworkJob> msg, Epoch startTime);

	public void initiateInterProcessSend(InterProcessNetworkJob job, Epoch startTime);

	public void appendAvailableDataToIncompleteSend(Message msg, long count, Epoch startTime);

	public void blockFurtherDataReceives();

	public void unblockFurtherDataReceives();
}

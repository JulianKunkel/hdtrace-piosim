package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.hardware;

import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;

/**
 * Use NICs as end points.
 * @author julian
 */
public class HardwareNICs extends BasicHardwareSetup{

	@Override
	public INetworkExit createNetworkExit() {
		NIC nic = new NIC();
		nic.setTotalBandwidth(1000 * MBYTE);
		return nic;
	}

	@Override
	public INetworkEntry createNetworkEntry() {
		NIC nic = new NIC();
		nic.setTotalBandwidth(1000 * MBYTE);
		return nic;
	}

}

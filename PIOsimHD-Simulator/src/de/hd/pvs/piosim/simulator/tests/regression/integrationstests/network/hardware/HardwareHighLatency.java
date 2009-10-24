package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.hardware;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkEdge.SimpleNetworkEdge;

/**
 * This Hardware setup uses latencies of 0.5 s == 500ms.
 * @author julian
 *
 */
public class HardwareHighLatency extends BasicHardwareSetup {
	@Override
	public NetworkEdge createEdge(){
		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setLatency(new Epoch(0.5));
		conn.setBandwidth(100 * MBYTE);
		return conn;
	}
}

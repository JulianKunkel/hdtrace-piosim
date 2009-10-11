package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.hardware;

import de.hd.pvs.piosim.model.components.NetworkEdge.CutThroughNetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.CutThroughForwardNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;

/**
 * Use Cut-Through network nodes and edges
 * @author julian
 */
public class HardwareCutThroughNetwork extends BasicHardwareSetup{
	@Override
	public INetworkNode createNetworkNode() {
		CutThroughForwardNode sw = new CutThroughForwardNode();
		sw.setName("Node");
		return (INetworkNode) sw;
	}

	public NetworkEdge createEdge(){
		CutThroughNetworkEdge conn = new CutThroughNetworkEdge();
		conn.setName("1GBit Ethernet");
		return conn;
	}
}

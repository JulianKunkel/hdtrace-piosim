/**
 * 
 */
package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkEdge.SimpleNetworkEdge;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicModelClassMapper;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.hardware.BasicHardwareSetup;

public class CutThroughTest extends BasicHardwareSetup{
	/**
	 * 
	 */
	private NetworkRoutingTest networkRoutingTest;

	/**
	 * @param networkRoutingTest
	 */
	CutThroughTest(NetworkRoutingTest networkRoutingTest) {
		this.networkRoutingTest = networkRoutingTest;
	}

	@Override
	protected NetworkEdge createEdge() {
		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setName("1GBit Ethernet");
		conn.setLatency(new Epoch(0.0001));
		conn.setBandwidth(100 * this.networkRoutingTest.MBYTE);
		return conn;
	}

	protected INetworkNode createNetworkNode() {
		CutThroughForwardNodeExit exitRouteNode = new CutThroughForwardNodeExit();
		// add our own implementation
		DynamicModelClassMapper.addComponentImplementation(exitRouteNode.getObjectType(),
				CutThroughForwardNodeExit.class.getCanonicalName(),
				GCutThroughForwardNodeExit.class.getCanonicalName());

		exitRouteNode.setName("Exit");
		return exitRouteNode;
	};
}
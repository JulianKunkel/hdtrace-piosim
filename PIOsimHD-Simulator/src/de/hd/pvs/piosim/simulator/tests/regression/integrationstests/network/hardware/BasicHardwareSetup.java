/**
 *
 */
package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.hardware;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkEdge.SimpleNetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardNode;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicModelClassMapper;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.GStoreAndForwardExitNode;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.StoreForwardNodeExit;

public class BasicHardwareSetup implements TestHardwareSetup{
	protected final long MBYTE = 1000 * 1000;

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.hardware.TestHardwareSetup#createEdge()
	 */
	public NetworkEdge createEdge(){
		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setName("1GBit Ethernet");
		conn.setLatency(Epoch.ZERO);
		conn.setBandwidth(100 * MBYTE);
		return conn;
	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.hardware.TestHardwareSetup#setupNetworkNode()
	 */
	public INetworkNode setupNetworkNode(){
		StoreForwardNodeExit exitRouteNode = new StoreForwardNodeExit();
		// add our own implementation
		DynamicModelClassMapper.addComponentImplementation(exitRouteNode.getObjectType(),
				StoreForwardNodeExit.class.getCanonicalName(),
				GStoreAndForwardExitNode.class.getCanonicalName());

		exitRouteNode.setTotalBandwidth(10000000 * MBYTE);

		return exitRouteNode;
	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.hardware.TestHardwareSetup#createNetworkNode()
	 */
	public INetworkNode createNetworkNode(){
		StoreForwardNode sw = new StoreForwardNode();
		sw.setName("Node");
		sw.setTotalBandwidth(10000000 * MBYTE);
		return (INetworkNode) sw;
	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.hardware.TestHardwareSetup#createNetworkExit()
	 */
	public INetworkExit createNetworkExit(){
		INetworkExit exit = (INetworkExit) setupNetworkNode();
		exit.setName("Exit");
		return exit;
	}

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.hardware.TestHardwareSetup#createNetworkEntry()
	 */
	public INetworkEntry createNetworkEntry(){
		INetworkEntry entry = (INetworkEntry) setupNetworkNode();
		entry.setName("Entry");
		return entry;
	}
}
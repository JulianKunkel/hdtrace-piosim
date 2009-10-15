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
import de.hd.pvs.piosim.simulator.components.NetworkNode.GStoreAndForwardExitNode;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.StoreForwardNodeExit;

public class BasicHardwareSetup implements TestHardwareSetup{
	protected final long MBYTE = 1000 * 1000;

	public NetworkEdge createEdge(){
		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setLatency(Epoch.ZERO);
		conn.setBandwidth(100 * MBYTE);
		return conn;
	}

	public INetworkNode setupNetworkNode(){
		StoreForwardNodeExit exitRouteNode = new StoreForwardNodeExit();
		// add our own implementation
		DynamicModelClassMapper.addComponentImplementation(exitRouteNode.getObjectType(),
				StoreForwardNodeExit.class.getCanonicalName(),
				GStoreAndForwardExitNode.class.getCanonicalName());

		exitRouteNode.setTotalBandwidth(1000 * MBYTE);

		return exitRouteNode;
	}
	public INetworkNode createNetworkNode(){
		StoreForwardNode sw = new StoreForwardNode();
		sw.setTotalBandwidth(1000 * MBYTE);
		return (INetworkNode) sw;
	}

	public INetworkExit createNetworkExit(){
		INetworkExit exit = (INetworkExit) setupNetworkNode();
		return exit;
	}

	public INetworkEntry createNetworkEntry(){
		INetworkEntry entry = (INetworkEntry) setupNetworkNode();
		return entry;
	}
}
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
import de.hd.pvs.piosim.simulator.components.NetworkNode.GEntryNode;
import de.hd.pvs.piosim.simulator.components.NetworkNode.GExitNode;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.ModelEntryNode;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.ModelExitNode;

public class BasicHardwareSetup implements TestHardwareSetup{
	protected final long MBYTE = 1000 * 1000;

	public NetworkEdge createEdge(){
		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setLatency(Epoch.ZERO);
		conn.setBandwidth(100 * MBYTE);
		return conn;
	}

	public INetworkNode createNetworkNode(){
		StoreForwardNode sw = new StoreForwardNode();
		sw.setTotalBandwidth(1000 * MBYTE);
		return (INetworkNode) sw;
	}

	public INetworkExit createNetworkExit(){
		ModelExitNode node = new ModelExitNode();
		// add our own implementation
		DynamicModelClassMapper.addComponentImplementation(node.getObjectType(),
				ModelExitNode.class.getCanonicalName(),
				GExitNode.class.getCanonicalName());

		return node;
	}

	public INetworkEntry createNetworkEntry(){
		ModelEntryNode node = new ModelEntryNode();
		// add our own implementation
		DynamicModelClassMapper.addComponentImplementation(node.getObjectType(),
				ModelEntryNode.class.getCanonicalName(),
				GEntryNode.class.getCanonicalName());
		return node;
	}
}
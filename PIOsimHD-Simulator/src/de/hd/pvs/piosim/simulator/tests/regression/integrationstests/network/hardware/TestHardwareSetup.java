package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.hardware;

import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;

public interface TestHardwareSetup {

	public abstract NetworkEdge createEdge();

	public abstract INetworkNode createNetworkNode();

	public abstract INetworkExit createNetworkExit();

	public abstract INetworkEntry createNetworkEntry();

}
package de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareExtensions;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;

public interface HardwareExtension {
	public void extendNetworkNode(String prefix, NetworkNode nodeToInterconnect,  ModelBuilder mb, INetworkTopology topology) throws Exception;
}

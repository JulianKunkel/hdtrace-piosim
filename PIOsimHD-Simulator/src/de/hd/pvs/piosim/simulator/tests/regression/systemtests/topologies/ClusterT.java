package de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;

/**
 * Create a cluster configuration with ethernet and a single switch.
 * @author julian
 */
public class ClusterT extends HardwareConfiguration {

	final int nodeCount;

	final NetworkEdge nodeEdge;
	final NetworkNode Switch;
	final SMTNodeT smtNodeT;

	public ClusterT(int nodeCount,
			NetworkEdge nodeEdge, NetworkNode Switch,
			SMTNodeT smtNodeT)
	{
		this.nodeCount = nodeCount;
		this.nodeEdge = nodeEdge;
		this.Switch = Switch;
		this.smtNodeT = smtNodeT;
	}

	@Override
	public NetworkNode createModel(String prefix, ModelBuilder mb, INetworkTopology topology) throws Exception {
		mb.addTemplateIf(nodeEdge);
		mb.addTemplateIf(Switch);

		// /// NOW BUILD OBJECTS BASED ON PREVIOUS SETUP...
		NetworkNode testSW = mb.cloneFromTemplate(Switch);

		testSW.setName(prefix + "Switch" + testSW.getName());

		mb.addNetworkNode(testSW);

		for (int i = 0; i < nodeCount; i++) {
			NetworkNode n = smtNodeT.createModel(prefix + i, mb, topology);

			NetworkEdge edge1 = mb.cloneFromTemplate(nodeEdge);
			NetworkEdge edge2 = mb.cloneFromTemplate(nodeEdge);
			edge1.setName(prefix + i + "_TX " + edge1.getName());
			edge2.setName(prefix + i + "_RX "+ edge2.getName());
			mb.connect(topology, n, edge1 , testSW);


			mb.connect(topology, testSW, edge2 , n);
		}

		return testSW;
	}

}

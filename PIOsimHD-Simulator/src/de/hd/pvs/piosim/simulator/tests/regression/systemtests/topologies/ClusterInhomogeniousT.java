package de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;

/**
 * Create a cluster configuration with ethernet and a single switch.
 * Two different types of nodes are added.
 * @author julian
 */
public class ClusterInhomogeniousT extends HardwareConfiguration {

	final int nodeCount;
	final int nodeCount2;

	final NetworkEdge nodeEdge;
	final NetworkEdge nodeEdge2;
	final NetworkNode Switch;
	final SMTNodeT smtNodeT;
	final SMTNodeT smtNode2T;

	public ClusterInhomogeniousT(int nodeCount,
			NetworkEdge nodeEdge, NetworkNode Switch,
			SMTNodeT smtNodeT, int nodeCount2, NetworkEdge nodeEdge2,SMTNodeT smtNode2T)
	{
		this.nodeCount = nodeCount;
		this.nodeEdge = nodeEdge;
		this.Switch = Switch;
		this.smtNodeT = smtNodeT;
		this.smtNode2T = smtNode2T;
		this.nodeEdge2 = nodeEdge2;
		this.nodeCount2 = nodeCount2;
	}

	@Override
	public NetworkNode createModel(String prefix, ModelBuilder mb, INetworkTopology topology) throws Exception {
		mb.addTemplateIf(nodeEdge);
		mb.addTemplateIf(nodeEdge2);
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

		for (int i = 0; i < nodeCount2; i++) {
			NetworkNode n = smtNode2T.createModel(prefix + i, mb, topology);

			NetworkEdge edge1 = mb.cloneFromTemplate(nodeEdge2);
			NetworkEdge edge2 = mb.cloneFromTemplate(nodeEdge2);
			edge1.setName(prefix + i + "_TX " + edge1.getName());
			edge2.setName(prefix + i + "_RX "+ edge2.getName());
			mb.connect(topology, n, edge1 , testSW);

			mb.connect(topology, testSW, edge2 , n);
		}

		return testSW;
	}

}

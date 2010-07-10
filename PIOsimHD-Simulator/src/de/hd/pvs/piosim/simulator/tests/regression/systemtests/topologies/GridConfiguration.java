package de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;

/**
 * Create a grid configuration.
 * Clusters of a given size are interconnected by a different network.
 *
 * @author julian
 */
public class GridConfiguration extends HardwareConfiguration {

	final int clusters;
	final NetworkEdge clusterInterconnectEdges;
	final NetworkNode clusterInterconnectSwitch;
	final ClusterT clusterT;

	public GridConfiguration(int clusters, 	NetworkEdge clusterInterconnectEdges,
			NetworkNode clusterInterconnectSwitch,
			ClusterT clusterT ) {
		this.clusters = clusters;
		this.clusterInterconnectEdges = clusterInterconnectEdges;
		this.clusterInterconnectSwitch = clusterInterconnectSwitch;
		this.clusterT = clusterT;
	}

	@Override
	public NetworkNode createModel(String prefix, ModelBuilder mb, INetworkTopology topology) throws Exception {
		mb.addTemplateIf(clusterInterconnectEdges);
		mb.addTemplateIf(clusterInterconnectSwitch);

		// /// NOW BUILD OBJECTS BASED ON PREVIOUS SETUP...
		NetworkNode testSW = mb.cloneFromTemplate(clusterInterconnectSwitch);

		for (int i = 0; i < clusters; i++) {
			NetworkNode n = clusterT.createModel("" + i, mb, topology);

			NetworkEdge edge1 = mb.cloneFromTemplate(clusterInterconnectEdges);
			mb.connect(topology, n, edge1 , testSW);

			NetworkEdge edge2 = mb.cloneFromTemplate(clusterInterconnectEdges);
			mb.connect(topology, testSW, edge2 , n);
		}

		return testSW;
	}

}

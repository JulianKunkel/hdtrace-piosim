package de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;

/**
 * Use a client cluster and interconnect it with an I/O server cluster.
 *
 * @author julian
 */
public class IODisjointConfiguration extends HardwareConfiguration {

	final NetworkEdge clusterInterconnectEdges;
	final NetworkNode clusterInterconnectSwitch;
	final ClusterT clusterClients;
	final ClusterT clusterServers;

	public IODisjointConfiguration(NetworkEdge clusterInterconnectEdges,
			NetworkNode clusterInterconnectSwitch,
			ClusterT clusterClients,  ClusterT clusterServers)
	{
		this.clusterInterconnectEdges = clusterInterconnectEdges;
		this.clusterInterconnectSwitch = clusterInterconnectSwitch;
		this.clusterClients = clusterClients;
		this.clusterServers = clusterServers;
	}

	@Override
	public NetworkNode createModel(String prefix, ModelBuilder mb, INetworkTopology topology) throws Exception {
		mb.addTemplateIf(clusterInterconnectEdges);
		mb.addTemplateIf(clusterInterconnectSwitch);

		// /// NOW BUILD OBJECTS BASED ON PREVIOUS SETUP...
		NetworkNode testSW = mb.cloneFromTemplate(clusterInterconnectSwitch);
		testSW.setName(prefix + "storageHub" + testSW.getName());

		mb.addNetworkNode(testSW);

		{
			NetworkNode n = clusterClients.createModel("c", mb, topology);

			NetworkEdge edge1 = mb.cloneFromTemplate(clusterInterconnectEdges);
			NetworkEdge edge2 = mb.cloneFromTemplate(clusterInterconnectEdges);

			edge1.setName(prefix + "c_TX " + edge1.getName());
			edge2.setName(prefix + "c_RX "+ edge2.getName());

			mb.connect(topology, n, edge1 , testSW);

			mb.connect(topology, testSW, edge2 , n);
		}


		{
			NetworkNode n = clusterServers.createModel("s", mb, topology);

			NetworkEdge edge1 = mb.cloneFromTemplate(clusterInterconnectEdges);
			NetworkEdge edge2 = mb.cloneFromTemplate(clusterInterconnectEdges);

			edge1.setName(prefix + "s_TX " + edge1.getName());
			edge2.setName(prefix + "s_RX "+ edge2.getName());

			mb.connect(topology, n, edge1 , testSW);

			mb.connect(topology, testSW, edge2 , n);
		}


		return testSW;
	}

}

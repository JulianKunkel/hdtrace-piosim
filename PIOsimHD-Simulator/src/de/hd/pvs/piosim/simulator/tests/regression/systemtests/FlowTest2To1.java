package de.hd.pvs.piosim.simulator.tests.regression.systemtests;

import org.junit.Test;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkEdge.SimpleNetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;
import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardNode;
import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.HardwareConfiguration;

public class FlowTest2To1 extends ModelTest{

	@Override
	protected void postSetup() {
	}

	HardwareConfiguration myConfig = new HardwareConfiguration(){
		@Override
		public NetworkNode createModel(String prefix, ModelBuilder mb, INetworkTopology topology) throws Exception {
			// Instantiate objects representing the templates.
			final Node node = new Node();
			final NIC nic = new NIC();
			final StoreForwardNode fastNode = new StoreForwardNode();
			final SimpleNetworkEdge fastEdge = new SimpleNetworkEdge();
			final SimpleNetworkEdge slowEdge = new SimpleNetworkEdge();

			// Set the model properties.
			nic.setTotalBandwidth(100*GBYTE);
			nic.setName("nic");

			node.setName("node");
			node.setInstructionsPerSecond(1000000000);
			node.setCPUs(1);
			node.setMemorySize(GBYTE);

			fastNode.setName("fastNode");
			fastNode.setTotalBandwidth(100*GBYTE);
			fastEdge.setName("fastEdge");
			fastEdge.setLatency(new Epoch(0.001));
			fastEdge.setBandwidth(100*MiB);

			slowEdge.setName("slowEdge");
			slowEdge.setLatency(new Epoch(0.1));
			slowEdge.setBandwidth(70*MiB);

			// Add those instances to the template library.
			mb.addTemplateIf(node);
			mb.addTemplateIf(nic);
			mb.addTemplateIf(fastNode);
			mb.addTemplateIf(fastEdge);
			mb.addTemplateIf(slowEdge);


			// Create the central node by cloning the template.
			final NetworkNode nodeA = mb.cloneFromTemplate(fastNode);
			nodeA.setName("node");
			mb.addNetworkNode(nodeA);

			// Add all the clients and interconnect them.
			// An array encodes the processes' names.
			final String [] names = {"A", "B", "C", "Y", "Z"};

			for(int i=0; i < 4; i++){
			  // Instantiate the NIC for the client.
			  NIC nm = mb.cloneFromTemplate(nic);
			  nm.setName(names[i]);

			  // Create the client.
			  final ClientProcess c = new ClientProcess();
			  c.setNetworkInterface(nm);
			  c.setName(names[i]);
			  c.setRank(i);
			  c.setNetworkInterface(nm);

			  // Instantiate the node.
			  final Node n = mb.cloneFromTemplate(node);
			  n.setName("N" + names[i]);
			  mb.addNode(n);
			  mb.addClient(n, c);

			  if(i == 1){
			  // Interconnect node Y.
				  NetworkEdge e1 = mb.cloneFromTemplate(slowEdge);
				  NetworkEdge e2 = mb.cloneFromTemplate(slowEdge);
				  mb.connect(topology, nm, e1, nodeA);
				  mb.connect(topology, nodeA, e2, nm);
			  }else{
				  // Interconnect nodes via the central network node.
				  NetworkEdge e1 = mb.cloneFromTemplate(fastEdge);
				  NetworkEdge e2 = mb.cloneFromTemplate(fastEdge);
				  mb.connect(topology, nm, e1, nodeA);
				  mb.connect(topology, nodeA, e2, nm);
			  }
			}


			return null;
		}
	};


	@Test public void three() throws Exception{
		setup(myConfig);

		mb.getGlobalSettings().setMaxEagerSendSize(1000 * MiB);
		//mb.getGlobalSettings().setMaxEagerSendSize(1100 * KiB);
		//mb.getGlobalSettings().setTransferGranularity(101 * KiB);

		parameters.setTraceFile("/tmp/three");
		parameters.setTraceEnabled(true);
		parameters.setTraceInternals(true);
		parameters.setTraceClientSteps(true);
		parameters.setTraceClientNestingOperations(true);

		// setup the commands

		pb.addSend(world, 0, 3, 100*MiB, 0);
		pb.addRecv(world, 0, 3, 0);

		pb.addSend(world, 1, 3, 100*MiB, 0);
		pb.addRecv(world, 1, 3, 0);


		runSimulationAllExpectedToFinish();
	}

}

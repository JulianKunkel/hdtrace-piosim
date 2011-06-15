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

public class FlowTest extends ModelTest{

	@Override
	protected void postSetup() {
	}

	HardwareConfiguration myConfig = new HardwareConfiguration(){
		@Override
		public NetworkNode createModel(String prefix, ModelBuilder mb, INetworkTopology topology) throws Exception {

			// instantiate the templates:
			final Node node = new Node();

			final NIC nic = new NIC();
			final StoreForwardNode fastNode = new StoreForwardNode();
			final SimpleNetworkEdge fastEdge = new SimpleNetworkEdge();
			final SimpleNetworkEdge slowEdge = new SimpleNetworkEdge();

			// set the model properties
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
			slowEdge.setBandwidth(10*MiB);

			// add those instances as templates

			mb.addTemplateIf(node);
			mb.addTemplateIf(nic);
			mb.addTemplateIf(fastNode);
			mb.addTemplateIf(fastEdge);
			mb.addTemplateIf(slowEdge);


			// create the central node by cloning the template
			final NetworkNode nodeA = mb.cloneFromTemplate(fastNode);
			nodeA.setName("node");
			mb.addNetworkNode(nodeA);

			// create the node to Y from the same template
			final NetworkNode nodetoY = mb.cloneFromTemplate(fastNode);
			nodetoY.setName("nodeToY");
			mb.addNetworkNode(nodetoY);

			// create the interconnect between those two nodes
			{
				NetworkEdge e1 = mb.cloneFromTemplate(fastEdge);
				NetworkEdge e2 = mb.cloneFromTemplate(fastEdge);

				mb.connect(topology, nodeA, e1, nodetoY);
				mb.connect(topology, nodetoY, e2, nodeA);
			}


			// add all the clients and interconnect them.
			// an array of the processes' names
			final String [] names = {"A", "B", "C", "Y", "Z"};

			for(int i=0; i < 5; i++){
				// create the NIC
				NIC nm = mb.cloneFromTemplate(nic);
				nm.setName(names[i]);

				// create the client
				final ClientProcess c = new ClientProcess();
				c.setNetworkInterface(nm);
				c.setName(names[i]);
				c.setRank(i);
				c.setNetworkInterface(nm);

				// create the node
				final Node n = mb.cloneFromTemplate(node);
				n.setName(names[i]);
				mb.addNode(n);
				mb.addClient(n, c);

				if(i < 3 || i == 4){
				// interconnect via crossbar switch
					NetworkEdge e1 = mb.cloneFromTemplate(fastEdge);
					NetworkEdge e2 = mb.cloneFromTemplate(fastEdge);
					mb.connect(topology, nm, e1, nodeA);
					mb.connect(topology, nodeA, e2, nm);
				}else if(i == 3){
					// node Y
					NetworkEdge e1 = mb.cloneFromTemplate(slowEdge);
					NetworkEdge e2 = mb.cloneFromTemplate(slowEdge);
					mb.connect(topology, nm, e1, nodetoY);
					mb.connect(topology, nodetoY, e2, nm);
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

		// setup the commands
//
//		for(int i=0; i < 3; i++){
//			for(int t=3; t < 5; t++){
//				pb.addSend(world, i, t, 100*MiB, 0);
//				pb.setLastCommandAsynchronous(i);
//
//				pb.addRecv(world, i, t, 0);
//				pb.setLastCommandAsynchronous(t);
//			}
//		}
//
//
//		for(int t=0; t < 5; t++){
//			pb.addWaitAll(t);
//		}
//
//		pb.addBarrier(world);

		pb.addSend(world, 0, 3, 1010*KiB, 0);
		pb.addRecv(world, 0, 3, 0);


		runSimulationAllExpectedToFinish();
	}

}

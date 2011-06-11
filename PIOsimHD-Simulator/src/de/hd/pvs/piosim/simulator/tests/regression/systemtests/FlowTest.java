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
			final NIC nic = new NIC();
			nic.setTotalBandwidth(100*GBYTE);
			nic.setName("nic");

			final Node node = new Node();
			final StoreForwardNode fastNode = new StoreForwardNode();
			final SimpleNetworkEdge fastEdge = new SimpleNetworkEdge();
			final SimpleNetworkEdge slowEdge = new SimpleNetworkEdge();

			node.setName("node");
			node.setInstructionsPerSecond(1000000000);
			node.setCPUs(1);
			node.setMemorySize(GBYTE);

			fastNode.setName("fn");
			fastNode.setTotalBandwidth(100*GBYTE);
			fastEdge.setName("fe");
			slowEdge.setName("se");
			fastEdge.setLatency(new Epoch(0.001));
			slowEdge.setLatency(new Epoch(0.1));
			fastEdge.setBandwidth(100*MiB);
			slowEdge.setBandwidth(10*MiB);

			mb.addTemplateIf(node);
			mb.addTemplateIf(nic);
			mb.addTemplateIf(fastNode);
			mb.addTemplateIf(fastEdge);
			mb.addTemplateIf(slowEdge);


			final NetworkNode nodeA = mb.cloneFromTemplate(fastNode);
			// create crossbar switch
			nodeA.setName("node");
			mb.addNetworkNode(nodeA);

			final NetworkNode nodeB = mb.cloneFromTemplate(fastNode);
			// create crossbar switch
			nodeB.setName("nodeY");
			mb.addNetworkNode(nodeB);

			{
				NetworkEdge e1 = mb.cloneFromTemplate(fastEdge);
				NetworkEdge e2 = mb.cloneFromTemplate(fastEdge);
				mb.connect(topology, nodeB, e1, nodeA);
				mb.connect(topology, nodeA, e2, nodeB);
			}


			// add all the clients:
			final String [] names = {"A", "B", "C", "Y", "Z"};

			for(int i=0; i < 5; i++){
				NIC nm = mb.cloneFromTemplate(nic);

				final ClientProcess c = new ClientProcess();
				c.setNetworkInterface(nm);
				c.setName(names[i] + "C");
				nm.setName(names[i]);
				c.setRank(i);


				c.setNetworkInterface(nm);

				final Node n = mb.cloneFromTemplate(node);
				n.setName(names[i] + "N");
				mb.addNode(n);
				mb.addClient(n, c);

				if(i < 3 || i == 4){
				// interconnect via crossbar switch
					NetworkEdge e1 = mb.cloneFromTemplate(fastEdge);
					NetworkEdge e2 = mb.cloneFromTemplate(fastEdge);
					e1.setName(i + "_TX " + e1.getName());
					e2.setName(i + "_RX " + e2.getName());
					mb.connect(topology, nm, e1, nodeA);
					mb.connect(topology, nodeA, e2, nm);
				}else if(i == 3){
					NetworkEdge e1 = mb.cloneFromTemplate(slowEdge);
					NetworkEdge e2 = mb.cloneFromTemplate(slowEdge);
					e1.setName(i + "_TX " + e1.getName());
					e2.setName(i + "_RX " + e2.getName());
					mb.connect(topology, nm, e1, nodeB);
					mb.connect(topology, nodeB, e2, nm);
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

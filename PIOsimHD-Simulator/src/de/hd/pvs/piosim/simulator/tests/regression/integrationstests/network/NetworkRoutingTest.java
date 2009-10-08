package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network;

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkEdge.SimpleNetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;
import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardForwardNode;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicModelClassMapper;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketFirstRoute;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoutingAlgorithm;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkEntry;
import de.hd.pvs.piosim.simulator.network.GNetworkTopology;
import de.hd.pvs.piosim.simulator.network.Message;

public class NetworkRoutingTest {
	final int KBYTE = 1024;
	final int MBYTE = 1024 * 1024;

	protected NetworkNode node;
	protected NetworkEdge edge;
	protected ModelBuilder mb;
	protected INetworkTopology topology;

	// node that is source and target of packets
	protected NetworkNode exitRouteNode;

	protected interface TestSetup{
		public void Setup();
	}

	public class Test1 implements TestSetup{
		@Override
		public void Setup() {
			SimpleNetworkEdge conn = new SimpleNetworkEdge();
			conn.setName("1GBit Ethernet");
			conn.setLatency(new Epoch(0.0001));
			conn.setBandwidth(100 * MBYTE);
			mb.getModel().getGlobalSettings().setTransferGranularity(100 * KBYTE);
			mb.addTemplate(conn);

			edge = conn;

			PaketRoutingAlgorithm routing = new PaketFirstRoute();
			topology = mb.createTopology("LAN");
			topology.setRoutingAlgorithm(routing);


			StoreForwardForwardNodeExit exitRouteNode = new StoreForwardForwardNodeExit();
			// add our own implementation
			DynamicModelClassMapper.addComponentImplementation(exitRouteNode.getObjectType(),
					StoreForwardForwardNodeExit.class.getCanonicalName(),
					GStoreAndForwardExitNode.class.getCanonicalName());

			exitRouteNode.setName("Exit");
			exitRouteNode.setTotalBandwidth(100 * MBYTE);
			getThis().exitRouteNode = exitRouteNode;

			mb.addTemplate(exitRouteNode);

			StoreForwardForwardNode sw = new StoreForwardForwardNode();
			sw.setName("PVS-Switch");
			sw.setTotalBandwidth(1000 * MBYTE);

			mb.addTemplate(sw);
			node = sw;
		}
	}

	public class Test2 implements TestSetup{
		@Override
		public void Setup() {
			SimpleNetworkEdge conn = new SimpleNetworkEdge();
			conn.setName("1GBit Ethernet");
			conn.setLatency(new Epoch(0.0001));
			conn.setBandwidth(100 * MBYTE);
			mb.getModel().getGlobalSettings().setTransferGranularity(100 * KBYTE);
			mb.addTemplate(conn);

			edge = conn;

			PaketRoutingAlgorithm routing = new PaketFirstRoute();
			topology = mb.createTopology("LAN");
			topology.setRoutingAlgorithm(routing);


			CutThroughForwardNodeExit exitRouteNode = new CutThroughForwardNodeExit();
			// add our own implementation
			DynamicModelClassMapper.addComponentImplementation(exitRouteNode.getObjectType(),
					CutThroughForwardNodeExit.class.getCanonicalName(),
					GCutThroughForwardNodeExit.class.getCanonicalName());

			exitRouteNode.setName("Exit");
			getThis().exitRouteNode = exitRouteNode;

			mb.addTemplate(exitRouteNode);

			StoreForwardForwardNode sw = new StoreForwardForwardNode();
			sw.setName("PVS-Switch");
			sw.setTotalBandwidth(1000 * MBYTE);

			mb.addTemplate(sw);
			node = sw;
		}
	}

	protected interface TestConfiguration{
		public void runTest() throws Exception;
	}

	public ArrayList<TestSetup> setups = new ArrayList<TestSetup>();
	public ArrayList<TestConfiguration> tests = new ArrayList<TestConfiguration>();

	public NetworkRoutingTest() {
		setups.add(new Test1());
		setups.add(new Test2());

		tests.add(new test3x3Grid());
	}

	public void runAllTests() throws Exception{
		for(TestSetup setup : setups){
			for(TestConfiguration test : tests){
				mb = new ModelBuilder();
				setup.Setup();

				assert(edge != null);

				test.runTest();
			}
		}
	}

	protected NetworkRoutingTest getThis(){
		return this;
	}

	public class test3x3Grid implements TestConfiguration{
		@Override
		public void runTest() throws Exception {
			final int HEIGHT = 3;
			final int WIDTH = 3;

			final ArrayList<INetworkNode> nodes = new ArrayList<INetworkNode>();
			// arrangement to:
			// 0, 1, 2,
			// 3, 4, 5
			// 6, 7, 8

			// create nodes:
			for(int y = 0 ; y < HEIGHT; y++){
				for (int x = 0; x < WIDTH; x++) {
					INetworkNode node = mb.cloneFromTemplate(getThis().exitRouteNode);
					nodes.add(node);
					node.setName(x + ":"  + y );
				}
			}
			// create horizontal edges:
			for(int y = 0 ; y < HEIGHT; y++){
				for (int x = 0; x < WIDTH - 1; x++) {
					NetworkEdge edge = mb.cloneFromTemplate(getThis().edge);
					NetworkEdge edge2 = mb.cloneFromTemplate(getThis().edge);

					INetworkNode src = nodes.get(x + y * WIDTH);
					INetworkNode tgt = nodes.get(x + 1 + y * WIDTH);

					edge.setName(src.getName() + " -> " + tgt.getName());
					edge2.setName(tgt.getName() + " -> " + src.getName());

					mb.connect(topology, src, edge , tgt);
					mb.connect(topology, tgt, edge2 , src);
				}
			}

			// create vertical edges:

			for (int x = 0; x < WIDTH; x++) {
				for(int y = 0 ; y < HEIGHT - 1 ; y++){
					NetworkEdge edge = mb.cloneFromTemplate(getThis().edge);
					NetworkEdge edge2 = mb.cloneFromTemplate(getThis().edge);

					INetworkNode src = nodes.get(x + y * WIDTH);
					INetworkNode tgt = nodes.get(x + (y+1) * WIDTH);

					edge.setName(src.getName() + " -> " + tgt.getName());
					edge2.setName(tgt.getName() + " -> " + src.getName());

					mb.connect(topology, src, edge , tgt);
					mb.connect(topology, tgt, edge2 , src);
				}
			}

			Simulator sim = new Simulator();
			sim.initModel(mb.getModel(), null);

			//printRouting(sim);

			// test some basic send & rcvs.
			IGNetworkEntry startNode = (IGNetworkEntry)  sim.getSimulatedComponent(nodes.get(0));
			Message msg = new Message(10000, null, (INetworkExit) nodes.get(nodes.size()-1));
			startNode.submitNewMessage(msg);

			sim.simulate();
		}
	}

	private void printRouting(Simulator sim) throws Exception{
		// now print the routing tables.
		System.out.println("Routing tables are as follows:");

		for(GNetworkTopology topo: sim.getExistingTopologies()){
			System.out.println("Topo: " + topo.getName());
			System.out.println(topo.getRouting());
		}
	}

	public static void main(String[] args) throws Exception {
		NetworkRoutingTest test = new NetworkRoutingTest();
		test.runAllTests();
	}
}

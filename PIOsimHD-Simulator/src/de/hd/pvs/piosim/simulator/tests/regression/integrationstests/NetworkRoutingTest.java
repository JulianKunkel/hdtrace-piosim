package de.hd.pvs.piosim.simulator.tests.regression.integrationstests;

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkEdge.SimpleNetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;
import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardNetworkNode;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicModelClassMapper;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketFirstRoute;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoutingAlgorithm;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.network.GNetworkTopology;

public class NetworkRoutingTest {
	final int KBYTE = 1024;
	final int MBYTE = 1024 * 1024;

	protected NetworkNode node;
	protected NetworkEdge edge;
	protected ModelBuilder mb;
	protected INetworkTopology topology;

	protected NetworkNode exitRouteNode;

	protected void setup(){
		mb = new ModelBuilder();

		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setName("1GBit Ethernet");
		conn.setLatency(new Epoch(0.0002));
		conn.setBandwidth(100 * MBYTE);
		mb.getModel().getGlobalSettings().setTransferGranularity(100 * KBYTE);
		mb.addTemplate(conn);

		this.edge = conn;

		PaketRoutingAlgorithm routing = new PaketFirstRoute();
		topology = mb.createTopology("LAN");
		topology.setRoutingAlgorithm(routing);


		StoreForwardNetworkNodeExit exitRouteNode = new StoreForwardNetworkNodeExit();
		// add our own implementation
		DynamicModelClassMapper.addComponentImplementation(exitRouteNode.getObjectType(),
				StoreForwardNetworkNodeExit.class.getCanonicalName(),
				GStoreAndForwardExitNode.class.getCanonicalName());

		exitRouteNode.setName("Exit");
		exitRouteNode.setTotalBandwidth(1000 * MBYTE);
		this.exitRouteNode = exitRouteNode;

		mb.addTemplate(exitRouteNode);

		StoreForwardNetworkNode sw = new StoreForwardNetworkNode();
		sw.setName("PVS-Switch");
		sw.setTotalBandwidth(1000 * MBYTE);

		mb.addTemplate(sw);
		this.node = sw;
	}

	public void test3x3Grid() throws Exception{
		setup();

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
				INetworkNode node = mb.cloneFromTemplate(this.exitRouteNode);
				nodes.add(node);
				node.setName(x + ":"  + y );
			}
		}
		// create horizontal edges:
		for(int y = 0 ; y < HEIGHT; y++){
			for (int x = 0; x < WIDTH - 1; x++) {
				NetworkEdge edge = mb.cloneFromTemplate(this.edge);
				NetworkEdge edge2 = mb.cloneFromTemplate(this.edge);

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
				NetworkEdge edge = mb.cloneFromTemplate(this.edge);
				NetworkEdge edge2 = mb.cloneFromTemplate(this.edge);

				INetworkNode src = nodes.get(x + y * WIDTH);
				INetworkNode tgt = nodes.get(x + (y+1) * WIDTH);

				edge.setName(src.getName() + " -> " + tgt.getName());
				edge2.setName(tgt.getName() + " -> " + src.getName());

				mb.connect(topology, src, edge , tgt);
				mb.connect(topology, tgt, edge2 , src);
			}
		}
		printRouting();
	}

	private void printRouting() throws Exception{
		Simulator sim = new Simulator();
		sim.simulate(mb.getModel());

		// now print the routing tables.
		System.out.println("Routing tables are as follows:");

		for(GNetworkTopology topo: sim.getExistingTopologies()){
			System.out.println("Topo: " + topo.getName());
			System.out.println(topo.getRouting());
		}
	}

	public static void main(String[] args) throws Exception {
		NetworkRoutingTest test = new NetworkRoutingTest();
		test.test3x3Grid();
	}
}

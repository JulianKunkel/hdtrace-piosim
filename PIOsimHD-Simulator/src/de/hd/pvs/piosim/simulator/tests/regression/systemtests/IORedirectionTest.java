package de.hd.pvs.piosim.simulator.tests.regression.systemtests;

import java.util.ArrayList;

import org.junit.Test;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.IOSubsystem.RefinedDiskModel;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkEdge.SimpleNetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardNode;
import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.components.Router.Router;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.NoCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerDirectedIO;
import de.hd.pvs.piosim.model.inputOutput.IORedirection;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketFirstRoute;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoutingAlgorithm;

public class IORedirectionTest extends ClusterTest {
	@Override
	protected ModelBuilder createDisjointClusterModel(int clients, int servers, ServerCacheLayer cacheLayer) throws Exception {
		int nodeCount = clients + servers + 1; // last one is the router.

		ModelBuilder mb = new ModelBuilder();

		// add a io redirection.
		final IORedirection redirection = new IORedirection();
		mb.getModel().addIORedirectionLayer(redirection);

		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setName("1GBit Ethernet");
		conn.setLatency(new Epoch(0.00001));
		conn.setBandwidth(100 * MBYTE);

		mb.getModel().getGlobalSettings().setTransferGranularity(100 * KBYTE);

		mb.addTemplate(conn);


		Node node = new Node();
		node.setName("PVS-Node");
		// maschine.setCacheSize(1024*1024*1024);
		node.setCPUs(1);
		node.setInstructionsPerSecond(1000000);

		node.setMemorySize(1000 * MBYTE);

		// SimpleDisk iosub = new SimpleDisk();
		// iosub.setAvgAccessTime(new Epoch(0.005));
		// iosub.setMaxThroughput(((int) 50 * MBYTE));

		RefinedDiskModel iosub = new RefinedDiskModel();
		iosub.setAverageSeekTime(new Epoch(0.01));
		iosub.setTrackToTrackSeekTime(new Epoch(0.001));
		iosub.setRPM(7200);
		iosub.setPositionDifferenceConsideredToBeClose(5 * MBYTE);
		iosub.setSequentialTransferRate((int) 50 * MBYTE);

		iosub.setName("IBM");
		// iosub.setAvgAccessTime(new Epoch(0.002));
		// iosub.setMaxThroughput(50 * MBYTE);

		mb.addTemplate(iosub);
		mb.addTemplate(node);


		PaketRoutingAlgorithm routing = new PaketFirstRoute();
		INetworkTopology topology = mb.createTopology("LAN");
		topology.setRoutingAlgorithm(routing);

		StoreForwardNode sw = new StoreForwardNode();
		sw.setName("PVS-Switch");
		sw.setTotalBandwidth(1000 * MBYTE);

		mb.addTemplate(sw);

		// /// NOW BUILD OBJECTS BASED ON PREVIOUS SETUP...

		StoreForwardNode testSW = mb.cloneFromTemplate(sw);
		StoreForwardNode testSW2 = mb.cloneFromTemplate(sw);
		ArrayList<Node> nodes = new ArrayList<Node>();

		mb.addNetworkNode(testSW);
		mb.addNetworkNode(testSW2);

		for (int i = 0; i < nodeCount; i++) {
			final Node node2 = mb.cloneFromTemplate(node);
			nodes.add(node2);

			mb.addNode(node2);
		}

		for (int i = 0; i < clients; i++) {
			final ClientProcess c = new ClientProcess();

			final NIC nic = new NIC();
			nic.setTotalBandwidth(1000 * MBYTE);
			nic.setName("CNIC" + i);
			c.setNetworkInterface(nic);

			c.setName("Client" + i);
			mb.addClient(nodes.get(i), c);
			c.setRank(i);
			c.setApplication("Jacobi");

			NetworkEdge edge = mb.cloneFromTemplate(conn);
			mb.connect(topology, c.getNetworkInterface(), edge , testSW);

			NetworkEdge edge2 = mb.cloneFromTemplate(conn);
			mb.connect(topology, testSW, edge2 , c.getNetworkInterface());

			redirection.addModifyingComponent(c.getIdentifier().getID());
		}

		{
			NetworkEdge edge2 = mb.cloneFromTemplate(conn);
			NetworkEdge edge = mb.cloneFromTemplate(conn);
			mb.connect(topology, testSW, edge , testSW2);
			mb.connect(topology, testSW2, edge2 , testSW);
		}


		final Router router = new Router();
		{
			router.setName("Router");

			final NIC nic = new NIC();
			nic.setTotalBandwidth(1000 * MBYTE);
			nic.setName("RNIC" );
			router.setNetworkInterface(nic);

			mb.addRouter(nodes.get(nodes.size() -1), router);

			NetworkEdge edge2 = mb.cloneFromTemplate(conn);
			NetworkEdge edge = mb.cloneFromTemplate(conn);
			mb.connect(topology, testSW, edge , nic);
			mb.connect(topology, nic, edge2 , testSW);
		}

		Server serverTemplate = new Server();
		serverTemplate.setName("Server");
		serverTemplate.setIOsubsystem(iosub);

		assert(cacheLayer != null);

		if (cacheLayer.getClass() == AggregationCache.class
				|| cacheLayer.getClass() == ServerDirectedIO.class) {
			((AggregationCache)cacheLayer).setReadDataSievingMaxHoleSizeToCombine(10 * (int) MBYTE);
		}

		cacheLayer.setMaxNumberOfConcurrentIOOps(1);

		serverTemplate.setCacheImplementation(cacheLayer);

		mb.addTemplate(serverTemplate);

		for (int i = 0; i < servers; i++) {
			final NIC nic = new NIC();
			nic.setTotalBandwidth(1000 * MBYTE);
			nic.setName("SNIC" + i);

			Server s = mb.cloneFromTemplate(serverTemplate);
			s.setNetworkInterface(nic);

			// disjoint client and server processes
			mb.addServer(nodes.get(i + clients), s);

			NetworkEdge edge = mb.cloneFromTemplate(conn);
			mb.connect(topology, s.getNetworkInterface(), edge , testSW);

			NetworkEdge edge2 = mb.cloneFromTemplate(conn);
			mb.connect(topology, testSW, edge2 , s.getNetworkInterface());

			redirection.addRedirect(s.getIdentifier().getID(), router.getIdentifier().getID());
		}

		return mb;
	}

	@Test
	public void writeTest1C1S() throws Exception{
		setup(1, 1, new NoCache());
		final SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(100 * KBYTE);

		final MPIFile f = aB.createFile("testfile", 0, dist);
		pb.addFileOpen(f, world, true);

		pb.addWriteSequential(0, f, 0 * MBYTE, 1 * MBYTE);

		runSimulationAllExpectedToFinish();
	}

	@Test
	public void readTest1C1S() throws Exception{
		setup(1, 1, new NoCache());
		final SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(100 * KBYTE);

		final MPIFile f = aB.createFile("testfile", 100 * MBYTE, dist);

		pb.addFileOpen(f, world, false);

		pb.addReadSequential(0, f, 0 * MBYTE, 1 * MBYTE);

		runSimulationAllExpectedToFinish();
	}
}

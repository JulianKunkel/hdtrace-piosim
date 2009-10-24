/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$
 */

//	Copyright (C) 2008, 2009 Julian M. Kunkel
//
//	This file is part of PIOsimHD.
//
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.
package de.hd.pvs.piosim.simulator.tests.regression.systemtests;

import java.util.ArrayList;

import junit.framework.Assert;
import junit.framework.TestSuite;

import org.junit.After;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.GlobalSettings;
import de.hd.pvs.piosim.model.Model;
import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.ModelXMLWriter;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.IOSubsystem.RefinedDiskModel;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkEdge.SimpleNetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardNode;
import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.NoCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerDirectedIO;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketFirstRoute;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoutingAlgorithm;
import de.hd.pvs.piosim.model.program.Application;
import de.hd.pvs.piosim.model.program.ApplicationBuilder;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.ProgramBuilder;
import de.hd.pvs.piosim.simulator.RunParameters;
import de.hd.pvs.piosim.simulator.SimulationResultSerializer;
import de.hd.pvs.piosim.simulator.SimulationResults;
import de.hd.pvs.piosim.simulator.Simulator;

public class ClusterTest extends TestSuite {
	protected final long KBYTE = 1024;
	protected final long MBYTE = 1024 * KBYTE;
	protected final long GBYTE = 1024 * MBYTE;

	protected ModelBuilder mb;
	protected ApplicationBuilder aB;
	protected ProgramBuilder pb;
	protected Application app;
	protected Model model;
	protected Simulator sim;
	protected SimulationResults simRes;
	protected Communicator world;

	RunParameters parameters = new RunParameters();

	@After
	public void tearDown() {
		System.out.println();
		Assert.assertTrue(true); /* to ensure assert stays */
	}

	protected void printStack() {
		System.err.println(new Exception().getStackTrace()[1]);
	}

	protected void setup(int clients, int servers) throws Exception {
		setup(clients, servers, new NoCache());
	}

	protected void setup(int clients, int servers, ServerCacheLayer cacheLayer) throws Exception {
		parameters.setLoggerDefinitionFile("loggerDefinitionFiles/example");

		parameters.setTraceEnabled(false);
		parameters.setTraceEnabled(true);
		parameters.setTraceInternals(true);
		parameters.setTraceClientSteps(true);
//		parameters.setDebugEverything(true);

		mb = createDisjointClusterModel(clients, servers, cacheLayer);
		aB = new ApplicationBuilder("Jacobi", "Example Jacobi", clients, 1);
		app = aB.getApplication();

		pb = new ProgramBuilder(aB);

		world = aB.getWorldCommunicator();

		assert (world != null);

		model = mb.getModel();

		// write out model to /tmp
		final ModelXMLWriter writer = new ModelXMLWriter();
		writer.writeXMLFromModel(model, "/tmp/model.xml");
	}

	protected ModelBuilder createDisjointClusterModel(int clients, int servers) throws Exception {
		return createDisjointClusterModel(clients, servers, new NoCache());
	}

	protected ModelBuilder createDisjointClusterModel(int clients, int servers, ServerCacheLayer cacheLayer) throws Exception {

		int nodeCount = clients + servers;

		ModelBuilder mb = new ModelBuilder();

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
		sw.setTotalBandwidth(100000 * MBYTE);

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
		}

		{
			NetworkEdge edge2 = mb.cloneFromTemplate(conn);
			mb.connect(topology, testSW, edge2 , testSW2);
			mb.connect(topology, testSW2, edge2 , testSW);
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
		}

		return mb;
	}

	protected SimulationResults runSimulationAllExpectedToFinish() throws Exception {
		mb.setApplication("Jacobi", app);

		//ModelSortIDbySubcomponents sorter = new ModelSortIDbySubcomponents();
		//sorter.sort(model);

		// ModelXMLWriter xmlW = new ModelXMLWriter();
		// xmlW.writeXMLFromProject(model, "/tmp/", "test.xml");
		// TESTING

		// parameters.setTraceInternals(false);

		sim = new Simulator();
		sim.initModel(model, parameters);
		simRes = sim.simulate();

		final SimulationResultSerializer serializer = new SimulationResultSerializer();
		System.out.println(serializer.serializeResults(simRes));

		if(simRes.isErrorDuringProcessing()){
			throw new IllegalArgumentException("Errors occured during processing");
		}

		return simRes;
	}

	public Application createSetSimpleApplication() throws Exception {
		// Start to create program:

		setup(4, 1);
		aB = new ApplicationBuilder("Jacobi", "Example Jacobi", 4, 1);
		ProgramBuilder pB = new ProgramBuilder(aB);
		Communicator world = aB.getWorldCommunicator();

		model.getGlobalSettings().setTransferGranularity(100 * KBYTE);

		SimpleStripe distribution = new SimpleStripe();
		distribution.setChunkSize(MBYTE);
		MPIFile file = aB.createFile("testfile", 100 * MBYTE, distribution);

		pB.addSendAndRecv(world, 0, 1, 10000, 1);

		pB.addComputate(0, 10000);
		pB.addComputate(1, 2000);
		pB.addComputate(2, 1000);
		pB.addComputate(3, 1000);

		pB.addWriteSequential(3, file, 0, 100 * MBYTE);
		pB.addReadSequential(0, file, 0, 100 * MBYTE);

		/*
		 * Transfer granularity 10 KByte (mit HPROF)
		 * simulation finished: 215250 events
		 * realtime: 782.785s
		 * events/sec: 274.9797
		 * virtual time: 2.457622613s
		 * virtualTime/realTime: 0.0031395882815843427
		 *
		 *
		 * Transfer granularity 1 KByte:
		 * simulation finished: 2150631 events
		 * realtime: 75.397s
		 * events/sec: 28524.092
		 * virtual time: 2.457362259s
		 * virtualTime/realTime: 0.03259230816876003
		 */

		pB.addBarrier(world);

		pB.addReadSequential(2, file, 0, 100 * MBYTE);

		return aB.getApplication();
	}

	/**
	 * One might change the method to invoke...
	 *
	 * @return
	 */
	public GlobalSettings getGlobalSettings() {
		return model.getGlobalSettings();
	}

	public static void main(String[] args) throws Exception {
		ClusterTest t = new ClusterTest();
		t.app = t.createSetSimpleApplication();
		t.runSimulationAllExpectedToFinish();
	}
}

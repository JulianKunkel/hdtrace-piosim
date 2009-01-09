
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

import org.junit.After;

import de.hd.pvs.piosim.model.Model;
import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.ModelSortIDbySubcomponents;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.Connection.Connection;
import de.hd.pvs.piosim.model.components.IOSubsystem.RefinedDiskModel;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.components.Port.Port;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationCache;
import de.hd.pvs.piosim.model.components.Switch.SimpleSwitch;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;
import de.hd.pvs.piosim.model.program.Application;
import de.hd.pvs.piosim.model.program.ApplicationBuilder;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.ProgramBuilder;
import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.piosim.simulator.RunParameters;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
public class ClusterTest {
	final long KBYTE = 1024;
	final long MBYTE = 1024*KBYTE;
	
	protected ModelBuilder mb;		
	protected ApplicationBuilder aB;
	protected ProgramBuilder pb;
	protected Application app;
	protected Model model;
	protected Simulator sim;
	protected Communicator world;
	
	@After public void tearDown(){
		System.out.println();
		Assert.assertTrue(true); /* to ensure assert stays */
	}

	protected void testMsg(){
		System.err.println(new Exception().getStackTrace()[1]);		
	}

	protected void setup(int clients, int servers) throws Exception{
		mb = createDisjointClusterModel(clients, servers);		
		aB = new ApplicationBuilder("Jacobi", "Example Jacobi", clients);
		app = aB.getApplication();

		pb = new ProgramBuilder(aB);
		
		world = aB.getWorldCommunicator();
		
		model = mb.getModel();		
	}
		
	protected ModelBuilder createDisjointClusterModel(int clients, int servers) throws Exception {

		int nodeCount = clients + servers;
		
		ModelBuilder mb = new ModelBuilder();
					
		Connection conn = new Connection();
		conn.setName("1GBit Ethernet");
		conn.setLatency(new Epoch(0.0002));
		conn.setBandwidth(100 * MBYTE);
		
		mb.getModel().getGlobalSettings().setTransferGranularity(100 * KBYTE);
		
		mb.addTemplate(conn);
				
		Node node = new Node();
		node.setName("PVS-Node");
		//maschine.setCacheSize(1024*1024*1024);
		node.setCPUs(1);
		node.setInstructionsPerSecond(1000000);
		node.setInternalDataTransferSpeed(1000 * MBYTE);
		node.setMemorySize(1000*1024*1024);
		
		//SimpleDisk iosub = new SimpleDisk();
		
		RefinedDiskModel iosub = new RefinedDiskModel();
		iosub.setAverageSeekTime(new Epoch(0.005));
		iosub.setTrackToTrackSeekTime(new Epoch(0.001));
		iosub.setRPM(7200);
		iosub.setPositionDifferenceConsideredToBeClose(MBYTE * 5);
		iosub.setSequentialTransferRate(((int) 50 * MBYTE));
				
		iosub.setName("IBM");
		//iosub.setAvgAccessTime(new Epoch(0.002));
		//iosub.setMaxThroughput(50 * MBYTE);
				
		mb.addTemplate(iosub);
		
		NIC nic = new NIC();
		//nic.setAttachedMaschine(maschine);
		nic.setConnection(conn);
		
		mb.addNIC(node, nic);
		
		mb.addTemplate(node);
		
		SimpleSwitch sw = new SimpleSwitch();
		sw.setName("PVS-Switch");
		sw.setTotalBandwidth(380 * MBYTE);
		
		Port  port = new Port();
		//port.setParentSwitch(sw);
		port.setConnection(conn);
		for(int i=0; i <= nodeCount; i++)
			mb.addPort(sw, port);
			
		mb.addTemplate(sw);
		
		//System.out.println(sw);
		///// NOW BUILD OBJECTS BASED ON PREVIOUS SETUP...
		
		SimpleSwitch testSW = mb.cloneFromTemplate(sw);		
		ArrayList<Node> nodes = new ArrayList<Node>();

		mb.addSwitch(testSW);
				
		for (int i=0; i < nodeCount; i++){
			Node node2 = mb.cloneFromTemplate(node);
			nodes.add(node2);
			
			mb.addNode(node2);
			
			mb.setConnection(node2.getNICs().get(0), testSW.getPorts().get(i));
		}
		
		SimpleSwitch testSW2 = mb.cloneFromTemplate(sw);
		mb.addSwitch(testSW2);
		mb.setConnection(testSW.getPorts().get(nodeCount), testSW2.getPorts().get(nodeCount));
				
		for(int i=0; i < clients; i++ ){
			ClientProcess c = new ClientProcess();
			c.setName("Client" + i);
			mb.addClient(nodes.get(i), c);
			c.setRank(i);
			c.setApplication("Jacobi");
		}
		
		Server serverTemplate = new Server();
		serverTemplate.setName("Server");
		serverTemplate.setIOsubsystem(iosub);
		
		AggregationCache cacheImpl = new AggregationCache(); //NoCache()
		cacheImpl.setReadDataSievingMaxHoleSizeToCombine(10 * (int) MBYTE);
		cacheImpl.setMaxNumberOfConcurrentIOOps(1);		
		serverTemplate.setCacheImplementation(cacheImpl);
		
		
		mb.addTemplate(serverTemplate);
		
		
		for(int i=0; i < servers; i++ ){
			Server s = mb.cloneFromTemplate(serverTemplate);
			// disjoint client and server processes
			mb.addServer(nodes.get(i + clients), s);
		}
		
		return mb;
	}
	
	protected  void runSimulationAllExpectedToFinish() throws Exception{
		mb.setApplication("Jacobi", app);
		
		ModelSortIDbySubcomponents sorter = new ModelSortIDbySubcomponents();
		sorter.sort(model);
		
		//ModelXMLWriter xmlW = new ModelXMLWriter();
		//xmlW.writeXMLFromProject(model, "/tmp/", "test.xml");		
		// TESTING
		RunParameters parameters = new RunParameters();
		parameters.setLoggerDefinitionFile("loggerDefinitionFiles/example");		
		
		//parameters.setTraceInternals(false);		
		
		sim = new Simulator();			
		sim.simulate(model, parameters);
		
		for(ClientProcess c: model.getClientProcesses()){
			GClientProcess gcp =  (GClientProcess) sim.getSimulatedComponent(c);
			if(! gcp.isProgramFinished()){
				throw new Exception("Client " + c.getIdentifier() + " not finished");
			}
		}
	}
	
	

	public Application createSetSimpleApplication() throws Exception{
	// Start to create program:
		
		ApplicationBuilder aB = new ApplicationBuilder("Jacobi", "Example Jacobi", 4);
		ProgramBuilder pB = new ProgramBuilder(aB);
		Communicator world = aB.getWorldCommunicator();
		
		model.getGlobalSettings().setTransferGranularity(100*KBYTE);
		
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
		 simulation finished: 215250 events	
	 realtime: 782.785s
	 events/sec: 274.9797
	 virtual time: 2.457622613s
	 virtualTime/realTime: 0.0031395882815843427
		 * 
		 * 
	 Transfer granularity 1 KByte:
	 simulation finished: 2150631 events
	 realtime: 75.397s
	 events/sec: 28524.092
	 virtual time: 2.457362259s
	 virtualTime/realTime: 0.03259230816876003
		 */
		
		pB.addBarrier(world);		
		pB.addReadSequential(2, file, 0, 100 * MBYTE);
		
		return aB.getApplication();
	}
	
	public static void main(String[] args) throws Exception{
		ClusterTest t = new ClusterTest();
		t.setup(5, 3);
		t.app = t.createSetSimpleApplication();
		t.runSimulationAllExpectedToFinish();
	}
}
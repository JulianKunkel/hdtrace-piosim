
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.ModelSortIDbySubcomponents;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.IOSubsystem.RefinedDiskModel;
import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.NoCache;
import de.hd.pvs.piosim.model.program.Application;
import de.hd.pvs.piosim.model.program.ApplicationXMLReader;
import de.hd.pvs.piosim.simulator.RunParameters;
import de.hd.pvs.piosim.simulator.SimulationResults;
import de.hd.pvs.piosim.simulator.Simulator;
public class JacobiTest {
	final long KBYTE = 1024;
	final long MBYTE = 1024*KBYTE;

	public boolean shouldSortModel = false;

	@Before public void setUp() {
	}

	@After public void tearDown(){
		System.out.println();
		Assert.assertTrue(true); /* to ensure assert stays */
	}

	// Jacobi program - traced
	@Test public void Test0S_1C_LAT_Jacobi() throws Exception{
		runJacobi("1-lat");
	}

	@Test public void Test0S_2C_LAT_Jacobi() throws Exception{
		runJacobi("2-lat");
	}

	@Test public void Test0S_3C_LAT_Jacobi() throws Exception{
		runJacobi("3-lat");
	}

	@Test public void Test0S_4C_LAT_Jacobi() throws Exception{
		runJacobi("4-lat");
	}

	@Test public void Test0S_5C_LAT_Jacobi() throws Exception{
		runJacobi("5-lat");
	}

	@Test public void Test0S_6C_LAT_Jacobi() throws Exception{
		runJacobi("6-lat");
	}

	@Test public void Test0S_7C_LAT_Jacobi() throws Exception{
		runJacobi("7-lat");
	}

	@Test public void Test0S_8C_LAT_Jacobi() throws Exception{
		runJacobi("8-lat");
	}

	@Test public void Test0S_9C_LAT_Jacobi() throws Exception{
		runJacobi("9-lat");
	}


	@Test public void Test0S_9C_LATCORE_Jacobi() throws Exception{
		runJacobi("9-lat-core");
	}

	@Test public void Test0S_1C_Jacobi() throws Exception{
		runJacobi("1");
	}

	@Test public void Test0S_2C_Jacobi() throws Exception{
		runJacobi("2");
	}

	@Test public void Test0S_3C_Jacobi() throws Exception{
		runJacobi("3");
	}

	@Test public void Test0S_4C_Jacobi() throws Exception{
		runJacobi("4");
	}

	@Test public void Test0S_5C_Jacobi() throws Exception{
		runJacobi("5");
	}

	@Test public void Test0S_6C_Jacobi() throws Exception{
		runJacobi("6");
	}

	@Test public void Test0S_7C_Jacobi() throws Exception{
		runJacobi("7");
	}

	@Test public void Test0S_8C_Jacobi() throws Exception{
		runJacobi("8");
	}

	@Test public void Test0S_9C_Jacobi() throws Exception{
		runJacobi("9");
	}

	public double runJacobi(String which) throws Exception{
		ModelBuilder mb = createDisjointClusterModel(10, 0);

		ApplicationXMLReader axml = new ApplicationXMLReader();
		Application app = axml.parseApplication("Examples/PDE/" + which + "/result.xml", false);
		mb.setApplication("Jacobi", app);

		RunParameters params = new RunParameters();
		//params.setDebugEverything(true);

		// test the sorter..
		if (shouldSortModel){
			ModelSortIDbySubcomponents sorter = new ModelSortIDbySubcomponents();
			sorter.sort(mb.getModel());
		}


		Simulator sim =  new Simulator();
		SimulationResults results = sim.simulate(mb.getModel(), params);

		if(results.getEventCount() == 0) {
			System.err.println("Nothing happened! Model:");
			System.err.println(sim.getModel());
		}

		return sim.getVirtualTime().getDouble();
	}


	public ModelBuilder createDisjointClusterModel(int clients, int servers) throws Exception {

		int nodeCount = clients + servers;

		ModelBuilder mb = new ModelBuilder();

		// set global settings disjoint from default configuration:
		mb.getGlobalSettings().setTransferGranularity(100 * KBYTE);

		// build templates
		Connection conn = new Connection();
		conn.setName("1GBit Ethernet");
		conn.setLatency(new Epoch(0.0002));
		conn.setBandwidth(100 * MBYTE);
		mb.addTemplate(conn);

		Node node = new Node();
		node.setName("PVS-Node");
		node.setCPUs(1);
		node.setInstructionsPerSecond(1000000);
		node.setInternalDataTransferSpeed(1000 * MBYTE);
		node.setMemorySize(1000*1024*1024);

		NIC nic = new NIC();
		nic.setConnection(conn);

		mb.addNIC(node, nic);
		mb.addTemplate(node);

		RefinedDiskModel iosub = new RefinedDiskModel();
		iosub.setAverageSeekTime(new Epoch(0.005));
		iosub.setTrackToTrackSeekTime(new Epoch(0.001));
		iosub.setRPM(7200);
		iosub.setPositionDifferenceConsideredToBeClose(MBYTE * 5);
		iosub.setSequentialTransferRate(((int) 50 * MBYTE));
		iosub.setName("IBM");

		mb.addTemplate(iosub);

		SimpleSwitch sw = new SimpleSwitch();
		sw.setName("PVS-Switch");
		sw.setTotalBandwidth(380 * MBYTE);

		Port  port = new Port();
		port.setConnection(conn);
		for(int i=0; i <= nodeCount; i++)
			mb.addPort(sw, port);

		mb.addTemplate(sw);

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

		// example showing howto link two switches together:
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

			NoCache cacheImpl = new AggregationCache(); //NoCache()
			cacheImpl.setMaxNumberOfConcurrentIOOps(1);
			serverTemplate.setCacheImplementation(cacheImpl);

			mb.addTemplate(serverTemplate);

			for(int i=0; i < servers; i++ ){
				Server s = mb.cloneFromTemplate(serverTemplate);
				// disjoint client and server processes
				mb.addServer(nodes.get(i + clients), s);
			}

			//// Example howto change settings on all components using a particular template
			//conn.setName("10_GBit Ethernet");
			//conn.setLatency(new Epoch(0.00001));
			//conn.setBandwidth(1000 * MBYTE);
			//mb.modifyTemplateAndDerivedObjects("1GBit Ethernet", conn);

			return mb;
	}

	public static void main(String[] args) throws Exception{
		JacobiTest t = new JacobiTest();

		t.shouldSortModel = true;
		t.Test0S_1C_Jacobi();
		System.exit(1);

		int cnt = 10;

		double [] runTimes = new double[cnt];
		double [] realRunTimes = new double[]{47.302645, 24.787603, 17.296363, 13.535730, 11.563651,
				10.094314, 9.160997, 8.388671,7.999543};

		for(int i=1; i < 10; i++){
			runTimes[i-1] = t.runJacobi("" + i);
		}

		System.out.println("Runtimes: ");

		for(int i=1; i < 10; i++){
			System.out.println(i + " sim: " + runTimes[i-1] + " real: " + realRunTimes[i-1] +  " % " +
					runTimes[i-1] / realRunTimes[i-1]);
		}

//   27.12.2008:
//		1 sim: 47.352798999 real: 47.302645 % 1.0010602789548027
//		2 sim: 24.92842302 real: 24.787603 % 1.0056810664589069
//		3 sim: 17.537108536 real: 17.296363 % 1.0139188531138021
//		4 sim: 13.745648113 real: 13.53573 % 1.0155084441696163
//		5 sim: 11.818808236 real: 11.563651 % 1.0220654563165215
//		6 sim: 10.332705942 real: 10.094314 % 1.0236164579385978
//		7 sim: 9.438248883 real: 9.160997 % 1.0302643787570283
//		8 sim: 8.729002096 real: 8.388671 % 1.0405703234755541
//		9 sim: 8.262118268 real: 7.999543 % 1.0328237835586358

//    Real values:
//		Examples/PDE/1/result.txt:[TRACER][0] Runtime 47.302645s
//		Examples/PDE/2/result.txt:[TRACER][0] Runtime 24.787603s
//		Examples/PDE/3/result.txt:[TRACER][0] Runtime 17.296363s
//		Examples/PDE/4/result.txt:[TRACER][0] Runtime 13.535730s
//		Examples/PDE/5/result.txt:[TRACER][0] Runtime 11.563651s
//		Examples/PDE/6/result.txt:[TRACER][0] Runtime 10.094314s
//		Examples/PDE/7/result.txt:[TRACER][0] Runtime 9.160997s
//		Examples/PDE/8/result.txt:[TRACER][0] Runtime 8.388671s
//		Examples/PDE/9/result.txt:[TRACER][0] Runtime 7.999543s

//
//		Tests für 9 clients:
//			100 k transfer size:
//
//				RootComputes Allreduce:    0.254553231s
//				Sync Allreduce:                   0.244217933s
//				Dummy Allreduce:               0.116453979s
//
//			Normales Allreduce
//			Eager size 0:
//				 RootComputes Allreduce:    0.689321108s
//
//			mit 1 k transfer size
//				0.298921546s
//
//			mit 100 Byte transfer size: 571627 events
//			     realtime: 118.779s
//				 virtual time: 0.293911152s
//
//			Mit 0.025 ms latency (connection + port)
//			 0.204429901s
//
//			Mit 0.0 ms latency
//			 0.154329901s
//
//			Mit 0.1 ms latency => 0.1 ms latency = 0.2 sekunden
//			0.354849891s
//
//			461 recvs bei Rank 0
//			100 allreduces = 0.01 sekunden => 0.06 sekunden gesamt
		//System.out.println(Simulator.getInstance().getModel());
	}
}

package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network;

import java.util.ArrayList;

import junit.framework.TestSuite;

import org.junit.Test;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.networkTopology.INetworkEdge;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.simulator.RunParameters;
import de.hd.pvs.piosim.simulator.SimulationResults;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.network.GNetworkTopology;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.hardware.BasicHardwareSetup;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.hardware.TestHardwareSetup;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution.TestExecution;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution.oneSendFromTwoNic;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution.oneToTwoNic;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution.twoSendsFromOneNic;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution.twoToOneNic;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.topology.TestTopology;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.topology.test3x3Grid;

public class NetworkRoutingTest extends TestSuite{
	final long KBYTE = 1000;
	final long MBYTE = 1000 * 1000;

	private Simulator sim;
	final RunParameters runParameters = new RunParameters();

	public NetworkRoutingTest() {
		runParameters.setTraceInternals(true);
		runParameters.setTraceEnabled(false);
	}

	public RunParameters getRunParameters() {
		return runParameters;
	}

	public void runTestFor(TestHardwareSetup setup, TestTopology topology, TestExecution execute) throws Exception{

		final ModelBuilder mb = new ModelBuilder();

		mb.getModel().getGlobalSettings().setTransferGranularity(100 * KBYTE);

		final INetworkEntry entryNode = setup.createNetworkEntry();
		final INetworkExit exitNode = setup.createNetworkExit();
		final INetworkEdge myEdge = setup.createEdge();
		final INetworkNode node = setup.createNetworkNode();

		mb.addTemplate(myEdge);
		mb.addTemplate(node);
		mb.addTemplate(exitNode);
		mb.addTemplate(entryNode);

		final ArrayList<INetworkEntry> entries = new ArrayList<INetworkEntry>();
		final ArrayList<INetworkExit> exits = new ArrayList<INetworkExit>();


		topology.createTopology(entries, exits, entryNode, exitNode, node, myEdge, mb);

		sim = new Simulator();

		sim.initModel(mb.getModel(), runParameters);
		execute.preSimulation(sim, entries, exits);

		SimulationResults results = sim.simulate();

		// print bandwidth ?
		// System.out.println("Bandwidth: " + rcvd / sim.getVirtualTime().getDouble() / 1000 / 1000 + " MB/s");

		execute.postSimulation(sim, results, entries, exits);
	}

	public void printRouting(){
		// now print the routing tables.
		System.out.println("Routing tables are as follows:");

		for(GNetworkTopology topo: sim.getExistingTopologies()){
			System.out.println("Topo: " + topo.getName());
			System.out.println(topo.getRouting());
		}
	}


	@Test
	public void crossSend() throws Exception{
		runTestFor(new BasicHardwareSetup(), new test3x3Grid(), new oneSendFromTwoNic());
	}

	@Test
	public void twoToOneTarget() throws Exception{
		runParameters.setTraceInternals(true);

		runTestFor(new BasicHardwareSetup(), new test3x3Grid(), new twoToOneNic());
	}

	@Test
	public void oneToTwoTargets() throws Exception{
		runParameters.setTraceEnabled(true);

		runTestFor(new BasicHardwareSetup(), new test3x3Grid(), new oneToTwoNic());
	}

	@Test
	public void twoSends() throws Exception{
		runTestFor(new BasicHardwareSetup(), new test3x3Grid(), new twoSendsFromOneNic());
	}

	public static void main(String[] args) throws Exception {
		NetworkRoutingTest test = new NetworkRoutingTest();
	}
}

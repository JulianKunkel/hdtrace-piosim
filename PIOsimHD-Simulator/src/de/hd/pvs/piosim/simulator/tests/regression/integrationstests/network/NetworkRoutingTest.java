package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network;

import java.util.ArrayList;

import junit.framework.TestSuite;

import org.junit.Test;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.networkTopology.INetworkEdge;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketFirstRoute;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoundRobinRoute;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoutingAlgorithm;
import de.hd.pvs.piosim.simulator.RunParameters;
import de.hd.pvs.piosim.simulator.SimulationResults;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.network.GNetworkTopology;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.hardware.BasicHardwareSetup;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.hardware.HardwareCutThroughNetwork;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.hardware.HardwareHighLatency;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.hardware.HardwareNICs;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.hardware.TestHardwareSetup;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution.NICPartialRecv;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution.NICTwoToOne;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution.NICTwoToTwoCross;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution.TestExecution;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution.oneSendFromTwoNic;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution.oneToTwoNodes;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution.twoSendsFromOneNic;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution.twoToOneNic;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.topology.TestTopology;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.topology.testGrid;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.topology.testSwitchingTopology;

public class NetworkRoutingTest extends TestSuite{
	final long KBYTE = 1000;
	final long MBYTE = 1000 * 1000;

	private Simulator sim;
	private final RunParameters runParameters = new RunParameters();
	private PaketRoutingAlgorithm routing = null;

	public NetworkRoutingTest() {
		runParameters.setTraceInternals(true);
		runParameters.setTraceEnabled(false);
	}

	public RunParameters getRunParameters() {
		return runParameters;
	}

	public void setRoutingAlgorithm(PaketRoutingAlgorithm routing) {
		this.routing = routing;
	}

	public void runTestFor(TestHardwareSetup setup, TestTopology topology, TestExecution execute) throws Exception{
		if(routing == null){
			routing = new PaketFirstRoute();
		}
		final ModelBuilder mb = new ModelBuilder();

		mb.getModel().getGlobalSettings().setTransferGranularity(100 * KBYTE);

		final INetworkEntry entryNode = setup.createNetworkEntry();
		final INetworkExit exitNode = setup.createNetworkExit();
		final INetworkEdge myEdge = setup.createEdge();
		final INetworkNode node = setup.createNetworkNode();

		myEdge.setName("Edge");
		node.setName("Node");
		exitNode.setName("Exit");
		entryNode.setName("Entry");

		mb.addTemplate(myEdge);
		mb.addTemplate(node);
		mb.addTemplate(exitNode);
		mb.addTemplate(entryNode);

		final ArrayList<INetworkEntry> entries = new ArrayList<INetworkEntry>();
		final ArrayList<INetworkExit> exits = new ArrayList<INetworkExit>();


		topology.createTopology(entries, exits, entryNode, exitNode, node, myEdge, mb, routing);

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
	public void crossSendHighLatency() throws Exception{
		runParameters.setTraceEnabled(true);

		runTestFor(new HardwareHighLatency(), new testGrid(), new oneSendFromTwoNic());
	}

	@Test
	public void sendHighLatency() throws Exception{
		runParameters.setTraceEnabled(true);

		runTestFor(new HardwareHighLatency(), new testGrid(), new twoSendsFromOneNic());
	}

	@Test
	public void crossSend() throws Exception{
		runTestFor(new BasicHardwareSetup(), new testGrid(), new oneSendFromTwoNic());
	}

	@Test
	public void crossSendRoungRobinRoute() throws Exception{

		runParameters.setTraceEnabled(true);

		setRoutingAlgorithm(new PaketRoundRobinRoute());
		runTestFor(new BasicHardwareSetup(), new testGrid(), new oneSendFromTwoNic());
		//printRouting();
	}

	@Test
	public void NICPartialRecvTest() throws Exception{
		runTestFor(new HardwareNICs(), new testSwitchingTopology(), new NICPartialRecv());
	}

	@Test
	public void NICTWOToOneTest() throws Exception{
		runTestFor(new HardwareNICs(), new testSwitchingTopology(), new NICTwoToOne());
	}

	@Test
	public void NICTWOToTwoCrossTest() throws Exception{
		runTestFor(new HardwareNICs(), new testSwitchingTopology(), new NICTwoToTwoCross());
	}

	@Test
	public void twoToOneTarget() throws Exception{
		runTestFor(new BasicHardwareSetup(), new testGrid(), new twoToOneNic());
	}

	@Test
	public void oneToTwoTargetsGrid() throws Exception{
		runParameters.setTraceEnabled(true);

		runTestFor(new BasicHardwareSetup(), new testGrid(), new oneToTwoNodes());
	}

	@Test
	public void twoToOneTarge1x4() throws Exception{
		final testGrid grid = new testGrid();
		grid.setGrid(1, 4);

		runTestFor(new BasicHardwareSetup(), grid, new twoToOneNic());
	}

	@Test
	public void twoToOneTargetSwitch() throws Exception{
		runTestFor(new BasicHardwareSetup(), new testSwitchingTopology(), new twoToOneNic());
	}

	@Test
	public void twoToOneTargetSwitchCutThrough() throws Exception{
		runTestFor(new HardwareCutThroughNetwork(), new testSwitchingTopology(), new twoToOneNic());
	}

	@Test
	public void oneToTwoTargetSwitchCutThrough() throws Exception{
		runParameters.setTraceEnabled(true);
		runTestFor(new HardwareCutThroughNetwork(), new testSwitchingTopology(), new oneToTwoNodes());
	}

	@Test
	public void twoSends() throws Exception{
		runTestFor(new BasicHardwareSetup(), new testGrid(), new twoSendsFromOneNic());
	}

	public static void main(String[] args) throws Exception {
		NetworkRoutingTest test = new NetworkRoutingTest();
	}
}

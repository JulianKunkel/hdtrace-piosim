/**
 *
 */
package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution;

import java.util.ArrayList;

import junit.framework.TestCase;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.simulator.SimulationResults;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.components.NetworkNode.GStoreAndForwardExitNode;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkEntry;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkNode;
import de.hd.pvs.piosim.simulator.network.Message;

/**
 * first start => last exit, last start => first exit
 * @author julian
 *
 */
public class oneSendFromTwoNic extends TestCase implements TestExecution{
	GStoreAndForwardExitNode exitGNode;
	GStoreAndForwardExitNode exitGNode2;

	final long SIZE = 1000 * 1000;

	@Override
	public void postSimulation(Simulator sim, SimulationResults results,
			ArrayList<INetworkEntry> entries, ArrayList<INetworkExit> exits)
			throws Exception
	{
		System.out.println("Bandwidth: " + (exitGNode.getRcvdData() + exitGNode2.getRcvdData() )/ sim.getVirtualTime().getDouble() / 1000 / 1000 + " MB/s");

		assertTrue(exitGNode.getRcvdData() + exitGNode2.getRcvdData() == SIZE * 2);
	}

	@Override
	public void preSimulation(Simulator sim,
			ArrayList<INetworkEntry> entries, ArrayList<INetworkExit> exits)
			throws Exception
	{
		INetworkExit endNode = exits.get(0);
		exitGNode = (GStoreAndForwardExitNode) sim.getSimulatedComponent(endNode);

		IGNetworkEntry startNode = (IGNetworkEntry) sim.getSimulatedComponent(entries.get(entries.size() - 1));
		Message msg = new Message(SIZE, null, entries.get(entries.size() - 1), endNode);
		startNode.submitNewMessage(msg);
		System.out.println("from " + ((IGNetworkNode) startNode).getIdentifier() + " to " + exitGNode.getIdentifier());

		endNode = exits.get(exits.size() -1 );
		exitGNode2 = (GStoreAndForwardExitNode) sim.getSimulatedComponent(endNode);
		startNode = (IGNetworkEntry)  sim.getSimulatedComponent(entries.get(0));
		msg = new Message(SIZE, null, entries.get(0), endNode);
		startNode.submitNewMessage(msg);

		System.out.println("from " + ((IGNetworkNode) startNode).getIdentifier() + " to " + exitGNode2.getIdentifier());

		assert(exitGNode != exitGNode2);
		assert(entries.get(entries.size() - 1) != entries.get(0));
	}
}
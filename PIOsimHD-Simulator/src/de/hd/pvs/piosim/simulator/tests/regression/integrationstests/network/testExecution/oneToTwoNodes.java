/**
 *
 */
package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution;

import java.util.ArrayList;

import junit.framework.TestCase;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.simulator.SimulationResults;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.components.NetworkNode.GExitNode;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkEntry;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkNode;
import de.hd.pvs.piosim.simulator.network.Message;

/**
 * first start => last exit, first start => first exit
 * @author julian
 *
 */
public class oneToTwoNodes extends TestCase implements TestExecution{
	GExitNode exitGNode;
	GExitNode exitGNode2;

	final long SIZE = 1000 * 1000;
	final int COUNT = 2;

	@Override
	public void postSimulation(Simulator sim, SimulationResults results,
			ArrayList<INetworkEntry> entries, ArrayList<INetworkExit> exits)
			throws Exception
	{
		final long rcvddata = exitGNode.getRcvdData() + exitGNode2.getRcvdData();

		System.out.println("Bandwidth: " + (rcvddata )/ sim.getVirtualTime().getDouble() / 1000 / 1000 + " MB/s");

		assertTrue("Warning, " + rcvddata / 1000  + " KB rcvd, but it should be " + COUNT *SIZE / 1000 , rcvddata == (SIZE * COUNT));
	}

	@Override
	public void preSimulation(Simulator sim,
			ArrayList<INetworkEntry> entries, ArrayList<INetworkExit> exits)
			throws Exception
	{

		INetworkExit endNode = exits.get(0);
		exitGNode = (GExitNode) sim.getSimulatedComponent(endNode);

		final IGNetworkEntry startNode = (IGNetworkEntry) sim.getSimulatedComponent(entries.get(0));
		Message msg = new Message(SIZE, null, entries.get(0), endNode);
		startNode.submitNewMessage(msg, Epoch.ZERO);

		endNode = exits.get(exits.size() -1 );

		System.out.println("from " + ((IGNetworkNode) startNode).getIdentifier() + " to " + exitGNode.getIdentifier());

		exitGNode2 = (GExitNode) sim.getSimulatedComponent(endNode);
		msg = new Message(SIZE, null, entries.get(0), endNode);
		startNode.submitNewMessage(msg, Epoch.ZERO);

		System.out.println("from " + ((IGNetworkNode) startNode).getIdentifier() + " to " + exitGNode2.getIdentifier());

		assert(exitGNode != exitGNode2);
	}
}
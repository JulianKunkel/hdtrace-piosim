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
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkEntry;
import de.hd.pvs.piosim.simulator.network.Message;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.GStoreAndForwardExitNode;

/**
 * first start => last exit, first start => first exit
 * @author julian
 *
 */
public class oneToTwoNic extends TestCase implements TestExecution{
	GStoreAndForwardExitNode exitGNode;
	GStoreAndForwardExitNode exitGNode2;

	final long SIZE = 1000 * 1000;

	@Override
	public void postSimulation(Simulator sim, SimulationResults results,
			ArrayList<INetworkEntry> entries, ArrayList<INetworkExit> exits)
			throws Exception
	{
		assertTrue(exitGNode.getRcvdData() + exitGNode2.getRcvdData() == SIZE * 2);
	}

	@Override
	public void preSimulation(Simulator sim,
			ArrayList<INetworkEntry> entries, ArrayList<INetworkExit> exits)
			throws Exception
	{
		INetworkExit endNode = exits.get(0);
		exitGNode = (GStoreAndForwardExitNode) sim.getSimulatedComponent(endNode);

		IGNetworkEntry startNode = (IGNetworkEntry) sim.getSimulatedComponent(entries.get(0));
		Message msg = new Message(SIZE, null, entries.get(0), endNode);
		startNode.submitNewMessage(msg);

		endNode = exits.get(exits.size() -1 );
		exitGNode2 = (GStoreAndForwardExitNode) sim.getSimulatedComponent(endNode);
		msg = new Message(SIZE, null, entries.get(0), endNode);
		startNode.submitNewMessage(msg);
	}
}
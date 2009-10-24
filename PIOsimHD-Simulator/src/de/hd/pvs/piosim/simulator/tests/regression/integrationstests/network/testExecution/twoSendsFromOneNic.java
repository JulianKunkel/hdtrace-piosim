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
import de.hd.pvs.piosim.simulator.network.Message;

public class twoSendsFromOneNic extends TestCase implements TestExecution{
	GExitNode exitGNode;
	final long SIZE = 1000 * 1000;

	@Override
	public void postSimulation(Simulator sim, SimulationResults results,
			ArrayList<INetworkEntry> entries, ArrayList<INetworkExit> exits)
			throws Exception
	{
		System.out.println("Bandwidth: " + exitGNode.getRcvdData() / sim.getVirtualTime().getDouble() / 1000 / 1000  + " MB/s");

		assertTrue(exitGNode.getRcvdData() == SIZE * 2);
	}

	@Override
	public void preSimulation(Simulator sim,
			ArrayList<INetworkEntry> entries, ArrayList<INetworkExit> exits)
			throws Exception
	{
		// test some basic send & rcvs.
		final INetworkExit endNode = exits.get(0);
		exitGNode = (GExitNode) sim.getSimulatedComponent(endNode);
		IGNetworkEntry startNode = (IGNetworkEntry) sim.getSimulatedComponent(entries.get(0));
		Message msg = new Message(SIZE, null, entries.get(0), endNode);
		startNode.submitNewMessage(msg, Epoch.ZERO);

		startNode = (IGNetworkEntry)  sim.getSimulatedComponent(entries.get(0));
		msg = new Message(SIZE, null, entries.get(0), endNode);
		startNode.submitNewMessage(msg, Epoch.ZERO);
	}
}
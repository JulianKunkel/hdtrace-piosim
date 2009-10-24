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
 * first start => first exit, last start => first exit
 * @author julian
 *
 */
public class twoToOneNic extends TestCase implements TestExecution{
	GExitNode exitGNode;

	final long SIZE = 1000 * 1000;

	@Override
	public void postSimulation(Simulator sim, SimulationResults results,
			ArrayList<INetworkEntry> entries, ArrayList<INetworkExit> exits)
			throws Exception
	{
		System.out.println("Bandwidth: " + (exitGNode.getRcvdData()  )/ sim.getVirtualTime().getDouble() / 1000 / 1000 + " MB/s");

		assertTrue("Warning, only " + exitGNode.getRcvdData() + " bytes rcvd, but it should be " + 2 *SIZE , exitGNode.getRcvdData() == SIZE * 2);
	}

	@Override
	public void preSimulation(Simulator sim,
			ArrayList<INetworkEntry> entries, ArrayList<INetworkExit> exits)
			throws Exception
	{
		final INetworkExit endNode = exits.get(0);
		exitGNode = (GExitNode) sim.getSimulatedComponent(endNode);

		IGNetworkEntry startNode = (IGNetworkEntry) sim.getSimulatedComponent(entries.get(entries.size() - 1));
		Message msg = new Message(SIZE, null, entries.get(entries.size() - 1), endNode);
		startNode.submitNewMessage(msg, Epoch.ZERO);

		System.out.println("from " + ((IGNetworkNode) startNode).getIdentifier() + " to " + exitGNode.getIdentifier());

		startNode = (IGNetworkEntry)  sim.getSimulatedComponent(entries.get(0));
		msg = new Message(SIZE, null, entries.get(0), endNode);
		startNode.submitNewMessage(msg, Epoch.ZERO);

		System.out.println("and from " + ((IGNetworkNode) startNode).getIdentifier() + " to " + exitGNode.getIdentifier());
	}
}
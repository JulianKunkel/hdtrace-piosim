/**
 *
 */
package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution;

import java.util.ArrayList;

import junit.framework.TestCase;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.simulator.SimulationResults;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.components.NIC.GProcessNetworkInterface;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.components.NIC.MessageMatchingCriterion;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.HostDummy;

/**
 * Test Node NIC
 * first and second start => first exit :)
 * @author julian
 *
 */
public class NICTwoToOne extends TestCase implements TestExecution{
	final long SIZE = 1000 * 1000;
	protected HostDummy hostSrc1;
	protected HostDummy hostSrc2;
	protected HostDummy hostTgt;

	@Override
	public void postSimulation(Simulator sim, SimulationResults results,
			ArrayList<INetworkEntry> entries, ArrayList<INetworkExit> exits)
			throws Exception
	{
	}

	@Override
	public void preSimulation(Simulator sim,
			ArrayList<INetworkEntry> entries, ArrayList<INetworkExit> exits)
			throws Exception
	{
		hostSrc1 = new HostDummy((GProcessNetworkInterface) sim.getSimulatedComponent(entries.get(0)));
		hostSrc2 = new HostDummy((GProcessNetworkInterface) sim.getSimulatedComponent(entries.get(1)));
		hostTgt =  new HostDummy((GProcessNetworkInterface) sim.getSimulatedComponent(exits.get(0)));

		final Communicator comm = new Communicator("WORLD");
		final MessageMatchingCriterion crit1 = new MessageMatchingCriterion(hostSrc1.getModelComponent(), hostTgt.getModelComponent(), 1, comm);
		final InterProcessNetworkJob job1 = InterProcessNetworkJob.createSendOperation(crit1, new NetworkSimpleData(SIZE), hostTgt);


		final MessageMatchingCriterion crit2 = new MessageMatchingCriterion(hostSrc2.getModelComponent(), hostTgt.getModelComponent(), 1, comm);
		final InterProcessNetworkJob job2 = InterProcessNetworkJob.createSendOperation(crit2, new NetworkSimpleData(SIZE), hostTgt);


		hostSrc1.nic.initiateInterProcessSend(job1, Epoch.ZERO);
		hostSrc2.nic.initiateInterProcessSend(job2, Epoch.ZERO);

		System.out.println("from " + hostSrc1.getIdentifier() + " to " + hostTgt.getIdentifier());
		System.out.println("and from " + hostSrc2.getIdentifier() + " to " + hostTgt.getIdentifier());

		hostTgt.nic.initiateInterProcessReceive(InterProcessNetworkJob.createReceiveOperation(new MessageMatchingCriterion(hostSrc1.getModelComponent(), hostTgt.getModelComponent(), 1, comm), hostTgt), Epoch.ZERO);
	}
}
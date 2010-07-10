/**
 *
 */
package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution;

import java.util.ArrayList;

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
public class NICTwoToTwoCross extends NICTwoToOne{


	final long SIZE = 15*100 * 1000;
	protected HostDummy hostSrc1;
	protected HostDummy hostSrc2;
	protected HostDummy hostTgt1;
	protected HostDummy hostTgt2;

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
		hostTgt1 =  new HostDummy((GProcessNetworkInterface) sim.getSimulatedComponent(exits.get(0)));
		hostTgt2 =  new HostDummy((GProcessNetworkInterface) sim.getSimulatedComponent(exits.get(1)));

		final Communicator comm = new Communicator("WORLD");
		final MessageMatchingCriterion crit1 = new MessageMatchingCriterion(hostSrc1.getModelComponent(), hostTgt1.getModelComponent(), 1, comm, NetworkSimpleData.class);
		final InterProcessNetworkJob job1 = InterProcessNetworkJob.createSendOperation(crit1, new NetworkSimpleData(SIZE), hostTgt1);


		final MessageMatchingCriterion crit2 = new MessageMatchingCriterion(hostSrc2.getModelComponent(), hostTgt1.getModelComponent(), 1, comm, NetworkSimpleData.class);
		final InterProcessNetworkJob job2 = InterProcessNetworkJob.createSendOperation(crit2, new NetworkSimpleData(SIZE), hostTgt1);

		final MessageMatchingCriterion crit3 = new MessageMatchingCriterion(hostSrc1.getModelComponent(), hostTgt2.getModelComponent(), 1, comm, NetworkSimpleData.class);
		final InterProcessNetworkJob job3 = InterProcessNetworkJob.createSendOperation(crit3, new NetworkSimpleData(SIZE), hostTgt2);

		final MessageMatchingCriterion crit4 = new MessageMatchingCriterion(hostSrc2.getModelComponent(), hostTgt2.getModelComponent(), 1, comm, NetworkSimpleData.class);
		final InterProcessNetworkJob job4 = InterProcessNetworkJob.createSendOperation(crit4, new NetworkSimpleData(SIZE), hostTgt2);


		hostSrc1.nic.initiateInterProcessSend(job1, Epoch.ZERO);
		hostSrc2.nic.initiateInterProcessSend(job2, Epoch.ZERO);

		hostSrc1.nic.initiateInterProcessSend(job3, Epoch.ZERO);
		hostSrc2.nic.initiateInterProcessSend(job4, Epoch.ZERO);

		System.out.println("from " + hostSrc1.getIdentifier() + "and from " + hostSrc2.getIdentifier() + " to " +
				hostTgt1.getIdentifier() + " and to " + hostTgt2.getIdentifier());

		hostTgt1.nic.initiateInterProcessReceive(InterProcessNetworkJob.createReceiveOperation(new MessageMatchingCriterion(hostSrc1.getModelComponent(), hostTgt1.getModelComponent(), 1, comm, NetworkSimpleData.class), hostTgt1), Epoch.ZERO);
		hostTgt1.nic.initiateInterProcessReceive(InterProcessNetworkJob.createReceiveOperation(new MessageMatchingCriterion(hostSrc2.getModelComponent(), hostTgt1.getModelComponent(), 1, comm, NetworkSimpleData.class), hostTgt1), Epoch.ZERO);
		hostTgt2.nic.initiateInterProcessReceive(InterProcessNetworkJob.createReceiveOperation(new MessageMatchingCriterion(hostSrc1.getModelComponent(), hostTgt2.getModelComponent(), 1, comm, NetworkSimpleData.class), hostTgt2), Epoch.ZERO);
		hostTgt2.nic.initiateInterProcessReceive(InterProcessNetworkJob.createReceiveOperation(new MessageMatchingCriterion(hostSrc2.getModelComponent(), hostTgt2.getModelComponent(), 1, comm, NetworkSimpleData.class), hostTgt2), Epoch.ZERO);
	}
}
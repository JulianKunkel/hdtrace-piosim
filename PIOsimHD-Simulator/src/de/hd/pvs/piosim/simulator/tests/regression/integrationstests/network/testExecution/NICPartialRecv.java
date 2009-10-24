package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution;

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.components.NIC.IProcessNetworkInterface;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.components.NIC.MessageMatchingCriterion;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.HostDummy;

public class NICPartialRecv extends NICTwoToOne{
	@Override
	public void preSimulation(Simulator sim,
			ArrayList<INetworkEntry> entries, ArrayList<INetworkExit> exits)
			throws Exception
	{
		hostSrc1 = new HostDummy((IProcessNetworkInterface) sim.getSimulatedComponent(entries.get(0)));
		hostSrc2 = new HostDummy((IProcessNetworkInterface) sim.getSimulatedComponent(entries.get(1)));
		hostTgt =  new HostDummy((IProcessNetworkInterface) sim.getSimulatedComponent(exits.get(0)));

		final Communicator comm = new Communicator("WORLD");
		final MessageMatchingCriterion crit1 = new MessageMatchingCriterion(hostSrc1, hostTgt, 1, comm);
		final InterProcessNetworkJob job1 = InterProcessNetworkJob.createSendOperation(crit1, new NetworkSimpleData(SIZE), true);


		final MessageMatchingCriterion crit2 = new MessageMatchingCriterion(hostSrc2, hostTgt, 1, comm);
		final InterProcessNetworkJob job2 = InterProcessNetworkJob.createSendOperation(crit2, new NetworkSimpleData(SIZE), true);


		hostSrc1.getNetworkInterface().initiateInterProcessSend(job1, Epoch.ZERO);
		hostSrc2.getNetworkInterface().initiateInterProcessSend(job2, Epoch.ZERO);

		System.out.println("from " + hostSrc1.getIdentifier() + " to " + hostTgt.getIdentifier());
		System.out.println("and from " + hostSrc2.getIdentifier() + " to " + hostTgt.getIdentifier());

		hostTgt.getNetworkInterface().initiateInterProcessReceive(InterProcessNetworkJob.createReceiveOperation(new MessageMatchingCriterion(hostSrc1, hostTgt, 1, comm), true), Epoch.ZERO);
	}
}

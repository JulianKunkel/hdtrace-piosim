/**
 *
 */
package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network.testExecution;

import java.util.ArrayList;

import junit.framework.TestCase;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.superclasses.ComponentIdentifier;
import de.hd.pvs.piosim.model.networkTopology.INetworkEntry;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.simulator.SimulationResults;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.components.NIC.GNIC;
import de.hd.pvs.piosim.simulator.components.NIC.INetworkRessource;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.components.NIC.MessageMatchingCriterion;
import de.hd.pvs.piosim.simulator.components.Node.ComputeJob;
import de.hd.pvs.piosim.simulator.components.Node.INodeRessources;
import de.hd.pvs.piosim.simulator.components.Node.ISNodeHostedComponent;
import de.hd.pvs.piosim.simulator.network.MessagePart;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;

/**
 * Test Node NIC
 * first and second start => first exit :)
 * @author julian
 *
 */
public class NICTwoToOne extends TestCase implements TestExecution{
	protected static class HostDummy implements ISNodeHostedComponent{

		private GNIC nic;

		public HostDummy(GNIC nic) {
			this.nic = nic;
		}

		@Override
		public void computeJobCompletedCV(ComputeJob job) {
		}

		@Override
		public ComponentIdentifier getIdentifier() {
			return nic.getIdentifier();
		}

		@Override
		public INetworkRessource getNetworkInterface() {
			return nic;
		}

		@Override
		public boolean mayIReceiveMessagePart(MessagePart part,
				InterProcessNetworkJob job)
		{
			return true;
		}

		@Override
		public void messagePartReceivedCB(MessagePart part,
				InterProcessNetworkJob remoteJob,
				InterProcessNetworkJob announcedJob, Epoch endTime)
		{
			System.out.println("Msg part rcvd " + part.getSize());
		}

		@Override
		public void messagePartSendCB(MessagePart part,
				InterProcessNetworkJob myJob, Epoch endTime)
		{
			System.out.println("Msg part send " + part.getSize());
		}

		@Override
		public void recvCompletedCB(InterProcessNetworkJob remoteJob,
				InterProcessNetworkJob announcedJob, Epoch endTime)
		{
			System.out.println(getIdentifier() +  " recvd data from " + remoteJob.getMatchingCriterion().getSourceComponent().getIdentifier() + " tag: " + announcedJob.getMatchingCriterion().getTag());
		}

		@Override
		public void sendCompletedCB(InterProcessNetworkJob myJob, Epoch endTime) {
			System.out.println(getIdentifier() +  " send data to " + myJob.getMatchingCriterion().getSourceComponent().getIdentifier() + " tag: " + myJob.getMatchingCriterion().getTag());
		}

		@Override
		public INodeRessources getNodeRessources() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setNetworkInterface(INetworkRessource nic) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setNodeRessources(INodeRessources ressources) {
			// TODO Auto-generated method stub

		}
	}

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
		hostSrc1 = new HostDummy((GNIC) sim.getSimulatedComponent(entries.get(0)));
		hostSrc2 = new HostDummy((GNIC) sim.getSimulatedComponent(entries.get(1)));
		hostTgt =  new HostDummy((GNIC) sim.getSimulatedComponent(exits.get(0)));

		final Communicator comm = new Communicator("WORLD");
		final MessageMatchingCriterion crit1 = new MessageMatchingCriterion(hostSrc1, hostTgt, 1, comm);
		final InterProcessNetworkJob job1 = InterProcessNetworkJob.createSendOperation(crit1, new NetworkSimpleData(SIZE), false);


		final MessageMatchingCriterion crit2 = new MessageMatchingCriterion(hostSrc2, hostTgt, 1, comm);
		final InterProcessNetworkJob job2 = InterProcessNetworkJob.createSendOperation(crit2, new NetworkSimpleData(SIZE), false);


		hostSrc1.nic.initiateInterProcessSend(job1);
		hostSrc2.nic.initiateInterProcessSend(job2);

		System.out.println("from " + hostSrc1.getIdentifier() + " to " + hostTgt.getIdentifier());
		System.out.println("and from " + hostSrc2.getIdentifier() + " to " + hostTgt.getIdentifier());

		hostTgt.nic.initiateInterProcessReceive(InterProcessNetworkJob.createReceiveOperation(new MessageMatchingCriterion(hostSrc1, hostTgt, 1, comm), false));
	}
}
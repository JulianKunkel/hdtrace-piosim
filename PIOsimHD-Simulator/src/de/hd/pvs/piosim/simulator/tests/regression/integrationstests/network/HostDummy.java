/**
 *
 */
package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.superclasses.ComponentIdentifier;
import de.hd.pvs.piosim.simulator.components.NIC.IProcessNetworkInterface;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.components.Node.ComputeJob;
import de.hd.pvs.piosim.simulator.components.Node.INodeRessources;
import de.hd.pvs.piosim.simulator.components.Node.ISNodeHostedComponent;
import de.hd.pvs.piosim.simulator.network.MessagePart;

public class HostDummy implements ISNodeHostedComponent{

	public IProcessNetworkInterface nic;

	public HostDummy(IProcessNetworkInterface nic) {
		setNetworkInterface(nic);
	}

	@Override
	public void computeJobCompletedCV(ComputeJob job) {
	}

	@Override
	public ComponentIdentifier getIdentifier() {
		return nic.getModelComponent().getIdentifier();
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
	public void setNodeRessources(INodeRessources ressources) {
		// TODO Auto-generated method stub

	}

	@Override
	public IProcessNetworkInterface getNetworkInterface() {
		return nic;
	}

	@Override
	public void setNetworkInterface(IProcessNetworkInterface nic) {
		this.nic = nic;
	}
}
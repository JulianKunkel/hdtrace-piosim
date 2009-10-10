package de.hd.pvs.piosim.simulator.tests.regression.integrationstests.network;

import de.hd.pvs.piosim.simulator.components.NetworkNode.GCutThroughForwardNode;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkEntry;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkEntryCallbacks;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkExit;
import de.hd.pvs.piosim.simulator.components.NetworkNode.IGNetworkExitCallbacks;
import de.hd.pvs.piosim.simulator.network.MessagePart;

public class GCutThroughForwardNodeExit extends GCutThroughForwardNode<CutThroughForwardNodeExit>
	implements IGNetworkExit, IGNetworkEntry
{
	IGNetworkExitCallbacks networkExitI;

	@Override
	public void setNetworkExitImplementor(IGNetworkExitCallbacks networkExitI) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setNetworkEntryImplementor(
			IGNetworkEntryCallbacks networkEntryImplementor) {

	}

	@Override
	public void messagePartReceived(MessagePart part) {
		System.out.println(getSimulator().getVirtualTime() + " " + this.getIdentifier() + " recveived data: " + part.getSize());
	}



	@Override
	public boolean mayIReceiveAMessagePart(MessagePart part) {
		return true;
	}

}

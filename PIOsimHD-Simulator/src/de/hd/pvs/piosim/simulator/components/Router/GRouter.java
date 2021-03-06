package de.hd.pvs.piosim.simulator.components.Router;

import java.util.HashMap;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.Router.Router;
import de.hd.pvs.piosim.model.components.superclasses.INodeHostedComponent;
import de.hd.pvs.piosim.model.inputOutput.IORedirection;
import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.components.NIC.IProcessNetworkInterface;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJobCallbackAdaptor;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJobRoutable;
import de.hd.pvs.piosim.simulator.components.NIC.MessageMatchingCriterion;
import de.hd.pvs.piosim.simulator.components.Node.ComputeJob;
import de.hd.pvs.piosim.simulator.components.Node.INodeRessources;
import de.hd.pvs.piosim.simulator.inputOutput.IORedirectionHelper;
import de.hd.pvs.piosim.simulator.network.Message;
import de.hd.pvs.piosim.simulator.network.MessagePart;

/**
 * Forwards network packets.
 *
 *
 * @author julian
 */
public class GRouter extends SPassiveComponent<Router>
	implements IGRouter<SPassiveComponent<? extends Router>>
{
	private IProcessNetworkInterface networkInterface;

	private IORedirection  ioRedirection = null;

	// map the remote job to the forwarding job.
	final private HashMap<InterProcessNetworkJob, Message> sourceMap = new HashMap<InterProcessNetworkJob, Message>();

	final private InterProcessNetworkJobCallbackAdaptor callback = new InterProcessNetworkJobCallbackAdaptor(){

		public void messagePartReceivedCB(MessagePart part, InterProcessNetworkJob remoteJob, InterProcessNetworkJob announcedJob, Epoch endTime) {
			Message<InterProcessNetworkJobRoutable> forwardingMsg = sourceMap.get(remoteJob);

			if(forwardingMsg == null){

				// first packet of the given message => create mapped job:
				final MessageMatchingCriterion crit = remoteJob.getMatchingCriterion();
				final InterProcessNetworkJobRoutable request = (InterProcessNetworkJobRoutable) remoteJob;

				final INodeHostedComponent nextHop = IORedirectionHelper.getNextHopFor(request.getFinalTarget(), ioRedirection, getSimulator().getModel());

				final InterProcessNetworkJobRoutable newJob = InterProcessNetworkJobRoutable.createRoutableSendOperation(
						new MessageMatchingCriterion(getModelComponent(), nextHop,	crit.getTag() , crit.getCommunicator(), crit.getRootCommand(), crit.getCurrentCommand()),
								remoteJob.getJobData(), callback,
								request.getOriginalSource(), request.getFinalTarget(),
								remoteJob.getRelationToken()
								);

				//System.out.println("Forwarding from " + request.getOriginalSource() + " to " + request.getFinalTarget() + " via " + nextHop);

				forwardingMsg = new Message<InterProcessNetworkJobRoutable>(newJob.getSize(),
						newJob,
						getNetworkInterface().getModelComponent(),
						request.getFinalTarget().getNetworkInterface(), part.getMessage().getRelationToken());

				forwardingMsg.resetMessage();

				getNetworkInterface().initiateInterProcessSend(forwardingMsg, endTime);

				sourceMap.put(remoteJob, forwardingMsg);

				// receive another message:
				submitRecv();
			}

			getNetworkInterface().appendAvailableDataToIncompleteSend(forwardingMsg, part.getPayloadSize(), endTime);
		}

		public void recvCompletedCB(InterProcessNetworkJob remoteJob, InterProcessNetworkJob announcedJob, Epoch endTime) {
			sourceMap.remove(remoteJob);
		}
	};

	private void submitRecv(){
		networkInterface.initiateInterProcessReceive(
				InterProcessNetworkJob.createReceiveOperation(null, callback, null),
				getSimulator().getVirtualTime());
	}

	@Override
	public void simulationModelIsBuild() {
		// build routing table
		submitRecv();

		// now setup IORedirection Layer if applicable.
		ioRedirection = IORedirectionHelper.getIORedirectionLayerFor(getSimulator().getModel().getIORedirectionLayers(), this.getIdentifier().getID());
	}

	@Override
	public void setNetworkInterface(IProcessNetworkInterface nic) {
		this.networkInterface = nic;
	}

	@Override
	public IProcessNetworkInterface getNetworkInterface() {
		return networkInterface;
	}

	@Override
	public void computeJobCompletedCV(ComputeJob job) {

	}

	@Override
	public INodeRessources getNodeRessources() {
		return null;
	}

	@Override
	public void setNodeRessources(INodeRessources ressources) {

	}
}

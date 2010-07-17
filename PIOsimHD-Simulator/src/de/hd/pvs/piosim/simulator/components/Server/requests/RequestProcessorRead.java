package de.hd.pvs.piosim.simulator.components.Server.requests;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.components.NIC.IInterProcessNetworkJobCallback;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJobCallbackAdaptor;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJobRoutable;
import de.hd.pvs.piosim.simulator.components.NIC.MessageMatchingCriterion;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IServerCacheLayerJobCallback;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.ServerCacheLayerJobCallbackAdaptor;
import de.hd.pvs.piosim.simulator.network.Message;
import de.hd.pvs.piosim.simulator.network.MessagePart;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkIOData;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestIO;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestRead;

public class RequestProcessorRead
	extends RequestProcessor<RequestRead>
{
	private final IServerCacheLayerJobCallback<Message> ioCallback = new ServerCacheLayerJobCallbackAdaptor<Message>() {
		@Override
		public void IORequestPartiallyCompleted(RequestIO req, Message msg, Epoch time, long size) {
			server.getNetworkInterface().appendAvailableDataToIncompleteSend(msg, size, time);
		}
	};

	private final IInterProcessNetworkJobCallback dataCallback = new InterProcessNetworkJobCallbackAdaptor() {
		@Override
		public void messagePartSendCB(MessagePart part, InterProcessNetworkJob myJob, Epoch endTime) {
			server.getCacheLayer().readDataFragmentSendByNIC( (RequestRead) (
					(NetworkIOData) myJob.getJobData()).getIORequest(),
					part.getSize());
		}
	};

	@Override
	public void process(RequestRead req, InterProcessNetworkJobRoutable request, Epoch time) {
		final MessageMatchingCriterion reqCrit = request.getMatchingCriterion();

		assert(reqCrit.getSourceComponent() != server.getModelComponent());

		final InterProcessNetworkJobRoutable resp =  InterProcessNetworkJobRoutable.createRoutableSendOperation(
				new MessageMatchingCriterion(server.getModelComponent(),
						reqCrit.getSourceComponent(),
						reqCrit.getTag(),
						reqCrit.getCommunicator(), NetworkIOData.class),
						new NetworkIOData(req),
						dataCallback, server.getModelComponent(), request.getOriginalSource());

		final Message<InterProcessNetworkJobRoutable> msg = new Message<InterProcessNetworkJobRoutable>(
				resp.getSize(),
				resp,
				server.getNetworkInterface().getModelComponent(),
				resp.getMatchingCriterion().getTargetComponent().getNetworkInterface() );

		// wait for data to send:
		msg.setAvailableDataPosition(0);

		server.getNetworkInterface().initiateInterProcessSend(msg, time);
		server.getCacheLayer().announceIORequest( req, msg, ioCallback, time );
	}
}

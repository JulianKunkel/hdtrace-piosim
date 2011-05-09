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
import de.hd.pvs.piosim.simulator.network.jobs.requests.FileRequest;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestRead;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;
import de.hd.pvs.piosim.simulator.program.Fileread.FileReadDirect;

public class RequestProcessorRead
	extends RequestProcessor<RequestRead>
{

	private final IServerCacheLayerJobCallback<Message> ioCallback = new ServerCacheLayerJobCallbackAdaptor<Message>() {
		@Override
		public void ReadPartialData(Epoch time, FileRequest req, Message userdata, long size) {
			server.getNetworkInterface().appendAvailableDataToIncompleteSend(userdata, size, time);

			if(userdata.isAllMessageDataAvailable()){
				finishRequest(req);
			}
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

		startRequest(req, request);

		final CommandImplementation impl = getCommandImplementation(FileReadDirect.class);

		final InterProcessNetworkJobRoutable resp =  InterProcessNetworkJobRoutable.createRoutableSendOperation(
				new MessageMatchingCriterion(server.getModelComponent(),
						reqCrit.getSourceComponent(),
						reqCrit.getTag(),
						reqCrit.getCommunicator(),
						null, impl
						),
						new NetworkIOData(req),
						dataCallback, server.getModelComponent(), request.getOriginalSource(), request.getRelationToken());

		final Message<InterProcessNetworkJobRoutable> msg = new Message<InterProcessNetworkJobRoutable>(
				resp.getSize(),
				resp,
				server.getNetworkInterface().getModelComponent(),
				resp.getMatchingCriterion().getTargetComponent().getNetworkInterface(), request.getRelationToken() );

		// wait for data to send:
		msg.setAvailableDataPosition(0);

		server.getNetworkInterface().initiateInterProcessSend(msg, time);
		server.getCacheLayer().announceIORequest( req, msg, ioCallback, time );
	}
}

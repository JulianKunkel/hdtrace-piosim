package de.hd.pvs.piosim.simulator.components.Server.requests;

import java.util.LinkedList;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.simulator.components.NIC.IInterProcessNetworkJobCallback;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJobCallbackAdaptor;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJobRoutable;
import de.hd.pvs.piosim.simulator.components.NIC.MessageMatchingCriterion;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IServerCacheLayerJobCallback;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.ServerCacheLayerJobCallbackAdaptor;
import de.hd.pvs.piosim.simulator.network.MessagePart;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkIOData;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestIO;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestWrite;

/**
 * Process write requests
 * @author julian
 */
public class RequestProcessorWrite
extends RequestProcessor<RequestWrite>
{
	private final LinkedList<MessagePart> blockedReceives = new LinkedList<MessagePart>();

	private final IInterProcessNetworkJobCallback dataCallback = new InterProcessNetworkJobCallbackAdaptor() {
		@Override
		public void messagePartReceivedCB(MessagePart part, InterProcessNetworkJob remoteJob,
				InterProcessNetworkJob announcedJob, Epoch endTime)
		{
			//this function is called right now only for writes
			boolean doesFit = server.getCacheLayer().canIPutDataIntoCache( (RequestWrite) ((NetworkIOData) remoteJob.getJobData()).getIORequest(), part.getSize());

			if (doesFit){
				processWritePart( part, endTime);
			}else{
				blockedReceives.add(part);

				server.getNetworkInterface().blockFurtherDataReceives();
			}
		}
	};

	private final IServerCacheLayerJobCallback ioCallback = new ServerCacheLayerJobCallbackAdaptor() {

		@Override
		public void IORequestPartiallyCompleted(RequestIO req, Object data, Epoch time, long size) {
			if( blockedReceives.size() > 0 ){
				while( ! blockedReceives.isEmpty() ){

					MessagePart p = blockedReceives.get(0);

					if (! server.getCacheLayer().canIPutDataIntoCache((RequestWrite) ((NetworkIOData) ((InterProcessNetworkJob) p.getMessage().getContainedUserData()).getJobData())
							.getIORequest(), p.getSize())){
						break;
					}

					blockedReceives.remove(0);

					processWritePart(p, time);
				}

				if(blockedReceives.size() == 0){
					server.getNetworkInterface().unblockFurtherDataReceives();
				}
			}
		}
	};

	private void processWritePart(MessagePart part, Epoch endTime){
		InterProcessNetworkJobRoutable job = (InterProcessNetworkJobRoutable) part.getMessage().getContainedUserData();
		server.getCacheLayer().writeDataToCache((NetworkIOData) job.getJobData(),  job, part.getSize(), part.getMessage(), ioCallback);

		if(part.getMessage().isReceivedCompletely()){
			/*
			 * Send an acknowledge to the client.
			 */
			server.sendAcknowledgeToClient(job);
		}
	}

	@Override
	public void process(RequestWrite req, InterProcessNetworkJobRoutable request, Epoch time) {
		server.getCacheLayer().announceIORequest( req, null, ioCallback, time);

		final MessageMatchingCriterion reqCrit = request.getMatchingCriterion();
		/* post receive of further message parts as fragmented flow parts */
		final InterProcessNetworkJob resp =  InterProcessNetworkJob.createReceiveOperation(
				new MessageMatchingCriterion(
						reqCrit.getSourceComponent(), server.getModelComponent(),
						reqCrit.getTag(),
						reqCrit.getCommunicator(), NetworkIOData.class
						),
						dataCallback);

		server.getNetworkInterface().initiateInterProcessReceive(resp, time);
	}
}

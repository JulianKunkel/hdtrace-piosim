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
import de.hd.pvs.piosim.simulator.network.jobs.requests.FileRequest;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestIO;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestWrite;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;
import de.hd.pvs.piosim.simulator.program.Filewrite.FileWriteDirect;

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

			//System.out.println("messagePartReceivedCB  does fit " + doesFit + " " + part);

			if (doesFit){
				processWritePart( part, endTime, ((NetworkIOData) remoteJob.getJobData()).getIORequest() );
			}else{
				blockedReceives.add(part);

				server.getNetworkInterface().blockFurtherDataReceives();
			}
		}
	};

	private final IServerCacheLayerJobCallback ioCallback = new ServerCacheLayerJobCallbackAdaptor() {
		@Override
		public void WritePartialData(Epoch time, FileRequest req, Object userdata, long size) {
			//System.out.println("IORequestPartiallyCompleted " + req);

			if( blockedReceives.size() > 0 ){
				while( ! blockedReceives.isEmpty() ){

					MessagePart p = blockedReceives.get(0);

					NetworkIOData data = (NetworkIOData) ((InterProcessNetworkJob) p.getMessage().getContainedUserData()).getJobData();
					RequestWrite writeReq = (RequestWrite) data.getIORequest();

					if (! server.getCacheLayer().canIPutDataIntoCache(writeReq, p.getSize())){
						break;
					}

					blockedReceives.remove(0);

					processWritePart(p, time,  writeReq);
				}

				if(blockedReceives.size() == 0){
					server.getNetworkInterface().unblockFurtherDataReceives();
				}
			}
		}
	};

	private void processWritePart(MessagePart part, Epoch endTime,  RequestIO remoteJob){
		InterProcessNetworkJobRoutable job = (InterProcessNetworkJobRoutable) part.getMessage().getContainedUserData();

		server.getCacheLayer().writeDataToCache((NetworkIOData) job.getJobData(),  job, part.getSize(), part.getMessage(), ioCallback);

		if(part.getMessage().isReceivedCompletely()){
			/*
			 * Send an acknowledge to the client.
			 */
			server.sendAcknowledgeToClient(job);

			finishRequest(remoteJob);
		}
	}

	@Override
	public void process(RequestWrite req, InterProcessNetworkJobRoutable request, Epoch time) {
		server.getCacheLayer().announceIORequest( req, null, ioCallback, time);

		startRequest(req, request);

		final CommandImplementation impl = getCommandImplementation(FileWriteDirect.class);

		final MessageMatchingCriterion reqCrit = request.getMatchingCriterion();
		/* post receive of further message parts as fragmented flow parts */
		final InterProcessNetworkJob resp =  InterProcessNetworkJob.createReceiveOperation(
				new MessageMatchingCriterion(
						reqCrit.getSourceComponent(), server.getModelComponent(),
						reqCrit.getTag(),
						reqCrit.getCommunicator(),
						null, impl),
						dataCallback, request.getRelationToken());

		server.getNetworkInterface().initiateInterProcessReceive(resp, time);
	}
}

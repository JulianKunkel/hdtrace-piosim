package de.hd.pvs.piosim.simulator.components.Server.requests;

import java.util.HashMap;

import de.hd.pvs.TraceFormat.relation.RelationToken;
import de.hd.pvs.piosim.model.inputOutput.ListIO.SingleIOOperation;
import de.hd.pvs.piosim.simulator.base.ISPassiveComponent;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJobRoutable;
import de.hd.pvs.piosim.simulator.components.Server.IGRequestProcessingServerInterface;
import de.hd.pvs.piosim.simulator.components.Server.IServerRequestProcessor;
import de.hd.pvs.piosim.simulator.network.IMessageUserData;
import de.hd.pvs.piosim.simulator.network.jobs.requests.FileRequest;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestIO;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestRead;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestWrite;
import de.hd.pvs.piosim.simulator.output.STraceWriter;
import de.hd.pvs.piosim.simulator.output.STraceWriter.TraceType;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public abstract class RequestProcessor<Type extends IMessageUserData>
	implements IServerRequestProcessor<Type>
{
	/**
	 * Map all the requests to the corresponding relation token.
	 * This is used to trace requests.
	 */
	private HashMap<IMessageUserData, RelationToken> processedJobs = new HashMap<IMessageUserData, RelationToken>();

	protected IGRequestProcessingServerInterface server;

	final @Override
	public void setServerInterface(IGRequestProcessingServerInterface server) {
		this.server = server;
	}

	protected CommandImplementation getCommandImplementation(Class<? extends CommandImplementation> cmdImpl){
		return DynamicImplementationLoader.getInstance().getCommandInstanceForCommand(cmdImpl);
	}

	/**
	 * Trace Requests. Announce the start of a new request.
	 * @param req
	 * @param job
	 */
	protected void startRequest(IMessageUserData req, InterProcessNetworkJobRoutable job){
		final STraceWriter tw = server.getSimulator().getTraceWriter();
		if(tw.isTracableComponent(TraceType.IOSERVER)){
			final RelationToken tk = tw.relRelateProcessLocalToken(job.getRelationToken(), TraceType.IOSERVER, (ISPassiveComponent) server);
			assert(tk != null);
			processedJobs.put(req, tk);

			String xmlTag = null;
			String [] attributeArray = null;
			if(req.getClass() == RequestWrite.class || req.getClass() == RequestRead.class){
				RequestIO freq = (RequestIO) req;
				attributeArray = new String[]{ "File", freq.getFile().getName(), "Size", freq.getListIO().getTotalSize() + ""};

				// trace each operation of the listio.
				StringBuffer buff = new StringBuffer();
				for(SingleIOOperation op : freq.getListIO().getIOOperations()){
					buff.append("<op size=\"" + op.getAccessSize() + "\" offset=\"" + op.getOffset() + "\"/>");
				}
				xmlTag = buff.toString();
			}else{
				FileRequest freq = (FileRequest) req;
				attributeArray = new String[]{ "File", freq.getFile().getName()};
			}

			tw.relStartState(TraceType.IOSERVER, (ISPassiveComponent) server, tk, req.getClass().getSimpleName(), xmlTag, attributeArray);
		}
	}

	/**
	 * Trace Requests, announce the completion of the request.
	 * @param req
	 */
	protected void finishRequest(IMessageUserData req){
		final STraceWriter tw = server.getSimulator().getTraceWriter();
		if(tw.isTracableComponent(TraceType.IOSERVER)){
			final RelationToken tk = processedJobs.remove(req);
			assert(tk != null);
			tw.relEndState(TraceType.IOSERVER, (ISPassiveComponent) server, tk);
			tw.relDestroy(TraceType.IOSERVER, (ISPassiveComponent) server, tk);
		}
	}
}

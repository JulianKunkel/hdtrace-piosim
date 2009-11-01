package de.hd.pvs.piosim.simulator.components.Server.requests;

import de.hd.pvs.piosim.simulator.components.Server.IGRequestProcessingServerInterface;
import de.hd.pvs.piosim.simulator.components.Server.IServerRequestProcessor;
import de.hd.pvs.piosim.simulator.network.IMessageUserData;

public abstract class RequestProcessor<Type extends IMessageUserData>
	implements IServerRequestProcessor<Type>
{
	protected IGRequestProcessingServerInterface server;

	final @Override
	public void setServerInterface(IGRequestProcessingServerInterface server) {
		this.server = server;
	}
}

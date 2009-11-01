package de.hd.pvs.piosim.simulator.components.ClientProcess;

import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.components.superclasses.INodeHostedComponent;
import de.hd.pvs.piosim.model.inputOutput.ListIO;

/**
 * A single set of IO operations.
 * One I/O target with a set of operations.
 *
 * @author julian
 */
public class SClientListIO {
	// might be an intermediate node.
	final INodeHostedComponent nhc;
	final Server targetServer;
	final ListIO listIO;

	public SClientListIO(Server targetServer, INodeHostedComponent nhc, ListIO ioList) {
		this.nhc = nhc;
		this.listIO = ioList;
		this.targetServer = targetServer;
	}

	public ListIO getListIO() {
		return listIO;
	}

	public INodeHostedComponent getNextHop() {
		return nhc;
	}

	public Server getTargetServer() {
		return targetServer;
	}
}

package de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;
import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;

/**
 * Construct a SMP node with internal crossbar switches for each socket which represents memory performance and one central node interconnecting all sockets.
 *
 * @author julian
 *
 */
public class SMTSocketNodeT  extends HardwareConfiguration {

	final int clientsPerSocket;
	final int sockets;
	final NIC nic;
	final Node node;

	// interconnect within the same socket
	final NetworkNode socketInterconnect;
	final NetworkEdge socketInterconnectEdge;

	// interconnect between sockets
	final NetworkNode switchInterconnect;
	final NetworkEdge switchInterconnectEdge;


	final IOServerCreator ioServerAddon; // null if none

	public SMTSocketNodeT(int clientsPerSocket, int sockets, NIC nic, Node node, NetworkNode socketInterconnect, NetworkEdge socketInterconnectEdge, NetworkNode switchInterconnect, NetworkEdge switchInterconnectEdge) {
		this(clientsPerSocket, sockets, nic, node, socketInterconnect, socketInterconnectEdge, switchInterconnect, switchInterconnectEdge, null);
	}

	public SMTSocketNodeT(int clientsPerSocket,  int sockets, NIC nic, Node node, NetworkNode socketInterconnect, NetworkEdge socketInterconnectEdge, NetworkNode switchInterconnect, NetworkEdge switchInterconnectEdge, IOServerCreator ioServerAddon) {
		this.nic = nic;
		this.clientsPerSocket = clientsPerSocket;
		this.sockets = sockets;

		this.node = node;
		this.socketInterconnect = socketInterconnect;
		this.socketInterconnectEdge = socketInterconnectEdge;
		this.switchInterconnect = switchInterconnect;
		this.switchInterconnectEdge = switchInterconnectEdge;
		this.ioServerAddon  = ioServerAddon;
	}

	@Override
	public NetworkNode createModel(String prefix, ModelBuilder mb, INetworkTopology topology) throws Exception {
		mb.addTemplateIf(nic);
		mb.addTemplateIf(node);
		mb.addTemplateIf(socketInterconnectEdge);
		mb.addTemplateIf(socketInterconnect);
		mb.addTemplateIf(this.switchInterconnect);
		mb.addTemplateIf(switchInterconnectEdge);

		final Node n = mb.cloneFromTemplate(node);
		n.setName(prefix + node.getName());
		mb.addNode(n);


		final NetworkNode switchInterconnect = mb.cloneFromTemplate(this.switchInterconnect);

		switchInterconnect.setName(prefix + socketInterconnect.getName());
		mb.addNetworkNode(switchInterconnect);

		// create at most one I/O server per node
		boolean createdIO = false;

		for(int s=0; s < sockets;s++){

			final NetworkNode socketCrossbar = mb.cloneFromTemplate(socketInterconnect);

			// create socket switch
			socketCrossbar.setName(prefix + "" + socketInterconnect.getName());

			mb.addNetworkNode(socketCrossbar);

			// socket switch via interconnect via crossbar switch
			{
			NetworkEdge e1 = mb.cloneFromTemplate(switchInterconnectEdge);
			NetworkEdge e2 = mb.cloneFromTemplate(switchInterconnectEdge);
			e1.setName(prefix + "_TX" + e1.getName());
			e2.setName(prefix + "_RX" + e2.getName());
			mb.connect(topology, switchInterconnect, e1, socketCrossbar);
			mb.connect(topology, socketCrossbar, e2, switchInterconnect);
			}


			// add all the clients:
			for(int i=0; i < clientsPerSocket; i++){
				NIC nm = mb.cloneFromTemplate(nic);

				final ClientProcess c = new ClientProcess();
				c.setNetworkInterface(nm);
				c.setName(prefix + i + "C");
				nm.setName(prefix + i + "N");

				c.setNetworkInterface(nm);

				mb.addClient(n, c);

				// interconnect via crossbar switch
				NetworkEdge e1 = mb.cloneFromTemplate(socketInterconnectEdge);
				NetworkEdge e2 = mb.cloneFromTemplate(socketInterconnectEdge);
				e1.setName(prefix + i + "_TX " + e1.getName());
				e2.setName(prefix + i + "_RX " + e2.getName());
				mb.connect(topology, nm, e1, socketCrossbar);
				mb.connect(topology, socketCrossbar, e2, nm);
			}

			if(ioServerAddon != null && ! createdIO){
				createdIO = true;

				// add the I/O-server to the node:
				Server serv = ioServerAddon.createServer(prefix + "io", mb);
				NIC nm = mb.cloneFromTemplate(nic);
				nm.setName(prefix + "ioN");
				serv.setNetworkInterface(nm);

				mb.addServer(n, serv);

				// interconnect via crossbar switch
				NetworkEdge e1 = mb.cloneFromTemplate(socketInterconnectEdge);
				NetworkEdge e2 = mb.cloneFromTemplate(socketInterconnectEdge);
				e1.setName(prefix + "io_TX" + e1.getName());
				e2.setName(prefix + "io_RX" + e2.getName());
				mb.connect(topology, nm, e1, socketCrossbar);
				mb.connect(topology, socketCrossbar, e2, nm);

			}

		}

		return switchInterconnect;
	}

}

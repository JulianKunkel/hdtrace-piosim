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
 * Construct a SMP node with internal crossbar switch (if more than 1 CPU).
 *
 * @author julian
 *
 */
public class SMTNodeT  extends HardwareConfiguration {

	final int smpPerNode;
	final NIC nic;
	final Node node;
	final NetworkNode smpInterconnect;
	final NetworkEdge smpInterconnectEdge;

	final IOServerCreator ioServerAddon; // null if none

	public SMTNodeT(int clientsPerNode, NIC nic, Node node, NetworkNode smpInterconnect, NetworkEdge smpInterconnectEdge) {
		this(clientsPerNode, nic, node, smpInterconnect, smpInterconnectEdge, null);
	}

	public SMTNodeT(int smpPerNode, NIC nic, Node node, NetworkNode smpInterconnect, NetworkEdge smpInterconnectEdge, IOServerCreator ioServerAddon) {
		this.nic = nic;
		this.smpPerNode = smpPerNode;

		this.node = node;
		this.smpInterconnect = smpInterconnect;
		this.smpInterconnectEdge = smpInterconnectEdge;
		this.ioServerAddon  = ioServerAddon;
	}

	@Override
	public NetworkNode createModel(String prefix, ModelBuilder mb, INetworkTopology topology) throws Exception {
		mb.addTemplateIf(nic);
		mb.addTemplateIf(node);
		mb.addTemplateIf(smpInterconnectEdge);
		mb.addTemplateIf(smpInterconnect);

		final Node n = mb.cloneFromTemplate(node);
		n.setName(prefix + node.getName());
		mb.addNode(n);

		final NetworkNode internalCrossbar = mb.cloneFromTemplate(smpInterconnect);

		// create crossbar switch
		internalCrossbar.setName(prefix + "" + smpInterconnect.getName());

		mb.addNetworkNode(internalCrossbar);


		// add all the clients:
		for(int i=0; i < smpPerNode; i++){
			NIC nm = mb.cloneFromTemplate(nic);

			final ClientProcess c = new ClientProcess();
			c.setNetworkInterface(nm);
			c.setName(prefix + i + "C");
			nm.setName(prefix + i + "N");

			c.setNetworkInterface(nm);

			mb.addClient(n, c);

			// interconnect via crossbar switch
			NetworkEdge e1 = mb.cloneFromTemplate(smpInterconnectEdge);
			NetworkEdge e2 = mb.cloneFromTemplate(smpInterconnectEdge);
			e1.setName(prefix + i + "_TX " + e1.getName());
			e2.setName(prefix + i + "_RX " + e2.getName());
			mb.connect(topology, nm, e1, internalCrossbar);
			mb.connect(topology, internalCrossbar, e2, nm);
		}

		if(ioServerAddon != null){
			// add the I/O-server to the node:
			Server s = ioServerAddon.createServer(prefix + "io", mb);
			NIC nm = mb.cloneFromTemplate(nic);
			nm.setName(prefix + "ioN");
			s.setNetworkInterface(nm);

			mb.addServer(n, s);

			// interconnect via crossbar switch
			NetworkEdge e1 = mb.cloneFromTemplate(smpInterconnectEdge);
			NetworkEdge e2 = mb.cloneFromTemplate(smpInterconnectEdge);
			e1.setName(prefix + "io_TX" + e1.getName());
			e2.setName(prefix + "io_RX" + e2.getName());
			mb.connect(topology, nm, e1, internalCrossbar);
			mb.connect(topology, internalCrossbar, e2, nm);

		}

		return internalCrossbar;
	}

}

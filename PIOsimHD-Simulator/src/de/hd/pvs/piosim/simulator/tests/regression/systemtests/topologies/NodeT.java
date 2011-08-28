package de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;
import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;

/**
 * Construct a simple node
 *
 * @author julian
 *
 */
public class NodeT  extends HardwareConfiguration {

	final NIC nic;
	final Node node;

	public NodeT(NIC nic, Node node) {
		this.nic = nic;

		this.node = node;
	}

	@Override
	public NetworkNode createModel(String prefix, ModelBuilder mb, INetworkTopology topology) throws Exception {
		mb.addTemplateIf(nic);
		mb.addTemplateIf(node);

		final Node n = mb.cloneFromTemplate(node);
		n.setName(prefix + node.getName());
		mb.addNode(n);


		// add all the clients:
		NIC nm = mb.cloneFromTemplate(nic);

		final ClientProcess c = new ClientProcess();
		c.setNetworkInterface(nm);
		c.setName(prefix + "C");
		nm.setName(prefix + "N");

		c.setNetworkInterface(nm);

		mb.addClient(n, c);
		return nm;
	}

}

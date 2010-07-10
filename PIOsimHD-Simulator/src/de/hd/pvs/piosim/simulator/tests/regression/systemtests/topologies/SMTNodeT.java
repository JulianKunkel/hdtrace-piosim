package de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;
import de.hd.pvs.piosim.model.components.Node.Node;
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

	public SMTNodeT(int smpPerNode, NIC nic, Node node, NetworkNode smpInterconnect, NetworkEdge smpInterconnectEdge) {
		this.nic = nic;
		this.smpPerNode = smpPerNode;

		this.node = node;
		this.smpInterconnect = smpInterconnect;
		this.smpInterconnectEdge = smpInterconnectEdge;
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

		if( smpPerNode > 0 ){
			// create crossbar switch
			mb.addNetworkNode(internalCrossbar);
		}

		for(int i=0; i < smpPerNode; i++){
			NIC nm = mb.cloneFromTemplate(nic);

			final ClientProcess c = new ClientProcess();
			c.setNetworkInterface(nm);
			c.setName(prefix + i + "C");
			nm.setName(prefix + i + "N");

			c.setNetworkInterface(nm);

			mb.addClient(n, c);

			if( smpPerNode > 0 ){
				// interconnect via crossbar switch
				NetworkEdge e1 = mb.cloneFromTemplate(smpInterconnectEdge);
				NetworkEdge e2 = mb.cloneFromTemplate(smpInterconnectEdge);
				mb.connect(topology, internalCrossbar, e2, nm);
				mb.connect(topology, nm, e1, internalCrossbar);

			}else{
				return nm;
			}
		}

		return internalCrossbar;
	}
}

package de.hd.pvs.piosim.model.components.superclasses;

import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.Node.Node;

public interface INodeHostedComponent extends IBasicComponent{
	public NIC getNetworkInterface();
	public Node getHostingNode();
}

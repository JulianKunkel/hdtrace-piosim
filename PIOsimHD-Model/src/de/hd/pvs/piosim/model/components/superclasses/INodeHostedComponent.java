package de.hd.pvs.piosim.model.components.superclasses;

import de.hd.pvs.piosim.model.components.NIC.NIC;

public interface INodeHostedComponent extends IBasicComponent{
	public NIC getNetworkInterface();
}

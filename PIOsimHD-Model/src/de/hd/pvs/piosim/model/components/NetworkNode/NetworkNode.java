package de.hd.pvs.piosim.model.components.NetworkNode;

import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;

public abstract class NetworkNode extends BasicComponent   implements INetworkNode {

	@Override
	public String getComponentType() {
		return NetworkNode.class.getSimpleName();
	}
}

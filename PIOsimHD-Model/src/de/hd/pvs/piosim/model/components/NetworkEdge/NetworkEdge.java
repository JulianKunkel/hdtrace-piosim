package de.hd.pvs.piosim.model.components.NetworkEdge;

import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.networkTopology.INetworkEdge;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;

public abstract class NetworkEdge extends BasicComponent implements INetworkEdge  {
	private INetworkTopology topology = null;

	final public String getObjectType() {
		return NetworkEdge.class.getSimpleName();
	}

	public INetworkTopology getTopology() {
		return topology;
	}

	public void setTopology(INetworkTopology topology) {
		this.topology = topology;
	}
}

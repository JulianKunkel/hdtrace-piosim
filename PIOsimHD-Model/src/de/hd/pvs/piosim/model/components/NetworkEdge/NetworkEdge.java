package de.hd.pvs.piosim.model.components.NetworkEdge;

import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.networkTopology.INetworkEdge;

public abstract class NetworkEdge extends BasicComponent implements INetworkEdge  {

	final public String getObjectType() {
		return NetworkEdge.class.getSimpleName();
	}
}

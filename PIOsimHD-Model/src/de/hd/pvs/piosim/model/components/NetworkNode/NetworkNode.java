package de.hd.pvs.piosim.model.components.NetworkNode;

import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.networkTopology.INetworkExit;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
/**
 * Has a link to one or multiple neighbour NetworkComponents.
 *
 * @author Julian M. Kunkel
 */
public abstract class NetworkNode extends BasicComponent   implements INetworkNode {

	public String getObjectType() {
		return NetworkNode.class.getSimpleName();
	}

	final public boolean isExitNode(){
		return INetworkExit.class.isInstance(this);
	}

	static public boolean isExitNode(INetworkNode node){
		return INetworkExit.class.isInstance(node);
	}
}

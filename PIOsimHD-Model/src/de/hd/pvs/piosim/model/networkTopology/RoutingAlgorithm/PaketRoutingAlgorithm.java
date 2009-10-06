package de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm;

import de.hd.pvs.piosim.model.interfaces.IDynamicImplementationObject;


/**
 * Describes how pakets are routed throughout the network.
 * Necessary, if there are multiple routes to one destination.
 *
 * @author julian
 */
public abstract class PaketRoutingAlgorithm implements IDynamicImplementationObject
{
	final public String getObjectType() {
		return PaketRoutingAlgorithm.class.getSimpleName();
	}
}

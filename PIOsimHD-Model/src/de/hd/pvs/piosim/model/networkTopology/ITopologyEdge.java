package de.hd.pvs.piosim.model.networkTopology;

import de.hd.pvs.piosim.model.components.superclasses.ComponentIdentifier;


/**
 * Represents interface to an edge in a network graph
 * @author julian
 */
public interface ITopologyEdge{

	public INetworkEdge getEdge();

	public INetworkNode getTarget();

	public ComponentIdentifier getIdentifier();
}

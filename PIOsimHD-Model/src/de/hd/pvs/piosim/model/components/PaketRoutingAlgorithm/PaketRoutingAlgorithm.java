package de.hd.pvs.piosim.model.components.PaketRoutingAlgorithm;

import java.util.ArrayList;

import de.hd.pvs.piosim.model.components.superclasses.NetworkComponent;

/**
 * Describes how pakets are routed throughout the network.
 * Necessary, if there are multiple routes to one destination.
 *
 * @author julian
 */
public abstract class PaketRoutingAlgorithm extends NetworkComponent<NetworkComponent<?>>
{
	private ArrayList<NetworkComponent> connectedComponents = new ArrayList<NetworkComponent>();

	public ArrayList<NetworkComponent> getConnectedComponents(){
		return connectedComponents;
	}

	public void addNewConnection(NetworkComponent component){
		if(connectedComponents.contains(component)){
			throw new IllegalArgumentException("Component already connected to PaketRoutingAlgorithm");
		}
		connectedComponents.add(component);
	}

	@Override
	final public String getComponentType() {
		return PaketRoutingAlgorithm.class.getSimpleName();
	}


	@Override
	final public void setConnectedComponent(NetworkComponent component) {
		throw new IllegalArgumentException("Paket routing is used for multiple targets");
	}
}

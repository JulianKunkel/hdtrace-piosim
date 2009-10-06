package de.hd.pvs.piosim.model.networkTopology;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.AttributeXMLType;
import de.hd.pvs.piosim.model.annotations.ChildComponents;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNull;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoutingAlgorithm;

/**
 * Contains the network graph consisting of components
 *
 * @author julian
 */
public class NetworkTopology extends NetworkGraph implements INetworkTopology {

	@Attribute(type=AttributeXMLType.ATTRIBUTE)
	@NotNull
	private String name = "";

	// routing protocol could maybe be set on one network topology (Bus system, link together with
	// redundant 2-D Torus or sth.

	@ChildComponents
	private PaketRoutingAlgorithm routingAlgorithm = null;

	public void setRoutingAlgorithm(PaketRoutingAlgorithm routingAlgorithm) {
		this.routingAlgorithm = routingAlgorithm;
	}

	public PaketRoutingAlgorithm getRoutingAlgorithm() {
		return routingAlgorithm;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "\"" +  name + "\"";
	}

	public String getObjectType() {
		return NetworkTopology.class.getSimpleName();
	}
}

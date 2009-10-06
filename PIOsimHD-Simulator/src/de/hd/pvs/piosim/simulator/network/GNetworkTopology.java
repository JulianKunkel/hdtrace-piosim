package de.hd.pvs.piosim.simulator.network;

import de.hd.pvs.piosim.simulator.network.routing.IPaketTopologyRouting;

public class GNetworkTopology  {
	String name;
	IPaketTopologyRouting routing;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public IPaketTopologyRouting getRouting() {
		return routing;
	}

	public void setRouting(IPaketTopologyRouting routing) {
		this.routing = routing;
	}
}

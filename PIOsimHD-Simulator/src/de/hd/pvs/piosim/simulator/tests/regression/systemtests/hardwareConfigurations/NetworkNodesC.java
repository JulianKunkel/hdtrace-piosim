package de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations;

import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardNode;

/**
 * @author julian
 */
public class NetworkNodesC implements HardwareComponents{
	static 	public StoreForwardNode QPI(){
		StoreForwardNode sw = new StoreForwardNode();
		sw.setName("QPI");
		sw.setTotalBandwidth(36 * GBYTE);
		return sw;
	}


	static 	public StoreForwardNode GIGSwitch(){
		StoreForwardNode sw = new StoreForwardNode();
		sw.setName("GIG-Switch");
		sw.setTotalBandwidth(48000 * MBYTE);
		return sw;
	}

	static public StoreForwardNode LimitedSwitch(){
		StoreForwardNode sw = new StoreForwardNode();
		sw.setName("PVS-Switch");
		sw.setTotalBandwidth(200 * MBYTE);
		return sw;
	}
}

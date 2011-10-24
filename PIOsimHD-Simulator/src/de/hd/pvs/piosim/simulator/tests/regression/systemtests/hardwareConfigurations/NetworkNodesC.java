package de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations;

import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardMemoryNode;
import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardNode;

/**
 * @author julian
 */
public class NetworkNodesC implements HardwareComponents{
	static 	public StoreForwardNode QPI(){
		StoreForwardNode sw = new StoreForwardNode();
		sw.setName("QPI");

		// determined on west1 by using memory-bandwidth.c 1000 iter, 104857600
		// 1000 iterations, time:13.106618s MB/s:7629.733218
		sw.setTotalBandwidth(7629  * MBYTE);
		return sw;
	}


	static 	public StoreForwardNode LocalNodeQPI(){
		StoreForwardMemoryNode sw = new StoreForwardMemoryNode();
		sw.setName("QPIL");
		sw.setTotalBandwidth(3427  *  MBYTE );
		sw.setLocalBandwidth(4556 * MBYTE );

		return sw;
	}


	static 	public StoreForwardNode SocketLocalNode(){
		StoreForwardMemoryNode sw = new StoreForwardMemoryNode();
		sw.setName("SLN");
		sw.setTotalBandwidth(3427  *  MIB );
		sw.setLocalBandwidth(4556 * MIB );

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

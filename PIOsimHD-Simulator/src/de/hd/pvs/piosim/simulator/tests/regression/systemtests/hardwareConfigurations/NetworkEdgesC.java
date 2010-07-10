package de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NetworkEdge.SimpleNetworkEdge;

/**
 * @author julian
 */
public class NetworkEdgesC implements HardwareComponents{

	static public SimpleNetworkEdge QPI(){
		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setName("1GBit Ethernet");
		conn.setLatency(new Epoch(0.0000001));
		conn.setBandwidth(12800 * M);
		return conn;
	}

	static public SimpleNetworkEdge GIGE(){
		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setName("1GBit Ethernet");
		conn.setLatency(new Epoch(0.00003));
		conn.setBandwidth(117 * MBYTE);
		return conn;
	}

	static public SimpleNetworkEdge TenGIGE(){
		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setName("10GBit Ethernet");
		conn.setLatency(new Epoch(0.000003));
		conn.setBandwidth(1000 * MBYTE);
		return conn;
	}


}

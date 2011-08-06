package de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NetworkEdge.SimpleNetworkEdge;

/**
 * @author julian
 */
public class NetworkEdgesC implements HardwareComponents{

	static public SimpleNetworkEdge SocketLocalEdge(){
		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setName("SLE");
		conn.setLatency(new Epoch(2.23 / 1000000));
		conn.setBandwidth(10864 * MBYTE);
		return conn;
	}


	static public SimpleNetworkEdge QPI(){
		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setName("QPI");
		conn.setLatency(new Epoch(2.23 / 1000000));
		conn.setBandwidth(10864 * MBYTE);
		return conn;
	}

	static public SimpleNetworkEdge GIGE(){
		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setName("1GBit Ethernet");
		conn.setLatency(new Epoch(0.0002753));
		conn.setBandwidth(67 * MBYTE);
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

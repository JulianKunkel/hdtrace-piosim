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
		conn.setLatency(new Epoch(0.000000207));
		conn.setBandwidth(10864 * MBYTE);
		return conn;
	}


	static public SimpleNetworkEdge SocketLocalNoLatencyEdge(){
		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setName("SLE");
		conn.setLatency(Epoch.ZERO); // sendRecvPaired10K
		conn.setBandwidth(10864 * MBYTE);
		return conn;
	}

	static public SimpleNetworkEdge QPI(){
		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setName("QPI");
		conn.setLatency(new Epoch(0.00000026679619779508)); // difference between intra-socket and inter-socket
		conn.setBandwidth(10864 * MBYTE);
		return conn;
	}


	static public SimpleNetworkEdge QPINoLatency(){
		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setName("QPI");
		conn.setLatency(Epoch.ZERO); // 5.4 - 2.8
		conn.setBandwidth(10864 * MBYTE);
		return conn;
	}

	static public SimpleNetworkEdge GIGEPVS(){
		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setName("1GBitEPVS");
		conn.setLatency(new Epoch(0.00004119364785427372));
		conn.setBandwidth(75392614); // 71.9 MiB/s
		return conn;
	}

	static public SimpleNetworkEdge GIGE(){
		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setName("1GBitE");
		conn.setLatency(new Epoch(0.0002753));
		conn.setBandwidth(117 * MBYTE);
		return conn;
	}


	static public SimpleNetworkEdge GIGEPVSNoLatency(){
		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setName("1GBitE");
		conn.setLatency(Epoch.ZERO);
		conn.setBandwidth(75392614); // 71.9 MiB/s
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

package de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.IOSubsystem.IOSubsystem;
import de.hd.pvs.piosim.model.components.IOSubsystem.RefinedDiskModel;
import de.hd.pvs.piosim.model.components.IOSubsystem.SimpleDisk;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.NoCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.SimpleWriteBehindCache;

public class IOC implements HardwareComponents{

	static public IOSubsystem PVSDiskSimple(){
		final SimpleDisk iosub = new SimpleDisk();

		iosub.setMaxThroughput(50 * MBYTE);
		iosub.setMaxConcurrentRequests(1);
		iosub.setAvgAccessTime(new Epoch(0.005));
		iosub.setName("IBM");

		return iosub;
	}

	static public IOSubsystem PVSDisk(){
		final RefinedDiskModel iosub = new RefinedDiskModel();
		iosub.setAverageSeekTime(new Epoch(0.01));
		iosub.setTrackToTrackSeekTime(new Epoch(0.001));
		iosub.setRPM(7200);
		iosub.setPositionDifferenceConsideredToBeClose(5 * MBYTE);
		iosub.setSequentialTransferRate((int) 50 * MBYTE);
		iosub.setName("IBM");

		return iosub;
	}

	static public Server PVSServer(){
		Server serverTemplate = new Server();
		serverTemplate.setName("PVSServer");
		return serverTemplate;
	}


	static public ServerCacheLayer SimpleWriteBehindCache(){
		ServerCacheLayer layer = new SimpleWriteBehindCache();
		layer.setMaxNumberOfConcurrentIOOps(1);
		layer.setName("WBHND");

		return layer;
	}

	static public ServerCacheLayer SimpleNoCache(){
		ServerCacheLayer layer = new NoCache();
		layer.setMaxNumberOfConcurrentIOOps(1);
		layer.setName("NoCache");
		return layer;
	}


	static public ServerCacheLayer AggregationCache(){
		ServerCacheLayer layer = new AggregationCache();
		layer.setMaxNumberOfConcurrentIOOps(1);
		layer.setName("AggregationCache");
		return layer;
	}
}

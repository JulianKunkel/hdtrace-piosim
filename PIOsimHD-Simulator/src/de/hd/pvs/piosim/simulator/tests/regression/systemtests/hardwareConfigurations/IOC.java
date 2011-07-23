package de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.IOSubsystem.IOSubsystem;
import de.hd.pvs.piosim.model.components.IOSubsystem.RefinedDiskModel;
import de.hd.pvs.piosim.model.components.IOSubsystem.SimpleDisk;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationReorderCache;
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


	// Barracuda 7200.12 SATA 3Gb/s 250GB Hard Drive (ST3250318AS).
	// http://www.seagate.com/staticfiles/support/disc/manuals/desktop/Barracuda%207200.12/100529369h.pdf
	static public IOSubsystem WestDisk(){
		final RefinedDiskModel iosub = new RefinedDiskModel();
		iosub.setAverageSeekTime(new Epoch(0.009));
		iosub.setTrackToTrackSeekTime(new Epoch(0.001));
		iosub.setRPM(7200);

		// look at the P.hD. thesis to understand why this value has been used.
		iosub.setPositionDifferenceConsideredToBeClose(MBYTE);

		// The value is measured with  dd if=/dev/zero of=test bs=1024k count=8000 and an active mem-eater limiting memory to 1\,GiB.
		// measured on two nodes: west6: 98.5 MiB/s, west4: 114 MiB/s.
		// With a block size of 256K (PVFS size)
		// west6: 96.7 MiB/s
		iosub.setSequentialTransferRate((int) 100 * MBYTE);
		iosub.setName("WestDisk");

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

	static public ServerCacheLayer AggregationReorderCache(){
		ServerCacheLayer layer = new AggregationReorderCache();
		layer.setMaxNumberOfConcurrentIOOps(1);
		layer.setName("AReorderCache");
		return layer;
	}
}

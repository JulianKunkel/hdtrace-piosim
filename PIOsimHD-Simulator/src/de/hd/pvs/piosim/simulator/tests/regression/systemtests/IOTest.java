/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$
 */

//	Copyright (C) 2008, 2009 Julian M. Kunkel
//	Copyright (C) 2009 Michael Kuhn
//
//	This file is part of PIOsimHD.
//
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.
package de.hd.pvs.piosim.simulator.tests.regression.systemtests;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.NoCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerDirectedIO;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.SimpleWriteBehindCache;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;
import de.hd.pvs.piosim.simulator.SimulationResults;

abstract public class IOTest extends ClusterTest {
	int serverNum = 10;
	int clientNum = 10;
	int fileNum = 10;
	int iterNum = 250;
	long elementSize = 0;
	// PVFS default
	long stripeSize = 64 * KBYTE;

	ServerCacheLayer cacheLayer = null;

	protected List<MPIFile> prepare(boolean isWrite) throws Exception {
		List<MPIFile> files = new ArrayList<MPIFile>();

		assert(elementSize > 0);

		testMsg();
		setup(clientNum, serverNum, cacheLayer);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(stripeSize);

		for (int i = 0; i < fileNum; i++) {
			files.add(aB.createFile("testfile" + i, (isWrite) ? 0 : (clientNum * iterNum) * elementSize, dist));
		}

		for (int i = 0; i < fileNum; i++) {
			pb.addFileOpen(files.get(i), world, isWrite);
		}

		return files;
	}

	abstract public void doWrite(List<MPIFile> files) throws Exception;

	abstract public void doRead(List<MPIFile> files) throws Exception;

	public SimulationResults writeTest() throws Exception {
		List<MPIFile> files = prepare(true);
		doWrite(files);
		for (MPIFile f : files) {
			pb.addFileClose(f, world);
		}
		return runSimulationAllExpectedToFinish();
	}

	public SimulationResults readTest() throws Exception {
		List<MPIFile> files = prepare(false);
		doRead(files);
		for (MPIFile f : files) {
			pb.addFileClose(f, world);
		}
		return runSimulationAllExpectedToFinish();
	}

	@Test
	public void run() throws Exception {
		final class CacheLayerResults {
			ServerCacheLayer cacheLayer = null;
			List<SimulationResults> readResults = new ArrayList<SimulationResults>();
			List<SimulationResults> writeResults = new ArrayList<SimulationResults>();

			public CacheLayerResults(ServerCacheLayer cacheLayer) {
				this.cacheLayer = cacheLayer;
			}
		}

		List<CacheLayerResults> results = new ArrayList<CacheLayerResults>();
		List<ServerCacheLayer> cacheLayers = new ArrayList<ServerCacheLayer>();
		List<Long> sizes = new ArrayList<Long>();

		cacheLayers.add(new NoCache());
		cacheLayers.add(new SimpleWriteBehindCache());
		cacheLayers.add(new AggregationCache());
		cacheLayers.add(new ServerDirectedIO());

		sizes.add((long)512);
		sizes.add((long)5 * KBYTE);
		sizes.add((long)50 * KBYTE);
		sizes.add((long)512 * KBYTE);

		for (ServerCacheLayer cacheLayer : cacheLayers) {
			CacheLayerResults res = new CacheLayerResults(cacheLayer);

			this.cacheLayer = cacheLayer;

			for (long size : sizes) {
				elementSize = size;

				res.readResults.add(readTest());
				res.writeResults.add(writeTest());
			}

			results.add(res);
		}

		for (CacheLayerResults res : results) {
			System.out.println(res.cacheLayer.getClass().getSimpleName());

			for (int i = 0; i < sizes.size(); i++) {
				if (res.readResults.size() > i) {
					System.out.println("  " + sizes.get(i) + " READ  " + res.readResults.get(i).getVirtualTime().getDouble() + "s");
				}

				if (res.writeResults.size() > i) {
					System.out.println("  " + sizes.get(i) + " WRITE " + res.writeResults.get(i).getVirtualTime().getDouble() + "s");
				}
			}
		}
	}
}
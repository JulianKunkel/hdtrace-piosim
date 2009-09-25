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

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerDirectedIO;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.SimpleWriteBehindCache;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;
import de.hd.pvs.piosim.simulator.SimulationResults;
import de.hd.pvs.piosim.simulator.base.ComponentRuntimeInformation;
import de.hd.pvs.piosim.simulator.components.IOSubsystem.GRefinedDiskModel.GRefinedDiskModelInformation;

abstract public class IOTest extends ClusterTest {
	protected int serverNum = 10;
	protected int clientNum = 10;
	protected int fileNum = 1;
	protected long elementSize = 0;
	protected long fileSize = 1000 * 1000 * 1024;
	// PVFS default
	protected long stripeSize = 64 * KBYTE;

	protected ServerCacheLayer cacheLayer = null;

	protected List<MPIFile> prepare(boolean isEmpty) throws Exception {
		List<MPIFile> files = new ArrayList<MPIFile>();

		assert(elementSize > 0);

		testMsg();
		setup(clientNum, serverNum, cacheLayer);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(stripeSize);

		for (int i = 0; i < fileNum; i++) {
			files.add(aB.createFile("testfile" + i, (isEmpty) ? 0 : fileSize, dist));
		}

		for (int i = 0; i < fileNum; i++) {
			pb.addFileOpen(files.get(i), world, isEmpty);
		}

		return files;
	}

	protected void unprepare (List<MPIFile> files) throws Exception {
		for (MPIFile f : files) {
			pb.addFileClose(f, world);
		}
	}

	abstract public void doWrite(List<MPIFile> files) throws Exception;

	abstract public void doRead(List<MPIFile> files) throws Exception;

	public SimulationResults writeTest() throws Exception {
		List<MPIFile> files = prepare(true);
		doWrite(files);
		unprepare(files);
		return runSimulationAllExpectedToFinish();
	}

	public SimulationResults readTest() throws Exception {
		List<MPIFile> files = prepare(false);
		doRead(files);
		unprepare(files);
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

//		cacheLayers.add(new NoCache());
		cacheLayers.add(new SimpleWriteBehindCache());
		cacheLayers.add(new AggregationCache());
		cacheLayers.add(new ServerDirectedIO());

//		sizes.add((long)512);
		sizes.add((long)5 * KBYTE);
		//sizes.add((long)50 * KBYTE);
		//sizes.add((long)512 * KBYTE);

		for (ServerCacheLayer cacheLayer : cacheLayers) {
			CacheLayerResults res = new CacheLayerResults(cacheLayer);

			this.cacheLayer = cacheLayer;

			for (long size : sizes) {
				elementSize = size;

				System.err.println(res.cacheLayer.getClass().getSimpleName() + " READ " + size);
				res.readResults.add(readTest());

				System.err.println(res.cacheLayer.getClass().getSimpleName() + " WRITE " + size);
				res.writeResults.add(writeTest());
			}

			results.add(res);
		}

		FileWriter out = new FileWriter("/tmp/iotest.txt");

		for (CacheLayerResults res : results) {
			out.write(res.cacheLayer.getClass().getSimpleName() + "\n");

			for (int i = 0; i < sizes.size(); i++) {
				if (res.readResults.size() > i) {
					out.write("  " + sizes.get(i) + " READ  " + (fileNum * fileSize) + " B, " + res.readResults.get(i).getVirtualTime().getDouble() + " s\n");
					out.write("  " + sizes.get(i) + " READ  " + (fileNum * fileSize / res.readResults.get(i).getVirtualTime().getDouble() / 1024 / 1024) + " MB/s\n");

					for (ComponentRuntimeInformation info : res.readResults.get(i).getComponentStatistics().values()) {
						if (info.getClass() == GRefinedDiskModelInformation.class) {
							out.write("    " + info + "\n");
						}
					}
				}

				if (res.writeResults.size() > i) {
					out.write("  " + sizes.get(i) + " WRITE " + (fileNum * fileSize) + " B, " + res.writeResults.get(i).getVirtualTime().getDouble() + " s\n");
					out.write("  " + sizes.get(i) + " WRITE " + (fileNum * fileSize / res.writeResults.get(i).getVirtualTime().getDouble() / 1024 / 1024) + " MB/s\n");

					for (ComponentRuntimeInformation info : res.writeResults.get(i).getComponentStatistics().values()) {
						if (info.getClass() == GRefinedDiskModelInformation.class) {
							out.write("    " + info + "\n");
						}
					}
				}
			}
		}

		out.close();
	}
}
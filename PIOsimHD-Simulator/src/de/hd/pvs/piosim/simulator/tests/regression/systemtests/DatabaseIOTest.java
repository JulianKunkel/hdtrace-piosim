/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$
 */

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
import java.util.Random;

import org.junit.Test;

import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.NoCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerDirectedIO;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.SimpleWriteBehindCache;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.simulator.SimulationResults;

public class DatabaseIOTest extends IOTest {
	private Random random = null;

	public DatabaseIOTest() {
		super();
		iterNum /= 25;
	}

	protected long getFileSize (long elementSize) {
		return getDataTotal(elementSize) * 10;
	}

	private long getDataPerIteration (long elementSize) {
		return elementSize * 50 * 50;
	}

	private long getDataTotal (long elementSize) {
		return getDataPerIteration(elementSize) * iterNum * clientNum;
	}

	private MPIFile getFile (List<MPIFile> files) {
		int i = random.nextInt(files.size());
		return files.get(i);
	}

	private long getBlockOffset () {
		long offset = Math.abs(random.nextLong()) % getFileSize(elementSize);
		long remainder = offset % elementSize;
		return offset - remainder;
	}

	private long getBlockSize (long offset) {
		int i = random.nextInt(100);
		return Math.min((i + 1) * elementSize, getFileSize(elementSize) - offset);
	}

	public void doWrite(List<MPIFile> files) throws Exception {
		for (int i = 0; i < iterNum; i++) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				MPIFile f = getFile(files);
				long dataToAccess = getDataPerIteration(elementSize);

				while (dataToAccess > 0) {
					long offset = getBlockOffset();
					long size = getBlockSize(offset);

					size = Math.min(size, dataToAccess);
					dataToAccess -= size;

					pb.addWriteSequential(rank, f, offset, size);
				}
			}
		}
	}

	public void doRead(List<MPIFile> files) throws Exception {
		for (int i = 0; i < iterNum; i++) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				MPIFile f = getFile(files);
				long dataToAccess = getDataPerIteration(elementSize);

				while (dataToAccess > 0) {
					long offset = getBlockOffset();
					long size = getBlockSize(offset);

					size = Math.min(size, dataToAccess);
					dataToAccess -= size;

					pb.addReadSequential(rank, f, offset, size);
				}
			}
		}
	}

	public SimulationResults writeTest() throws Exception {
		List<MPIFile> files = prepare(false);
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
			List<Double> readAvgs = new ArrayList<Double>();
			List<Double> readDevs = new ArrayList<Double>();
			List<Double> writeAvgs = new ArrayList<Double>();
			List<Double> writeDevs = new ArrayList<Double>();

			public CacheLayerResults(ServerCacheLayer cacheLayer) {
				this.cacheLayer = cacheLayer;
			}
		}

		List<CacheLayerResults> results = new ArrayList<CacheLayerResults>();
		List<ServerCacheLayer> cacheLayers = new ArrayList<ServerCacheLayer>();
		List<Long> sizes = new ArrayList<Long>();
		List<Long> seeds = new ArrayList<Long>();

		cacheLayers.add(new NoCache());
		cacheLayers.add(new SimpleWriteBehindCache());
		cacheLayers.add(new AggregationCache());
		cacheLayers.add(new ServerDirectedIO());

		sizes.add((long)512);
//		sizes.add((long)5 * KBYTE);
//		sizes.add((long)50 * KBYTE);
//		sizes.add((long)512 * KBYTE);

		for (long i = 0; i < 10; i++) {
			seeds.add(i);
		}

		for (ServerCacheLayer cacheLayer : cacheLayers) {
			CacheLayerResults res = new CacheLayerResults(cacheLayer);

			this.cacheLayer = cacheLayer;

			for (long size : sizes) {
				double readAvg = 0;
				double readDev = 0;
				double writeAvg = 0;
				double writeDev = 0;

				for (long seed : seeds) {
					SimulationResults r;
					SimulationResults w;

					elementSize = size;
					random = new Random(seed);

					r = readTest();
					w = writeTest();

					readAvg += r.getVirtualTime().getDouble();
					readDev += Math.pow(r.getVirtualTime().getDouble(), 2);

					writeAvg += w.getVirtualTime().getDouble();
					writeDev += Math.pow(w.getVirtualTime().getDouble(), 2);
				}

				readAvg /= seeds.size();
				readDev /= seeds.size();
				readDev = Math.sqrt(readDev - Math.pow(readAvg, 2));

				res.readAvgs.add(readAvg);
				res.readDevs.add(readDev);

				writeAvg /= seeds.size();
				writeDev /= seeds.size();
				writeDev = Math.sqrt(writeDev - Math.pow(writeAvg, 2));

				res.writeAvgs.add(writeAvg);
				res.writeDevs.add(writeDev);
			}

			results.add(res);
		}

		FileWriter out = new FileWriter("/tmp/iotest.txt");

		for (CacheLayerResults res : results) {
			out.write(res.cacheLayer.getClass().getSimpleName() + "\n");

			for (int i = 0; i < sizes.size(); i++) {
				if (res.readAvgs.size() > i) {
					out.write("  " + sizes.get(i) + " READ  " + getDataTotal(sizes.get(i)) + " B, " + res.readAvgs.get(i) + " s (" + res.readDevs.get(i) + " s)\n");
					out.write("  " + sizes.get(i) + " READ  " + (getDataTotal(sizes.get(i)) / res.readAvgs.get(i) / 1024 / 1024) + " MB/s\n");
				}

				if (res.readAvgs.size() > i) {
					out.write("  " + sizes.get(i) + " WRITE " + getDataTotal(sizes.get(i)) + " B, " + res.writeAvgs.get(i) + " s (" + res.writeDevs.get(i) + " s)\n");
					out.write("  " + sizes.get(i) + " WRITE " + (getDataTotal(sizes.get(i)) / res.writeAvgs.get(i) / 1024 / 1024) + " MB/s\n");
				}
			}
		}

		out.close();
	}

	public static void main(String[] args) throws Exception {
		new DatabaseIOTest().run();
	}
}
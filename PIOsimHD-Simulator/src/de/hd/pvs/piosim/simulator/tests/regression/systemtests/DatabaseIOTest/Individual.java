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
package de.hd.pvs.piosim.simulator.tests.regression.systemtests.DatabaseIOTest;

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
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.program.commands.Fileread;
import de.hd.pvs.piosim.model.program.commands.Filewrite;
import de.hd.pvs.piosim.simulator.SimulationResults;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.IOTest;

public class Individual extends IOTest {
	private Random random = null;

	public Individual() {
		super();
		iterNum /= 25;
	}

	private int perIteration () {
		return 1;
	}

	protected long getFileSize (long elementSize) {
		return getDataTotal(elementSize) * 10;
	}

	protected long getDataPerIteration (long elementSize) {
		return elementSize * 50 * 50;
	}

	protected long getDataTotal (long elementSize) {
		return getDataPerIteration(elementSize) * iterNum * clientNum;
	}

	protected MPIFile getFile (List<MPIFile> files) {
		int i = random.nextInt(files.size());
		return files.get(i);
	}

	protected long getBlockOffset () {
		long offset = Math.abs(random.nextLong()) % getFileSize(elementSize);
		long remainder = offset % elementSize;
		return offset - remainder;
	}

	protected long getBlockSize (long offset) {
		int i = random.nextInt(100);
		return Math.min((i + 1) * elementSize, getFileSize(elementSize) - offset);
	}

	public void doWrite(List<MPIFile> files) throws Exception {
		int perIteration = perIteration();

		assert(iterNum % perIteration == 0);

		for (int i = 0; i < iterNum; i += perIteration) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				MPIFile f = getFile(files);
				long dataToAccess = getDataPerIteration(elementSize);

				while (dataToAccess > 0) {
					Filewrite com = new Filewrite();
					ListIO lio = new ListIO();

					com.setFile(f);

					for (int j = 0; j < perIteration; j++) {
						long offset = getBlockOffset();
						long size = getBlockSize(offset);

						size = Math.min(size, dataToAccess);
						dataToAccess -= size;

						lio.addIOOperation(offset, size);

						if (dataToAccess == 0) {
							break;
						}
					}

					com.setListIO(lio);
					aB.addCommand(rank, com);
				}
			}
		}
	}

	public void doRead(List<MPIFile> files) throws Exception {
		int perIteration = perIteration();

		assert(iterNum % perIteration == 0);

		for (int i = 0; i < iterNum; i += perIteration) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				MPIFile f = getFile(files);
				long dataToAccess = getDataPerIteration(elementSize);

				while (dataToAccess > 0) {
					Fileread com = new Fileread();
					ListIO lio = new ListIO();

					com.setFile(f);

					for (int j = 0; j < perIteration; j++) {
						long offset = getBlockOffset();
						long size = getBlockSize(offset);

						size = Math.min(size, dataToAccess);
						dataToAccess -= size;

						lio.addIOOperation(offset, size);

						if (dataToAccess == 0) {
							break;
						}
					}

					com.setListIO(lio);
					aB.addCommand(rank, com);
				}
			}
		}
	}

	public void doReadWrite(List<MPIFile> files) throws Exception {
		int perIteration = perIteration();

		assert(iterNum % perIteration == 0);

		for (int i = 0; i < iterNum; i += perIteration) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				MPIFile f = getFile(files);
				long dataToAccess = getDataPerIteration(elementSize);

				while (dataToAccess > 0) {
					ListIO lio = new ListIO();

					for (int j = 0; j < perIteration; j++) {
						long offset = getBlockOffset();
						long size = getBlockSize(offset);

						size = Math.min(size, dataToAccess);
						dataToAccess -= size;

						lio.addIOOperation(offset, size);

						if (dataToAccess == 0) {
							break;
						}
					}

					if (random.nextBoolean()) {
						Fileread com = new Fileread();

						com.setFile(f);
						com.setListIO(lio);
						aB.addCommand(rank, com);
					} else {
						Filewrite com = new Filewrite();

						com.setFile(f);
						com.setListIO(lio);
						aB.addCommand(rank, com);
					}
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

	public SimulationResults readWriteTest() throws Exception {
		List<MPIFile> files = prepare(false);
		doReadWrite(files);
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
			List<Double> readWriteAvgs = new ArrayList<Double>();
			List<Double> readWriteDevs = new ArrayList<Double>();

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
				double readWriteAvg = 0;
				double readWriteDev = 0;

				for (long seed : seeds) {
					SimulationResults r;
					SimulationResults w;
					SimulationResults rw;

					elementSize = size;

					random = new Random(seed);
					r = readTest();

					random = new Random(seed);
					w = writeTest();

					random = new Random(seed);
					rw = readWriteTest();

					readAvg += r.getVirtualTime().getDouble();
					readDev += Math.pow(r.getVirtualTime().getDouble(), 2);

					writeAvg += w.getVirtualTime().getDouble();
					writeDev += Math.pow(w.getVirtualTime().getDouble(), 2);

					readWriteAvg += rw.getVirtualTime().getDouble();
					readWriteDev += Math.pow(rw.getVirtualTime().getDouble(), 2);
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

				readWriteAvg /= seeds.size();
				readWriteDev /= seeds.size();
				readWriteDev = Math.sqrt(readWriteDev - Math.pow(readWriteAvg, 2));

				res.readWriteAvgs.add(readWriteAvg);
				res.readWriteDevs.add(readWriteDev);
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

				if (res.readWriteAvgs.size() > i) {
					out.write("  " + sizes.get(i) + " RW  " + getDataTotal(sizes.get(i)) + " B, " + res.readWriteAvgs.get(i) + " s (" + res.readWriteDevs.get(i) + " s)\n");
					out.write("  " + sizes.get(i) + " RW  " + (getDataTotal(sizes.get(i)) / res.readWriteAvgs.get(i) / 1024 / 1024) + " MB/s\n");
				}
			}
		}

		out.close();
	}

	public static void main(String[] args) throws Exception {
		new Individual().run();
	}
}
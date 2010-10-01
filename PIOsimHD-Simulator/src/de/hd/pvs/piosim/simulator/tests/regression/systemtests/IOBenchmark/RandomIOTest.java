/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$
 */

//	Copyright (C) 2009 Michael Kuhn, 2010 Julian Kunkel
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
package de.hd.pvs.piosim.simulator.tests.regression.systemtests.IOBenchmark;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.inputOutput.FileDescriptor;
import de.hd.pvs.piosim.simulator.SimulationResults;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.IOC;

/**
 * In the random I/O Test the whole file is read or written, however the
 * order in which reads/writes occur is determined randomly.
 */
public class RandomIOTest extends IOBenchmark {
	final class TestTuple {
		int rank;
		FileDescriptor file;
		long offset;
		long size;

		public TestTuple(int rank, FileDescriptor file, long offset, long size) {
			this.rank = rank;
			this.file = file;
			this.offset = offset;
			this.size = size;
		}
	}

	private Random random = null;



	@Override
	public void doWrite(List<FileDescriptor> files) throws Exception {
		ArrayList<TestTuple> tuples = new ArrayList<TestTuple>();

		final long fileSize = computeFileSize();
		int iterNum = (int)(fileSize / blockSize / clientNum);

		for (int i = 0; i < iterNum; i++) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				for (FileDescriptor f : files) {
					TestTuple tuple = new TestTuple(rank, f, ((i * clientNum) + rank) * blockSize, blockSize);
					tuples.add(tuple);
				}
			}
		}

		Collections.shuffle(tuples, random);

		for (TestTuple t : tuples) {
			pb.addWriteSequential(t.rank, t.file, t.offset, t.size);
		}
	}

	@Override
	public void doRead(List<FileDescriptor> files) throws Exception {
		ArrayList<TestTuple> tuples = new ArrayList<TestTuple>();

		final long fileSize = computeFileSize();
		int iterNum = (int)(fileSize / blockSize / clientNum);

		for (int i = 0; i < iterNum; i++) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				for (FileDescriptor f : files) {
					TestTuple tuple = new TestTuple(rank, f, ((i * clientNum) + rank) * blockSize, blockSize);
					tuples.add(tuple);
				}
			}
		}

		Collections.shuffle(tuples, random);

		for (TestTuple t : tuples) {
			pb.addReadSequential(t.rank, t.file, t.offset, t.size);
		}
	}

	@Test
	public void benchmark() throws Exception {

		final class CacheLayerResults {
			ServerCacheLayer cacheLayer = null;
			List<Double> readAvgs = new ArrayList<Double>();
			List<Double> readDevs = new ArrayList<Double>();
			List<Double> writeAvgs = new ArrayList<Double>();
			List<Double> writeDevs = new ArrayList<Double>();
			List<Long>   fileSizes = new ArrayList<Long>();

			public CacheLayerResults(ServerCacheLayer cacheLayer) {
				this.cacheLayer = cacheLayer;
			}
		}

		List<ServerCacheLayer> cacheLayers = new ArrayList<ServerCacheLayer>();
		List<Long> sizes = new ArrayList<Long>();
		List<Long> seeds = new ArrayList<Long>();

		cacheLayers.add(IOC.SimpleNoCache());
		cacheLayers.add(IOC.SimpleWriteBehindCache());
		cacheLayers.add(IOC.AggregationCache());
		cacheLayers.add(IOC.AggregationReorderCache());

//		sizes.add((long)512);
		sizes.add((long)5 * KBYTE);
		sizes.add((long)50 * KBYTE);
		sizes.add((long)500 * KBYTE);

		for (long i = 0; i < 10; i++) {
			seeds.add(i);
		}

		FileWriter out = new FileWriter("/tmp/randomiotest.txt");
		for (ServerCacheLayer cacheLayer : cacheLayers) {
			CacheLayerResults res = new CacheLayerResults(cacheLayer);

			this.cacheLayer = cacheLayer;

			out.write(res.cacheLayer.getClass().getSimpleName() + "\n");

			for (long size : sizes) {
				double readAvg = 0;
				double readDev = 0;
				double writeAvg = 0;
				double writeDev = 0;

				this.blockSize = size;
				long fileSize = computeFileSize();

				for (long seed : seeds) {
					SimulationResults r;
					SimulationResults w;

					this.random = new Random(seed);

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

				out.write("  " + size + " READ  " + (fileNum * fileSize) + " B, " + readAvg + " s (" + readDev + " s)\n");
				out.write("  " + size + " READ  " + (fileNum * fileSize / readAvg / 1024 / 1024) + " MB/s\n");
				out.write("  " + size + " WRITE " + (fileNum * fileSize) + " B, " + writeAvg + " s (" + writeDev + " s)\n");
				out.write("  " + size + " WRITE " + (fileNum * fileSize / writeAvg / 1024 / 1024) + " MB/s\n");
				out.flush();
			}
		}
		out.close();
	}

	public static void main(String[] args) throws Exception {
		new RandomIOTest().benchmark();
	}
}
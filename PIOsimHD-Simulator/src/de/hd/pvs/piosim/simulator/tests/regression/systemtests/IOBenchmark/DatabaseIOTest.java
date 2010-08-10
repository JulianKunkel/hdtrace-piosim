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
import java.util.List;
import java.util.Random;

import org.junit.Test;

import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.inputOutput.FileDescriptor;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.program.commands.Fileread;
import de.hd.pvs.piosim.model.program.commands.Filewrite;
import de.hd.pvs.piosim.simulator.SimulationResults;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.IOC;

public class DatabaseIOTest extends IOBenchmark {
	private int iterNum = 10;

	private Random random = null;

	final long fileSize;

	public DatabaseIOTest() {
		super();
		fileNum = 10;
		fileSize = 1250 * MBYTE;
	}

	private int perIteration () {
		return 1;
	}

	private long getDataPerIteration (long elementSize) {
		return elementSize * 50 * 50;
	}

	private long getDataTotal (long elementSize) {
		return getDataPerIteration(elementSize) * iterNum * clientNum;
	}

	private FileDescriptor getFile (List<FileDescriptor> files) {
		int i = random.nextInt(files.size());
		return files.get(i);
	}

	private long getBlockOffset () {
		long offset = Math.abs(random.nextLong()) % fileSize;
		long remainder = offset % blockSize;
		return offset - remainder;
	}

	private long getBlockSize (long offset) {
		int i = random.nextInt(100);
		return Math.min((i + 1) * blockSize, fileSize - offset);
	}

	@Override
	public void doWrite(List<FileDescriptor> files) throws Exception {
		int perIteration = perIteration();

		assert(iterNum % perIteration == 0);

		for (int i = 0; i < iterNum; i += perIteration) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				FileDescriptor f = getFile(files);
				long dataToAccess = getDataPerIteration(blockSize);

				while (dataToAccess > 0) {
					Filewrite com = new Filewrite();
					ListIO lio = new ListIO();

					com.setFileDescriptor(f);

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

	@Override
	public void doRead(List<FileDescriptor> files) throws Exception {
		int perIteration = perIteration();

		assert(iterNum % perIteration == 0);

		for (int i = 0; i < iterNum; i += perIteration) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				FileDescriptor f = getFile(files);
				long dataToAccess = getDataPerIteration(blockSize);

				while (dataToAccess > 0) {
					Fileread com = new Fileread();
					ListIO lio = new ListIO();

					com.setFileDescriptor(f);

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

	public void doReadWrite(List<FileDescriptor> files) throws Exception {
		int perIteration = perIteration();

		assert(iterNum % perIteration == 0);

		for (int i = 0; i < iterNum; i += perIteration) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				FileDescriptor f = getFile(files);
				long dataToAccess = getDataPerIteration(blockSize);

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

						com.setFileDescriptor(f);
						com.setListIO(lio);
						aB.addCommand(rank, com);
					} else {
						Filewrite com = new Filewrite();

						com.setFileDescriptor(f);
						com.setListIO(lio);
						aB.addCommand(rank, com);
					}
				}
			}
		}
	}

	@Override
	public SimulationResults writeTest() throws Exception {
		List<FileDescriptor> files = prepareFilelist(false, fileSize);
		doWrite(files);
		closeFiles(files);
		return runSimulationAllExpectedToFinish();
	}

	@Override
	public SimulationResults readTest() throws Exception {
		List<FileDescriptor> files = prepareFilelist(false, fileSize);
		doRead(files);
		closeFiles(files);
		return runSimulationAllExpectedToFinish();
	}

	public SimulationResults readWriteTest() throws Exception {
		List<FileDescriptor> files = prepareFilelist(false, fileSize);
		doReadWrite(files);
		closeFiles(files);
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

		List<ServerCacheLayer> cacheLayers = new ArrayList<ServerCacheLayer>();
		List<Long> sizes = new ArrayList<Long>();
		List<Long> seeds = new ArrayList<Long>();

		cacheLayers.add(IOC.SimpleNoCache());
		cacheLayers.add(IOC.SimpleWriteBehindCache());
		cacheLayers.add(IOC.AggregationCache());
		cacheLayers.add(IOC.AggregationReorderCache());

		sizes.add((long)512);
		sizes.add((long)5 * KBYTE);
		sizes.add((long)50 * KBYTE);
		//		sizes.add((long)512 * KBYTE);

		for (long i = 0; i < 10; i++) {
			seeds.add(i);
		}

		FileWriter out = new FileWriter("/tmp/databaseiotest.txt");

		for (ServerCacheLayer cacheLayer : cacheLayers) {
			CacheLayerResults res = new CacheLayerResults(cacheLayer);

			this.cacheLayer = cacheLayer;


			out.write(res.cacheLayer.getClass().getSimpleName() + "\n");

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

					blockSize = size;

					random = new Random(seed);
					r = readTest();
					w = writeTest();
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

				out.write("  " + size + " READ  " + (fileNum * fileSize) + " B, " + readAvg + " s (" + readDev + " s)\n");
				out.write("  " + size + " READ  " + (fileNum * fileSize / readAvg / 1024 / 1024) + " MB/s\n");
				out.write("  " + size + " WRITE " + (fileNum * fileSize) + " B, " + writeAvg + " s (" + writeDev + " s)\n");
				out.write("  " + size + " WRITE " + (fileNum * fileSize / writeAvg / 1024 / 1024) + " MB/s\n");

				out.write("  " + size + " RW  " + fileSize + " B, " + readWriteAvg + " s (" + readWriteDev + " s)\n");
				out.write("  " + size + " RW  " + (fileSize / readWriteAvg / 1024 / 1024) + " MiB/s\n");

				out.flush();
			}
		}

		out.close();
	}

	public static void main(String[] args) throws Exception {
		new DatabaseIOTest().run();
	}
}
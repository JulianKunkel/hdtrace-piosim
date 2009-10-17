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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerDirectedIO;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;
import de.hd.pvs.piosim.simulator.SimulationResults;
import de.hd.pvs.piosim.simulator.base.ComponentRuntimeInformation;
import de.hd.pvs.piosim.simulator.components.IOSubsystem.GRefinedDiskModel.GRefinedDiskModelInformation;

abstract public class IOTest extends ClusterTest {

	// number of I/O servers
	protected int serverNum = 10;

	// number of I/O clients
	protected int clientNum = 10;

	// number of outer iterations == repeats of the inner loop
	protected int outerIterations = 1;

	// number of data blocks accessed per inner iteration
	protected int innerNonContigIterations = 100;

	// number of files acessed
	protected int fileNum = 1;

	// block size of the contiguous access
	protected long blockSize = 0;

	// PVFS default
	protected long stripeSize = 64 * KBYTE;


	protected long computeFileSize(){
		return blockSize * innerNonContigIterations * outerIterations * clientNum;
	}

	protected ServerCacheLayer cacheLayer = null;

	protected List<MPIFile> prepare(boolean isEmpty) throws Exception {
		List<MPIFile> files = new ArrayList<MPIFile>();

		assert(blockSize > 0);

		testMsg();
		setup(clientNum, serverNum, cacheLayer);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(stripeSize);

		final long fileSize = computeFileSize();

		for (int i = 0; i < fileNum; i++) {
			files.add(aB.createFile("testfile" + i, ((isEmpty) ? 0 : fileSize) , dist));
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

	private void writeTestResults(String type, final long fileSize, final FileWriter out, SimulationResults res) throws IOException{
		final long iosize = (fileNum * fileSize);
		out.write("\n  Config<C,S,Inner,Outer,BS> <" + clientNum + "," + serverNum + "," + innerNonContigIterations + "," + outerIterations + "," + blockSize + ">\n");
		out.write("   " + blockSize + " " +  type + "   " + iosize/1024/1024 + " MiB == " + iosize + " B " + res.getVirtualTime().getDouble() + " s\n");
		out.write("   " + blockSize + " " +  type + "   " + iosize / res.getVirtualTime().getDouble() / 1024 / 1024 + " MiB/s\n");

		long accessedAmount = 0;

		for (ComponentRuntimeInformation info : res.getComponentStatistics().values()) {
			if (info.getClass() == GRefinedDiskModelInformation.class) {
				out.write("    " + info + "\n");
				GRefinedDiskModelInformation diskInfo = (GRefinedDiskModelInformation) info;
				accessedAmount += diskInfo.getTotalAmountOfData();
			}
		}

		out.write("   Accessed Data: " + accessedAmount + " isEqual: " + (accessedAmount == iosize) + "\n");
	}

	@Test
	public void run() throws Exception {
		List<ServerCacheLayer> cacheLayers = new ArrayList<ServerCacheLayer>();
		List<Long> sizes = new ArrayList<Long>();

//		cacheLayers.add(new NoCache());
//		cacheLayers.add(new SimpleWriteBehindCache());
//		cacheLayers.add(new AggregationCache());
		cacheLayers.add(new ServerDirectedIO());

		//		sizes.add((long)512);
		sizes.add((long)5 * KBYTE);
		//sizes.add((long)50 * KBYTE);
		//sizes.add((long)500 * KBYTE);
		//sizes.add((long)5000 * KBYTE);
		final FileWriter out = new FileWriter("/tmp/iotest.txt");

		for (ServerCacheLayer cacheLayer : cacheLayers) {
			this.cacheLayer = cacheLayer;

			out.write(cacheLayer.getClass().getSimpleName() + "\n");

			System.out.println(cacheLayer.getClass().getSimpleName() + "\n");

			for (long size : sizes) {
				blockSize = size;

				final long fileSize = computeFileSize();

				//writeTestResults("READ", fileSize, out, readTest());
				out.flush();
				writeTestResults("WRITE", fileSize, out, writeTest());
				out.flush();
			}

		}

		out.close();
	}
}
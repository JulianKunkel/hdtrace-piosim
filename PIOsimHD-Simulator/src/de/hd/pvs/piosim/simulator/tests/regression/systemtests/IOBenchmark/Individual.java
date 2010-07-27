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
package de.hd.pvs.piosim.simulator.tests.regression.systemtests.IOBenchmark;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.inputOutput.FileDescriptor;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.program.commands.Fileread;
import de.hd.pvs.piosim.model.program.commands.Filewrite;
import de.hd.pvs.piosim.model.program.commands.superclasses.FileIOCommand;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.IOC;

public class Individual extends IOBenchmark {
	@Test
	public void runTest1x1Write() throws Exception{
		final FileWriter out = new FileWriter("/tmp/test-write-1x1.txt");
		runOneTestWrite(IOC.SimpleNoCache(), 4*100000, 1, 1, 1, 1, 1, out);
		out.close();
	}

	@Test
	public void runTest1x1Read() throws Exception{
		final FileWriter out = new FileWriter("/tmp/test-read-1x1.txt");
		runOneTestRead(IOC.SimpleNoCache(), 4*100000, 1, 1, 1, 1, 1, out);
		out.close();
	}


	@Test
	public void runTest2x2Write() throws Exception{
		final FileWriter out = new FileWriter("/tmp/test-write-2x2.txt");
		runOneTestWrite(IOC.SimpleNoCache(), 	MBYTE, 2, 2, 1, 1, 20, out);
		out.close();
	}

	@Test
	public void runTest2x2Read() throws Exception{
		final FileWriter out = new FileWriter("/tmp/test-read-2x2.txt");
		runOneTestRead(IOC.SimpleNoCache(), 	MBYTE, 2, 2, 1, 1, 20, out);
		out.close();
	}



	@Test
	public void runTest2x2AggregationCache() throws Exception{
		final FileWriter out = new FileWriter("/tmp/test-2x2Aggregation.txt");
		runOneTestWrite(IOC.AggregationCache(), MBYTE, 2, 2, 1, 1, 20, out);
		out.close();
	}

	@Test
	public void runTest5x5AggregationCache() throws Exception{
		final FileWriter out = new FileWriter("/tmp/test-5x5Aggregation.txt");
		runOneTestWrite(IOC.AggregationCache(), MBYTE, 5, 5, 1, 1, 50, out);
		out.close();
	}

	@Test
	public void runTest10x10AggregationCache() throws Exception{
		final FileWriter out = new FileWriter("/tmp/test-10x10Aggregation.txt");
		runOneTestWrite(IOC.AggregationCache(),  MBYTE , 10, 10, 1, 1, 100, out);
		out.close();
	}

	@Test
	public void benchmarkServers() throws Exception{
		List<ServerCacheLayer> cacheLayers = new ArrayList<ServerCacheLayer>();
		List<Long> sizes = new ArrayList<Long>();

		cacheLayers.add(IOC.SimpleNoCache());
		cacheLayers.add(IOC.SimpleWriteBehindCache());
		cacheLayers.add(IOC.AggregationCache());
		//cacheLayers.add(new ServerDirectedIO());

		//		sizes.add((long)512);
		sizes.add((long)5 * KBYTE);
		sizes.add((long)50 * KBYTE);
		sizes.add((long)500 * KBYTE);
		sizes.add((long)5000 * KBYTE);


		super.benchmarkServers("/tmp/benchmark.txt", cacheLayers, sizes);
	}

	private void createIOOps(List<FileDescriptor> files, Class ioClass) throws Exception{
		for (int i = 0; i < outerIterations; i += 1) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				for (FileDescriptor f : files) {
					final FileIOCommand com = (FileIOCommand) ioClass.newInstance();

					final ListIO lio = new ListIO();

					com.setFileDescriptor(f);

					for (long j = 0; j < innerNonContigIterations; j++) {
						final long offset = blockSize * (i * innerNonContigIterations * clientNum
							+ j * clientNum + rank);

						lio.addIOOperation(offset, blockSize);
					}

					com.setListIO(lio);
					aB.addCommand(rank, com);
				}
			}
		}
	}

	public void doWrite(List<FileDescriptor> files) throws Exception {
		createIOOps(files, Filewrite.class);
	}

	public void doRead(List<FileDescriptor> files) throws Exception {
		createIOOps(files, Fileread.class);
	}

	public static void main(String[] args) throws Exception {
		new Individual().benchmarkServers();
	}
}
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
package de.hd.pvs.piosim.simulator.tests.regression.systemtests.ClusterIOTest;

import java.io.FileWriter;
import java.util.List;

import org.junit.Test;

import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.NoCache;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.program.commands.Fileread;
import de.hd.pvs.piosim.model.program.commands.Filewrite;
import de.hd.pvs.piosim.model.program.commands.superclasses.FileIOCommand;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.IOTest;

public class Individual extends IOTest {
	@Test
	public void runTest1x1Write() throws Exception{
		final FileWriter out = new FileWriter("/tmp/test-write-1x1.txt");
		runOneTestWrite(new NoCache(),
				4*100000, 1, 1, 1, 1, 1, out);
		out.close();
	}

	@Test
	public void runTest1x1Read() throws Exception{
		final FileWriter out = new FileWriter("/tmp/test-read-1x1.txt");
		runOneTestRead(new NoCache(),
				4*100000, 1, 1, 1, 1, 1, out);
		out.close();
	}


	@Test
	public void runTest2x2Write() throws Exception{
		final FileWriter out = new FileWriter("/tmp/test-write-2x2.txt");
		runOneTestWrite(new NoCache(), 	MBYTE, 2, 2, 1, 1, 20, out);
		out.close();
	}

	@Test
	public void runTest2x2Read() throws Exception{
		final FileWriter out = new FileWriter("/tmp/test-read-2x2.txt");
		runOneTestRead(new NoCache(), 	MBYTE, 2, 2, 1, 1, 20, out);
		out.close();
	}



	@Test
	public void runTest2x2AggregationCache() throws Exception{
		final FileWriter out = new FileWriter("/tmp/test-2x2Aggregation.txt");
		runOneTestWrite(new AggregationCache(), MBYTE, 2, 2, 1, 1, 20, out);
		out.close();
	}

	@Test
	public void runTest5x5AggregationCache() throws Exception{
		final FileWriter out = new FileWriter("/tmp/test-5x5Aggregation.txt");
		runOneTestWrite(new AggregationCache(), MBYTE, 5, 5, 1, 1, 50, out);
		out.close();
	}

	@Test
	public void runTest10x10AggregationCache() throws Exception{
		final FileWriter out = new FileWriter("/tmp/test-10x10Aggregation.txt");
		runOneTestWrite(new AggregationCache(),  MBYTE , 10, 10, 1, 1, 100, out);
		out.close();
	}

	@Test
	public void benchmarkServers() throws Exception{
		super.benchmarkServers();
	}

	private void createIOOps(List<MPIFile> files, Class ioClass) throws Exception{
		for (int i = 0; i < outerIterations; i += 1) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				for (MPIFile f : files) {
					final FileIOCommand com = (FileIOCommand) ioClass.newInstance();

					final ListIO lio = new ListIO();

					com.setFile(f);

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

	public void doWrite(List<MPIFile> files) throws Exception {
		createIOOps(files, Filewrite.class);
	}

	public void doRead(List<MPIFile> files) throws Exception {
		createIOOps(files, Fileread.class);
	}

	public static void main(String[] args) throws Exception {
		new Individual().benchmarkServers();
	}
}
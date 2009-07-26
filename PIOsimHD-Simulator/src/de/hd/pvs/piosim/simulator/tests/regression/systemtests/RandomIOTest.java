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

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;

import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;
import de.hd.pvs.piosim.simulator.SimulationResults;

public class RandomIOTest extends ClusterTest {
	final class TestTuple {
		int rank;
		MPIFile file;
		long offset;
		long size;

		public TestTuple(int rank, MPIFile file, long offset, long size) {
			this.rank = rank;
			this.file = file;
			this.offset = offset;
			this.size = size;
		}
	}

	int serverNum = 5;
	int clientNum = 10;
	int fileNum = 10;
	int iterNum = 10;
	long elementSize = 4 * KBYTE;

	@Test
	public SimulationResults writeTest() throws Exception {
		ArrayList<TestTuple> tuples = new ArrayList<TestTuple>();
		ArrayList<MPIFile> files = new ArrayList<MPIFile>();

		testMsg();
		setup(clientNum, serverNum);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(elementSize);

		for (int i = 0; i < fileNum; i++) {
			files.add(aB.createFile("testfile" + i, 0, dist));
		}

		for (int i = 0; i < fileNum; i++) {
			pb.addFileOpen(files.get(i), world, true);
		}

		for (int i = 0; i < iterNum; i++) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				for (MPIFile f : files) {
					TestTuple tuple = new TestTuple(i, f, ((i * clientNum) + rank) * elementSize, elementSize);
					tuples.add(tuple);
				}
			}
		}

		Collections.shuffle(tuples);

		for (TestTuple t : tuples) {
			pb.addWriteSequential(t.rank, t.file, t.offset, t.size);
		}

		Collections.shuffle(tuples);

		for (TestTuple t : tuples) {
			pb.addReadSequential(t.rank, t.file, t.offset, t.size);
		}

		return runSimulationAllExpectedToFinish();
	}

	public static void main(String[] args) throws Exception {
		final int iterations = 10;

		double time = 0;
		RandomIOTest t = new RandomIOTest();

		for (int i = 0; i < iterations; i++) {
			time += t.writeTest().getVirtualTime().getDouble();
		}

		System.out.println(iterations + " runs: " + time + "s, " + (time / iterations) + "s avg");
	}
}
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
import java.util.List;
import java.util.Random;

import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.simulator.SimulationResults;

public class RandomIOTest extends IOTest {
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

	public SimulationResults writeTest() throws Exception {
		List<MPIFile> files = prepare(true);
		ArrayList<TestTuple> tuples = new ArrayList<TestTuple>();

		for (int i = 0; i < iterNum; i++) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				for (MPIFile f : files) {
					TestTuple tuple = new TestTuple(rank, f, ((i * clientNum) + rank) * elementSize, elementSize);
					tuples.add(tuple);
				}
			}
		}

		Collections.shuffle(tuples, new Random(23));

		for (TestTuple t : tuples) {
			pb.addWriteSequential(t.rank, t.file, t.offset, t.size);
		}

		return super.writeTest();
	}

	public SimulationResults readTest() throws Exception {
		List<MPIFile> files = prepare(false);
		ArrayList<TestTuple> tuples = new ArrayList<TestTuple>();

		for (int i = 0; i < iterNum; i++) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				for (MPIFile f : files) {
					TestTuple tuple = new TestTuple(rank, f, ((i * clientNum) + rank) * elementSize, elementSize);
					tuples.add(tuple);
				}
			}
		}

		Collections.shuffle(tuples, new Random(42));

		for (TestTuple t : tuples) {
			pb.addReadSequential(t.rank, t.file, t.offset, t.size);
		}

		return super.readTest();
	}

	public static void main(String[] args) throws Exception {
		new RandomIOTest().run();
	}
}
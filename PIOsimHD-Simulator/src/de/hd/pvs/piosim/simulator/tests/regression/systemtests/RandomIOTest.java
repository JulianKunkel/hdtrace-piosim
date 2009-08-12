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

import org.junit.Test;

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

	private Random random = null;

	public void doWrite(List<MPIFile> files) throws Exception {
		ArrayList<TestTuple> tuples = new ArrayList<TestTuple>();

		for (int i = 0; i < iterNum; i++) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				for (MPIFile f : files) {
					TestTuple tuple = new TestTuple(rank, f, ((i * clientNum) + rank) * elementSize, elementSize);
					tuples.add(tuple);
				}
			}
		}

		Collections.shuffle(tuples, random);

		for (TestTuple t : tuples) {
			pb.addWriteSequential(t.rank, t.file, t.offset, t.size);
		}
	}

	public void doRead(List<MPIFile> files) throws Exception {
		ArrayList<TestTuple> tuples = new ArrayList<TestTuple>();

		for (int i = 0; i < iterNum; i++) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				for (MPIFile f : files) {
					TestTuple tuple = new TestTuple(rank, f, ((i * clientNum) + rank) * elementSize, elementSize);
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
	public void run() throws Exception {
		List<Long> sizes = new ArrayList<Long>();
		List<Long> seeds = new ArrayList<Long>();
		List<Double> readAvgs = new ArrayList<Double>();
		List<Double> readDevs = new ArrayList<Double>();
		List<Double> writeAvgs = new ArrayList<Double>();
		List<Double> writeDevs = new ArrayList<Double>();

		sizes.add((long)512);
		sizes.add((long)5 * KBYTE);
		sizes.add((long)50 * KBYTE);
		sizes.add((long)512 * KBYTE);

		for (long i = 0; i < 100; i++) {
			seeds.add(i);
		}

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

			readAvgs.add(readAvg);
			readDevs.add(readDev);

			writeAvg /= seeds.size();
			writeDev /= seeds.size();
			writeDev = Math.sqrt(writeDev - Math.pow(writeAvg, 2));

			writeAvgs.add(writeAvg);
			writeDevs.add(writeDev);
		}

		for (int i = 0; i < sizes.size(); i++) {
			System.out.println(sizes.get(i) + " READ  " + readAvgs.get(i) + "s (" + readDevs.get(i) + "s)");
			System.out.println(sizes.get(i) + " WRITE " + writeAvgs.get(i) + "s (" + writeDevs.get(i) + "s)");
		}
	}

	public static void main(String[] args) throws Exception {
		new RandomIOTest().run();
	}
}
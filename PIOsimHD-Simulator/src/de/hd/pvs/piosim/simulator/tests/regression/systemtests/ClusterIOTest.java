/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$
 */

//	Copyright (C) 2008, 2009 Julian M. Kunkel
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
import java.util.HashMap;

import org.junit.Test;

import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;
import de.hd.pvs.piosim.simulator.SimulationResults;

public class ClusterIOTest extends ClusterTest {
	int serverNum = 5;
	int clientNum = 10;
	int fileNum = 1;
	int iterNum = 100;
	long elementSize = 512;
//	long elementSize = 50 * KBYTE;
//	long elementSize = 5 * MBYTE;
	// PVFS default
	long stripeSize = 64 * KBYTE;
	ArrayList<MPIFile> files = new ArrayList<MPIFile>();

	private void prepare(boolean isWrite) throws Exception{
		testMsg();
		setup(clientNum, serverNum);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(stripeSize);

		for (int i = 0; i < fileNum; i++) {
			files.add(aB.createFile("testfile" + i, (isWrite) ? 0 : (clientNum * iterNum) * elementSize, dist));
		}

		for (int i = 0; i < fileNum; i++) {
			pb.addFileOpen(files.get(i), world, isWrite);
		}
	}

	@Test
	public SimulationResults writeTest() throws Exception {
		prepare(true);

		for (int i = 0; i < iterNum; i++) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				for (MPIFile f : files) {
					pb.addWriteSequential(rank, f, ((i * clientNum) + rank) * elementSize, elementSize);
				}
			}
		}

		for (int i = 0; i < iterNum; i++) {
			for (MPIFile file : files) {
				HashMap<Integer, ListIO> io = new HashMap<Integer, ListIO>();

				for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
					ListIO list = new ListIO();
					list.addIOOperation(((i * clientNum) + rank) * elementSize, elementSize);
					io.put(rank, list);
				}

//				pb.addWriteCollective(aB.getWorldCommunicator(), file, io);
			}
		}

		return runSimulationAllExpectedToFinish();
	}

	@Test
	public SimulationResults readTest() throws Exception {
		prepare(false);

		for (int i = 0; i < iterNum; i++) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				for (MPIFile f : files) {
					pb.addReadSequential(rank, f, ((i * clientNum) + rank) * elementSize, elementSize);
				}
			}
		}

		for (int i = 0; i < iterNum; i++) {
			for (MPIFile file : files) {
				HashMap<Integer, ListIO> io = new HashMap<Integer, ListIO>();

				for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
					ListIO list = new ListIO();
					list.addIOOperation(((i * clientNum) + rank) * elementSize, elementSize);
					io.put(rank, list);
				}

//				pb.addReadCollective(aB.getWorldCommunicator(), file, io);
			}
		}

		return runSimulationAllExpectedToFinish();
	}

	public static void main(String[] args) throws Exception {
		final int iterations = 10;

		double writeTime = 0;
		double readTime = 0;

		ClusterIOTest t;

		for (int i = 0; i < iterations; i++) {
			t = new ClusterIOTest();
			writeTime += t.writeTest().getVirtualTime().getDouble();
		}

		for (int i = 0; i < iterations; i++) {
			t = new ClusterIOTest();
			readTime += t.readTest().getVirtualTime().getDouble();
		}

		System.out.println("WRITE " + iterations + " runs: " + writeTime + "s, " + (writeTime / iterations) + "s avg");
		System.out.println("READ  " + iterations + " runs: " + readTime + "s, " + (readTime / iterations) + "s avg");
	}
}
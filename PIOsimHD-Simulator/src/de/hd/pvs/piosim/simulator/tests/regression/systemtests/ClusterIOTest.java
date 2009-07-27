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
	int serverNum = 1;
	int clientNum = 4;
	int fileNum = 1;
	int iterNum = 1;
	long elementSize = 512;
//	long elementSize = 50 * KBYTE;
//	long elementSize = 5 * MBYTE;
	// PVFS default
	long stripeSize = 64 * KBYTE;

	@Test
	public SimulationResults writeTest() throws Exception {
		ArrayList<MPIFile> files = new ArrayList<MPIFile>();

		testMsg();
		setup(clientNum, serverNum);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(stripeSize);

		for (int i = 0; i < fileNum; i++) {
			files.add(aB.createFile("testfile" + i, 0, dist));
			System.out.println("File size: " + files.get(files.size() -1).getSize() );
		}

		for (int i = 0; i < fileNum; i++) {
			pb.addFileOpen(files.get(i), world, true);
		}

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
		ArrayList<MPIFile> files = new ArrayList<MPIFile>();

		testMsg();
		setup(clientNum, serverNum);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(stripeSize);

		for (int i = 0; i < fileNum; i++) {
			files.add(aB.createFile("testfile" + i, (clientNum * iterNum) * elementSize, dist));
		}

		for (int i = 0; i < fileNum; i++) {
			pb.addFileOpen(files.get(i), world, false);
		}

		for (int i = 0; i < iterNum; i++) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				for (MPIFile f : files) {
					pb.addReadSequential(rank, f, ((i * clientNum) + rank) * elementSize, elementSize);
				}
			}
		}

		return runSimulationAllExpectedToFinish();
	}

	public static void main(String[] args) throws Exception {
		SimulationResults writeRes = null;
		SimulationResults readRes = null;

		ClusterIOTest t;

		t = new ClusterIOTest();
		writeRes = t.writeTest();

		t = new ClusterIOTest();
		//readRes = t.readTest();

		if (writeRes != null) {
			System.out.println("WRITE " + writeRes.getVirtualTime().getDouble() + "s");
		}

		if (readRes != null) {
			System.out.println("READ  " + readRes.getVirtualTime().getDouble() + "s");
		}
	}
}
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

import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;

public class ClusterIOTest extends ClusterTest {
	int serverNum = 5;
	int clientNum = 10;
	int fileNum = 10;
	int iterNum = 10;
	long elementSize = 4 * MBYTE;

	@Test
	public void writeTest() throws Exception {
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
//					pb.addWriteSequential(rank, f, ((i * clientNum) + rank) * elementSize, elementSize);
//					pb.addReadSequential(rank, f, ((i * clientNum) + rank) * elementSize, elementSize);
				}
			}
		}

		for (int i = 0; i < iterNum; i++) {
			for (MPIFile file : files) {
				HashMap<Integer, Long> offsets = new HashMap<Integer, Long>();
				HashMap<Integer, Long> sizes = new HashMap<Integer, Long>();

				for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
					offsets.put(rank, (long) ((i * clientNum) + rank) * elementSize);
					sizes.put(rank, (long) elementSize);
				}

				pb.addWriteCollective(aB.getWorldCommunicator(), file, offsets, sizes);
				pb.addReadCollective(aB.getWorldCommunicator(), file, offsets, sizes);
			}
		}

		runSimulationAllExpectedToFinish();
	}

	public static void main(String[] args) throws Exception {
		ClusterIOTest t = new ClusterIOTest();
		t.writeTest();
	}
}
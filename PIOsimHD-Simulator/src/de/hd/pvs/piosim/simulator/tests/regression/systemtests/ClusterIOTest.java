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

public class ClusterIOTest extends ClusterTest {
	int serverNum = 5;
	int clientNum = 10;
	int fileNum = 10;
	int iterNum = 10;
	long elementSize = 4 * MBYTE;
	ArrayList<MPIFile> files = new ArrayList<MPIFile>();

	private void prepare() throws Exception{
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
	}


	@Test
	public void writeTest() throws Exception {
		prepare();

		for (int i = 0; i < iterNum; i++) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				for (MPIFile f : files) {
//					pb.addWriteSequential(rank, f, ((i * clientNum) + rank) * elementSize, elementSize);
//					pb.addWriteSequential(rank, f, (((i + 1) * clientNum) + rank) * elementSize, elementSize);
//					pb.addWriteSequential(rank, f, (((i + 2) * clientNum) + rank) * elementSize, elementSize);
//					pb.addReadSequential(rank, f, ((i * clientNum) + rank) * elementSize, elementSize);
//					pb.addReadSequential(rank, f, (((i + 1) * clientNum) + rank) * elementSize, elementSize);
//					pb.addReadSequential(rank, f, (((i + 2) * clientNum) + rank) * elementSize, elementSize);
				}
			}
		}

		for (int i = 0; i < iterNum; i++) {
			for (MPIFile file : files) {
				HashMap<Integer, ListIO> io = new HashMap<Integer, ListIO>();

				for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
					ListIO list = new ListIO();
					list.addIOOperation(((i * clientNum) + rank) * elementSize, elementSize);
//					list.addIOOperation((((i + 1) * clientNum) + rank) * elementSize, elementSize);
					list.addIOOperation((((i + 2) * clientNum) + rank) * elementSize, elementSize);
					io.put(rank, list);
				}

				pb.addWriteCollective(aB.getWorldCommunicator(), file, io);
//				pb.addReadCollective(aB.getWorldCommunicator(), file, io);
			}
		}

		runSimulationAllExpectedToFinish();
	}

	public static void main(String[] args) throws Exception {
		ClusterIOTest t = new ClusterIOTest();
		t.writeTest();
	}
}
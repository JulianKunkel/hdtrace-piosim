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
import java.util.Collections;

import org.junit.Test;

import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;

public class ClusterIOTest extends ClusterTest {
	int clientNum = 10;
	int fileNum = 10;
	int iterNum = 100;
	long elementSize = 4 * KBYTE;

	@Test
	public void writeTest() throws Exception {
		ArrayList<MPIFile> files = new ArrayList<MPIFile>();
		ArrayList<Integer> clients = new ArrayList<Integer>();

		testMsg();
		setup(clientNum, 1);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(MBYTE);

		for (int i = 0; i < fileNum; i++) {
			files.add(aB.createFile("testfile" + i, 0, dist));
		}

		for (int i = 0; i < fileNum; i++) {
			pb.addFileOpen(files.get(i), world, true);
		}

		for (int i = 0; i < clientNum; i++) {
			clients.add(i);
		}

		for (int i = 0; i < iterNum; i++) {
			Collections.shuffle(clients);

			for (int j : clients) {
				Collections.shuffle(files);

				for (MPIFile f : files) {
					pb.addWriteSequential(j, f, ((i * clientNum) + j)
							* elementSize, elementSize);
				}
			}
		}

		runSimulationAllExpectedToFinish();
	}

	public static void main(String[] args) throws Exception {
		ClusterIOTest t = new ClusterIOTest();
		t.writeTest();
	}
}
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
package de.hd.pvs.piosim.simulator.tests.regression.systemtests;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;
import de.hd.pvs.piosim.simulator.SimulationResults;

public class IOTest extends ClusterTest {
	int serverNum = 5;
	int clientNum = 10;
	int fileNum = 1;
	int iterNum = 100;
	long elementSize = 512;
//	long elementSize = 50 * KBYTE;
//	long elementSize = 5 * MBYTE;
	// PVFS default
	long stripeSize = 64 * KBYTE;

	protected List<MPIFile> prepare(boolean isWrite) throws Exception {
		List<MPIFile> files = new ArrayList<MPIFile>();

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

		return files;
	}

	public SimulationResults writeTest() throws Exception {
		return runSimulationAllExpectedToFinish();
	}

	public SimulationResults readTest() throws Exception {
		return runSimulationAllExpectedToFinish();
	}

	@Test
	public void run() throws Exception {
		SimulationResults writeRes = null;
		SimulationResults readRes = null;

		writeRes = writeTest();
		readRes = readTest();

		if (writeRes != null) {
			System.out.println("WRITE " + writeRes.getVirtualTime().getDouble() + "s");
		}

		if (readRes != null) {
			System.out.println("READ  " + readRes.getVirtualTime().getDouble() + "s");
		}
	}
}
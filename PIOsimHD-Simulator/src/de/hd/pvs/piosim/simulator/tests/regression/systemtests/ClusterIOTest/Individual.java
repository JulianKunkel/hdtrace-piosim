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

import java.util.List;

import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.program.commands.Fileread;
import de.hd.pvs.piosim.model.program.commands.Filewrite;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.IOTest;

public class Individual extends IOTest {
	private int perIteration () {
		return 1;
	}

	public void doWrite(List<MPIFile> files) throws Exception {
		int perIteration = perIteration();

		assert(iterNum % perIteration == 0);

		for (int i = 0; i < iterNum; i += perIteration) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				for (MPIFile f : files) {
					Filewrite com = new Filewrite();
					ListIO lio = new ListIO();

					com.setFile(f);

					for (int j = 0; j < perIteration; j++) {
						lio.addIOOperation((((i + j) * clientNum) + rank) * elementSize, elementSize);
					}

					com.setListIO(lio);
					aB.addCommand(rank, com);
				}
			}
		}
	}

	public void doRead(List<MPIFile> files) throws Exception {
		int perIteration = perIteration();

		assert(iterNum % perIteration == 0);

		for (int i = 0; i < iterNum; i += perIteration) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				for (MPIFile f : files) {
					Fileread com = new Fileread();
					ListIO lio = new ListIO();

					com.setFile(f);

					for (int j = 0; j < perIteration; j++) {
						lio.addIOOperation((((i + j) * clientNum) + rank) * elementSize, elementSize);
					}

					com.setListIO(lio);
					aB.addCommand(rank, com);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		new Individual().run();
	}
}
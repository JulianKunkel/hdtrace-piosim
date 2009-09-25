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
package de.hd.pvs.piosim.simulator.tests.regression.systemtests.ClusterIOTest;

import java.util.HashMap;
import java.util.List;

import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.IOTest;

public class Collective extends IOTest {
	private int perIteration () {
		return (int)(fileSize / elementSize / clientNum);
	}

	public void doWrite(List<MPIFile> files) throws Exception {
		int perIteration = perIteration();
		int iterNum = (int)(fileSize / elementSize / clientNum);

		//assert(iterNum % perIteration == 0);

		for (MPIFile file : files) {
			for (int i = 0; i < iterNum; i += perIteration) {
				HashMap<Integer, ListIO> io = new HashMap<Integer, ListIO>();

				for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
					io.put(rank, new ListIO());
				}

				for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
					for (int j = 0; j < perIteration; j++) {
						io.get(rank).addIOOperation((((i + j) * clientNum) + rank) * elementSize, elementSize);
					}
				}

				pb.addWriteCollective(aB.getWorldCommunicator(), file, io);
			}
		}
	}

	public void doRead(List<MPIFile> files) throws Exception {
		int perIteration = perIteration();
		int iterNum = (int)(fileSize / elementSize / clientNum);

		//assert(iterNum % perIteration == 0);

		for (MPIFile file : files) {
			for (int i = 0; i < iterNum; i += perIteration) {
				HashMap<Integer, ListIO> io = new HashMap<Integer, ListIO>();

				for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
					io.put(rank, new ListIO());
				}

				for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
					for (int j = 0; j < perIteration; j++) {
						io.get(rank).addIOOperation((((i + j) * clientNum) + rank) * elementSize, elementSize);
					}
				}

				pb.addReadCollective(aB.getWorldCommunicator(), file, io);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		new Collective().run();
	}
}
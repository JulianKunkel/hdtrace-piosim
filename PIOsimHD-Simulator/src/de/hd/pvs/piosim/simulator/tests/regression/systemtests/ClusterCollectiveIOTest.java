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

import java.util.HashMap;
import java.util.List;

import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.simulator.SimulationResults;

public class ClusterCollectiveIOTest extends IOTest {
	public SimulationResults writeTest() throws Exception {
		List<MPIFile> files = prepare(true);

		for (MPIFile file : files) {
			HashMap<Integer, ListIO> io = new HashMap<Integer, ListIO>();

			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				io.put(rank, new ListIO());
			}

			for (int i = 0; i < iterNum; i++) {
				for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
					io.get(rank).addIOOperation(((i * clientNum) + rank) * elementSize, elementSize);
				}
			}

			pb.addWriteCollective(aB.getWorldCommunicator(), file, io);
		}

		return super.writeTest();
	}

	public SimulationResults readTest() throws Exception {
		List<MPIFile> files = prepare(false);

		for (MPIFile file : files) {
			HashMap<Integer, ListIO> io = new HashMap<Integer, ListIO>();

			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				io.put(rank, new ListIO());
			}

			for (int i = 0; i < iterNum; i++) {
				for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
					io.get(rank).addIOOperation(((i * clientNum) + rank) * elementSize, elementSize);
				}
			}

			pb.addReadCollective(aB.getWorldCommunicator(), file, io);
		}

		return super.readTest();
	}

	public static void main(String[] args) throws Exception {
		new ClusterCollectiveIOTest().run();
	}
}
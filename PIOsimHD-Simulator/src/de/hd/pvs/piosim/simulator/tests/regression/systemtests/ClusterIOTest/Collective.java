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
	int collectiveOperationsPerIteration = 100;

	private HashMap<Integer, ListIO>  prepareIOList(final int outerIter, final int collectiveOperationsPerIteration){
		final HashMap<Integer, ListIO> io = new HashMap<Integer, ListIO>();

		for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
			io.put(rank, new ListIO());
		}

		for (int iter = 0; iter < collectiveOperationsPerIteration; iter++) {
			for (Integer rank : aB.getWorldCommunicator().getParticipatingRanks()) {
				io.get(rank).addIOOperation((( (outerIter * collectiveOperationsPerIteration + iter) * clientNum) + rank) * elementSize, elementSize);
			}
		}
		return io;
	}

	public void doWrite(List<MPIFile> files) throws Exception {
		int iterNum = (int)(fileSize / elementSize / clientNum);

		int maxIter;
		if(collectiveOperationsPerIteration <= 0){
			collectiveOperationsPerIteration = iterNum;
			maxIter = 1;
		}else{
			if(iterNum % collectiveOperationsPerIteration != 0){
				throw new IllegalArgumentException("Running with:" + iterNum + " " + collectiveOperationsPerIteration);
			}
			maxIter = iterNum / collectiveOperationsPerIteration;
		}

		for (MPIFile file : files) {
			for (int curIter = 0; curIter < maxIter; curIter++){
				HashMap<Integer, ListIO> io =  prepareIOList(curIter, collectiveOperationsPerIteration);

				pb.addWriteCollective(aB.getWorldCommunicator(), file, io);
			}
		}
	}

	public void doRead(List<MPIFile> files) throws Exception {
		int iterNum = (int)(fileSize / elementSize / clientNum);

		int maxIter;
		if(collectiveOperationsPerIteration <= 0){
			collectiveOperationsPerIteration = iterNum;
			maxIter = 1;
		}else{
			if(iterNum % collectiveOperationsPerIteration != 0){
				throw new IllegalArgumentException("Running with:" + iterNum + " " + collectiveOperationsPerIteration);
			}
			maxIter = iterNum / collectiveOperationsPerIteration;
		}

		for (MPIFile file : files) {
			for (int curIter = 0; curIter < maxIter; curIter++){
				HashMap<Integer, ListIO> io =  prepareIOList(curIter, collectiveOperationsPerIteration);

				pb.addReadCollective(aB.getWorldCommunicator(), file, io);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		new Collective().run();
	}
}
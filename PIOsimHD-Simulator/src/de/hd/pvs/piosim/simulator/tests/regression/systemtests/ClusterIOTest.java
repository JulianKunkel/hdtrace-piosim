
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

import org.junit.Test;

import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;

public class ClusterIOTest  extends ClusterTest {

	@Test public void writeTest() throws Exception{
		testMsg();		
		setup(1, 1);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(MBYTE);
		
		MPIFile file =  aB.createFile("testfile", 0, dist);
		
		pb.addFileOpen(file, world, true);
		
		pb.addWriteSequential(0, file, 0, 4);
		pb.addWriteSequential(0, file, 4, 4);
		pb.addWriteSequential(0, file, 8, 32);
		runSimulationAllExpectedToFinish();
	}


	public static void main(String[] args) throws Exception{
		ClusterIOTest t = new ClusterIOTest();
		t.writeTest();
	}
}

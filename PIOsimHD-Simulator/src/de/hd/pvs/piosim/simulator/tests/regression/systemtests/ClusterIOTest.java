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


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

public class NormalCommandsClusterTest extends ClusterTest{

	@Test public void barrierTest() throws Exception{
		testMsg();
		int cnt = 10;
		
		double [] times = new double[cnt+1]; 
		
		for(int i=1; i <= cnt; i++){
			setup(i, 0);
		
			pb.addBarrier(world);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}
		
		System.out.println("Barrier timing:");
		
		for(int i=1; i <= cnt; i++){
			System.out.println(i + " " + times[i]);
		}
	}
	

	@Test public void reduceTest() throws Exception{
		testMsg();
		int cnt = 10;
		
		double [] times = new double[cnt+1]; 
				
		for(int i=1; i <= cnt; i++){
			setup(i, 0);
		
			pb.addReduce(world, 0, 100);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}
		
		System.out.println("Reduce timing:");
		
		for(int i=1; i <= cnt; i++){
			System.out.println(i + " " + times[i]);
		}
	}


	@Test public void bcastTest() throws Exception{
		testMsg();
		int cnt = 10;
		
		double [] times = new double[cnt+1]; 
				
		for(int i=1; i <= cnt; i++){
			setup(i, 0);
		
			pb.addBroadcast(world, 0, KBYTE);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}
		
		System.out.println("Broadcast timing:");
		
		for(int i=1; i <= cnt; i++){
			System.out.println(i + " " + times[i]);
		}
	}



	@Test public void allreduceTest() throws Exception{
		testMsg();
		int cnt = 10;
		
		double [] times = new double[cnt+1]; 
		
		for(int i=1; i <= cnt; i++){
			setup(i, 0);
		
			pb.addAllreduce(world, MBYTE);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}
		
		System.out.println("Allreduce timing:");
		
		for(int i=1; i <= cnt; i++){
			System.out.println(i + " " + times[i]);
		}
	}
	
	public static void main(String[] args) throws Exception {
		NormalCommandsClusterTest t = new NormalCommandsClusterTest();
		//t.allreduceTest();
		t.reduceTest();
	}
}

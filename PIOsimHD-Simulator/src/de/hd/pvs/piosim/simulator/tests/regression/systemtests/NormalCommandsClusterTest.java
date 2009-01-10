
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

	public int maxClient = 10;
	public int minClient = 1;
	
	double [] times = new double[maxClient+1];
	
	private void printTiming(String header, double[] times){
		System.out.println(header + " timing:");
		
		for(int i=minClient; i <= maxClient; i++){
			System.out.println(i + " " + times[i]);
		}		
	}
	
	@Test public void barrierTest() throws Exception{
		testMsg();		
		for(int i=minClient; i <= maxClient; i++){
			setup(i, 0);
		
			pb.addBarrier(world);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}
		
		printTiming("Barrier", times);
	}
	

	@Test public void reduceTest() throws Exception{
		testMsg();
		for(int i=minClient; i <= maxClient; i++){
			setup(i, 0);
		
			pb.addReduce(world, 0, 100);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}
		
		printTiming("Reduce", times);
	}


	@Test public void bcastTest() throws Exception{
		testMsg();
		
		for(int i=minClient; i <= maxClient; i++){
			setup(i, 0);
		
			pb.addBroadcast(world, 0, KBYTE);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}
		
		printTiming("Broadcast", times);
	}



	@Test public void allreduceTest() throws Exception{
		testMsg();
			
		for(int i=minClient; i <= maxClient; i++){
			setup(i, 0);
		
			pb.addAllreduce(world, MBYTE);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}
		
		printTiming("Allreduce", times);
	}
	
	public void testBinary(){

		final int commSize = 12;
		for (int i=0 ; i < commSize; i++){
			
			// real loop
			final int clientRankInComm = i;
			final int iterations = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(commSize-1);
			final int trailingZeros = Integer.numberOfTrailingZeros(clientRankInComm);
			final int phaseStart = iterations - trailingZeros;
			
			//int numberOfOnes = 0;		
			//for(int bit=0; bit < iterations; bit++){
			//numberOfOnes += (clientRankInComm & 1<<bit) > 0 ? 1 : 0;
			//}
			
			
			if(clientRankInComm != 0){				
				// recv first, then send.
				System.out.println(clientRankInComm + " phaseStart: " + phaseStart +" tz:" + trailingZeros + " send to: " + 
						(clientRankInComm ^ 1<<trailingZeros));
				
				
				for (int iter = iterations - 1 - phaseStart ; iter >= 0 ; iter--){
					int target = (1<<iter | clientRankInComm);
					if (target >= commSize) continue;
					System.out.println(clientRankInComm +" from " + target );				
				}
			}else{
				// send all				
				for (int iter = iterations-1 ; iter >= 0 ; iter--){
					System.out.println(clientRankInComm +" from " + (1<<iter | clientRankInComm) );				
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		NormalCommandsClusterTest t = new NormalCommandsClusterTest();
		t.minClient = 10;
		
		t.allreduceTest();
		//t.bcastTest();
		//t.reduceTest();
		
	}
}

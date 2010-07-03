
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

import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;

public class NormalCommandsClusterTest extends ClusterTest{
	public int minClient = 1;

	public int maxClient = 10;

	double [] times = new double[maxClient+1];

	private void printTiming(String header, double[] times) throws IOException{
		final FileWriter fo = new FileWriter("/tmp/timing-" + this.getClass().getSimpleName() + ".txt", true);

		fo.write(header + " timing:\n");

		for(int i=minClient; i <= maxClient; i++){
			fo.write(i + " " + times[i] + "\n");
		}
		fo.write("\n");
		fo.close();
	}


	@Test public void sendAndRecvEagerTest() throws Exception{
		printStack();
		setup(2, 0);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);

		pb.addSendAndRecv(world, 0, 1, 100 * KBYTE, 1);

		runSimulationAllExpectedToFinish();
	}

	@Test public void sendAndRecvTest() throws Exception{
		printStack();
		setup(2, 0);

		pb.addSendAndRecv(world, 0, 1, 1 * MBYTE, 1);

		runSimulationAllExpectedToFinish();
	}

	@Test public void sendAndRecvCrossTest() throws Exception{
		printStack();
		setup(10, 0);

		pb.addSendAndRecv(world, 0, 1, 1 * MBYTE, 1);
		pb.addSendAndRecv(world, 1, 0, 1 * MBYTE, 1);

		runSimulationAllExpectedToFinish();
	}


	@Test public void sendRecvEagerTest() throws Exception{
		printStack();
		setup(2, 0);

		getGlobalSettings().setMaxEagerSendSize(200 * KBYTE);

		pb.addSendRecv(world, 0, 1, 1, 200 * KBYTE, 1, 1);
		pb.addSendRecv(world, 1, 0, 0, 100 * KBYTE, 1, 1);

		runSimulationAllExpectedToFinish();
	}


	@Test public void sendRecvOneRendevouzTest() throws Exception{
		printStack();
		setup(2, 0);

		getGlobalSettings().setMaxEagerSendSize(200 * KBYTE);

		pb.addSendRecv(world, 0, 1, 1, 200 * KBYTE, 1, 1);
		pb.addSendRecv(world, 1, 0, 0, 1 * MBYTE, 1, 1);

		runSimulationAllExpectedToFinish();
	}


	@Test public void sendRecvRendevouzTest() throws Exception{
		printStack();
		setup(2, 0);

		getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);

		pb.addSendRecv(world, 0, 1, 1, 1 * MBYTE, 1, 1);
		pb.addSendRecv(world, 1, 0, 0, 1 * MBYTE, 1, 1);

		runSimulationAllExpectedToFinish();
	}


	@Test public void barrierTest() throws Exception{
		printStack();
		for(int i=minClient; i <= maxClient; i++){
			setup(i, 0);

			pb.addBarrier(world);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}

		printTiming("Barrier", times);
	}


	@Test public void reduceTest() throws Exception{
		printStack();
		for(int i=minClient; i <= maxClient; i++){
			setup(i, 0);

			pb.addReduce(world, ( i - 2 >= 0 ? i -2 : 0 ), 10 * MBYTE);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}

		printTiming("Reduce", times);
	}


	@Test public void gatherTest() throws Exception{
		printStack();
		for(int i=minClient; i <= maxClient; i++){
			setup(i, 0);

			pb.addGather(world, ( i - 2 >= 0 ? i -2 : 0 ), 10 * MBYTE);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}

		printTiming("Gather", times);
	}


	@Test public void bcastTest() throws Exception{
		printStack();

		for(int i=minClient; i <= maxClient; i++){
			setup(i, 0);

			pb.addBroadcast(world,  (i - 2 >= 0 ? i -2 : 0), 10 * MBYTE);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}

		printTiming("Broadcast", times);
	}



	@Test public void allreduceTest() throws Exception{
		printStack();

		for(int i=minClient; i <= maxClient; i++){
			setup(i, 0);

			pb.addAllreduce(world, 10 * MBYTE);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}

		printTiming("Allreduce", times);
	}


	@Test public void allgatherTest() throws Exception{
		printStack();

		for(int i=minClient; i <= maxClient; i++){
			setup(i, 0);

			pb.addAllgather(world, 10 * MBYTE);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}

		printTiming("Allgather", times);
	}

	public void testBinary(int commSize, int rootRank){

		for (int i=0 ; i < commSize; i++){

			// real loop
			final int myRank = i;
			final int iterations = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(commSize-1);

			int clientRankInComm = myRank;

			//exchange rank 0 with cmd.root to receive data on the correct node
			if(clientRankInComm == rootRank) {
				clientRankInComm = 0;
			}else if(clientRankInComm == 0) {
				clientRankInComm = rootRank;
			}


			final int trailingZeros = Integer.numberOfTrailingZeros(clientRankInComm);
			final int phaseStart = iterations - trailingZeros;

			if(clientRankInComm != 0){
				int sendTo = (clientRankInComm ^ 1<<trailingZeros);

				if(sendTo == 0){
					sendTo = rootRank;
				}else if(sendTo == rootRank){
					sendTo = 0;
				}

				// recv first, then send.
				System.out.println(myRank + " phaseStart: " + phaseStart +" tz:" + trailingZeros + " send to: " +  sendTo);


				for (int iter = iterations - 1 - phaseStart ; iter >= 0 ; iter--){
					int target = (1<<iter | clientRankInComm);
					if (target >= commSize) continue;
					System.out.println(myRank +" from " + target );
				}
			}else{
				// send all
				for (int iter = iterations-1 ; iter >= 0 ; iter--){
					int target = (1<<iter);
					System.out.println(myRank +" recv from " + (target == rootRank ? 0 : target) );
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		NormalCommandsClusterTest t = new NormalCommandsClusterTest();
		//t.minClient = 1;
		//t.maxClient = 10;

		//t.allreduceTest();
		//t.bcastTest();
		//t.reduceTest();
		t.gatherTest();
		t.allgatherTest();
		//t.testBinary(4, 1);

	}
}

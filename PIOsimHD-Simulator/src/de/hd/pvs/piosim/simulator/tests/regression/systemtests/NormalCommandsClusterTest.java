
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$
  */


//	Copyright (C) 2008, 2009, 2011 Julian M. Kunkel
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
import java.util.HashMap;

import org.junit.Test;

import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NICC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NetworkEdgesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NetworkNodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.ClusterT;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.SMTNodeT;

public class NormalCommandsClusterTest extends ModelTest{
	public int minClient = 1;

	public int maxClient = 10;

	double [] times = new double[maxClient+1];

	@Override
	protected void postSetup() {
	}

	protected void setup(int nodeCount, int smtPerNode) throws Exception {
		SMTNodeT smtNodeT = new SMTNodeT(smtPerNode,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(smtPerNode),
				NetworkNodesC.QPI(),
				NetworkEdgesC.QPI()
				);
		super.setup( new ClusterT(nodeCount,
				NetworkEdgesC.GIGE(),
				NetworkNodesC.GIGSwitch(),
				smtNodeT) );
	}


	protected void setupSMP(int smtPerNode) throws Exception {
		SMTNodeT smtNodeT = new SMTNodeT(smtPerNode,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(smtPerNode),
				NetworkNodesC.QPI(),
				NetworkEdgesC.QPI()
				);
		super.setup( smtNodeT );
	}

	private void printTiming(String header, double[] times) throws IOException{
		final FileWriter fo = new FileWriter("/tmp/timing-" + this.getClass().getSimpleName() + ".txt", true);

		fo.write(header + " timing:\n");

		for(int i=minClient; i <= maxClient; i++){
			fo.write(i + " " + times[i] + "\n");
		}
		fo.write("\n");
		fo.close();
	}

	@Test public void asynchronousTest() throws Exception{
		setupSMP(1);
		pb.addBarrier(world);
		pb.setLastCommandAsynchronous(0, 0);

		pb.addWaitAll(0);

		runSimulationAllExpectedToFinish();
	}

	@Test public void asynchronousReadWriteTest() throws Exception{
		setupSMP(2);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		pb.addSendAndRecv(world, 0, 1, 100 * KBYTE, 1);

		pb.setLastCommandAsynchronous(0, 0);
		pb.setLastCommandAsynchronous(1, 0);

		pb.addWaitAll(0);
		pb.addWaitAll(1);

		// should not do anything:
		pb.addWaitAll(1);

		runSimulationAllExpectedToFinish();
	}

	@Test public void asynchronousReadWriteSyncTest() throws Exception{
		setupSMP(2);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		pb.addSendAndRecv(world, 0, 1, 100 * KBYTE, 1);

		pb.setLastCommandAsynchronous(0, 0);
		pb.setLastCommandAsynchronous(1, 0);

		pb.addSendAndRecv(world, 1, 0, 100 * KBYTE, 1);

		pb.addWaitAll(0);
		pb.addWaitAll(1);

		runSimulationAllExpectedToFinish();
	}


	@Test public void sendAndRecvSNMPinternalTest() throws Exception{
		setupSMP(2);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		pb.addSendAndRecv(world, 0, 1, 100 * KBYTE, 1);

		runSimulationAllExpectedToFinish();
	}



	@Test public void sendAndRecvEagerTestSMP() throws Exception{
		setupSMP(2);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		pb.addSendAndRecv(world, 0, 1, 100 * KBYTE, 1);

		runSimulationAllExpectedToFinish();
	}

	@Test public void sendAndRecvEagerTest() throws Exception{
		setup(2,1);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);

		pb.addSendAndRecv(world, 0, 1, 100 * KBYTE, 1);

		runSimulationAllExpectedToFinish();
	}

	@Test public void sendAndRecvTest() throws Exception{
		setup(2,1);

		pb.addSendAndRecv(world, 0, 1, 1 * MBYTE, 1);

		runSimulationAllExpectedToFinish();
	}

	@Test public void sendAndRecvCrossTest() throws Exception{
		setup(2,1);

		pb.addSendAndRecv(world, 0, 1, 1 * MBYTE, 1);
		pb.addSendAndRecv(world, 1, 0, 1 * MBYTE, 1);

		runSimulationAllExpectedToFinish();
	}

	@Test public void sendRecvEagerTest() throws Exception{
		setup(2,1);

		getGlobalSettings().setMaxEagerSendSize(200 * KBYTE);

		pb.addSendRecv(world, 0, 1, 1, 200 * KBYTE, 1, 1);
		pb.addSendRecv(world, 1, 0, 0, 100 * KBYTE, 1, 1);

		runSimulationAllExpectedToFinish();
	}


	@Test public void sendRecvOneRendevouzTest() throws Exception{
		setup(2,1);

		getGlobalSettings().setMaxEagerSendSize(200 * KBYTE);

		pb.addSendRecv(world, 0, 1, 1, 200 * KBYTE, 1, 2);
		pb.addSendRecv(world, 1, 0, 0, 1 * MBYTE, 2, 1);

		runSimulationAllExpectedToFinish();
	}

	@Test public void sendRecvOneRendevouzTestAnyTag() throws Exception{
		setup(2,1);

		getGlobalSettings().setMaxEagerSendSize(200 * KBYTE);

		pb.addSendRecv(world, 0, 1, 1, 200 * KBYTE, 1, 2);
		pb.addSendRecv(world, 1, 0, 0, 1 * MBYTE, -1 , 1);

		runSimulationAllExpectedToFinish();
	}

	@Test public void sendRecvOneRendevouzTestAnySource() throws Exception{
		setup(2,1);

		getGlobalSettings().setMaxEagerSendSize(200 * KBYTE);

		pb.addSendRecv(world, 0, 1, 1, 200 * KBYTE, 1, 2);
		pb.addSendRecv(world, 1, -1, 0, 1 * MBYTE, 2, 1);

		runSimulationAllExpectedToFinish();
	}

	@Test public void sendRecvOneRendevouzSameTagsTest() throws Exception{
		setup(2,1);

		getGlobalSettings().setMaxEagerSendSize(200 * KBYTE);

		pb.addSendRecv(world, 0, 1, 1, 200 * KBYTE, 1, 1);
		pb.addSendRecv(world, 1, 0, 0, 1 * MBYTE, 1, 1);

		runSimulationAllExpectedToFinish();
	}

	@Test public void sendrecvSendAndRecvOneRendevouzTest() throws Exception{
		setup(2,1);

		getGlobalSettings().setMaxEagerSendSize(200 * KBYTE);

		pb.addSendRecv(world, 0, 1, 1, 200 * KBYTE, 1, 1);
		pb.addRecv(world, 0, 1, 1);
		pb.addSend(world, 1, 0, 1 * MBYTE, 1);

		runSimulationAllExpectedToFinish();
	}

	@Test public void sendrecvSendRecvAndRecvOneRendevouzTest() throws Exception{
		setup(2,1);
		getGlobalSettings().setMaxEagerSendSize(200 * KBYTE);

		pb.addSendRecv(world, 0, 1, 1, 200 * KBYTE, 1, 1);
		pb.addSend(world, 1, 0, 1 * MBYTE, 1);
		pb.addRecv(world, 0, 1, 1);

		runSimulationAllExpectedToFinish();
	}


	@Test public void sendrecvSendAndRecvBothRendevouzTest() throws Exception{
		setup(2,1);
		getGlobalSettings().setMaxEagerSendSize(200 * KBYTE);

		pb.addSendRecv(world, 0, 1, 1, 1 * MBYTE, 1, 1);
		pb.addRecv(world, 0, 1, 1);
		pb.addSend(world, 1, 0, 1 * MBYTE, 1);

		runSimulationAllExpectedToFinish();
	}

	@Test public void sendrecvSendRecvAndRecvBothRendevouzTest() throws Exception{
		setup(2,1);
		getGlobalSettings().setMaxEagerSendSize(200 * KBYTE);

		pb.addSendRecv(world, 0, 1, 1, 1 * MBYTE, 1, 1);

		pb.addSend(world, 1, 0, 1 * MBYTE, 1);
		pb.addRecv(world, 0, 1, 1);

		runSimulationAllExpectedToFinish();
	}

	@Test public void sendrecvSendRecvAndRecvBothRendevouzTestDifferentTags() throws Exception{
		setup(2,1);
		getGlobalSettings().setMaxEagerSendSize(200 * KBYTE);

		pb.addSendRecv(world, 0, 1, 1, 1 * MBYTE, 1, 2);

		pb.addSend(world, 1, 0, 1 * MBYTE, 1);
		pb.addRecv(world, 0, 1, 2);

		runSimulationAllExpectedToFinish();
	}

	@Test public void sendRecvRendevouzLocal() throws Exception{
		setup(1,1);
		getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);

		pb.addSendRecv(world, 0, 0, 0, 1 * MBYTE, 100, 100);

		runSimulationAllExpectedToFinish();
	}


	@Test public void sendRecvRendevouzTest() throws Exception{
		setup(2,1);
		getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);

		pb.addSendRecv(world, 0, 1, 1, 1 * MBYTE, 100, 1111);
		pb.addSendRecv(world, 1, 0, 0, 1 * MBYTE, 1111, 100);

		runSimulationAllExpectedToFinish();
	}


	@Test public void barrierTest() throws Exception{
		for(int i=minClient; i <= maxClient; i++){
			setup(i,1);

			pb.addBarrier(world);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}

		printTiming("Barrier", times);
	}


	@Test public void reduceTest() throws Exception{
		for(int i=minClient; i <= maxClient; i++){
			setup(i,1);

			pb.addReduce(world, ( i - 2 >= 0 ? i -2 : 0 ), 10 * MBYTE);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}

		printTiming("Reduce", times);
	}


	@Test public void gatherTest() throws Exception{
		for(int i=minClient; i <= maxClient; i++){
			setup(i,1);

			pb.addGather(world, ( i - 2 >= 0 ? i -2 : 0 ), 10 * MBYTE);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}

		printTiming("Gather", times);
	}


	@Test public void reduceScatterSingle() throws Exception{
			setup(4,1);

			parameters.setTraceFile("/tmp/scatter");
			parameters.setTraceEnabled(true);

			final HashMap<Integer, Long> map = new HashMap<Integer, Long>();
			map.put(0, 100l);
			map.put(1, 200l);
			map.put(2, 300l);
			map.put(3, 400l);

			pb.addReduceScatter(world, map);
			runSimulationAllExpectedToFinish();
	}

	@Test public void scatterSingle() throws Exception{
			setup(4,1);

			parameters.setTraceFile("/tmp/scatter");
			parameters.setTraceEnabled(true);

			pb.addScatter(world, 0, 10 * MBYTE);
			runSimulationAllExpectedToFinish();
	}

	@Test public void scatterTest() throws Exception{
		for(int i=minClient; i <= maxClient; i++){
			setup(i,1);

			pb.addScatter(world, ( i - 2 >= 0 ? i -2 : 0 ), 10 * MBYTE);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}

		printTiming("Scatter", times);
	}



	@Test public void bcastTest() throws Exception{
		for(int i=minClient; i <= maxClient; i++){
			setup(i,1);

			pb.addBroadcast(world,  (i - 2 >= 0 ? i -2 : 0), 10 * MBYTE);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}

		printTiming("Broadcast", times);
	}

	@Test public void allreduceTest() throws Exception{
		for(int i=minClient; i <= maxClient; i++){
			setup(i,1);
			pb.addAllreduce(world, 1 * MBYTE);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}

		printTiming("Allreduce", times);
	}


	@Test public void allgatherTest1() throws Exception{

			parameters.setTraceFile("/tmp/allgather1");
			parameters.setTraceEnabled(true);

			setup(5,1);
			pb.addAllgather(world, 10 * MBYTE);
			runSimulationAllExpectedToFinish();
	}

	@Test public void allgatherTest() throws Exception{
		for(int i=minClient; i <= maxClient; i++){
			setup(i,1);
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

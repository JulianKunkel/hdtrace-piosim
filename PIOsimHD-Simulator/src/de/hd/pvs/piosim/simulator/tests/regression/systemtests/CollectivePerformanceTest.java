
//	Copyright (C) 2008-2011 Julian M. Kunkel
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

import de.hd.pvs.piosim.model.dynamicMapper.CommandType;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NICC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NetworkEdgesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NetworkNodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.ClusterT;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.SMTNodeT;

public class CollectivePerformanceTest extends ModelTest{
	public int maxClient = 10;

	final int MAXTESTS = maxClient*10;

	double [] times	= new double[MAXTESTS];
	String [] config =  new String[MAXTESTS];

	@Override
	protected void postSetup() {
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setTransferGranularity(100 * KiB);
	}

	protected void setupCompute(int nodeCount, int smtPerNode) throws Exception {
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

	private void printTiming(String testname, double[] times, String[]config) throws IOException{
		final FileWriter fo = new FileWriter("/tmp/timing-" + testname + ".txt", false);

		fo.write(this.getClass().getCanonicalName() + " " + testname + " timing:\n");

		for(int i=0 ; i < times.length; i++){
			if(config[i] != null){
				fo.write(config[i] + " " + times[i] + "\n");
			}
		}
		fo.write("\n");
		fo.close();

		// reset times & config for new input
		for(int i=0; i < config.length ; i++){
			config[i] = null;
		}

	}

	public void sendRecvPairTestFixedNodes(int maxProc, int nodeCount, long size) throws Exception{
		final int increment = (nodeCount % 2 == 0) ? 1 : 2;

		for(int i=increment; i <= maxProc; i+=increment){

			setupCompute(nodeCount, i);
			config[i] = nodeCount + "N" + (i*nodeCount) + "Proc";

			for(int c = 0; c < (i * nodeCount); c+=2){
				pb.addSendRecv(world, c, c+1, c+1 , size, 100, 1111);
				pb.addSendRecv(world, c+1,c,c , size, 1111, 100);
			}

			parameters.setTraceInternals(false);
			parameters.setTraceEnabled(false);

			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}
		printTiming("SendRecvOverlapped" + maxProc + "PerNode" + nodeCount + "N" + size + "Size" , times, config);
	}


	public void barrierDJTest() throws Exception{
		for(int i=1; i <= maxClient; i++){
			setupCompute(i,1);
			config[i] = i + "N" + i + "Proc";

			parameters.setTraceEnabled(false);

			pb.addBarrier(world);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}

		printTiming("Barrier", times, config);
	}


	public void reduceDJTest(long size) throws Exception{
		for(int i=1; i <= maxClient; i++){
			setupCompute(i,1);
			config[i] = i + "N" + i + "Proc";

			parameters.setTraceEnabled(false);
			pb.addReduce(world, 0, size);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}

		printTiming("ReduceDJ" + size, times, config);
	}

	public void gatherDJTest(long size) throws Exception{
		for(int i=1; i <= maxClient; i++){
			setupCompute(i,1);
			config[i] = i + "N" + i + "Proc";

			parameters.setTraceEnabled(false);
			pb.addGather(world, 0, size);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}

		printTiming("GatherDJ" + size, times, config);
	}


	public void bcastTraceExample(int proc, long size) throws Exception{
			setupCompute(proc,1);
			parameters.setTraceEnabled(true);
			pb.addBroadcast(world, 0, size);
			runSimulationAllExpectedToFinish();
	}

	public void bcastDJTest(long size) throws Exception{
		for(int i=1; i <= maxClient; i++){
			setupCompute(i,1);
			config[i] = i + "N" + i + "Proc";

			parameters.setTraceEnabled(false);
			pb.addBroadcast(world,  0, size);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}

		printTiming("BroadcastDJ" + size, times, config);
	}

	public void allreduceDJTest(long size) throws Exception{
		for(int i=1; i <= maxClient; i++){
			setupCompute(i,1);

			parameters.setTraceEnabled(false);
			pb.addAllreduce(world, size);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
			config[i] = i + "N" + i + "Proc";
		}

		printTiming("AllreduceDJ" + size, times, config);
	}


	public void reduceDJVisualization(int clientProc, long size) throws Exception{
			setupCompute(clientProc,1);

			parameters.setTraceFile("/tmp/reduce-" + clientProc + size);
			parameters.setTraceEnabled(true);

			pb.addReduce(world, 0, size);
			runSimulationAllExpectedToFinish();
	}


	public void bcastDJVisualization(int clientProc, long size) throws Exception{
			setupCompute(clientProc,1);

			// Example: Howto set a specific implementation for a given command.
			mb.getGlobalSettings().setClientFunctionImplementation(
					new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTree");

			parameters.setTraceFile("/tmp/bcast-" + clientProc + size);
			parameters.setTraceEnabled(true);

			pb.addBroadcast(world, 0, size);
			runSimulationAllExpectedToFinish();
	}

//  Example: How to run a trace with a model created within the simulator!

//	public void runJacobiIO_1C1S() throws Exception{
//		final String which =
//			"/home/julian/Dokumente/Geschäft/Dissertation/Simulation-Results/paper/trace/partdiff-par.proj";
//
//		AggregationCache cache = new AggregationCache();
//		cache.setName("PVS-CACHE");
//		cache.setMaxNumberOfConcurrentIOOps(1);
//
//		setupDisjointIO(1, 1, 1, 12000, cache);
//
//		parameters.setTraceFile("/tmp/jacobi1C1S");
//		parameters.setTraceEnabled(true);
//
//		final ApplicationXMLReader axml = new ApplicationXMLReader();
//		final Application app = axml.parseApplication(which, true);
//
//		mb.setApplication("Jacobi", app);
//
//		final ClientProcess p = mb.getModel().getClientProcesses().get(0);
//		p.setApplication("Jacobi");
//		p.setRank(0);
//
//		runSimulationAllExpectedToFinish();
//	}



	public static void main(String[] args) throws Exception {
		CollectivePerformanceTest t = new CollectivePerformanceTest();
		final long MByte = t.MBYTE;
		final long KByte = t.KBYTE;

		//t.reduceDJVisualization(8, 100*MByte);
		//t.bcastDJVisualization(8, 100*MByte);
		//t.reduceDJTest(100*MByte);

		final Long sizes[] = {10 * KByte, 1*MByte, 100*MByte};

		for (Long size : sizes) {
			t.sendRecvPairTestFixedNodes(4, 2, size);
			t.sendRecvPairTestFixedNodes(10, 1, size);
		}

	}
}

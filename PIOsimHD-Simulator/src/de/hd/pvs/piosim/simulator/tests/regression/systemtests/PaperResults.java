
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

import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationReorderCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.NoCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.dynamicMapper.CommandType;
import de.hd.pvs.piosim.model.inputOutput.FileDescriptor;
import de.hd.pvs.piosim.model.inputOutput.FileMetadata;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;
import de.hd.pvs.piosim.model.program.Application;
import de.hd.pvs.piosim.model.program.ApplicationXMLReader;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.IOC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NICC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NetworkEdgesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NetworkNodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.ClusterInhomogeniousT;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.ClusterT;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.IOServerCreator;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.SMTNodeT;

public class PaperResults extends ModelTest{
	public int maxClient = 10;

	final int MAXTESTS = maxClient*10;

	double [] times	= new double[MAXTESTS];
	String [] config =  new String[MAXTESTS];

	FileMetadata fc0;
	FileMetadata fc1;
	FileMetadata fvis;

	@Override
	protected void postSetup() {
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setTransferGranularity(100 * KiB);
		mb.getGlobalSettings().setIOGranularity(MiB);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(100 * KiB);


		fc0 = aB.createFile("checkpoint.0", 0, dist);
		fc1 = aB.createFile("checkpoint.1", 0, dist);
		fvis = aB.createFile("visualization.dat", 0, dist);
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

	/**
	 *
	 * @param nodeCount Total number of nodes
	 * @param clientsPerNode
	 * @param serverCount Number of nodes with I/O (<= nodeCount)
	 * @param memoryInMB
	 * @param cacheLayer
	 * @throws Exception
	 */
	protected void setupOverlappedIO(int nodeCount, int clientsPerNode, int serverCount, long memoryInMB, ServerCacheLayer cacheLayer)
		throws Exception
	{
		final IOServerCreator ios = new IOServerCreator(IOC.PVSServer(), IOC.WestDisk(), cacheLayer);

		assert(nodeCount <= serverCount);

		SMTNodeT smtNodeT = new SMTNodeT(clientsPerNode,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(clientsPerNode),
				NetworkNodesC.LocalNodeQPI(),
				NetworkEdgesC.QPI()
				);

		SMTNodeT serverNodeT = new SMTNodeT(clientsPerNode,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(clientsPerNode, memoryInMB),
				NetworkNodesC.LocalNodeQPI(),
				NetworkEdgesC.QPI(), ios
				);

		super.setup( new ClusterInhomogeniousT(
				nodeCount - serverCount, NetworkEdgesC.GIGE(), NetworkNodesC.GIGSwitch(), smtNodeT,
				serverCount, NetworkEdgesC.GIGE(), serverNodeT) );
	}

	protected void setupDisjointIO(int nodeCount, int clientsPerNode, int serverCount, long memoryInMB, ServerCacheLayer cacheLayer)
		throws Exception
	{
		final IOServerCreator ios = new IOServerCreator(IOC.PVSServer(), IOC.WestDisk(), cacheLayer);

		SMTNodeT smtNodeT = new SMTNodeT(clientsPerNode,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(clientsPerNode),
				NetworkNodesC.QPI(),
				NetworkEdgesC.QPI()
				);

		SMTNodeT serverNodeT = new SMTNodeT(0,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(clientsPerNode, memoryInMB),
				NetworkNodesC.QPI(),
				NetworkEdgesC.QPI(), ios
				);

		super.setup( new ClusterInhomogeniousT(
				nodeCount, NetworkEdgesC.GIGE(), NetworkNodesC.GIGSwitch(), smtNodeT,
				serverCount, NetworkEdgesC.GIGE(), serverNodeT) );
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

	/**
	 * @throws Exception
	 */
	public void sendRecvPairTest(int maxProc, long size) throws Exception{

		assert(maxProc % 2 == 0);

		for(int i=2; i <= maxProc; i+=2){
			setupCompute(i,1);
			config[i] = i + "N" + i + "Proc";

			for(int c = 0; c < i; c+=2){
				pb.addSendRecv(world, c, c+1, c+1 , size, 100, 1111);
				pb.addSendRecv(world, c+1,c,c , size, 1111, 100);
			}

			parameters.setTraceInternals(false);
			parameters.setTraceEnabled(false);

			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}
		printTiming("SendRecvDJ" + maxProc + "P" + size + "Size" , times, config);
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


	public void sendRecvPairTestFixedNodesMapped(int maxProc, int nodeCount, long size) throws Exception{
		final int increment = (nodeCount % 2 == 0) ? 1 : 2;

		for(int i=increment; i <= maxProc; i+=increment){

			setupCompute(nodeCount, i);
			config[i] = nodeCount + "N" + (i*nodeCount) + "Proc";

			int half = (i * nodeCount) / 2;

			for(int c = 0; c < half; c++){
				pb.addSendRecv(world, c, c+half, c+half , size, 100, 1111);
				pb.addSendRecv(world, c+half,c,c , size, 1111, 100);
			}

			parameters.setTraceInternals(false);
			parameters.setTraceEnabled(false);

			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}
		printTiming("SendRecvOverlappedMapped" + maxProc + "PerNode" + nodeCount + "N" + size + "Size" , times, config);
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


	public void bcastExample(int proc, long size) throws Exception{
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


			mb.getGlobalSettings().setClientFunctionImplementation(
					new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTree");

			parameters.setTraceFile("/tmp/bcast-" + clientProc + size);
			parameters.setTraceEnabled(true);

			pb.addBroadcast(world, 0, size);
			runSimulationAllExpectedToFinish();
	}

	public void runJacobiIO_1C1S() throws Exception{
		final String which =
			"/home/julian/Dokumente/Geschäft/Dissertation/Simulation-Results/paper/trace/partdiff-par.proj";

		AggregationCache cache = new AggregationCache();
		cache.setName("PVS-CACHE");
		cache.setMaxNumberOfConcurrentIOOps(1);

		setupDisjointIO(1, 1, 1, 12000, cache);

		parameters.setTraceFile("/tmp/jacobi1C1S");
		parameters.setTraceEnabled(true);

		final ApplicationXMLReader axml = new ApplicationXMLReader();
		final Application app = axml.parseApplication(which, true);

		mb.setApplication("Jacobi", app);

		final ClientProcess p = mb.getModel().getClientProcesses().get(0);
		p.setApplication("Jacobi");
		p.setRank(0);

		runSimulationAllExpectedToFinish();
	}


	public void runJacobiIONC_1C1S() throws Exception{
		final String which =
			"/home/julian/Dokumente/Geschäft/Dissertation/Simulation-Results/paper/trace/partdiff-par.proj";

		NoCache cache = new NoCache();
		cache.setName("PVS-CACHE");
		cache.setMaxNumberOfConcurrentIOOps(1);

		setupDisjointIO(1, 1, 1, 12000, cache);

		parameters.setTraceFile("/tmp/jacobi1C1S-NoCache");
		parameters.setTraceEnabled(true);

		final ApplicationXMLReader axml = new ApplicationXMLReader();
		final Application app = axml.parseApplication(which, true);

		mb.setApplication("Jacobi", app);

		final ClientProcess p = mb.getModel().getClientProcesses().get(0);
		p.setApplication("Jacobi");
		p.setRank(0);

		runSimulationAllExpectedToFinish();
	}


	public void runParabenchIO_1C1S() throws Exception{
		final String which =
			"/home/kunkel/Dokumente/Dissertation/Trace/results-git/pvfs2-ram-limited/4-levels-of-access/100/N2-P1-C1-P1-S1-RAM1000/parabench-instrumented.proj";

		AggregationCache cache = new AggregationCache();
		cache.setName("PVS-CACHE");
		cache.setMaxNumberOfConcurrentIOOps(1);

		setupDisjointIO(2, 1, 1, 1000, cache);

		parameters.setTraceFile("/tmp/parabench-1C1S");
		parameters.setTraceEnabled(true);

		final ApplicationXMLReader axml = new ApplicationXMLReader();
		final Application app = axml.parseApplication(which, true);

		mb.setApplication("Jacobi", app);

		final ClientProcess p = mb.getModel().getClientProcesses().get(0);
		p.setApplication("Jacobi");
		p.setRank(0);

		runSimulationAllExpectedToFinish();
	}


	public void runParabenchIO_2C2S() throws Exception{
		final String which =
			"/home/kunkel/Dokumente/Dissertation/Trace/results-git/pvfs2-ram-limited/4-levels-of-access/100/N4-P1-C2-P2-S2-RAM1000/parabench-instrumented.proj";

		AggregationReorderCache cache = new AggregationReorderCache();
		cache.setName("PVS-CACHE");
		cache.setMaxNumberOfConcurrentIOOps(1);

		setupDisjointIO(4, 1, 2, 1000, cache);

		parameters.setTraceFile("/tmp/parabench-2C2S");
		parameters.setTraceEnabled(true);

		final ApplicationXMLReader axml = new ApplicationXMLReader();
		final Application app = axml.parseApplication(which, true);

		mb.setApplication("Jacobi", app);

		runSimulationAllExpectedToFinish();
	}

	@Test
	public void runIOTest2S2C() throws Exception{
		AggregationCache cache = new AggregationCache();
		cache.setName("PVS-CACHE");
		cache.setMaxNumberOfConcurrentIOOps(1);

		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Filereadall"), "de.hd.pvs.piosim.simulator.program.Filereadall.TwoPhase");

		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Filewriteall"), "de.hd.pvs.piosim.simulator.program.Filereadall.TwoPhase");


		setupDisjointIO(4, 1, 2, 1000, cache);

		parameters.setTraceFile("/tmp/parabench-2C2S");
		parameters.setTraceEnabled(true);
		parameters.setTraceInternals(true);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(100 * KiB);

		FileMetadata f =  aB.createFile("test", 0, dist);

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		pb.addWriteSequential(0, fd, 0,       100*MiB);
		pb.addWriteSequential(1, fd, 100*MiB, 100*MiB);
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}



	public static void main(String[] args) throws Exception {
		PaperResults t = new PaperResults();
		final long MByte = t.MBYTE;
		final long KByte = t.KBYTE;

		//t.runParabenchIO_1C1S();
		t.runParabenchIO_2C2S();
		//t.reduceDJVisualization(8, 100*MByte);
		//t.bcastDJVisualization(8, 100*MByte);
		//t.reduceDJTest(100*MByte);
		System.exit(0);


		final Long sizes[] = {10 * KByte, 1*MByte, 100*MByte};

		for (Long size : sizes) {
			t.sendRecvPairTestFixedNodes(4, 2, size);
			t.sendRecvPairTestFixedNodes(10, 1, size);
			t.sendRecvPairTestFixedNodesMapped(4, 2, size);
			t.sendRecvPairTest(10, size);
		}

	}
}

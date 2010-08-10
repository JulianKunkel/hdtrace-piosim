/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$
 */

//	Copyright (C) 2008, 2009 Julian M. Kunkel
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

import java.util.LinkedList;

import org.junit.Test;

import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.dynamicMapper.CommandType;
import de.hd.pvs.piosim.model.inputOutput.FileDescriptor;
import de.hd.pvs.piosim.model.inputOutput.FileMetadata;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.IOC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NICC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NetworkEdgesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NetworkNodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.ClusterT;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.IODisjointConfiguration;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.IOServerCreator;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.SMTNodeT;

public class IOTest extends ModelTest {

	FileMetadata f;

	protected void postSetup(){

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setTransferGranularity(100 * KiB);
		mb.getGlobalSettings().setIOGranularity(MiB);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(100 * KiB);


		f = aB.createFile("test", GBYTE, dist);
	}

	/**
	 * Configure one node with one server.
	 * @param smtPerNode
	 * @param cacheLayer
	 * @throws Exception
	 */
	protected void setupOneNodeOneServer(int smtClients, ServerCacheLayer cacheLayer)
	throws Exception
	{
		System.err.println(new Exception().getStackTrace()[1]);

		final IOServerCreator ios = new IOServerCreator(IOC.PVSServer(), IOC.PVSDisk(), cacheLayer);

		SMTNodeT serverNodeT = new SMTNodeT(smtClients,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(smtClients),
				NetworkNodesC.QPI(),
				NetworkEdgesC.QPI(), ios
				);

		super.setup( serverNodeT );
	}


	protected void setup(int nodeCount, int smtPerNode, int serverCount, ServerCacheLayer cacheLayer)
		throws Exception
	{
		final IOServerCreator ios = new IOServerCreator(IOC.PVSServer(), IOC.PVSDisk(), cacheLayer);

		SMTNodeT smtNodeT = new SMTNodeT(smtPerNode,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(smtPerNode),
				NetworkNodesC.QPI(),
				NetworkEdgesC.QPI()
				);
		ClusterT clientCluster =  new ClusterT(nodeCount,
				NetworkEdgesC.GIGE(),
				NetworkNodesC.GIGSwitch(),
				smtNodeT);


		SMTNodeT serverNodeT = new SMTNodeT(0,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(smtPerNode),
				NetworkNodesC.QPI(),
				NetworkEdgesC.QPI(), ios
				);
		ClusterT serverCluster =  new ClusterT(serverCount,
				NetworkEdgesC.GIGE(),
				NetworkNodesC.GIGSwitch(),
				serverNodeT) ;


		super.setup( new IODisjointConfiguration(NetworkEdgesC.TenGIGE(), NetworkNodesC.QPI(), clientCluster, serverCluster ) );
	}

	@Test public void OpenCloseTest() throws Exception{
		setupOneNodeOneServer(1, IOC.SimpleNoCache());

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}

	@Test public void Writebehind1Test() throws Exception{
		setupOneNodeOneServer(1, IOC.SimpleWriteBehindCache());

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		pb.addWriteSequential(0, fd, 0, MiB);
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}


	@Test public void OpenCloseTest3() throws Exception{
		setupOneNodeOneServer(3, IOC.SimpleNoCache());

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}

	@Test public void ReadWrite2TestAll() throws Exception{
		final List<ServerCacheLayer> cacheLayers = new ArrayList<ServerCacheLayer>();
		cacheLayers.add(IOC.SimpleNoCache());
		cacheLayers.add(IOC.SimpleWriteBehindCache());
		cacheLayers.add(IOC.AggregationCache());
		cacheLayers.add(IOC.AggregationReorderCache());

		for (ServerCacheLayer cache: cacheLayers){
			setupOneNodeOneServer(2, cache);

			FileDescriptor fd = pb.addFileOpen(f, world , false);

			pb.addReadSequential(1, fd, 0, 100 * KBYTE);
			pb.addWriteSequential(0, fd, 0, 100 * KBYTE);
			pb.addFileClose(fd);

			runSimulationAllExpectedToFinish();
		}
	}

	@Test public void Writebehind3Test() throws Exception{
		setupOneNodeOneServer(3, IOC.SimpleWriteBehindCache());

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		for(int i= 0 ; i < 3; i++){
			pb.addWriteSequential(i, fd, i*MiB, MiB);
		}
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}

	@Test public void AggregationCache3Test() throws Exception{
		setupOneNodeOneServer(3, IOC.AggregationCache());

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		for(int i= 0 ; i < 3; i++){
			pb.addWriteSequential(i, fd, i*MiB, MiB);
		}
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}

	@Test public void AggregationCache3V10Test() throws Exception{
		ServerCacheLayer cache = IOC.AggregationCache();
		cache.setMaxNumberOfConcurrentIOOps(10);
		setupOneNodeOneServer(3, cache);

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		for(int i= 0 ; i < 3; i++){
			pb.addWriteSequential(i, fd, i*MiB, MiB);
		}
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}


	@Test public void AggregationCache3OverlapsTest() throws Exception{
		ServerCacheLayer cache = IOC.AggregationCache();
		setupOneNodeOneServer(3, cache);

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		for(int i= 0 ; i < 3; i++){
			pb.addWriteSequential(i, fd, 0, MiB);
		}
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}


	@Test public void Writebehind3OverflowTest() throws Exception{
		setupOneNodeOneServer(3, IOC.SimpleWriteBehindCache());

		for(Node node: mb.getModel().getNodes()){
			node.setMemorySize(2 * MiB);
		}

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		for(int i= 0 ; i < 3; i++){
			pb.addWriteSequential(i, fd, i*MiB, 2*MiB);
		}
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}

	@Test public void NoCache2Test() throws Exception{
		setupOneNodeOneServer(2, IOC.SimpleNoCache());

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		for(int i= 0 ; i < 2; i++){
			pb.addWriteSequential(i, fd, 0, 200*KiB);
		}
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}

	@Test public void AggregationReorderReRead2Test() throws Exception{
		setupOneNodeOneServer(2, IOC.AggregationReorderCache());

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		for(int i= 0 ; i < 2; i++){
			pb.addReadSequential(i, fd, 0, 10*MiB);
		}
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}

	@Test public void AggregationReorderRead2Test() throws Exception{
		setupOneNodeOneServer(2, IOC.AggregationReorderCache());

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		for(int i= 0 ; i < 2; i++){
			pb.addReadSequential(i, fd, 10*MiB * i, 10*MiB);
		}
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}

	@Test public void AggregationReorderOverwrite2Test() throws Exception{
		setupOneNodeOneServer(2, IOC.AggregationReorderCache());

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		for(int i= 0 ; i < 2; i++){
			pb.addWriteSequential(i, fd, 0, 10*MiB);
		}
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}


	@Test public void AggregationReorderNoOverwrite2Test() throws Exception{
		setupOneNodeOneServer(2, IOC.AggregationReorderCache());

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		for(int i= 0 ; i < 2; i++){
			pb.addWriteSequential(i, fd, 10*MiB * i, 10*MiB);
		}
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}





	@Test public void CollectiveWrite2DirectTest() throws Exception{
		setupOneNodeOneServer(2, IOC.SimpleNoCache());

		mb.getGlobalSettings().setClientFunctionImplementation(new CommandType("Filewriteall"), "de.hd.pvs.piosim.simulator.program.Filewriteall.Direct");

		LinkedList<ListIO> listIO = new LinkedList<ListIO>();

		ListIO ios = new ListIO();
		ios.addIOOperation(0, MiB);
		listIO.add(ios);

		ios = new ListIO();
		ios.addIOOperation(0, MiB);
		listIO.add(ios);

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		pb.addWriteCollective(fd, listIO);
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}



	@Test public void CollectiveWritetwoPhaseTest() throws Exception{
		setupOneNodeOneServer(2, IOC.SimpleNoCache());
		mb.getGlobalSettings().setClientFunctionImplementation(new CommandType("Filewriteall"), "de.hd.pvs.piosim.simulator.program.Filewriteall.TwoPhase");
		LinkedList<ListIO> listIO = new LinkedList<ListIO>();

		ListIO ios = new ListIO();
		ios.addIOOperation(0, MiB);
		listIO.add(ios);

		ios = new ListIO();
		ios.addIOOperation(0, MiB);
		listIO.add(ios);

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		pb.addWriteCollective(fd, listIO);
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}


	@Test public void CollectiveWrite2Test() throws Exception{
		setupOneNodeOneServer(2, IOC.SimpleNoCache());
		LinkedList<ListIO> listIO = new LinkedList<ListIO>();
		ListIO ios = new ListIO();
		ios.addIOOperation(0, MiB);
		listIO.add(ios);

		ios = new ListIO();
		ios.addIOOperation(0, MiB);
		listIO.add(ios);

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		pb.addWriteCollective(fd, listIO);
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}


	@Test public void CollectiveRead2Test() throws Exception{
		setupOneNodeOneServer(2, IOC.SimpleNoCache());
		LinkedList<ListIO> listIO = new LinkedList<ListIO>();

		ListIO ios = new ListIO();
		ios.addIOOperation(0, MiB);
		listIO.add(ios);

		ios = new ListIO();
		ios.addIOOperation(0, MiB);
		listIO.add(ios);

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		pb.addReadCollective(fd, listIO);
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}

	@Test public void CollectiveRead2TwoPhaseTest() throws Exception{
		setupOneNodeOneServer(2, IOC.SimpleNoCache());

		//mb.getGlobalSettings().setClientFunctionImplementation(new CommandType("Filereadall"), "de.hd.pvs.piosim.simulator.program.Filereadall.Direct");
		mb.getGlobalSettings().setClientFunctionImplementation(new CommandType("Filereadall"), "de.hd.pvs.piosim.simulator.program.Filereadall.TwoPhase");

		LinkedList<ListIO> listIO = new LinkedList<ListIO>();

		ListIO ios = new ListIO();
		ios.addIOOperation(0, MiB);
		listIO.add(ios);

		ios = new ListIO();
		ios.addIOOperation(0, MiB);
		//ios.addIOOperation(MiB, MiB);
		listIO.add(ios);

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		pb.addReadCollective(fd, listIO);
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}

	@Test public void CollectiveRead2OneEmptyTest() throws Exception{
		setupOneNodeOneServer(2, IOC.SimpleNoCache());

		//mb.getGlobalSettings().setClientFunctionImplementation(new CommandType("Filereadall"), "de.hd.pvs.piosim.simulator.program.Filereadall.Direct");
		mb.getGlobalSettings().setClientFunctionImplementation(new CommandType("Filereadall"), "de.hd.pvs.piosim.simulator.program.Filereadall.TwoPhase");

		LinkedList<ListIO> listIO = new LinkedList<ListIO>();

		ListIO ios = new ListIO();
		ios.addIOOperation(0, MiB);
		listIO.add(ios);

		ios = new ListIO();
		listIO.add(ios);

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		pb.addReadCollective(fd, listIO);
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}

	@Test public void Write1Test() throws Exception{
		setupOneNodeOneServer(1, IOC.SimpleNoCache());

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		pb.addWriteSequential(0, fd, 0, MiB);
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}


	@Test public void Read1Test() throws Exception{
		setupOneNodeOneServer(1, IOC.SimpleNoCache());

		FileDescriptor fd = pb.addFileOpen(f, world , false);
		pb.addReadSequential(0, fd, 0, MiB);
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}


	@Test public void Read1TestNonBlocking() throws Exception{
		setupOneNodeOneServer(1, IOC.SimpleNoCache());
		FileDescriptor fd = pb.addFileOpen(f, world , false);

		pb.addReadSequential(0, fd, 0, MiB);
		pb.setLastCommandAsynchronous(0);

		pb.addReadSequential(0, fd, 0, MiB);
		pb.setLastCommandAsynchronous(0);

		pb.addWaitAll(0);
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}
}
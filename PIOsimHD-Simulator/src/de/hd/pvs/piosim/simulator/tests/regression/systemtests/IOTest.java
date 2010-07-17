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

import org.junit.Test;

import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
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

	MPIFile f;

	private void initGlobals(){

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
		final IOServerCreator ios = new IOServerCreator(IOC.PVSServer(), IOC.PVSDisk(), cacheLayer);

		SMTNodeT serverNodeT = new SMTNodeT(smtClients,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(smtClients),
				NetworkNodesC.QPI(),
				NetworkEdgesC.QPI(), ios
				);

		super.setup( serverNodeT );

		initGlobals();
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
		ClusterT serverCluster =  new ClusterT(nodeCount,
				NetworkEdgesC.GIGE(),
				NetworkNodesC.GIGSwitch(),
				serverNodeT) ;


		super.setup( new IODisjointConfiguration(NetworkEdgesC.TenGIGE(), NetworkNodesC.QPI(), clientCluster, serverCluster ) );

		initGlobals();
	}

	@Test public void OpenCloseTest() throws Exception{
		setupOneNodeOneServer(1, IOC.SimpleNoCache());

		pb.addFileOpen(f, world , false);
		pb.addFileClose(f, world);

		runSimulationAllExpectedToFinish();
	}

	@Test public void Writebehind1Test() throws Exception{
		setupOneNodeOneServer(1, IOC.SimpleWriteBehindCache());

		pb.addFileOpen(f, world , false);

		pb.addWriteSequential(0, f, 0, MiB);
		pb.addFileClose(f, world);

		runSimulationAllExpectedToFinish();
	}

	@Test public void Write1Test() throws Exception{
		setupOneNodeOneServer(1, IOC.SimpleNoCache());

		pb.addFileOpen(f, world , false);

		pb.addWriteSequential(0, f, 0, MiB);
		pb.addFileClose(f, world);

		runSimulationAllExpectedToFinish();
	}


	@Test public void Read1Test() throws Exception{
		setupOneNodeOneServer(1, IOC.SimpleNoCache());

		pb.addFileOpen(f, world , false);

		pb.addReadSequential(0, f, 0, MiB);
		pb.addFileClose(f, world);

		runSimulationAllExpectedToFinish();
	}


	@Test public void Read1TestNonBlocking() throws Exception{
		setupOneNodeOneServer(1, IOC.SimpleNoCache());

		pb.addFileOpen(f, world , false);

		pb.addReadSequential(0, f, 0, MiB);
		pb.setLastCommandAsynchronous(0);

		pb.addReadSequential(0, f, 0, MiB);
		pb.setLastCommandAsynchronous(0);

		pb.addWaitAll(0);
		pb.addFileClose(f, world);

		runSimulationAllExpectedToFinish();
	}
}
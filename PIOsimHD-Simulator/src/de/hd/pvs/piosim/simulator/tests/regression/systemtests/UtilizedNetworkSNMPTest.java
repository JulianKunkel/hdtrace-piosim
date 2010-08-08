
 /** Version Control Information $Id: NormalCommandsClusterTest.java 775 2010-07-10 16:46:50Z kunkel $
  * @lastmodified    $Date: 2010-07-10 18:46:50 +0200 (Sa, 10. Jul 2010) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 775 $
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

import org.junit.Test;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.components.NetworkEdge.SimpleNetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardNode;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NICC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.SMTNodeT;

public class UtilizedNetworkSNMPTest extends ModelTest{
	@Override
	protected void postSetup() {

	}

	protected void setupSMP(int smtPerNode) throws Exception {
		StoreForwardNode sw = new StoreForwardNode();
		sw.setName("SLOWSwitch");
		sw.setTotalBandwidth(100 * KBYTE);

		SimpleNetworkEdge conn = new SimpleNetworkEdge();
		conn.setName("Edge");
		conn.setLatency(new Epoch(0.000000001));
		conn.setBandwidth(1 * MBYTE);

		SMTNodeT smtNodeT = new SMTNodeT(smtPerNode,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(smtPerNode),
				sw,
				conn
				);
		super.setup( smtNodeT );
	}

	@Test public void slowNetworkSwitch() throws Exception{
		setupSMP(2);

		mb.getGlobalSettings().setTransferGranularity(100 * KBYTE);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);

		pb.addSendAndRecv(world, 0, 1, 100* KBYTE, 1);

		runSimulationAllExpectedToFinish();
	}



	@Test public void slowNetworkSwitchLowTransferGranularity1() throws Exception{
		setupSMP(2);

		mb.getGlobalSettings().setTransferGranularity(10 * KBYTE);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);

		pb.addSendAndRecv(world, 0, 1, 10* KBYTE, 1);

		runSimulationAllExpectedToFinish();
	}



	@Test public void slowNetworkSwitchLowTransferGranularity5() throws Exception{
		setupSMP(2);

		mb.getGlobalSettings().setTransferGranularity(10 * KBYTE);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);

		pb.addSendAndRecv(world, 0, 1, 100* KBYTE, 1);

		runSimulationAllExpectedToFinish();
	}


	@Test public void slowNetworkSwitchLowTransferGranularity() throws Exception{
		setupSMP(2);

		mb.getGlobalSettings().setTransferGranularity(10 * KBYTE);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);

		pb.addSendAndRecv(world, 0, 1, 40* KBYTE, 1);

		runSimulationAllExpectedToFinish();
	}


	@Test public void slowNetworkSwitchTwoConnects() throws Exception{
		setupSMP(4);

		mb.getGlobalSettings().setTransferGranularity(10 * KBYTE);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);

		pb.addSendAndRecv(world, 0, 1, 100* KBYTE, 1);
		pb.addSendAndRecv(world, 2, 3, 100* KBYTE, 1);

		runSimulationAllExpectedToFinish();
	}
}

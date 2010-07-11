
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

import org.junit.Before;
import org.junit.Test;

public class SendRecvTest extends NormalCommandsClusterTest{

	@Before public void setUp() throws Exception{
		setup(4, 1);
	}

	@Test public void empytAppTest() throws Exception{
		runSimulationAllExpectedToFinish();
	}

	@Test public void normalReceiveTest() throws Exception{
		pb.addRecv(world, 3, 0, 0);
		pb.addSend(world, 3, 0, KBYTE, 0);
		runSimulationAllExpectedToFinish();
	}

	@Test public void lateSenderTest() throws Exception{
		pb.addRecv(world, 3, 0, 0);

		pb.addComputate(3, 1000000);
		pb.addSend(world, 3, 0, KBYTE, 0);
		runSimulationAllExpectedToFinish();
	}

	@Test public void lateReceiverTest() throws Exception{
		pb.addComputate(0, 1000000);
		pb.addRecv(world, 3, 0, 0);

		pb.addSend(world, 3, 0, KBYTE, 0);
		runSimulationAllExpectedToFinish();
	}

	@Test public void doubleReceiveTest() throws Exception{
		pb.addComputate(0, 1000000);

		// this works only for non-blocking !
		pb.addRecv(world, 3, 0, 0);
		pb.addRecv(world, 3, 0, 1);

		pb.addSend(world, 3, 0, KBYTE, 1);
		pb.addSend(world, 3, 0, KBYTE, 0);
		runSimulationAllExpectedToFinish();
	}


	@Test public void anyReceiveTest() throws Exception{
		pb.addRecv(world, -1, 0, 0);
		pb.addSend(world, 3, 0, KBYTE, 0);
		runSimulationAllExpectedToFinish();
	}

	@Test public void sendRecvTestTwoClients() throws Exception{
		pb.addSendRecv(world, 0, 1, 1, MBYTE, 0, 1);

		pb.addSendRecv(world, 1, 0, 0, KBYTE, 1, 0);

		runSimulationAllExpectedToFinish();
	}

	@Test public void sendRecvTestTwoClientsEager() throws Exception{
		mb.getGlobalSettings().setMaxEagerSendSize(200 * KBYTE);
		pb.addSendRecv(world, 0, 1, 1, KBYTE, 0, 1);

		pb.addSendRecv(world, 1, 0, 0, KBYTE, 1, 0);

		runSimulationAllExpectedToFinish();
	}


	@Test public void sendRecvTestThreeClients() throws Exception{
		pb.addSendRecv(world, 0, 1, 2, MBYTE, 0, 0);

		pb.addSendRecv(world, 1, 2, 0, KBYTE, 0, 0);

		pb.addSendRecv(world, 2, 0, 1, KBYTE, 0, 0);
		runSimulationAllExpectedToFinish();
	}

	public static void main(String[] args) throws Exception{
		SendRecvTest t = new SendRecvTest();
		t.setUp();
		t.sendRecvTestTwoClientsEager();
	}
}

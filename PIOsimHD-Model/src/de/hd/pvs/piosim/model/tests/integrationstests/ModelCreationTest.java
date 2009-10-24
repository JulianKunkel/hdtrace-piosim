
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

/**
 *
 */
package de.hd.pvs.piosim.model.tests.integrationstests;

import java.io.FileOutputStream;

import junit.framework.TestSuite;

import org.junit.Before;
import org.junit.Test;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.ModelXMLWriter;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.NetworkEdge.SimpleNetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardNode;
import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketFirstRoute;

/**
 * Test if a model can be created.
 *
 * @author Julian M. Kunkel
 */
public class ModelCreationTest   extends TestSuite  {
  @Before public void setUp() {
  	/* setup log4j */
  	//PropertyConfigurator.configureAndWatch( "log4j-debug.properties", 60*1000 );
  	System.out.println();
  }

	@Test public void test() throws Exception{
		final ModelBuilder mb = new ModelBuilder();
		final Node machine = new Node();
		final Node machine2 = new Node();

		final ClientProcess client = new ClientProcess();
		final ClientProcess client2 = new ClientProcess();

		final NIC nic1 = new NIC();
		final NIC nic2 = new NIC();

		machine.setName("Test1");

		nic2.setTotalBandwidth(10000);

		client.setNetworkInterface(nic1);
		client2.setNetworkInterface(nic2);

		mb.addNode(machine);
		mb.addNode(machine2);

		mb.addClient(machine, client);
		mb.addClient(machine2, client2);


		final StoreForwardNode interNode = new StoreForwardNode();

		final INetworkTopology topology = mb.createTopology("Patch-Cable");

		topology.setRoutingAlgorithm(new PaketFirstRoute());

		mb.addNetworkNode(interNode);

		SimpleNetworkEdge mi = new SimpleNetworkEdge();

		mb.connect(topology, client.getNetworkInterface(), mi, interNode);
		mi = new SimpleNetworkEdge();
		mb.connect(topology, interNode, mi, client.getNetworkInterface());
		mi = new SimpleNetworkEdge();
		mb.connect(topology, client2.getNetworkInterface(), mi, interNode);
		mi = new SimpleNetworkEdge();
		mb.connect(topology, interNode, mi, client2.getNetworkInterface());

		// write XML to file
		final ModelXMLWriter writer = new ModelXMLWriter();
		final StringBuffer sb = new StringBuffer();
		writer.createXMLFromModel(mb.getModel(), sb);

		final FileOutputStream file = new FileOutputStream("/tmp/model.xml");
		file.write(sb.toString().getBytes());
		file.close();

		System.out.println(mb.getModel());
	}
}

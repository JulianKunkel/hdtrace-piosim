
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
import de.hd.pvs.piosim.model.components.NetworkEdge.SimpleNetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.StoreForwardNetworkNode;
import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;

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
		ModelBuilder mb = new ModelBuilder();
		ClientProcess client = new ClientProcess();
		Node maschine = new Node();

		ClientProcess client2 = new ClientProcess();
		Node maschine2 = new Node();

		mb.addClient(maschine, client);
		mb.addClient(maschine2, client2);

		StoreForwardNetworkNode interNode = new StoreForwardNetworkNode();

		INetworkTopology topology = mb.createTopology("Patch-Cable");

		SimpleNetworkEdge mi = new SimpleNetworkEdge();

		mb.connect(topology, maschine, mi, interNode);
		mi = new SimpleNetworkEdge();
		mb.connect(topology, interNode, mi, maschine);
		mi = new SimpleNetworkEdge();
		mb.connect(topology, maschine2, mi, interNode);
		mi = new SimpleNetworkEdge();
		mb.connect(topology, interNode, mi, maschine2);

		// write XML to file
		ModelXMLWriter writer = new ModelXMLWriter();
		StringBuffer sb = new StringBuffer();
		writer.createXMLFromModel(mb.getModel(), sb);

		FileOutputStream file = new FileOutputStream("/tmp/model.xml");
		file.write(sb.toString().getBytes());
		file.close();
	}
}

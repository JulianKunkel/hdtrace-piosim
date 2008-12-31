
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

import junit.framework.TestSuite;

import org.junit.Before;
import org.junit.Test;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.components.Port.Port;
import de.hd.pvs.piosim.model.components.Switch.SimpleSwitch;

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
		NIC nic = new NIC();
		Node maschine = new Node();
		SimpleSwitch switche = new SimpleSwitch();
		Port port = new Port();
		
		mb.addClient(maschine, client);
		mb.addPort(switche, port);
		mb.addNIC(maschine, nic);
		mb.setConnection(nic, port);	
	}
	
}

//	Copyright (C) 2010 Timo Minartz
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
package de.hd.pvs.piosim.power.tests;

import org.junit.Test;

import de.hd.pvs.piosim.power.ComponentException;
import de.hd.pvs.piosim.power.Time;
import de.hd.pvs.piosim.power.acpi.ACPIAnalyzer;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.ACPIDeviceException;
import de.hd.pvs.piosim.power.cluster.NodeFactory;
import de.hd.pvs.piosim.power.cluster.SimpleNode;

public class SimpleNodeTest extends AbstractTestCase {

	@Test
	public void testBasicFeatures() {
		SimpleNode node = NodeFactory.createSimpleNode();

		Time t = Time.getInstance();
		t.reset();

		try {
			node.run();
		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		t.timePassed(100000);
		node.stop();

		ACPIAnalyzer analyzer = new ACPIAnalyzer();
		analyzer.setIACPIAnalyzable(node);

		analyzer.printIACPIAnalyzableStatistics();

		ACPIDevice.deregisterAllDevices();
	}

	@Test
	public void testWithoutACPI() {
		SimpleNode node = NodeFactory.createSimpleNode();

		Time t = Time.getInstance();
		t.reset();

		try {
			node.run();
			t.timePassed(40000);
			node.stop();
			t.timePassed(100000);
			node.run();
			t.timePassed(8000);
			node.stop();
			t.timePassed(2000);
		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		ACPIAnalyzer analyzer = new ACPIAnalyzer();
		analyzer.setIACPIAnalyzable(node);

		analyzer.printIACPIAnalyzableStatistics();

		ACPIDevice.deregisterAllDevices();
	}

	@Test
	public void testWithACPI() {

		SimpleNode node = NodeFactory.createSimpleNode();
		Time t = Time.getInstance();
		t.reset();

		try {
			node.run();
			t.timePassed(40000);
			node.stop();
			node.toSleep();
			t.timePassed(100000);
			node.run();
			t.timePassed(8000);
			node.stop();
			node.toSuspend();
			t.timePassed(2000);
		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ACPIDeviceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		ACPIAnalyzer nodeAnalyzer = new ACPIAnalyzer();
		nodeAnalyzer.setIACPIAnalyzable(node);

		nodeAnalyzer.printIACPIAnalyzableStatistics();

		ACPIDevice.deregisterAllDevices();
	}

}

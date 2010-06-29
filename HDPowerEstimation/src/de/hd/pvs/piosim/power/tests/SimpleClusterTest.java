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
import de.hd.pvs.piosim.power.cluster.ClusterFactory;
import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.cluster.SimpleCluster;

public class SimpleClusterTest extends AbstractTestCase {

	@Test
	public void testWithoutACPI() {

		Time t = Time.getInstance();
		t.reset();

		SimpleCluster cluster = ClusterFactory
				.createSimpleClusterWithSimpleNodes(2);

		try {
			cluster.run();
			t.timePassed(40000);
			cluster.stop();
			t.timePassed(100000);
			cluster.run();
			t.timePassed(8000);
			cluster.stop();
			t.timePassed(2000);
		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		ACPIAnalyzer analyzer = new ACPIAnalyzer();
		analyzer.setIACPIAnalyzable(cluster);

		analyzer.printIACPIAnalyzableStatistics();

		ACPIDevice.deregisterAllDevices();
	}

	@Test
	public void testWithACPI() {

		Time t = Time.getInstance();
		t.reset();

		SimpleCluster cluster = ClusterFactory
				.createSimpleClusterWithSimpleNodes(2);

		Node oneNode = cluster.getNodes().get(0);
		// ///////////////////////////////////////////////////////////////////

		try {
			cluster.run();
			t.timePassed(40000);
			cluster.stop();
			cluster.toSleep();
			t.timePassed(100000);
			cluster.run();
			t.timePassed(8000);
			cluster.stop();
			cluster.toSuspend();
			oneNode.toSleep();
			t.timePassed(2000);
		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ACPIDeviceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		// //////////////////////////////////////////////////////////////////

		ACPIAnalyzer analyzer = new ACPIAnalyzer();
		analyzer.setIACPIAnalyzable(cluster);

		analyzer.printIACPIAnalyzableStatistics();

		analyzer = new ACPIAnalyzer();

		for (Node node : cluster.getNodes()) {
			analyzer.setIACPIAnalyzable(node);
			analyzer.printIACPIAnalyzableStatistics();
		}

		ACPIDevice.deregisterAllDevices();
	}
}

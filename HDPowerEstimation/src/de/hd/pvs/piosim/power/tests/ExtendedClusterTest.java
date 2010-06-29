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

import java.math.BigDecimal;

import org.junit.Test;

import de.hd.pvs.piosim.power.ComponentException;
import de.hd.pvs.piosim.power.Time;
import de.hd.pvs.piosim.power.acpi.ACPIAnalyzer;
import de.hd.pvs.piosim.power.acpi.ACPIDeviceException;
import de.hd.pvs.piosim.power.cluster.ClusterFactory;
import de.hd.pvs.piosim.power.cluster.ExtendedCluster;
import de.hd.pvs.piosim.power.cluster.ExtendedNode;
import de.hd.pvs.piosim.power.cluster.Node;

public class ExtendedClusterTest extends AbstractTestCase {

	
	@Test
	public void testExtendedCluster() {
		Time t = Time.getInstance();
		t.reset();
			
		ExtendedCluster clusterWithSimpleNodes = ClusterFactory.createExtendedClusterWithSimpleNodes(2);
		ExtendedCluster clusterWithExtendedNodesAndEfficiencyOne = ClusterFactory.createExtendedClusterWithExtendedNodes(2);
		for(Node node : clusterWithExtendedNodesAndEfficiencyOne.getNodes()) {
			ExtendedNode extNode = (ExtendedNode) node;
			extNode.setOverhead(new BigDecimal("0"));
		}
		ExtendedCluster clusterWithExtendedNodes = ClusterFactory.createExtendedClusterWithExtendedNodes(2, new BigDecimal("0.7"));
		
		try {
			clusterWithSimpleNodes.run();
			clusterWithExtendedNodesAndEfficiencyOne.run();
			clusterWithExtendedNodes.run();
			t.timePassed(40000);
			clusterWithSimpleNodes.stop();
			clusterWithSimpleNodes.toSleep();
			clusterWithExtendedNodesAndEfficiencyOne.stop();
			clusterWithExtendedNodesAndEfficiencyOne.toSleep();
			clusterWithExtendedNodes.stop();
			clusterWithExtendedNodes.toSleep();
			t.timePassed(100000);
			clusterWithSimpleNodes.run();
			clusterWithExtendedNodesAndEfficiencyOne.run();
			clusterWithExtendedNodes.run();
			t.timePassed(8000);
			clusterWithSimpleNodes.stop();
			clusterWithSimpleNodes.toSuspend();
			clusterWithExtendedNodesAndEfficiencyOne.stop();
			clusterWithExtendedNodesAndEfficiencyOne.toSuspend();
			clusterWithExtendedNodes.stop();
			clusterWithExtendedNodes.toSuspend();
			t.timePassed(2000);
		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ACPIDeviceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
		ACPIAnalyzer clusterWithSimpleNodesAnalyzer = new ACPIAnalyzer();
		ACPIAnalyzer clusterWithExtendedNotesAndEfficencyOneAnalyzer = new ACPIAnalyzer();
		ACPIAnalyzer clusterWithExtendedNotesAnalyzer = new ACPIAnalyzer();
		
		clusterWithSimpleNodesAnalyzer.setIACPIAnalyzable(clusterWithSimpleNodes);
		clusterWithExtendedNotesAndEfficencyOneAnalyzer.setIACPIAnalyzable(clusterWithExtendedNodesAndEfficiencyOne);
		clusterWithExtendedNotesAnalyzer.setIACPIAnalyzable(clusterWithExtendedNodes);
		
		assertAllEquals(clusterWithSimpleNodesAnalyzer,clusterWithExtendedNotesAndEfficencyOneAnalyzer);
		assertTrue(clusterWithSimpleNodesAnalyzer.getPowerConsumption().doubleValue() < clusterWithExtendedNotesAnalyzer.getPowerConsumption().doubleValue());

		clusterWithSimpleNodesAnalyzer.printIACPIAnalyzableStatistics();
		clusterWithExtendedNotesAndEfficencyOneAnalyzer.printIACPIAnalyzableStatistics();
		clusterWithExtendedNotesAnalyzer.printIACPIAnalyzableStatistics();

	}
}

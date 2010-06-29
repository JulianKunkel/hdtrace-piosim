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

import java.util.Map;

import org.junit.Test;

import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.cluster.SimpleNode;
import de.hd.pvs.piosim.power.data.DeviceData;
import de.hd.pvs.piosim.power.data.visualizer.VisualizationData;
import de.hd.pvs.piosim.power.devices.MockDevice;
import de.hd.pvs.piosim.power.replay.Replay;
import de.hd.pvs.piosim.power.replay.ReplayException;
import de.hd.pvs.piosim.power.replay.strategy.SimplePlayStrategy;
import de.hd.pvs.piosim.power.tests.util.TestObjectCreator;

public class VisualizationDataTest extends AbstractTestCase {
	
	@Test
	public void testScaling() {
		
		// 110 items -> y Axis should label every tenth value
		VisualizationData visData = createVisualizationData(110);
		
		assertEquals(10.0,visData.getUtilizationScaling());
		
		// 20 items -> every value
		visData = createVisualizationData(20);
		
		assertEquals(1.0,visData.getUtilizationScaling());
		
		// 199999 items -> every tenthousands value
		visData = createVisualizationData(199999);
		
		assertEquals(10000.0,visData.getUtilizationScaling());
	}
	
	private VisualizationData createVisualizationData(int countSteps) {
		
		TestObjectCreator.countSteps = countSteps;
		
		Node node = new SimpleNode();
		node.add(new MockDevice("testDevice"));
		Map<String,DeviceData> deviceData = TestObjectCreator.createRandomDeviceData(new String[]{"testDevice"});
		
		Replay replay = TestObjectCreator.createReplay(node, deviceData);
		replay.setPlayStrategy(new SimplePlayStrategy());
		try {
			replay.play();
		} catch (ReplayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		VisualizationData visData = new VisualizationData();
		visData.addReplayItems(replay.getReplayItems(), "testItems");
		
		return visData;
	}

}

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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import de.hd.pvs.piosim.power.cluster.BuildException;
import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.data.DeviceData;
import de.hd.pvs.piosim.power.data.visualizer.VisualizerException;
import de.hd.pvs.piosim.power.replay.Replay;
import de.hd.pvs.piosim.power.replay.ReplayException;
import de.hd.pvs.piosim.power.replay.strategy.ApproachPlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.PlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.SimplePlayStrategy;
import de.hd.pvs.piosim.power.tests.util.TestObjectCreator;

public class ReplayTest extends AbstractTestCase {

	@Test
	public void testReplayWithOneDeviceAndOneStrategy() {
		String[] deviceNames = { "CPU1" };
		Map<String, String> mapping = new HashMap<String, String>();
		mapping.put("CPU1", "SimpleCPU");
		Map<String, PlayStrategy> playStrategies = new HashMap<String, PlayStrategy>();
		playStrategies.put("CPU1", new SimplePlayStrategy());
		Map<String, DeviceData> deviceData = new HashMap<String, DeviceData>();

		Node node = null;
		Replay replay = null;
		try {
			node = TestObjectCreator.createSimpleNode(deviceNames, mapping);
			deviceData = TestObjectCreator
					.createApproachDeviceData(deviceNames);
			replay = TestObjectCreator.createReplay(node, playStrategies,
					deviceData);
		} catch (BuildException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		try {
			replay.play();
			replay.visualize(testVisualizer);
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (VisualizerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testReplayWithTwoDevicesAndOneStrategy() {
		String[] deviceNames = { "CPU1", "CPU2" };
		Map<String, String> mapping = new HashMap<String, String>();
		mapping.put("CPU1", "SimpleCPU");
		mapping.put("CPU2", "SimpleCPU");
		Map<String, PlayStrategy> playStrategies = new HashMap<String, PlayStrategy>();
		playStrategies.put("CPU1", new SimplePlayStrategy());
		playStrategies.put("CPU2", new SimplePlayStrategy());
		Map<String, DeviceData> deviceData = new HashMap<String, DeviceData>();

		Node node = null;
		Replay replay = null;
		try {
			node = TestObjectCreator.createSimpleNode(deviceNames, mapping);
			deviceData = TestObjectCreator
					.createApproachDeviceData(deviceNames);
			replay = TestObjectCreator.createReplay(node, playStrategies,
					deviceData);
		} catch (BuildException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}

		try {
			replay.play();
			replay.visualize(testVisualizer);
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (VisualizerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testReplayWithTwoDevicesAndTwoStrategies() {
		String[] deviceNames = { "CPU1", "CPU2" };
		Map<String, String> mapping = new HashMap<String, String>();
		mapping.put("CPU1", "SimpleCPU");
		mapping.put("CPU2", "SimpleCPU");
		Map<String, PlayStrategy> playStrategies = new HashMap<String, PlayStrategy>();
		playStrategies.put("CPU1", new SimplePlayStrategy());
		playStrategies.put("CPU2", new ApproachPlayStrategy());
		Map<String, DeviceData> deviceData = new HashMap<String, DeviceData>();

		Node node = null;
		Replay replay = null;
		try {
			node = TestObjectCreator.createSimpleNode(deviceNames, mapping);
			deviceData = TestObjectCreator
					.createApproachDeviceData(deviceNames);
			replay = TestObjectCreator.createReplay(node, playStrategies,
					deviceData);
		} catch (BuildException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}

		try {
			replay.play();
			replay.visualize(testVisualizer);
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (VisualizerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}

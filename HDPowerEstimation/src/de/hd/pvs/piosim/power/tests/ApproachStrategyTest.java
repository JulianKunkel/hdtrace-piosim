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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.AssertionFailedError;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.cluster.SimpleNode;
import de.hd.pvs.piosim.power.data.DeviceData;
import de.hd.pvs.piosim.power.data.visualizer.VisualizationData;
import de.hd.pvs.piosim.power.data.visualizer.Visualizer;
import de.hd.pvs.piosim.power.data.visualizer.VisualizerException;
import de.hd.pvs.piosim.power.devices.MockDevice;
import de.hd.pvs.piosim.power.replay.Replay;
import de.hd.pvs.piosim.power.replay.ReplayException;
import de.hd.pvs.piosim.power.replay.StrategyDiffer;
import de.hd.pvs.piosim.power.replay.strategy.ApproachPlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.PlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.SimplePlayStrategy;
import de.hd.pvs.piosim.power.tests.util.TestObjectCreator;

public class ApproachStrategyTest extends AbstractTestCase {

	private String componentName = "MockDevice";
	private int countSteps = 15;
	private Node node;
	
	private Replay createReplay(DeviceData deviceData) {
		MockDevice device = createMockDevice(componentName);
		
		node = new SimpleNode();
		node.add(device);

		Map<String,DeviceData> deviceDataMap = new HashMap<String,DeviceData>();
		deviceDataMap.put(componentName,deviceData);
		
		Replay replay = TestObjectCreator.createReplay(node, deviceDataMap);
		replay.setCountSteps(countSteps);
		replay.setStepsize(1000);
		replay.setPlayStrategy(new ApproachPlayStrategy());
		
		return replay;
	}
	
	@Test
	public void testRearrangingOfLoad() {
		
		DeviceData deviceData = new DeviceData();
		
		// data for components
		BigDecimal[] data = new BigDecimal[countSteps];
		
		assert countSteps == 15;
		
		for(int i=0; i<10; ++i) {
			data[i] = new BigDecimal("0.09");
		}
		for(int i=10; i<15; ++i) {
			data[i] = new BigDecimal("0.1");
		}

		deviceData.setUtilization(data);
		
		Replay replay = createReplay(deviceData);
		
		// now rearrange the load
		try {
			replay.play();
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} 
		
		BigDecimal[] utilization = replay.getReplayItems().get(0).getReplayDevice().getUtilization();
		
		// 10 steps with 9 percent utilization, 5 steps with 10 percent utilization
		// => 13 steps with 0 percent utilization, 1 step with 40 percent utilization, 1 step with 100 percent utilization
		
		for(int i=0; i<15; ++i) {
			try {
			if(i < 13)
				assertEquals(0.0,utilization[i].doubleValue());
			if(i == 13)
				assertEquals(0.4,utilization[i].doubleValue());
			if(i == 14)
				assertEquals(1.0,utilization[i].doubleValue());
			} catch (AssertionFailedError e) {
				System.err.println("Assertion failed for i=" + i);
				throw e;
			}
		}
		
		// reset
		replay.reset();
		ApproachPlayStrategy playStrategy = new ApproachPlayStrategy();
		playStrategy.setTolerance(0.2);
		replay.setPlayStrategy(playStrategy);
		
		for(int i=0; i<10; ++i) {
			data[i] = new BigDecimal("0.19");
		}
		for(int i=10; i<15; ++i) {
			data[i] = new BigDecimal("0.2");
		}

		deviceData.setUtilization(data);
		
		try {
			replay.play();
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} 
		
		utilization = replay.getReplayItems().get(0).getReplayDevice().getUtilization();
		
		// 10 steps with 19 percent utilization, 5 steps with 20 percent utilization
		// => 12 steps with 0 percent utilization, 1 step with 90 percent utilization, 2 steps with 100 percent
		
		for(int i=0; i<15; ++i) {
			try {
			if(i < 12)
				assertEquals(0.0,utilization[i].doubleValue());
			if(i == 12)
				assertEquals(0.9,utilization[i].doubleValue());
			if(i > 12)
				assertEquals(1.0,utilization[i].doubleValue());
			} catch (AssertionFailedError e) {
				System.err.println("Assertion failed for i=" + i);
				throw e;
			}
		}
		
		// reset
		replay.reset();
		playStrategy.setTolerance(0.19);
		
		for(int i=0; i<10; ++i) {
			data[i] = new BigDecimal("0.19");
		}
		for(int i=10; i<15; ++i) {
			data[i] = new BigDecimal("0.2");
		}

		deviceData.setUtilization(data);
		
		try {
			replay.play();
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} 
		
		utilization = replay.getReplayItems().get(0).getReplayDevice().getUtilization();
		
		// 10 steps with 19 percent utilization, 5 steps with 20 percent utilization
		// => nothing rearranged, because steps for efficient state change is 12
		
		for(int i=0; i<15; ++i) {
			try {
			if(i < 10)
				assertEquals(0.19,utilization[i].doubleValue());
			else
				assertEquals(0.2,utilization[i].doubleValue());
			} catch (AssertionFailedError e) {
				System.err.println("Assertion failed for i=" + i);
				throw e;
			}
		}
		
		// reset
		replay.reset();
		playStrategy.setTolerance(0.19);
		
		for(int i=0; i<12; ++i) {
			data[i] = new BigDecimal("0.19");
		}
		for(int i=12; i<15; ++i) {
			data[i] = new BigDecimal("0.2");
		}

		deviceData.setUtilization(data);
		
		try {
			replay.play();
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} 
		
		utilization = replay.getReplayItems().get(0).getReplayDevice().getUtilization();
		
		// 10 steps with 19 percent utilization, 5 steps with 20 percent utilization
		// => nothing rearranged, because steps for efficient state change is 12
		
		for(int i=0; i<15; ++i) {
			try {
			if(i < 12)
				assertEquals(0.19,utilization[i].doubleValue());
			else
				assertEquals(0.2,utilization[i].doubleValue());
			} catch (AssertionFailedError e) {
				System.err.println("Assertion failed for i=" + i);
				throw e;
			}
		}
		
		// reset
		replay.reset();
		playStrategy.setTolerance(0.19);
		
		for(int i=0; i<13; ++i) {
			data[i] = new BigDecimal("0.19");
		}
		for(int i=13; i<15; ++i) {
			data[i] = new BigDecimal("0.2");
		}

		deviceData.setUtilization(data);
		
		try {
			replay.play();
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} 
		
		utilization = replay.getReplayItems().get(0).getReplayDevice().getUtilization();
		
		// 10 steps with 19 percent utilization, 5 steps with 20 percent utilization
		// => 10 steps with 0 percent, 1 step with 47 percent, 2 steps with 100 percent, 2 steps with 20 percent
		
		for(int i=0; i<15; ++i) {
			try {
			if(i < 10)
				assertEquals(0.0,utilization[i].doubleValue());
			if(i == 10)
				assertEquals(0.47,utilization[i].doubleValue());
			if(i == 11 || i == 12)
				assertEquals(1.0,utilization[i].doubleValue());
			if(i > 12)
				assertEquals(0.2,utilization[i].doubleValue());
			} catch (AssertionFailedError e) {
				System.err.println("Assertion failed for i=" + i);
				throw e;
			}
		}

	}
	
	@Test
	public void testPlay() {
		
		Logger.getRootLogger().setLevel(Level.OFF);
		Logger.getLogger(Visualizer.class).setLevel(Level.INFO);
		Logger.getLogger(VisualizationData.class).setLevel(Level.INFO);
		
		DeviceData deviceData = createDeviceData();
		MockDevice device = createMockDevice(componentName);
		
		Node node = new SimpleNode();
		node.add(device);

		Map<String,DeviceData> deviceDataMap = new HashMap<String,DeviceData>();
		deviceDataMap.put(componentName,deviceData);
		
		Replay replay = TestObjectCreator.createReplay(node, deviceDataMap);
		replay.setCountSteps(countSteps);
		replay.setStepsize(1000);
		
		StrategyDiffer differ = new StrategyDiffer();
		
		differ.setReplay(replay);
		
		List<PlayStrategy> strategyList = new ArrayList<PlayStrategy>();
		strategyList.add(new SimplePlayStrategy());
		strategyList.add(new ApproachPlayStrategy());
		
		differ.setStrategyList(strategyList);

		try {
			differ.playAndVisualize(countSteps, testVisualizer);
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (VisualizerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}
	
	private MockDevice createMockDevice(String name) {
		MockDevice mockDevice = new MockDevice();
		mockDevice.setName(name);
		mockDevice.setIncStateDuration(0, new BigDecimal("2000"));
		mockDevice.setDecStateDuration(3, new BigDecimal("2000"));
		mockDevice.setIncStateEnergyConsumption(0, new BigDecimal("0.05"));
		mockDevice.setDecStateEnergyConsumption(3, new BigDecimal("0.05"));
		mockDevice.setIdlePowerConsumption(70);
		mockDevice.setLoadPowerConsumption(100);
		mockDevice.setStatePowerConsumption(3, new BigDecimal("10"));
		
		return mockDevice;
	}
	
	private DeviceData createDeviceData() {
		DeviceData deviceData = new DeviceData();
		
		// data for components
		BigDecimal[] data = new BigDecimal[countSteps];
		
		for(int i=0; i<10; ++i) {
			data[i] = new BigDecimal("0.09");
		}
		for(int i=10; i<15; ++i) {
			data[i] = new BigDecimal("0.1");
		}

		deviceData.setUtilization(data);
		
		return deviceData;
	}	
	
}

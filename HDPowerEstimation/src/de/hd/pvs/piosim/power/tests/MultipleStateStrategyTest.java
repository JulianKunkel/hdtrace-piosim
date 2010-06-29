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
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import de.hd.pvs.piosim.power.InvalidValueException;
import de.hd.pvs.piosim.power.acpi.history.ACPIStateChangesHistory;
import de.hd.pvs.piosim.power.acpi.history.CommandLineHistoryExporter;
import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.cluster.SimpleNode;
import de.hd.pvs.piosim.power.data.DeviceData;
import de.hd.pvs.piosim.power.data.visualizer.VisualizerException;
import de.hd.pvs.piosim.power.devices.FourGigMainMemory;
import de.hd.pvs.piosim.power.devices.MockDevice;
import de.hd.pvs.piosim.power.replay.Replay;
import de.hd.pvs.piosim.power.replay.ReplayException;
import de.hd.pvs.piosim.power.replay.strategy.MultipleStatePlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.PlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.SimplePlayStrategy;
import de.hd.pvs.piosim.power.tests.util.TestObjectCreator;

public class MultipleStateStrategyTest extends AbstractTestCase {
	
	private String componentNameWithStates = "FoxyMockMemory";
	private String componentNameWithoutStates = "SimpleMockMemory";
	private int countSteps = 20;
	
	@Test
	public void testWithMainMemory() {
		
		DeviceData deviceDataWithStates = createDeviceData();
		DeviceData deviceDataWithoutStates = createDeviceData();
		
		MockDevice deviceWithStates = createMockDevice(componentNameWithStates);
		MockDevice deviceWithoutStates = createMockDevice(componentNameWithoutStates);
		
		Node node = new SimpleNode();
		node.add(deviceWithStates);
		node.add(deviceWithoutStates);
		
		Map<String,DeviceData> deviceDataMap = new HashMap<String,DeviceData>();
		deviceDataMap.put(componentNameWithStates,deviceDataWithStates);
		deviceDataMap.put(componentNameWithoutStates,deviceDataWithoutStates);
		
		Map<String, PlayStrategy> playStrategies = new HashMap<String, PlayStrategy>();
		playStrategies.put(componentNameWithStates, new MultipleStatePlayStrategy());
		playStrategies.put(componentNameWithoutStates, new SimplePlayStrategy());
		
		Replay replay = TestObjectCreator.createReplay(node, playStrategies,deviceDataMap);
		replay.setCountSteps(countSteps);
		replay.setStepsize(1000);
		
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
	public void testWith4Gig() {
		DeviceData deviceData = createDeviceData();
		
		FourGigMainMemory memory = new FourGigMainMemory();
		memory.setName("4Gig,2Banks");
		
		Node node = new SimpleNode();
		node.add(memory);
		
		Map<String,DeviceData> deviceDataMap = new HashMap<String,DeviceData>();
		deviceDataMap.put("4Gig,2Banks",deviceData);
		
		MultipleStatePlayStrategy memStrategy = new MultipleStatePlayStrategy();
		try {
			memStrategy.setUtilizationStates(new double[]{0,0,0,0.5});
		} catch (InvalidValueException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		Map<String, PlayStrategy> playStrategies = new HashMap<String, PlayStrategy>();
		playStrategies.put("4Gig,2Banks", memStrategy);
		
		Replay replay = TestObjectCreator.createReplay(node, playStrategies,deviceDataMap);
		replay.setCountSteps(countSteps);
		replay.setStepsize(1000);
		
		try {
			replay.play();
			replay.visualize(testVisualizer);

			CommandLineHistoryExporter.export(ACPIStateChangesHistory.getInstance());
			
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (VisualizerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private MockDevice createMockDevice(String name) {
		MockDevice mock = new MockDevice();
		mock.setName(name);
		mock.setCountBanks(4);
		mock.setMemorySize(new BigDecimal("4096"));
		
		mock.setStatePowerConsumption(1, new BigDecimal("30"));
		mock.setStatePowerConsumption(2, new BigDecimal("20"));
		mock.setStatePowerConsumption(3, new BigDecimal("10"));
		mock.setIdlePowerConsumption(40);
		mock.setLoadPowerConsumption(40);
		
		return mock;
	}

	private DeviceData createDeviceData() {

		DeviceData deviceData = new DeviceData();
		
		// data for components
		BigDecimal[] data = new BigDecimal[countSteps];
		
		for(int i=0; i<3; ++i) {
			data[i] = new BigDecimal("0.76");
		}
		for(int i=3; i<6; ++i) {
			data[i] = new BigDecimal("0.51");
		}
		for(int i=6; i<9; ++i) {
			data[i] = new BigDecimal("0.26");
		}
		for(int i=9; i<18; ++i) {
			data[i] = new BigDecimal("0");
		}
		for(int i=18; i<20; ++i) {
			data[i] = new BigDecimal("0.51");
		}

		deviceData.setUtilization(data);
		
		return deviceData;
	}

}

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
package de.hd.pvs.piosim.power.tests.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hd.pvs.piosim.power.DeviceBuilder;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.cluster.BuildException;
import de.hd.pvs.piosim.power.cluster.ExtendedNode;
import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.cluster.NodeFactory;
import de.hd.pvs.piosim.power.cluster.SimpleNode;
import de.hd.pvs.piosim.power.data.DeviceData;
import de.hd.pvs.piosim.power.replay.Replay;
import de.hd.pvs.piosim.power.replay.ReplayDevice;
import de.hd.pvs.piosim.power.replay.ReplayItem;
import de.hd.pvs.piosim.power.replay.strategy.ApproachPlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.OptimalPlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.PlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.SimplePlayStrategy;

public class TestObjectCreator {

	public static int countSteps = 10;
	public static int stepsize = 1000;

	public static Replay createTestReplayWith1DeviceAndDifferentStrategies()
			throws BuildException {
		String deviceName = "DISK";
		Map<String, String> mapping = new HashMap<String, String>();
		DeviceData deviceData = new DeviceData();

		// data for components
		BigDecimal[] data = new BigDecimal[countSteps];

		for (int i = 0; i < countSteps; ++i) {
			MathContext mc = new MathContext(2);
			if (i % 2 == 0) {
				data[i] = new BigDecimal(0.1 * i, mc);
			} else {
				data[i] = data[i - 1];
			}
		}

		deviceData.setUtilization(data);

		mapping.put("DISK", "SimpleDisk");

		List<ReplayItem> items = new ArrayList<ReplayItem>();

		ACPIDevice device = buildACPIDevice(deviceName, mapping);
		ReplayDevice replayDevice = buildReplayDevice(deviceData, device);
		ReplayItem item = buildReplayItem(replayDevice,
				new SimplePlayStrategy());
		items.add(item);

		device = buildACPIDevice(deviceName, mapping);
		replayDevice = buildReplayDevice(deviceData, device);
		item = buildReplayItem(replayDevice, new OptimalPlayStrategy());
		items.add(item);

		device = buildACPIDevice(deviceName, mapping);
		replayDevice = buildReplayDevice(deviceData, device);
		item = buildReplayItem(replayDevice, new ApproachPlayStrategy());
		items.add(item);

		Replay replay = new Replay();
		replay.setReplayItems(items);
		replay.setCountSteps(countSteps);
		replay.setStepsize(stepsize);

		return replay;
	}
	
	public static Replay createReplay(Node node,
			Map<String, DeviceData> deviceData) {

		List<ReplayItem> items = new ArrayList<ReplayItem>();

		for (ACPIDevice device : node.getNodeDevices()) {
			ReplayDevice replayDevice = buildReplayDevice(deviceData.get(device
					.getName()), device);
			ReplayItem item = buildReplayItem(replayDevice, null);
			items.add(item);
		}

		Replay replay = new Replay();
		replay.setReplayItems(items);
		replay.setCountSteps(countSteps);
		replay.setStepsize(stepsize);

		return replay;
	}

	public static Replay createReplay(Node node,
			Map<String, PlayStrategy> playStrategies,
			Map<String, DeviceData> deviceData) {

		List<ReplayItem> items = new ArrayList<ReplayItem>();

		for (ACPIDevice device : node.getNodeDevices()) {
			ReplayDevice replayDevice = buildReplayDevice(deviceData.get(device
					.getName()), device);
			ReplayItem item = buildReplayItem(replayDevice, playStrategies
					.get(device.getName()));
			items.add(item);
		}

		Replay replay = new Replay();
		replay.setReplayItems(items);
		replay.setCountSteps(countSteps);
		replay.setStepsize(stepsize);

		return replay;
	}

	public static Map<String, DeviceData> createRandomDeviceData(String[] names) {

		Map<String, DeviceData> map = new HashMap<String, DeviceData>();

		for (int j = 0; j < names.length; ++j) {
			DeviceData deviceData = new DeviceData();

			// data for component
			BigDecimal[] data = new BigDecimal[countSteps];

			for (int i = 0; i < countSteps; ++i) {
				MathContext mc = new MathContext(2);
				data[i] = new BigDecimal(Math.random(), mc);
			}

			deviceData.setUtilization(data);
			
			

			map.put(names[j], deviceData);
		}

		return map;
	}

	public static SimpleNode createSimpleNode(String[] deviceNames,
			Map<String, String> mapping) throws BuildException {
		SimpleNode node = new SimpleNode();
		for (String name : deviceNames) {
			ACPIDevice device = DeviceBuilder.createACPIDevice(mapping
					.get(name));
			device.setName(name);
			node.add(device);
		}
		return node;
	}
	
	public static ExtendedNode createExtendedNode(String[] deviceNames,
			Map<String, String> mapping) throws BuildException {
		ExtendedNode node = new ExtendedNode();
		for (String name : deviceNames) {
			ACPIDevice device = DeviceBuilder.createACPIDevice(mapping
					.get(name));
			device.setName(name);
			node.add(device);
		}
		return node;
	}

	public static Replay createTestReplayWith3DevicesAndDifferentUtilization(
			PlayStrategy playStrategy) throws BuildException {
		String[] deviceNames = { "CPU1", "CPU2", "DISK" };
		Map<String, String> mapping = new HashMap<String, String>();
		DeviceData[] deviceData = new DeviceData[deviceNames.length];

		// data for components
		BigDecimal[] data = new BigDecimal[countSteps];
		BigDecimal[] data2 = new BigDecimal[countSteps];
		BigDecimal[] data3 = new BigDecimal[countSteps];

		for (int i = 0; i < countSteps; ++i) {
			MathContext mc = new MathContext(2);
			if (i % 2 == 0) {
				data[i] = new BigDecimal(0.1 * i, mc);
				data2[i] = new BigDecimal(1 - (0.1 * i), mc);
				data3[i] = new BigDecimal(1 - (0.1 * i), mc);
			} else {
				data[i] = data[i - 1];
				data2[i] = data2[i - 1];
				data3[i] = data[i - 1];
			}
		}

		deviceData[0] = new DeviceData();
		deviceData[0].setUtilization(data);
		deviceData[1] = new DeviceData();
		deviceData[1].setUtilization(data2);
		deviceData[2] = new DeviceData();
		deviceData[2].setUtilization(data3);

		mapping.put("CPU1", "SimpleCPU");
		mapping.put("CPU2", "SimpleCPU");
		mapping.put("DISK", "SimpleDisk");
		
		Node node = NodeFactory.createEmptySimpleNode("node01");

		List<ReplayItem> items = new ArrayList<ReplayItem>();
		for (int i = 0; i < deviceNames.length; ++i) {
			ACPIDevice device = buildACPIDevice(deviceNames[i], mapping);
			node.add(device);
			ReplayDevice replayDevice = buildReplayDevice(deviceData[i], device);
			ReplayItem item = buildReplayItem(replayDevice, playStrategy);
			items.add(item);
		}

		Replay replay = new Replay();
		replay.setReplayItems(items);
		replay.setCountSteps(countSteps);
		replay.setStepsize(stepsize);

		return replay;
	}

	public static Replay createTestReplayWith3DevicesAndDifferentUtilizationAndSimpleStrategy()
			throws BuildException {

		return createTestReplayWith3DevicesAndDifferentUtilization(new SimplePlayStrategy());
	}

	public static Replay createTestReplayWith3DevicesAndDifferentUtilizationAndOptimalStrategy()
			throws BuildException {

		return createTestReplayWith3DevicesAndDifferentUtilization(new OptimalPlayStrategy());
	}

	public static ACPIDevice buildACPIDevice(String name,
			Map<String, String> mapping) throws BuildException {
		ACPIDevice device = DeviceBuilder.createACPIDevice(mapping.get(name));
		device.setName(name);
		return device;
	}

	public static ReplayDevice buildReplayDevice(DeviceData deviceData,
			ACPIDevice device) {

		ReplayDevice replayDevice = new ReplayDevice();
		replayDevice.setACPIDevice(device);
		replayDevice.setDeviceData(deviceData);

		return replayDevice;
	}

	public static ReplayItem buildReplayItem(ReplayDevice replayDevice,
			PlayStrategy playStrategy) {

		ReplayItem item = new ReplayItem();
		item.setPlayStrategy(playStrategy);
		item.setReplayDevice(replayDevice);
		return item;
	}

	public static Map<String, DeviceData> createLinearDeviceData(String[] names) {

		Map<String, DeviceData> map = new HashMap<String, DeviceData>();

		for (int j = 0; j < names.length; ++j) {
			DeviceData deviceData = new DeviceData();

			// data for component
			BigDecimal[] data = new BigDecimal[countSteps];

			for (int i = 0; i < countSteps; ++i) {
				MathContext mc = new MathContext(2);
				data[i] = new BigDecimal(0.1 * i, mc);
			}

			deviceData.setUtilization(data);

			map.put(names[j], deviceData);
		}

		return map;
	}

	/**
	 * 
	 * 
	 * for (int i = 0; i < countSteps/2; ++i)
	 * data[i] = new BigDecimal(0.1 * i);
	 * 
	 * for (int i = countSteps/2; i < countSteps; ++i) 
	 * data[i] = new BigDecimal(0.09);
	 * 
	 * @param names
	 * @return
	 */
	public static Map<String, DeviceData> createApproachDeviceData(
			String[] names) {

		Map<String, DeviceData> map = new HashMap<String, DeviceData>();

		for (int j = 0; j < names.length; ++j) {
			DeviceData deviceData = new DeviceData();

			// data for component
			BigDecimal[] data = new BigDecimal[countSteps];

			for (int i = 0; i < countSteps / 2; ++i) {
				MathContext mc = new MathContext(2);
				data[i] = new BigDecimal(0.1 * i, mc);
			}

			for (int i = countSteps / 2; i < countSteps; ++i) {
				data[i] = new BigDecimal(0.09);
			}

			deviceData.setUtilization(data);

			map.put(names[j], deviceData);
		}

		return map;
	}
	
	public static DeviceData createMultipleStateDeviceData() {
		DeviceData deviceData = new DeviceData();
		
		BigDecimal[] data = new BigDecimal[countSteps];
		
		assert countSteps == 15;

		for (int i = 0; i < 3; ++i) {
			data[i] = new BigDecimal("1");
		}
		for (int i = 3; i < 6; ++i) {
			data[i] = new BigDecimal("0.7");
		}
		for (int i = 6; i < 9; ++i) {
			data[i] = new BigDecimal("0.09");
		}
		for (int i = 9; i < 12; ++i) {
			data[i] = new BigDecimal("0");
		}
		for (int i = 12; i < 15; ++i) {
			data[i] = new BigDecimal("1");
		}

		deviceData.setUtilization(data);
		
		return deviceData;
	}

	public static Map<String, DeviceData> createZeroUtilizationDeviceData(
			String[] names) {
		
		Map<String, DeviceData> map = new HashMap<String, DeviceData>();

		for (int j = 0; j < names.length; ++j) {
			DeviceData deviceData = new DeviceData();

			// data for component
			BigDecimal[] data = new BigDecimal[countSteps];

			for (int i = 0; i < countSteps; ++i) {
				data[i] = new BigDecimal("0");
			}

			deviceData.setUtilization(data);

			map.put(names[j], deviceData);
		}

		return map;
	}
}

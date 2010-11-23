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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import de.hd.pvs.piosim.power.Time;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;
import de.hd.pvs.piosim.power.cluster.BuildException;
import de.hd.pvs.piosim.power.cluster.ExtendedNode;
import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.cluster.NodeFactory;
import de.hd.pvs.piosim.power.cluster.PowerSupply;
import de.hd.pvs.piosim.power.data.DeviceData;
import de.hd.pvs.piosim.power.data.StatisticData;
import de.hd.pvs.piosim.power.data.visualizer.VisualizerException;
import de.hd.pvs.piosim.power.replay.Replay;
import de.hd.pvs.piosim.power.replay.ReplayDevice;
import de.hd.pvs.piosim.power.replay.ReplayException;
import de.hd.pvs.piosim.power.replay.ReplayItem;
import de.hd.pvs.piosim.power.replay.strategy.ApproachPlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.OptimalPlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.SimplePlayStrategy;
import de.hd.pvs.piosim.power.trace.HDTraceImporter;
import de.hd.pvs.piosim.power.trace.HDTraceImporterException;

public class PVSClusterTest extends AbstractTestCase {

	private String[] componentNames;
	private HDTraceImporter reader;
	private Map<String, String> nameToACPIDeviceMapping;
	private String project;
	private List<ExtendedNode> nodes;

	@Override
	@Before
	public void setUp() {
		super.setUp();
		reader = new HDTraceImporter();
		nameToACPIDeviceMapping = new HashMap<String, String>();
		nodes = new ArrayList<ExtendedNode>();
	}

	@Test
	public void testWithStresstest() {

		project = "stresstest";

		Logger.getRootLogger().setLevel(Level.OFF);

		String[] hostnames = {"node07"};
		String inputProject = inputFolder + "/stresstest/"
				+ project + ".proj";

		nameToACPIDeviceMapping.put("CPU_TOTAL_0", "pvscluster.CPU");
		nameToACPIDeviceMapping.put("CPU_TOTAL_1", "pvscluster.CPU");
		nameToACPIDeviceMapping.put("MEM_USED", "pvscluster.Memory");
		nameToACPIDeviceMapping.put("NET_OUT", "pvscluster.NIC");
		nameToACPIDeviceMapping.put("NET_IN", "pvscluster.NIC");
		nameToACPIDeviceMapping.put("HDD_WRITE", "pvscluster.Disk");

		componentNames = new String[nameToACPIDeviceMapping.size()];
		componentNames = nameToACPIDeviceMapping.keySet().toArray(
				componentNames);

		reader.setFilename(inputProject);

		List<ReplayItem> items = new ArrayList<ReplayItem>();

		int countValues = 0;

		PowerSupply powerSupply = new PowerSupply();

		powerSupply.setProcentualOverhead(new BigDecimal("0.35"));

		for (String hostname : hostnames) {
			try {
				ExtendedNode node = NodeFactory.createExtendedNode(
						nameToACPIDeviceMapping, hostname, new BigDecimal(
								"6.305"));
				node.setPowerSupply(powerSupply);
				reader.addNode(node);
				nodes.add(node);
			} catch (BuildException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}

		try {
			reader.setUtilization(componentNames);

//			Map<Node, BigDecimal> nodeOriginalPowerConsumption = reader
//					.getOriginalPowerConsumption();
//			BigDecimal sum = new BigDecimal("0");
//			for (Node node : nodeOriginalPowerConsumption.keySet()) {
//				System.out.println(node.getName() + ": "
//						+ nodeOriginalPowerConsumption.get(node) + " watt-h");
//				sum = BaseCalculation.sum(sum, nodeOriginalPowerConsumption
//						.get(node));
//			}
//			System.out.println("Sum: " + sum);
		} catch (HDTraceImporterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		Map<ACPIDevice, DeviceData> data = reader.getDeviceData();

		for (ACPIDevice device : data.keySet()) {
			ReplayItem item = new ReplayItem();
			ReplayDevice replayDevice = new ReplayDevice();
			replayDevice.setACPIDevice(device);
			DeviceData deviceData = data.get(device);
			countValues = deviceData.getCountValues();
			replayDevice.setDeviceData(deviceData);
			item.setReplayDevice(replayDevice);
			item.setPlayStrategy(new SimplePlayStrategy());
			items.add(item);
		}

		System.out.println("Global Time: " + (1000 * countValues));

		Replay replay = new Replay();

		replay.setReplayItems(items);
		replay.setStepsize(1000);
		replay.setCountSteps(countValues);

		try {
			replay.play();
			System.out.println("Global Time: "
					+ Time.getInstance().getCurrentTimeInMillis());

			System.out.println("Simple");
			printNodesConsumption();
			StatisticData.getInstance().printStatisticComponentData();
			StatisticData.getInstance().reset();

			replay.visualize(testVisualizer);

			replay.reset();
			replay.setPlayStrategy(new OptimalPlayStrategy());
			replay.play();

			System.out.println("Optimal:");
			printNodesConsumption();
			StatisticData.getInstance().printStatisticComponentData();
			StatisticData.getInstance().reset();

			replay.visualize(testVisualizer);

			replay.reset();
			ApproachPlayStrategy playStrategy = new ApproachPlayStrategy();
			replay.setPlayStrategy(playStrategy);
			replay.play();

			System.out.println("Approach:");
			printNodesConsumption();
			StatisticData.getInstance().printStatisticComponentData();
			StatisticData.getInstance().reset();

			replay.visualize(testVisualizer);

		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (VisualizerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		//		
		// HDTraceMerger traceMerger = new HDTraceMerger();
		// String outputProject = testFolder + "/" + project + ".proj";
		//		
		// try {
		// copyProject(inputProject,outputProject);
		// } catch (IOException e) {
		// e.printStackTrace();
		// fail(e.getMessage());
		// }
		//		
		// traceMerger.merge(outputProject, hostnames[0], componentNames);

	}
	
//	@Test
//	public void testWithBla() {
//
//		project = "partdiff-mpi-non-collective";
//
//		Logger.getRootLogger().setLevel(Level.OFF);
//
//		String[] hostnames = {"node06","node07","node08","node09"};
//		String inputProject = "/home/quark/studium/masterthesis/traces/partdiff-mpi-non-collective-8-4-1800/"
//				+ project + ".proj";
//
//		nameToACPIDeviceMapping.put("CPU_TOTAL_0", "pvscluster.Core");
//		nameToACPIDeviceMapping.put("CPU_TOTAL_1", "pvscluster.Core");
//		nameToACPIDeviceMapping.put("MEM_USED", "pvscluster.Memory");
//		nameToACPIDeviceMapping.put("NET_OUT", "pvscluster.NIC");
//		nameToACPIDeviceMapping.put("NET_IN", "pvscluster.NIC");
//		nameToACPIDeviceMapping.put("HDD_WRITE", "pvscluster.Disk");
//
//		componentNames = new String[nameToACPIDeviceMapping.size()];
//		componentNames = nameToACPIDeviceMapping.keySet().toArray(
//				componentNames);
//
//		reader.setFilename(inputProject);
//
//		List<ReplayItem> items = new ArrayList<ReplayItem>();
//
//		int countValues = 0;
//
//		PowerSupply powerSupply = new PowerSupply();
//
//		powerSupply.setProcentualOverhead(new BigDecimal("0.35"));
//
//		for (String hostname : hostnames) {
//			try {
//				ExtendedNode node = NodeFactory.createExtendedNode(
//						nameToACPIDeviceMapping, hostname, new BigDecimal(
//								"6.305"));
//				node.setPowerSupply(powerSupply);
//				reader.addNode(node);
//				nodes.add(node);
//			} catch (BuildException e) {
//				e.printStackTrace();
//				fail(e.getMessage());
//			}
//		}
//
//		try {
//			reader.setUtilization(componentNames);
//		} catch (HDTraceImporterException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//
//		Map<ACPIDevice, DeviceData> data = reader.getDeviceData();
//
//		for (ACPIDevice device : data.keySet()) {
//			ReplayItem item = new ReplayItem();
//			ReplayDevice replayDevice = new ReplayDevice();
//			replayDevice.setACPIDevice(device);
//			DeviceData deviceData = data.get(device);
//			countValues = deviceData.getCountValues();
//			replayDevice.setDeviceData(deviceData);
//			item.setReplayDevice(replayDevice);
//			item.setPlayStrategy(new SimplePlayStrategy());
//			items.add(item);
//		}
//
//		System.out.println("Global Time: " + (1000 * countValues));
//
//		Replay replay = new Replay();
//
//		replay.setReplayItems(items);
//		replay.setStepsize(1000);
//		replay.setCountSteps(countValues);
//
//		try {
//			replay.play();
//			System.out.println("Global Time: "
//					+ Time.getInstance().getCurrentTimeInMillis());
//
//			System.out.println("Simple");
//			printNodesConsumption();
//			StatisticData.getInstance().printStatisticComponentData();
//			StatisticData.getInstance().reset();
//
//			replay.visualize(testVisualizer);
//
//			replay.reset();
//			replay.setPlayStrategy(new OptimalPlayStrategy());
//			replay.play();
//
//			System.out.println("Optimal:");
//			printNodesConsumption();
//			StatisticData.getInstance().printStatisticComponentData();
//			StatisticData.getInstance().reset();
//
//			replay.visualize(testVisualizer);
//
//			replay.reset();
//			replay.setPlayStrategy(new ApproachPlayStrategy());
//			replay.play();
//
//			System.out.println("Approach:");
//			printNodesConsumption();
//			StatisticData.getInstance().printStatisticComponentData();
//			StatisticData.getInstance().reset();
//
//			replay.visualize(testVisualizer);
//
//		} catch (ReplayException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		} catch (VisualizerException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//
//	}

	private void printNodesConsumption() {
		BigDecimal sum = new BigDecimal("0");

		for (Node node : nodes) {
			System.out.println(node.getName() + ": "
					+ node.getEnergyConsumption() + " watt-h");
			sum = BaseCalculation.sum(sum, node.getEnergyConsumption());
		}

		System.out.println("Sum: " + sum + " watt-h");

	}

}

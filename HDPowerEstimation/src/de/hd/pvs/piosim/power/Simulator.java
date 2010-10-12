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

package de.hd.pvs.piosim.power;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;
import de.hd.pvs.piosim.power.cluster.BuildException;
import de.hd.pvs.piosim.power.cluster.ExtendedNode;
import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.cluster.NodeFactory;
import de.hd.pvs.piosim.power.cluster.PowerSupply;
import de.hd.pvs.piosim.power.data.DeviceData;
import de.hd.pvs.piosim.power.data.visualizer.CommandlineVisualizer;
import de.hd.pvs.piosim.power.data.visualizer.Visualizer;
import de.hd.pvs.piosim.power.data.visualizer.VisualizerException;
import de.hd.pvs.piosim.power.replay.Replay;
import de.hd.pvs.piosim.power.replay.ReplayDevice;
import de.hd.pvs.piosim.power.replay.ReplayException;
import de.hd.pvs.piosim.power.replay.ReplayItem;
import de.hd.pvs.piosim.power.replay.strategy.SimplePlayStrategy;
import de.hd.pvs.piosim.power.trace.HDTraceImporter;
import de.hd.pvs.piosim.power.trace.HDTraceImporterException;

/**
 * @author Timo Minartz
 * 
 */
public class Simulator {
	
	private static List<Node> createEEclustNodes(List<String> hostnames, Map<String, String> nameToACPIDeviceMapping) {
		
		PowerSupply powerSupply = new PowerSupply();

		powerSupply.setProcentualOverhead(new BigDecimal("0.35"));

		BigDecimal overhead = new BigDecimal("6.305");
		
		List<Node> nodes = new ArrayList<Node>();

		for (String hostname : hostnames) {

			try {
				ExtendedNode node = NodeFactory.createExtendedNode(
						nameToACPIDeviceMapping, hostname);
				node.setPowerSupply(powerSupply);
				node.setOverhead(overhead);
				nodes.add(node);
			} catch (BuildException e) {
				e.printStackTrace();
				return null;
			}
			
		}
		
		return nodes;
	}

	private static void printUsage() {
		System.out
				.println("usage: Simulator projectFile timestep [Visualizer]\nargs:\n\tprojectFile: Path to .proj File\n\ttimestep: Optional - Duration of one timestep\n\tVisualizer: Optional - one out of CommandlineVisualizer (std), StepCharVisualizer\n");
	}

	public static void main(String[] args) {

		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);

		Logger logger = Logger.getLogger(Simulator.class);

		logger.info("Simulator started");

		if (args.length < 1) {
			System.err.println("Not enough arguments specified.");
			printUsage();
			return;
		}

		if (args[0].equals("-h") || args[0].equals("--help")
				|| args[0].equals("--usage")) {
			printUsage();
			return;
		}

		String inputProject = "";
		int stepsize = -1;
		Visualizer visualizer = null;
		String visualizerString = "StepChartVisualizer";

		try {
			inputProject = args[0];
			
			if(args.length >= 2) {
				stepsize = Integer.parseInt(args[1]);
			}

			if (args.length == 3) {
				visualizerString = args[2];
			} else if (args.length > 3)
				throw new Exception("Invalid count of arguments.");

			Class<?> clazz = Class
					.forName("de.hd.pvs.piosim.power.data.visualizer."
							+ visualizerString);
			visualizer = (Visualizer) clazz.newInstance();
			visualizer.printDetails(false);
		} catch (Exception ex) {
			System.err.println("Problem with commandline args: "
					+ ex.getMessage());
			ex.printStackTrace();
			printUsage();
			return;
		}

		Map<String, String> nameToACPIDeviceMapping = new HashMap<String, String>();

		nameToACPIDeviceMapping.put("CPU_TOTAL", "pvscluster.CPU");
		nameToACPIDeviceMapping.put("CPU_TOTAL_0", "pvscluster.CPU");
		nameToACPIDeviceMapping.put("CPU_TOTAL_1", "pvscluster.CPU");
		nameToACPIDeviceMapping.put("CPU_TOTAL_2", "pvscluster.CPU");
		nameToACPIDeviceMapping.put("CPU_TOTAL_3", "pvscluster.CPU");
//		nameToACPIDeviceMapping.put("CPU_TOTAL_4", "pvscluster.CPU");
//		nameToACPIDeviceMapping.put("CPU_TOTAL_5", "pvscluster.CPU");
//		nameToACPIDeviceMapping.put("CPU_TOTAL_6", "pvscluster.CPU");
//		nameToACPIDeviceMapping.put("CPU_TOTAL_7", "pvscluster.CPU");
//		nameToACPIDeviceMapping.put("CPU_TOTAL_8", "pvscluster.CPU");
//		nameToACPIDeviceMapping.put("CPU_TOTAL_9", "pvscluster.CPU");
//		nameToACPIDeviceMapping.put("CPU_TOTAL_10", "pvscluster.CPU");
//		nameToACPIDeviceMapping.put("CPU_TOTAL_11", "pvscluster.CPU");
//		nameToACPIDeviceMapping.put("CPU_TOTAL_12", "pvscluster.CPU");
//		nameToACPIDeviceMapping.put("CPU_TOTAL_13", "pvscluster.CPU");
//		nameToACPIDeviceMapping.put("CPU_TOTAL_14", "pvscluster.CPU");
//		nameToACPIDeviceMapping.put("CPU_TOTAL_15", "pvscluster.CPU");
		nameToACPIDeviceMapping.put("MEM_USED", "eeclust.Memory");
		nameToACPIDeviceMapping.put("NET_OUT_eth0", "pvscluster.NIC");
		nameToACPIDeviceMapping.put("NET_IN_eth0", "pvscluster.NIC");
		nameToACPIDeviceMapping.put("HDD_WRITE", "eeclust.Disk");
		
		String[] componentNames = new String[nameToACPIDeviceMapping.size()];
		componentNames = nameToACPIDeviceMapping.keySet().toArray(componentNames);
		
		HDTraceImporter reader = new HDTraceImporter();
		
		reader.setFilename(inputProject);
		List<String> hostnames = reader.getHostnames();
		
		List<Node> nodes = createEEclustNodes(hostnames, nameToACPIDeviceMapping);
		
		for(Node node : nodes)
			reader.addNode(node);

		long before_read, after_read, before_sim, after_sim;

		try {

			before_read = System.currentTimeMillis();

			logger.info("Read utilization from project file...");

			before_read = System.currentTimeMillis();
			reader.setUtilization(componentNames);
			after_read = System.currentTimeMillis();

			logger.info("Read utilization from project file: Found "
					+ nodes.size() + " nodes.");
			logger.info("Trace file started at " + reader.getMinTimestamp()
					+ " till " + reader.getMaxTimestamp());

			logger.info("The trace file duration is "
					+ (reader.getMaxTimestamp() - reader.getMinTimestamp())
					+ " seconds");

			logger.info("The stepsize is varying from "
					+ reader.getMinStepsize() + " to "
					+ reader.getMaxStepsize());
			
			if(stepsize <= 0) 
				stepsize = (int) (reader.getMinStepsize() + reader.getMaxStepsize()) / 2;

			Map<ACPIDevice, DeviceData> data = reader.getDeviceData();

			List<ReplayItem> items = new ArrayList<ReplayItem>();

			int countValues = 0;

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

			logger.info("Found " + countValues + " values. Doing replay with "
					+ countValues + " steps with a size of " + stepsize + "ms");

			Replay replay = new Replay();

			replay.setReplayItems(items);
			replay.setStepsize(stepsize);
			replay.setCountSteps(countValues);

			replay.setPlayStrategy(new SimplePlayStrategy());

			before_sim = System.currentTimeMillis();
			replay.play();
			after_sim = System.currentTimeMillis();

			logger.info("Trace read duration: " + (after_read - before_read)
					+ " ms");
			logger.info("Replay duration: " + (after_sim - before_sim) + " ms");

			Map<ACPIDevice, ReplayDevice> deviceMap = new HashMap<ACPIDevice, ReplayDevice>();

			for (ReplayItem item : items) {
				deviceMap.put(item.getReplayDevice().getACPIDevice(),
						item.getReplayDevice());
			}

			for (Node node : nodes) {
				System.out.println(node.getName());
				List<ACPIDevice> devices = node.getNodeDevices();
				for (ACPIDevice device : devices) {
					System.out.println(device.getName());
					ReplayDevice replayDevice = deviceMap.get(device);

					System.out.println("Average utilization: "
							+ BaseCalculation.getAverage(replayDevice
									.getUtilization()) + " %");

					System.out.println("Total energy consumption: "
							+ device.getPowerConsumption() + " Wh");
				}
			}

			replay.visualize(visualizer);

		} catch (HDTraceImporterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReplayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (VisualizerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

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

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
import de.hd.pvs.piosim.power.data.visualizer.StepChartVisualizer;
import de.hd.pvs.piosim.power.data.visualizer.Visualizer;
import de.hd.pvs.piosim.power.data.visualizer.VisualizerException;
import de.hd.pvs.piosim.power.replay.Replay;
import de.hd.pvs.piosim.power.replay.ReplayDevice;
import de.hd.pvs.piosim.power.replay.ReplayException;
import de.hd.pvs.piosim.power.replay.ReplayItem;
import de.hd.pvs.piosim.power.replay.strategy.SimplePlayStrategy;
import de.hd.pvs.piosim.power.tools.MappingFileReader;
import de.hd.pvs.piosim.power.tools.MappingFileReaderException;
import de.hd.pvs.piosim.power.trace.HDTraceImporter;
import de.hd.pvs.piosim.power.trace.HDTraceImporterException;

/**
 * @author Timo Minartz
 * 
 */
public class Simulator {

	private static String applicationName = "Power Estimator";
	private static String commandLineSyntax = "java de.hd.pvs.piosim.power.Simulator <args>";

	private static List<Node> createEEclustNodes(List<String> hostnames,
			Map<String, String> nameToACPIDeviceMapping) {

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
	
	public static String[] getVisualizers() {
		List<String> visualizers = new ArrayList<String>();
		
		String packageName = "de.hd.pvs.piosim.power.data.visualizer";

		File folder = new File("src/" + packageName.replace('.', '/'));

		String[] filenames = folder.list();

		for (String filename : filenames) {
			if (filename.endsWith("Visualizer.java")) {
				filename = filename.substring(0, filename.lastIndexOf(".java"));
				if(!filename.equals("Visualizer"))
					visualizers.add(filename);
			}
		}

		String[] visualizer = new String[visualizers.size()];
		return visualizers.toArray(visualizer);
	}
	
	public static String[] getLogLevels() {
		
		Field[] fields;
		try {
			fields = Class.forName("org.apache.log4j.Level").getFields();
		} catch (ClassNotFoundException e) {
			return null;
		}
		
		Set<String> set = new HashSet<String>();
		
		for(Field field : fields) {
			if(!field.getName().contains("_")) {
				set.add(field.getName());
			}
		}
		
		String[] loglevels = new String[set.size()];
		
		return set.toArray(loglevels);
	}

	public static void main(String[] args) {

		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);

		Logger logger = Logger.getLogger(Simulator.class);

		logger.info("Simulator started");
		
		// params to be read from commandline
		String inputProject = "";
		String mappingFile = "";
		int stepsize = -1;
		Visualizer visualizer = new StepChartVisualizer();

		Options opt = new Options();

		opt.addOption("h", false, "Print help for " + applicationName);
		opt.addOption("p", true, "Path to .proj file");
		opt.addOption("m", true, "Path to device mapping file");
		opt.addOption("t", true,
				"Optional - Duration of one timestep, default reading from trace file");
		opt.addOption("v", true,
				"Optional - Visualizer for output, default " + visualizer.getClass().getSimpleName());
		opt.addOption("l", true,
				"Optional - Sets the loglevel to argument. Default: "
						+ Logger.getRootLogger().getLevel().toString());

		HelpFormatter f = new HelpFormatter();

		

		try {

			BasicParser parser = new BasicParser();
			CommandLine cl = parser.parse(opt, args);

			if (cl.hasOption('h')) {
				f.printHelp(commandLineSyntax, opt);
			} else {
				// process needed arguments
				if (!cl.hasOption('p')) {
					System.err.println("Missing argument -p");
					f.printHelp(commandLineSyntax, opt);
					return;
				} else {
					inputProject = cl.getOptionValue('p');
				}
				if (!cl.hasOption('m')) {
					System.err.println("Missing argument -m");
					f.printHelp(commandLineSyntax, opt);
					return;
				} else {
					mappingFile = cl.getOptionValue('m');
				}

				if (cl.hasOption('t')) {
					try {
						stepsize = Integer.parseInt(cl.getOptionValue('t'));
					} catch (NumberFormatException e) {
						System.err
								.println("Please specify integer value for param -t");
						throw e;
					}
				}
				if (cl.hasOption('v')) {
					try {
						if(cl.getOptionValue('v').contains("."))
							visualizer = (Visualizer) Class.forName(cl.getOptionValue('v')).newInstance();
						else
							visualizer = (Visualizer) Class.forName("de.hd.pvs.piosim.power.data.visualizer." + cl.getOptionValue('v')).newInstance();
						
						visualizer.printDetails(false);
					} catch (ClassNotFoundException e) {
						System.err.println("No such visualizer: "
								+ cl.getOptionValue('v') + ". Has to be one out of:");
						String[] visualizers = getVisualizers();
						for(String visualizerString : visualizers)
							System.err.print(visualizerString + " ");
						System.err.println();
						throw e;
					}

				}
				if (cl.hasOption('l')) {
					try {
						Level level = null;
						Logger.getRootLogger().setLevel(
								(Level) Class.forName("org.apache.log4j.Level")
										.getField(cl.getOptionValue('l'))
										.get(level));
					} catch (NoSuchFieldException e) {
						System.err.println("No such loglevel: "
								+ cl.getOptionValue('l') + ". Has to be one out of:");
						String[] loglevels = getLogLevels();
						for(String level : loglevels)
							System.err.print(level + " ");
						System.err.println();
						
						throw e;
					}

				}

			}
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			f.printHelp(commandLineSyntax, opt);
			return;
		} catch (Exception e) {
			f.printHelp(commandLineSyntax, opt);
			return;
		}

		Map<String, String> nameToACPIDeviceMapping;
		try {
			nameToACPIDeviceMapping = MappingFileReader
					.readNameToACPIDeviceMapping(mappingFile);
		} catch (MappingFileReaderException ex) {
			System.err.println("Problems while trying to read mappingFile: "
					+ mappingFile);
			ex.printStackTrace();
			return;
		}

		String[] componentNames = new String[nameToACPIDeviceMapping.size()];
		componentNames = nameToACPIDeviceMapping.keySet().toArray(
				componentNames);

		HDTraceImporter reader = new HDTraceImporter();

		reader.setFilename(inputProject);
		List<String> hostnames = reader.getHostnames();

		List<Node> nodes = createEEclustNodes(hostnames,
				nameToACPIDeviceMapping);

		for (Node node : nodes)
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

			if (stepsize <= 0)
				stepsize = (int) (reader.getMinStepsize() + reader
						.getMaxStepsize()) / 2;

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

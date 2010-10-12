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
import org.junit.Test;

import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;
import de.hd.pvs.piosim.power.cluster.BuildException;
import de.hd.pvs.piosim.power.cluster.ExtendedNode;
import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.cluster.NodeFactory;
import de.hd.pvs.piosim.power.cluster.PowerSupply;
import de.hd.pvs.piosim.power.data.DeviceData;
import de.hd.pvs.piosim.power.replay.Replay;
import de.hd.pvs.piosim.power.replay.ReplayDevice;
import de.hd.pvs.piosim.power.replay.ReplayException;
import de.hd.pvs.piosim.power.replay.ReplayExporter;
import de.hd.pvs.piosim.power.replay.ReplayItem;
import de.hd.pvs.piosim.power.replay.strategy.ApproachPlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.OptimalPlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.PlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.SimplePlayStrategy;
import de.hd.pvs.piosim.power.trace.HDTraceExporter;
import de.hd.pvs.piosim.power.trace.HDTraceExporterException;
import de.hd.pvs.piosim.power.trace.HDTraceImporter;
import de.hd.pvs.piosim.power.trace.HDTraceImporterException;
import de.hd.pvs.piosim.power.trace.TopologyNodeSet;

public class ImportAndExportTest extends AbstractTestCase {

	String project;
	BigDecimal offset = new BigDecimal("0");

	@Test
	public void testImportAndExportWithStresstest() {

		Logger.getRootLogger().setLevel(Level.OFF);
		Logger.getLogger(TopologyNodeSet.class).setLevel(Level.DEBUG);

		String[] componentNames;
		HDTraceImporter reader = new HDTraceImporter();
		Map<String, String> nameToACPIDeviceMapping = new HashMap<String, String>();

		List<Node> nodes = new ArrayList<Node>();

		String traceDescription = "Stresstest of node07 on PVS cluster";
		String applicationName = "stresstest";

		project = "stresstest";

		String[] hostnames = { "node06", "node07" };
		String inputProject = this.inputFolder + "/stresstest/" + project
				+ ".proj";

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

		try {

			// import

			PowerSupply powerSupply = new PowerSupply();

			powerSupply.setProcentualOverhead(new BigDecimal("0.35"));

			BigDecimal overhead = new BigDecimal("6.305");

			for (String hostname : hostnames) {
				ExtendedNode node = NodeFactory.createExtendedNode(
						nameToACPIDeviceMapping, hostname);
				node.setPowerSupply(powerSupply);
				node.setOverhead(overhead);
				reader.addNode(node);
				nodes.add(node);
			}

			reader.setUtilization(componentNames);
			offset = BaseCalculation.multiply(new BigDecimal(reader
					.getMinTimestamp() + 1), BaseCalculation.THOUSAND);
			
			System.err.println(reader.getMinStepsize());
			System.err.println(reader.getMaxStepsize());

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

			Replay replay = new Replay();

			replay.setReplayItems(items);
			replay.setStepsize(1000);
			replay.setCountSteps(countValues);

			String projectPath = this.outputFolder.getAbsolutePath() + "/"
					+ project + ".proj";

			
			Map<String, PlayStrategy> playStrategies = new HashMap<String,PlayStrategy>();
			
			playStrategies.put("Simple Strategy", new SimplePlayStrategy());
			playStrategies.put("Optimal Strategy", new OptimalPlayStrategy());
			playStrategies.put("Approach Strategy", new ApproachPlayStrategy());
			
			Map<String,String> grouping = new HashMap<String,String>();
			grouping.put("CPU_TOTAL_0", "CPU");
			grouping.put("CPU_TOTAL_1", "CPU");
			grouping.put("MEM_USED", "MEM");
			grouping.put("HDD_WRITE", "HDD");
			grouping.put("HDD_READ", "HDD");
			grouping.put("NET_IN", "NET");
			grouping.put("NET_OUT", "NET");
			grouping.put("TOTAL", "Power");
			
			HDTraceExporter traceExporter = new HDTraceExporter(projectPath,
					traceDescription, applicationName, nodes, new ArrayList<String>(playStrategies.keySet()),
					grouping, offset);


			ReplayExporter exporter = new ReplayExporter();
			
			exporter.play(replay,playStrategies);

			// and do the export

			exporter.export(traceExporter);

			traceExporter.finalize();

			// HDTraceMerger traceMerger = new HDTraceMerger();
			//
			// try {
			// copyProject(inputProject, projectPath);
			// } catch (IOException e) {
			// e.printStackTrace();
			// fail(e.getMessage());
			// }
			//
			// traceMerger.merge(projectPath, hostnames, componentNames,
			// reader.getMinValue());
			//
			// System.out.println(projectPath);

		} catch (HDTraceImporterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (BuildException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (HDTraceExporterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}
}

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

import de.hd.pvs.piosim.power.acpi.ACPIComponent;
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
import de.hd.pvs.piosim.power.devices.MockDevice;
import de.hd.pvs.piosim.power.replay.Replay;
import de.hd.pvs.piosim.power.replay.ReplayDevice;
import de.hd.pvs.piosim.power.replay.ReplayException;
import de.hd.pvs.piosim.power.replay.ReplayExporter;
import de.hd.pvs.piosim.power.replay.ReplayItem;
import de.hd.pvs.piosim.power.replay.strategy.ApproachPlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.MultipleStatePlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.OptimalPlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.PlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.SimplePlayStrategy;
import de.hd.pvs.piosim.power.tests.util.TestObjectCreator;
import de.hd.pvs.piosim.power.trace.HDTraceExporter;
import de.hd.pvs.piosim.power.trace.HDTraceExporterException;
import de.hd.pvs.piosim.power.trace.HDTraceImporter;
import de.hd.pvs.piosim.power.trace.HDTraceImporterException;

public class ThesisPicturesTest extends AbstractTestCase {
	
	private String componentName = "Component";
	private Node node = null;
	private Replay replay = null;
	private Visualizer visualizer;
	
	@Override
	public void setUp() {
		super.setUp();
		TestObjectCreator.countSteps = 15;
		Map<String,String> mapping = new HashMap<String,String>();
		mapping.put(componentName,"MockDevice");
		try {
			node = TestObjectCreator.createSimpleNode(new String[]{componentName}, mapping);
		} catch (BuildException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		MockDevice mock = (MockDevice) node.getDevice(componentName);
		setMockDevice(mock);
		
		visualizer = new StepChartVisualizer();
		visualizer.isPrintLegend(false);
	}
	
//	@Test
//	public void testSimpleStrategy() {
//		replay = buildReplay(new SimplePlayStrategy());
//		
//		try {
//			replay.play();
//			replay.visualize(visualizer);
//		} catch (ReplayException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		} catch (VisualizerException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//	}
//	
//	@Test
//	public void testOptimalStrategy() {
//		replay = buildReplay(new OptimalPlayStrategy());
//		
//		try {
//			replay.play();
//			replay.visualize(visualizer);
//		} catch (ReplayException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		} catch (VisualizerException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//	}
	
	@Test
	public void testExactApproachStrategy() {
		ApproachPlayStrategy strategy = new ApproachPlayStrategy();
		replay = buildReplay(strategy);
		
		try {
			replay.play();
			replay.visualize(visualizer);
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (VisualizerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testMultipleStateStrategy() {
		
		replay = buildReplay(new MultipleStatePlayStrategy());
		
		try {
			replay.play();
			replay.visualize(visualizer);
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (VisualizerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testProjectWithStresstest() {
		
		Logger.getRootLogger().setLevel(Level.OFF);
		Logger.getLogger(Visualizer.class).setLevel(Level.DEBUG);
		visualizer.isPrintLegend(true);
		
		String[] componentNames;
		HDTraceImporter reader = new HDTraceImporter();
		Map<String, String> nameToACPIDeviceMapping = new HashMap<String, String>();

		List<Node> nodes = new ArrayList<Node>();

		String traceDescription = "Stresstest of node07 on PVS cluster";
		String applicationName = "stresstest";

		String project = "stresstest";

		String[] hostnames = { "node07" };
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
			BigDecimal offset = BaseCalculation.multiply(new BigDecimal(reader
					.getMinTimestamp() + 1), BaseCalculation.THOUSAND);

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
			
			/* exporting stuff */
			
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
			
			/* replay with different strategies */

			Replay replay = new Replay();

			replay.setReplayItems(items);
			replay.setStepsize(1000);
			replay.setCountSteps(countValues);
			
			replay.setPlayStrategy(playStrategies.get("Simple Strategy"));
			replay.play();
			
			Map<ACPIDevice, BigDecimal> simple = new HashMap<ACPIDevice, BigDecimal>();
			
			BigDecimal simpleSumConsumption = new BigDecimal("0");
			
			for(ACPIDevice device : nodes.get(0).getNodeDevices()) {
				simple.put(device,device.getEnergyConsumption());
				simpleSumConsumption = BaseCalculation.sum(simpleSumConsumption, device.getEnergyConsumption());
			}
			
			replay.visualize(new StepChartVisualizer());
			
			exporter.add(replay, "Simple Strategy");
			
			for(String strategyName : new String[]{"Optimal Strategy","Approach Strategy"}) {
				replay.reset();
				
				replay.setPlayStrategy(playStrategies.get(strategyName));
				replay.play();
				
				System.out.println(strategyName);
				
				BigDecimal thisSumConsumption = new BigDecimal("0");
				BigDecimal thisSumDuration = new BigDecimal("0");
				int thisSumCountChanges = 0;
				
				for(ACPIDevice device : nodes.get(0).getNodeDevices()) {
					BigDecimal simpleConsumption = simple.get(device);
					BigDecimal thisConsumption = device.getEnergyConsumption();
					ACPIComponent component = device.getACPIComponent();
					System.out.println(device.getName() + " " + formattedString(simpleConsumption) + " " + 
							formattedString(thisConsumption) + " " + formattedString(BaseCalculation.multiply(BaseCalculation.substract(BaseCalculation.ONE,BaseCalculation.divide(thisConsumption, simpleConsumption)),BaseCalculation.HUNDRED))
							+ " " + component.getACPIStateCount(3) + " " + formattedString(component.getACPIStateTimes(3)));
					
					thisSumConsumption = BaseCalculation.sum(thisSumConsumption, thisConsumption);
					thisSumDuration = BaseCalculation.sum(thisSumDuration, component.getACPIStateTimes(3));
					thisSumCountChanges += component.getACPIStateCount(3);
				
				}
				
				System.out.println("total " + formattedString(simpleSumConsumption) + " " + formattedString(thisSumConsumption) + " " + formattedString(BaseCalculation.multiply(BaseCalculation.substract(BaseCalculation.ONE,BaseCalculation.divide(thisSumConsumption, simpleSumConsumption)),BaseCalculation.HUNDRED))
						+ " " + thisSumCountChanges + " " + formattedString(thisSumDuration));
				
				replay.visualize(new StepChartVisualizer());
				
				exporter.add(replay, strategyName);
			}
			
			

			// and do the export

			exporter.export(traceExporter);

			traceExporter.finalize();

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
		} catch (VisualizerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void setMockDevice(MockDevice mock) {

		mock.setLoadPowerConsumption(100);
		mock.setIdlePowerConsumption(80);
		
		mock.setStateEnergyConsumption(1, 75);
		mock.setStateEnergyConsumption(2, 50);
		mock.setStateEnergyConsumption(3, 25);
		
		mock.setDecStateDuration(0, 2000);
		mock.setIncStateDuration(3, 0);
		
		mock.setDecStateEnergyConsumption(0, 2);
	}
	
	private Replay buildReplay(PlayStrategy playStrategy) {
		Map<String,DeviceData> deviceData = new HashMap<String,DeviceData>();
		deviceData.put(componentName, TestObjectCreator.createMultipleStateDeviceData());
		
		Map<String,PlayStrategy> playStrategies = new HashMap<String,PlayStrategy>();
		playStrategies.put(componentName,playStrategy);
		
		visualizer.setChartTitle(playStrategy.getClass().getSimpleName());
		
		return TestObjectCreator.createReplay(node, playStrategies, deviceData);
	}

	
	private String formattedString(BigDecimal bigDecimal) {
		String s = bigDecimal.toString();
		
		if(s.length() > 8) {
			s = s.substring(0,6);
		}
		
		return s;
	}
}

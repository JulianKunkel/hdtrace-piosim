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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.hd.pvs.TraceFormat.TraceFormatWriter;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsEntryType;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.power.acpi.ACPIComponent;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.cluster.NodeFactory;

public class TraceWriterTest extends AbstractTestCase {

	@Test
	public void testSimpleWriter() throws IOException {
		
		TraceFormatWriter writer = new TraceFormatWriter(this.outputFolder.getAbsolutePath() + "/simple", "test descrpition", "test application",  
				new String []{"Host","Energy","Component"});
		
		Node node = NodeFactory.createExtendedNode("node06");
		
		TopologyNode topoNode = writer.createInitalizeTopology(new String[]{node.getName()});
		writer.createInitalizeTopology(new String[]{node.getName(), "EstimatedEnergyStates"});
		

		Map<ACPIComponent,StatisticsDescription> componentStatistics = new HashMap<ACPIComponent,StatisticsDescription>();
		Map<ACPIComponent,TopologyNode> componentStates = new HashMap<ACPIComponent,TopologyNode>();

		for(ACPIDevice device : node.getNodeDevices()) {
			TopologyNode topologyNode = writer.createInitalizeTopology(new String[]{node.getName(), "EstimatedEnergyStates", device.getName()});
			componentStates.put(device.getACPIComponent(), topologyNode);
		}

		for(ACPIDevice device : node.getNodeDevices())
			writer.writeStateStart(componentStates.get(device.getACPIComponent()), "ACPI0", new Epoch(0.0));

		int state = 0;
		for(int time=1; time < 4; ++time) {
			for(ACPIDevice device : node.getNodeDevices()) {	
				writer.writeStateEnd(componentStates.get(device.getACPIComponent()), "ACPI" + state, new Epoch(time,0));
				writer.writeStateStart(componentStates.get(device.getACPIComponent()), "ACPI" + (state+1), new Epoch(time,0));
			}
			state++;
		}

		for(ACPIDevice device : node.getNodeDevices())
			writer.writeStateEnd(componentStates.get(device.getACPIComponent()), "ACPI" + state, new Epoch(5.0));

		
		// now try to write some statistics:
		StatisticsGroupDescription estimatedEnergy = new StatisticsGroupDescription("EstimatedEnergy");
		
		for(ACPIDevice device : node.getNodeDevices()) {
			StatisticsDescription description = new StatisticsDescription(estimatedEnergy, device.getName(), StatisticsEntryType.FLOAT, estimatedEnergy.getSize(), "Watt", device.getName());
			estimatedEnergy.addStatistic(description);
			componentStatistics.put(device.getACPIComponent(),description);
			System.out.println("Created description for device: " + device.getName());
		}

		writer.initStatisticsTopology(topoNode, estimatedEnergy, new Epoch(0.0));
		
		int value=0;
		for(int time=0; time < 6; ++time) {
				writer.writeStatisticsTimestamp(topoNode, estimatedEnergy, new Epoch(time,0));
				for(ACPIDevice device : node.getNodeDevices()) {	
					System.out.println(time + ": " + device.getName() + " " + value);
					writer.writeStatisticValue(topoNode, componentStatistics.get(device.getACPIComponent()), new Float(value));
					value++;
				}
			
		}
	
		writer.finalizeTrace();
	}
	
	@Test
	public void testSimpleWriterWithMultipleStrategiesAsExternalStatistic() throws IOException {
		
		TraceFormatWriter writer = new TraceFormatWriter(this.outputFolder.getAbsolutePath() + "/simpleWithMultipleStrategiesExternalStatistic", "test descrpition", "test application",  
				new String []{"Host","Energy","Component"});
		
		Node node = NodeFactory.createExtendedNode("node06");
		
		List<String> strategies = new ArrayList<String>();
		strategies.add("SimpleStrategy");
		strategies.add("OptimalStrategy");
		
		TopologyNode topoNode = writer.createInitalizeTopology(new String[]{node.getName()});
		writer.createInitalizeTopology(new String[]{node.getName(), "EstimatedEnergyStates"});
		

		Map<ACPIComponent,StatisticsDescription> componentStatistics = new HashMap<ACPIComponent,StatisticsDescription>();
		Map<ACPIComponent,TopologyNode> componentStates = new HashMap<ACPIComponent,TopologyNode>();
		Map<String,StatisticsGroupDescription> strategyDescription = new HashMap<String,StatisticsGroupDescription>();

		for(ACPIDevice device : node.getNodeDevices()) {
			TopologyNode topologyNode = writer.createInitalizeTopology(new String[]{node.getName(), "EstimatedEnergyStates", device.getName()});
			componentStates.put(device.getACPIComponent(), topologyNode);
		}

		for(ACPIDevice device : node.getNodeDevices())
			writer.writeStateStart(componentStates.get(device.getACPIComponent()), "ACPI0", new Epoch(0.0));

		int state = 0;
		for(int time=1; time < 4; ++time) {
			for(ACPIDevice device : node.getNodeDevices()) {	
				writer.writeStateEnd(componentStates.get(device.getACPIComponent()), "ACPI" + state, new Epoch(time,0));
				writer.writeStateStart(componentStates.get(device.getACPIComponent()), "ACPI" + (state+1), new Epoch(time,0));
			}
			state++;
		}

		for(ACPIDevice device : node.getNodeDevices())
			writer.writeStateEnd(componentStates.get(device.getACPIComponent()), "ACPI" + state, new Epoch(5.0));

		
		// now try to write some statistics:
		int multiplicator = 1;
		for(String strategy : strategies) {
			StatisticsGroupDescription estimatedEnergy = new StatisticsGroupDescription("EstimatedEnergy" + strategy);
			strategyDescription.put(strategy, estimatedEnergy);
		
			for(ACPIDevice device : node.getNodeDevices()) {
				StatisticsDescription description = new StatisticsDescription(estimatedEnergy, device.getName(), StatisticsEntryType.FLOAT, estimatedEnergy.getSize(), "Watt", device.getName());
				estimatedEnergy.addStatistic(description);
				componentStatistics.put(device.getACPIComponent(),description);
				System.out.println("Created description for device: " + device.getName());
			}
			
			writer.initStatisticsTopology(topoNode, estimatedEnergy, new Epoch(0.0));

			int value=0;
			for(int time=0; time < 6; ++time) {
				writer.writeStatisticsTimestamp(topoNode, estimatedEnergy, new Epoch(time,0));
				for(ACPIDevice device : node.getNodeDevices()) {	
					System.out.println(time + ": " + device.getName() + " " + value);
					writer.writeStatisticValue(topoNode, componentStatistics.get(device.getACPIComponent()), new Float(value * multiplicator));
					value++;
				}

			}
			
			multiplicator++;
		}
	
		writer.finalizeTrace();
	}
	
	@Test
	public void testSimpleWriterWithMultipleStrategiesAsTopologyNode() throws IOException {
		
		TraceFormatWriter writer = new TraceFormatWriter(this.outputFolder.getAbsolutePath() + "/simpleWithMultipleStrategiesAsTopologyNode", "test descrpition", "test application",  
				new String []{"Host","Strategy","Energy","Component"});
		
		Node node = NodeFactory.createExtendedNode("node06");
		
		List<String> strategies = new ArrayList<String>();
		strategies.add("SimpleStrategy");
		strategies.add("OptimalStrategy");
		
		Map<ACPIComponent,StatisticsDescription> componentStatistics = new HashMap<ACPIComponent,StatisticsDescription>();
		Map<String,TopologyNode> componentStates = new HashMap<String,TopologyNode>();
		Map<String,TopologyNode> strategyNodes = new HashMap<String,TopologyNode>();
				
		writer.createInitalizeTopology(new String[]{node.getName()});
		
		for(String strategy : strategies) {
			strategyNodes.put(strategy,writer.createInitalizeTopology(new String[]{node.getName(), strategy}));
			writer.createInitalizeTopology(new String[]{node.getName(), strategy,"EstimatedEnergyStates"});

			for(ACPIDevice device : node.getNodeDevices()) {
				TopologyNode topologyNode = writer.createInitalizeTopology(new String[]{node.getName(), strategy, "EstimatedEnergyStates", device.getName()});
				componentStates.put(device.getName() + strategy, topologyNode);
			}

			for(ACPIDevice device : node.getNodeDevices())
				writer.writeStateStart(componentStates.get(device.getName() + strategy), "ACPI0", new Epoch(0.0));

			int state = 0;
			for(int time=1; time < 4; ++time) {
				for(ACPIDevice device : node.getNodeDevices()) {	
					writer.writeStateEnd(componentStates.get(device.getName() + strategy), "ACPI" + state, new Epoch(time,0));
					writer.writeStateStart(componentStates.get(device.getName() + strategy), "ACPI" + (state+1), new Epoch(time,0));
				}
				state++;
			}

			for(ACPIDevice device : node.getNodeDevices())
				writer.writeStateEnd(componentStates.get(device.getName() + strategy), "ACPI" + state, new Epoch(5.0));
		}
		
	
		// now try to write some statistics:
		StatisticsGroupDescription estimatedEnergy = new StatisticsGroupDescription("EstimatedEnergy");
		
		for(ACPIDevice device : node.getNodeDevices()) {
			StatisticsDescription description = new StatisticsDescription(estimatedEnergy, device.getName(), StatisticsEntryType.FLOAT, estimatedEnergy.getSize(), "Watt", device.getName());
			estimatedEnergy.addStatistic(description);
			componentStatistics.put(device.getACPIComponent(),description);
			System.out.println("Created description for device: " + device.getName());
		}

		for(TopologyNode topoNode : strategyNodes.values())
			writer.initStatisticsTopology(topoNode, estimatedEnergy, new Epoch(0.0));
		
		
		int multiplicator = 1;
		for(String strategy : strategies) {
			
			TopologyNode topoNode = strategyNodes.get(strategy);
			
			int value=0;
			for(int time=0; time < 6; ++time) {
				writer.writeStatisticsTimestamp(topoNode, estimatedEnergy, new Epoch(time,0));
				for(ACPIDevice device : node.getNodeDevices()) {	
					System.out.println(time + ": " + device.getName() + " " + value);
					writer.writeStatisticValue(topoNode, componentStatistics.get(device.getACPIComponent()), new Float(value*multiplicator));
					value++;
				}

			}
			
			multiplicator++;
		}
		
		writer.finalizeTrace();
	}
	
	@Test
	public void testWriterWithMultipleNodes() throws IOException {
		TraceFormatWriter writer = new TraceFormatWriter(this.outputFolder.getAbsolutePath() + "/simpleWithMultipleNodes", "test descrpition", "test application",  
				new String []{"Hostname","EstimatedEnergy","Component"});
		
		Node node06 = NodeFactory.createExtendedNode("node06");
		Node node07 = NodeFactory.createExtendedNode("node07");
		Node node08 = NodeFactory.createExtendedNode("node08");
		Node node09 = NodeFactory.createExtendedNode("node09");
		
		List<Node> nodes = new ArrayList<Node>();
		nodes.add(node06);
		nodes.add(node07);
		nodes.add(node08);
		nodes.add(node09);

		Map<String,StatisticsDescription> componentStatistics = new HashMap<String,StatisticsDescription>();
		Map<ACPIComponent,TopologyNode> componentStates = new HashMap<ACPIComponent,TopologyNode>();
		Map<Node,TopologyNode> componentNodes = new HashMap<Node,TopologyNode>();
		
		for(Node node : nodes) {
			componentNodes.put(node,writer.createInitalizeTopology(new String[]{node.getName()}));
			writer.createInitalizeTopology(new String[]{node.getName(), "EstimatedEnergyStates"});
		}
		
		for(Node node : nodes) {
			for(ACPIDevice device : node.getNodeDevices()) {
				TopologyNode topologyNode = writer.createInitalizeTopology(new String[]{node.getName(), "EstimatedEnergyStates", device.getName()});
				componentStates.put(device.getACPIComponent(), topologyNode);
			}
		}

		for(Node node : nodes) {
			for(ACPIDevice device : node.getNodeDevices())
				writer.writeStateStart(componentStates.get(device.getACPIComponent()), "ACPI0", new Epoch(0.0));

			int state = 0;
			for(int time=1; time < 4; ++time) {
				for(ACPIDevice device : node.getNodeDevices()) {	
					writer.writeStateEnd(componentStates.get(device.getACPIComponent()), "ACPI" + state, new Epoch(time,0));
					writer.writeStateStart(componentStates.get(device.getACPIComponent()), "ACPI" + (state+1), new Epoch(time,0));
				}
				state++;
			}

			for(ACPIDevice device : node.getNodeDevices())
				writer.writeStateEnd(componentStates.get(device.getACPIComponent()), "ACPI" + state, new Epoch(5.0));
		}
		
		// now try to write some statistics:
		StatisticsGroupDescription estimatedEnergy = new StatisticsGroupDescription("EstimatedEnergy");
		
		Node tmpnode = nodes.get(0);
		for(ACPIDevice device : tmpnode.getNodeDevices()) {
			StatisticsDescription description = new StatisticsDescription(estimatedEnergy, device.getName(), StatisticsEntryType.FLOAT, estimatedEnergy.getSize(), "Watt", device.getName());
			estimatedEnergy.addStatistic(description);
			componentStatistics.put(device.getName(),description);
			System.out.println("Created description for device: " + device.getName());
		}
		
		for(Node node : nodes)
			writer.initStatisticsTopology(componentNodes.get(node), estimatedEnergy, new Epoch(0.0));
	
		int value=0;
		for(int time=0; time < 6; ++time) {
			for(Node node : nodes) {
				writer.writeStatisticsTimestamp(componentNodes.get(node), estimatedEnergy, new Epoch(time,0));
				for(ACPIDevice device : node.getNodeDevices()) {	
					System.out.println(time + ": " + device.getName() + " " + value);
					writer.writeStatisticValue(componentNodes.get(node), componentStatistics.get(device.getName()), new Float(value));
					value++;
				}
			}
		}
		
		writer.finalizeTrace();
	}

}

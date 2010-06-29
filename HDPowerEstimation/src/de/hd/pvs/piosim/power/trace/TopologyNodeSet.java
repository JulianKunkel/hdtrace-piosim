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
package de.hd.pvs.piosim.power.trace;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.hd.pvs.TraceFormat.TraceFormatWriter;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsEntryType;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.power.acpi.ACPIComponent;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;
import de.hd.pvs.piosim.power.cluster.Node;

public class TopologyNodeSet {
	
	private String[] topologyLevels = {"Hostname","Strategy","EstimatedEnergy","Component"};
	private String nodeTotal = "TOTAL";
	private Map<String, String> grouping = new HashMap<String,String>();
	
	private Map<String, TopologyNode> statisticNodes = new HashMap<String,TopologyNode>();
	private Map<String, TopologyNode> stateNodes = new HashMap<String,TopologyNode>();
	private Map<String, StatisticsDescription> statisticDescriptions = new HashMap<String,StatisticsDescription>();
	private StatisticsGroupDescription statisticsGroupDescription;
	private Logger logger = Logger.getLogger(TopologyNodeSet.class);
	
	public void setGrouping(Map<String,String> grouping) {
		this.grouping = grouping;
	}
	
	public Map<String,String> getGrouping() {
		return grouping;
	}
	
	public TraceFormatWriter buildInitializedWriter(String path, String description, String application, List<Node> nodes, List<String> replayNames) {
		return buildInitializedWriter(path, description, application, nodes, replayNames, new BigDecimal("0"));
	}
	
	public TraceFormatWriter buildInitializedWriter(String path, String description, String application, List<Node> nodes, List<String> replayNames, BigDecimal offset) {
		
		TraceFormatWriter writer = new TraceFormatWriter(path,description,application,topologyLevels);
		
		for(Node node : nodes) {
			writer.createInitalizeTopology(new String[] {node.getName()});
			
			for(String replayName : replayNames) {

				statisticNodes.put(encode(node,replayName),writer.createInitalizeTopology(new String[] {node.getName(), replayName}));
				
				writer.createInitalizeTopology(new String[]{node.getName(), replayName,"EstimatedEnergyStates"});
				
				for(ACPIDevice device : node.getNodeDevices()) {
					
					// create topology node
					TopologyNode topologyNode = writer.createInitalizeTopology(new String[]{node.getName(), replayName, "EstimatedEnergyStates", device.getName()});
					
					logger.debug("Created topology node with topology [" + node.getName() + "," +  replayName + ",EstimatedEnergyStates," + device.getName() + "]");
					
					// remember this topology node as state node (specific for replay and device)
					stateNodes.put(encode(replayName,device.getACPIComponent()), topologyNode);
				}
			}
		}
		
		statisticsGroupDescription = new StatisticsGroupDescription("EstimatedEnergy");
		
		Node firstNode = nodes.get(0);
		
		if(firstNode == null)
			return null;

		for(ACPIDevice device : firstNode.getNodeDevices()) {
			// set grouping for this device
			String deviceGrouping = grouping.get(device.getName());
			if(deviceGrouping == null)
				deviceGrouping = device.getName();
			
			StatisticsDescription statisticsDescription = new StatisticsDescription(statisticsGroupDescription, device.getName(), StatisticsEntryType.FLOAT, statisticsGroupDescription.getSize(), "Watt", deviceGrouping);
			
			logger.debug("Created StatisticsDescription(" + statisticsGroupDescription.getName() + "," + device.getName() + ",FLOAT," + statisticsGroupDescription.getSize() + ",Watt," + deviceGrouping + ")");
			
			statisticsGroupDescription.addStatistic(statisticsDescription);
			statisticDescriptions.put(device.getName(),statisticsDescription);
		}
		
		String totalGrouping = grouping.get(nodeTotal);
		if(totalGrouping == null)
			totalGrouping = nodeTotal;

		StatisticsDescription nodeTotalStatisticsDescription = new StatisticsDescription(statisticsGroupDescription, nodeTotal, StatisticsEntryType.FLOAT, statisticsGroupDescription.getSize(), "Watt",totalGrouping);
		logger.debug("Created StatisticsDescription(" + statisticsGroupDescription.getName() + ",TOTAL,FLOAT," + statisticsGroupDescription.getSize() + ",Watt," + totalGrouping + ")");
		statisticsGroupDescription.addStatistic(nodeTotalStatisticsDescription);
		statisticDescriptions.put(nodeTotal,nodeTotalStatisticsDescription);
		
		for(TopologyNode topoNode : statisticNodes.values())
			writer.initStatisticsTopology(topoNode, statisticsGroupDescription, new Epoch(BaseCalculation.toNs(offset).longValue()));
		
		return writer;
	}
	
	private String encode(Node node, String name) {
		return "" + node.hashCode() + name.hashCode();
	}
	
	private String encode(String replayName, ACPIComponent component) {
		return "" + component.hashCode() + replayName.hashCode();
	}

	public TopologyNode getStatisticNode(ACPIComponent component, String replayName) {
		return getStatisticNode(component.getNode(),replayName);
	}
	
	public TopologyNode getStateNode(ACPIComponent component, String replayName) {
		return stateNodes.get(encode(replayName,component));
	}

	public TopologyNode getStatisticNode(Node node, String replayName) {
		return statisticNodes.get(encode(node,replayName));
	}
	
	public StatisticsDescription getStatisticsDescription(ACPIComponent component) {
		return statisticDescriptions.get(component.getName());
	}
	
	public StatisticsDescription getStatisticsDescription() {
		return statisticDescriptions.get(nodeTotal);
	}

	public StatisticsGroupDescription getStatisticsGroupDescription() {
		return statisticsGroupDescription;
	}

}

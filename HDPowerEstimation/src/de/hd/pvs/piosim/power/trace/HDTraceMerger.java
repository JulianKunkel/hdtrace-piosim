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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.project.ProjectDescriptionXMLReader;
import de.hd.pvs.TraceFormat.project.ProjectDescriptionXMLWriter;
import de.hd.pvs.TraceFormat.statistics.StatisticsReader;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.topology.TopologyTypes;
import de.hd.pvs.TraceFormat.util.Epoch;

public class HDTraceMerger {
	
	private String[] components;
	private Logger logger = Logger.getLogger(HDTraceMerger.class);
	private double minTime;
	
	
	public void merge(String pathToProject, String[] hostnames, String[] components, double minTime) {
		ProjectDescriptionXMLReader reader = new ProjectDescriptionXMLReader();
		
		this.components = components;
		this.minTime = minTime;
		
		ProjectDescription projectDescription = new ProjectDescription();
		
		try {
			reader.readProjectDescription(projectDescription, pathToProject);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TopologyTypes topologyTypes = projectDescription.getTopologyTypes();

		
		List<String> types = topologyTypes.getTypes();
		
		if(types.get(0).equals("Hostname")) {
			types.add(1, "Energy");
			types.add(2, "Component");
		}
		
		String[] topologyLabels = new String[types.size()];
		
		topologyTypes.setTopologyTypes(types.toArray(topologyLabels));
		
		projectDescription.setTopologyTypes(topologyTypes);
		
		projectDescription.addStatisticsGroup("EstimatedEnergyConsumption");
		
		for(String hostname : hostnames)
			addComponentsToHost(hostname, projectDescription.getTopologyRoot());

		ProjectDescriptionXMLWriter writer = new ProjectDescriptionXMLWriter();
		
		try {
			writer.writeXMLToProjectFile(projectDescription,null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// set time adjustment for created nodes
		projectDescription = new ProjectDescription();
		
		try {
			reader.readProjectDescription(projectDescription, pathToProject);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(String hostname : hostnames)
			setTimeAdjustment(hostname, projectDescription.getTopologyRoot());
		
		writer = new ProjectDescriptionXMLWriter();
		
		try {
			writer.writeXMLToProjectFile(projectDescription,null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addComponentsToHost(String hostname, TopologyNode topologyNode) {
		
		if (topologyNode == null)
			return;		
		
		if(topologyNode.getName().equals(hostname) && topologyNode.getType().equals("Hostname")) {	
			
			TopologyNode estimatedEnergyNode = new TopologyNode("Energy",topologyNode,"Energy");
			TopologyNode energyStatesNode = new TopologyNode("EnergyStates",topologyNode,"Energy");
			
			for(String component : components) {
				new TopologyNode(component, estimatedEnergyNode, "Component");
				new TopologyNode(component, energyStatesNode, "Component");
			}
			
			return;
		}
		
		if (!topologyNode.isLeaf()) {

			for (TopologyNode childNode : topologyNode.getChildElements()
					.values()) {
				addComponentsToHost(hostname,childNode);
			}
		}
	}
	
	private void setTimeAdjustment(String hostname, TopologyNode topologyNode) {
		if (topologyNode == null)
			return;		
		
		if(topologyNode.getName().equals(hostname) && topologyNode.getType().equals("Hostname")) {	
			
			StatisticsReader reader = null; 
			
			Map<String,TopologyNode> childs = topologyNode.getChildElements();
			
			for(String name : new String[]{"Energy","EnergyStates"}) {
				TopologyNode node = childs.get(name);
				if(node != null && node.getType().equals("Energy")) {
					reader = (StatisticsReader) node.getStatisticsSource("EstimatedEnergyConsumption");
					reader.getGroup().setTimeAdjustment(new Epoch(minTime));
				} else {
					if(node == null)
						logger.debug("Node: " + name + " not found in node " + hostname);
				}
			}
			
			for(String name : components) {
				TopologyNode node = childs.get(name);
				if(node != null && node.getType().equals("Component")) {
					reader = (StatisticsReader) node.getStatisticsSource("EstimatedEnergyConsumption");
					reader.getGroup().setTimeAdjustment(new Epoch(minTime));
				} else {
					if(node == null)
						logger.debug("Node: " + name + " not found in node " + hostname);
				}
			}

			return;
		}
		
		if (!topologyNode.isLeaf()) {

			for (TopologyNode childNode : topologyNode.getChildElements()
					.values()) {
				setTimeAdjustment(hostname,childNode);
			}
		}
	}

}

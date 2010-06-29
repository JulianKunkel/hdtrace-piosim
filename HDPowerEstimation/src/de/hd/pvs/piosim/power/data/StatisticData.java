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
package de.hd.pvs.piosim.power.data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.hd.pvs.piosim.power.acpi.ACPIComponent;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.calculation.ACPICalculation;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;
import de.hd.pvs.piosim.power.cluster.Node;

public class StatisticData {

	private static StatisticData instance = null;

	private int currentStep;
	private Map<Node, StatisticNodeData> nodeDataset;
	private Map<ACPIComponent, StatisticComponentData> componentDataset;
	private int countValues;
	private Logger logger = Logger.getLogger(StatisticData.class);

	private Map<Node, BigDecimal[]> nodeConsumption;
	private Map<Node, BigDecimal> totalNodeConsumption;
	private Set<Node> nodes;

	private StatisticData() {
		nodeDataset = new HashMap<Node, StatisticNodeData>();
		componentDataset = new HashMap<ACPIComponent, StatisticComponentData>();
		nodeConsumption = new HashMap<Node, BigDecimal[]>();
		totalNodeConsumption = new HashMap<Node, BigDecimal>();
		nodes = new HashSet<Node>();
		reset();
	}

	public static StatisticData getInstance() {
		if (instance == null)
			instance = new StatisticData();

		return instance;
	}

	public void setCountValues(int cValues) {
		countValues = cValues;
	}

	public void addValues(ACPIDevice device, int step,
			BigDecimal stepUtilization, BigDecimal stepPowerConsumption) {

		StatisticNodeData nodeData = nodeDataset.get(device.getNode());

		if (nodeData == null) {
			nodeData = new StatisticNodeData(device.getNode().getNodeDevices()
					.size(), countValues);
			nodeDataset.put(device.getNode(), nodeData);
		}

		if (step > currentStep) {

			for (StatisticNodeData statisticNodeData : nodeDataset.values())
				statisticNodeData.finishStep();

			currentStep++;
			nodes.clear();
		}

		nodeData.addValues(stepUtilization, stepPowerConsumption);
		
		if(device.getNode() != null && !nodes.contains(device.getNode()))
			setNodeConsumptionForStep(device.getNode(),step);
	}

	public BigDecimal[] getMeanUtilizationForEachStep(Node node) {
		return nodeDataset.get(node).getMeanUtilization();
	}

	public BigDecimal[] getSumPowerConsumptionForEachStep(Node node) {
		return nodeConsumption.get(node);
	}

	public void reset() {
		nodeDataset.clear();
		componentDataset.clear();
		nodeConsumption.clear();
		totalNodeConsumption.clear();
		nodes.clear();
		currentStep = 0;
	}

	public int getCountValues() {
		return this.countValues;
	}

	public void startState(BigDecimal time, int state, ACPIComponent component) {

		StatisticComponentData componentData = componentDataset.get(component);

		if (componentData == null)
			componentData = new StatisticComponentData(component);

		componentData.startState(time, state);

		componentDataset.put(component, componentData);
	}

	public void endState(BigDecimal time, ACPIComponent component) {

		StatisticComponentData componentData = componentDataset.get(component);

		if (componentData == null)
			componentData = new StatisticComponentData(component);

		componentData.endState(time);

		componentDataset.put(component, componentData);
	}

	public void printStatisticComponentData() {
		for (ACPIComponent component : componentDataset.keySet()) {
			System.out.print(component.getName() + ": ");
			componentDataset.get(component).print();
			System.out.println(component.getPowerConsumption() + " Watt-h");
		}
	}
	
	private void setNodeConsumptionForStep(Node node, int step) {
		
		BigDecimal[] powerConsumption = nodeConsumption.get(node);

		if (powerConsumption == null) {
			powerConsumption = new BigDecimal[countValues];
			nodeConsumption.put(node, powerConsumption);
		}

		BigDecimal totalConsumption = totalNodeConsumption.get(node);

		if (totalConsumption == null)
			totalConsumption = new BigDecimal("0");
		
		if(step == 0) {
			logger.debug(totalConsumption);
		}

		powerConsumption[step] = ACPICalculation.calculateInWatt(BaseCalculation.substract(node
				.getPowerConsumption(), totalConsumption),new BigDecimal("1000"));

		totalNodeConsumption.put(node, node.getPowerConsumption());
	}

	public void step(Set<Node> nodes, int step) {
		for (Node node : nodes) {
			setNodeConsumptionForStep(node, step);
		}
	}
}

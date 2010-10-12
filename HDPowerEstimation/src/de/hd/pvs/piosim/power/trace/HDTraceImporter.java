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
import java.util.Set;

import org.apache.log4j.Logger;

import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.ConvertingException;
import de.hd.pvs.piosim.power.calculation.ACPICalculation;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;
import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.data.DeviceData;

public class HDTraceImporter {

	private ExternalStatisticReader reader = new ExternalStatisticReader();
	private Map<ACPIDevice, DeviceData> deviceData = new HashMap<ACPIDevice, DeviceData>();
	private Map<String, Node> nodes = new HashMap<String, Node>();
	private Map<Node,BigDecimal> nodePowerConsumption;
	private double min = Double.MAX_VALUE;
	private double max = Double.MIN_VALUE;
	private double minStepsize = Double.MAX_VALUE;
	private double maxStepsize = Double.MIN_VALUE;
	private Logger logger = Logger.getLogger(HDTraceImporter.class);

	/* set this variables */
	private String filename;

	public Map<String, Node> getNodes() {
		return nodes;
	}

	public void addNode(Node node) {
		nodes.put(node.getName(), node);
	}

	public Map<ACPIDevice, DeviceData> getDeviceData() {
		return deviceData;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		reader.setFilename(filename);
	}
	
	/**
	 * 
	 * @return Hostnames read from ExternalStatisticsReader or null in case of HDTraceImporterException
	 */
	public List<String> getHostnames() {
		try {
			return reader.getHostnames();
		} catch (HDTraceImporterException e) {
			return null;
		}
	}
	
	public void setUtilization(String[] componentNames) throws HDTraceImporterException {
		setExternalStatistic(componentNames, "Utilization");
	}
	
	public Map<Node,BigDecimal> getOriginalPowerConsumption() throws HDTraceImporterException {
		
		nodePowerConsumption = new HashMap<Node,BigDecimal>();
		
		setExternalStatistic(new String[]{"P"}, "Energy",nodes.keySet());
		
		return nodePowerConsumption;

	}
	
	private void setExternalStatistic(String[] componentNames, String statisticName, Set<String> hostnames) throws HDTraceImporterException {
		for(String hostname : hostnames) {

			reader.readStatistic(hostname, statisticName);	
			
			int[] indexPermutation = new int[componentNames.length];
			String[] unit = new String[componentNames.length];
			
			List<StatisticsDescription> statistics = reader.getStatisticsDescriptions();
			
			if(statistics == null)
				throw new HDTraceImporterException("Unknown statisticName " + statisticName + " or unknown host " + hostname);

			for (StatisticsDescription description : statistics) {
				for (int i = 0; i < componentNames.length; ++i) {
					if (description.getName().equals(componentNames[i])) {
						indexPermutation[i] = description
								.getNumberInGroup();
						unit[i] = description.getUnit();
						logger.debug("Read unit " + unit[i] + " for component " + componentNames[i]);
					}
				}
			}
			
			Node node = nodes.get(hostname);
			
			for(StatisticsGroupEntry inputEntry : reader.getStatisticGroupEntries()) {
				
				for (int i = 0; i < componentNames.length; ++i) {
					try {
					if(statisticName.equals("Utilization")) {
						if(inputEntry.getEarliestTime().getDouble() < min)
							min = inputEntry.getEarliestTime().getDouble();
						
						if(inputEntry.getLatestTime().getDouble() > max)
							max = inputEntry.getLatestTime().getDouble();
						
						if(inputEntry.getDurationTime().getDouble() > maxStepsize)
							maxStepsize = inputEntry.getDurationTime().getDouble();
						
						if(inputEntry.getDurationTime().getDouble() < minStepsize)
							minStepsize = inputEntry.getDurationTime().getDouble();
							
						setUtilization(node, componentNames[i], inputEntry.getNumeric(indexPermutation[i]), unit[i]);
					}
					else if(statisticName.equals("Energy"))
						setPowerConsumption(node,inputEntry.getNumeric(indexPermutation[i]),inputEntry.getDurationTime().getBigDecimal());
					else
						throw new HDTraceImporterException("Unknown statisticName: " + statisticName);
					} catch (ConvertingException ex) {
						throw new HDTraceImporterException("Error while converting value for statistic: " + statisticName, ex);
					}
				}
			}
		}
	}
	
	private void setExternalStatistic(String[] componentNames, String statisticName) throws HDTraceImporterException {	
		setExternalStatistic(componentNames, statisticName, nodes.keySet());	
	}

	private void setPowerConsumption(Node node, double value, BigDecimal stepsizeInSec) {
		BigDecimal powerConsumption = nodePowerConsumption.get(node);
		
		if(powerConsumption == null)
			powerConsumption = new BigDecimal("0");
		
		powerConsumption = BaseCalculation.sum(powerConsumption,ACPICalculation.calculateInWattH(new BigDecimal(value), BaseCalculation.toMs(stepsizeInSec)));
		nodePowerConsumption.put(node, powerConsumption);
	}

	private void setUtilization(Node node, String componentName, double value,
			String unit) throws ConvertingException {

		ACPIDevice device = node.getDevice(componentName);

		BigDecimal utilization = device.convertToPercentualUtilization(value,
				unit);

		DeviceData data = deviceData.get(device);

		if (data == null) {
			data = new DeviceData();
			deviceData.put(device, data);
		}

		data.addUtiliziation(utilization);

	}

	public double getMinValue() {
		return min + 1;
	}

	public double getMinStepsize() {
		return minStepsize;
	}

	public double getMaxStepsize() {
		return maxStepsize;
	}

}

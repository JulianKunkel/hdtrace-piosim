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
package de.hd.pvs.piosim.power.data.visualizer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import de.hd.pvs.piosim.power.calculation.BaseCalculation;
import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.replay.ReplayItem;

public class VisualizationData {
	
	private DefaultCategoryDataset utilizationDataset;
	private DefaultCategoryDataset powerConsumptionDataset;
	private List<String> summary;
	private Map<String, String> details;
	private XYSeriesCollection utilizationSeries = new XYSeriesCollection();
	private XYSeriesCollection powerConsumptionSeries = new XYSeriesCollection();
	private double utilizationScaling = -1;
	private double maxPowerConsumption = Double.MIN_VALUE;
	
	private static Logger logger = Logger.getLogger(VisualizationData.class);
	
	public VisualizationData() {	
		
		utilizationDataset = new DefaultCategoryDataset();
		powerConsumptionDataset = new DefaultCategoryDataset();
		
		summary = new ArrayList<String>();
		details = new HashMap<String,String>();
		details.put("PowerConsumption","Watt-h");
//		details.put("ACPIStateChangesTime","ms");
//		details.put("ACPIStateChangesTimeOverhead","ms");
	}

	public void addReplayItems(List<ReplayItem> replayItems, String title) {
		
		logger.debug("adding " + replayItems.size() + " replayItems with title " + title);

		for (ReplayItem item : replayItems) {

			BigDecimal[] utilizationArray = item.getReplayDevice()
					.getUtilization();
			BigDecimal[] powerConsumptionArray = item.getReplayDevice()
					.getPowerConsumption();
			
			String deviceName = item.getReplayDevice().getACPIDevice().getName();

			if (title != null)
				deviceName += " (" + title + ")";
			
			XYSeries utilizationSerie = new XYSeries(deviceName);		
			XYSeries powerConsumptionSerie = new XYSeries(deviceName);

			
			if(utilizationScaling == -1) {
	
				int countValues = utilizationArray.length;
				
				logger.debug("countValues: " + countValues);
				
				int digits = (new Integer(utilizationArray.length)).toString().length();
				
				String utilizationScalingString = "1";
				for(int i = 1; i<digits; ++i)
					utilizationScalingString += "0";
				
				utilizationScaling = Integer.parseInt(utilizationScalingString);
				
			}
			
			

			for (int i = 0; i < utilizationArray.length; ++i) {		
				
				utilizationDataset.addValue(utilizationArray[i].doubleValue(),
						deviceName, Integer.toString(i));
				powerConsumptionDataset.addValue(powerConsumptionArray[i]
						.doubleValue(), deviceName, Integer.toString(i));
				
				utilizationSerie.add(i,utilizationArray[i].doubleValue());
				
				powerConsumptionSerie.add(i,powerConsumptionArray[i].doubleValue());
				
				if(powerConsumptionArray[i].doubleValue() > maxPowerConsumption)
					maxPowerConsumption = powerConsumptionArray[i].doubleValue();

			}
			
			utilizationSeries.addSeries(utilizationSerie);
			powerConsumptionSeries.addSeries(powerConsumptionSerie);

			summary.add(item.getReplayDevice().getACPIDevice().getName());
			summary.add("");

			for (String detail : details.keySet()) {
				summary.add(detail + " (" + details.get(detail) + ")");
				summary.add(getAttribut(item.getReplayDevice()
						.getACPIDevice(), detail));
			}
		}

		Node[] nodes = getNodes(replayItems);
		
		if (nodes != null) {
			BigDecimal sum = new BigDecimal("0");
			
			for (Node node : nodes) {
				summary.add(node.getName());
				summary.add("");

				for (String detail : details.keySet()) {
					summary.add(detail + " (" + details.get(detail) + ")");
					summary.add(getAttribut(node, detail));
					sum = BaseCalculation.sum(sum, node.getPowerConsumption());
				}
			}
			
			summary.add("Sum of nodes consumption (Watt-h): ");
			summary.add(sum.toString());
		}

	}

	private Node[] getNodes(List<ReplayItem> replayItems) {

		Set<Node> nodes = new LinkedHashSet<Node>();

		for (ReplayItem item : replayItems) {
			if (item.getReplayDevice().getACPIDevice().getNode() != null)
				nodes.add(item.getReplayDevice().getACPIDevice().getNode());
		}

		if (nodes.size() == 0)
			return null;

		Node[] nodesArray = new Node[nodes.size()];
		return nodes.toArray(nodesArray);
	}

	private String getAttribut(Object object, String attributName) {

		Method method = null;
		try {
			method = object.getClass().getMethod("get" + attributName);
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		} catch (NoSuchMethodException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		} 

		try {
			return method.invoke(object, (Object[]) null).toString();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setDetails(Map<String, String> details) {
		this.details = details;		
	}

	public Map<String, String> getDetails() {
		return details;
	}

	public int getCountRows() {
		return summary.size() / 2;
	}

	public List<String> getSummary() {
		return summary;
	}

	public DefaultCategoryDataset getUtilizationDataset() {
		return utilizationDataset;
	}

	public DefaultCategoryDataset getPowerConsumptionDataset() {
		return powerConsumptionDataset;
	}

	public XYSeriesCollection getUtilizationAsXYSeriesCollection() {
	
		return utilizationSeries;
	}

	public XYSeriesCollection getPowerConsumptionAsXYSeriesCollection() {
		
		return powerConsumptionSeries;
	}

	public double getUtilizationScaling() {
		return utilizationScaling;
	}
	
	public double getPowerConsumptionScaling() {
		int tenth = (int) maxPowerConsumption / 10;
		int ones = (int) maxPowerConsumption % 10;
		
		if(ones >= 5) {
			return tenth + 1;
		} 
		
		return tenth;
	}
	
	
}

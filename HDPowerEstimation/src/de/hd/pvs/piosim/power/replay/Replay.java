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
package de.hd.pvs.piosim.power.replay;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hd.pvs.piosim.power.Time;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.history.ACPIStateChangesHistory;
import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.data.StatisticData;
import de.hd.pvs.piosim.power.data.visualizer.Visualizer;
import de.hd.pvs.piosim.power.data.visualizer.VisualizerException;
import de.hd.pvs.piosim.power.replay.strategy.PlayStrategy;

public class Replay {

	private int stepsize;
	private int countSteps;
	private List<ReplayItem> items;
//	private Logger logger = Logger.getLogger(Replay.class);
	private Set<Node> nodes = null;
	
	public void setStepsize(int stepsize) {
		this.stepsize = stepsize;
	}

	public int getStepsize() {
		return stepsize;
	}

	public void setCountSteps(int countSteps) {
		this.countSteps = countSteps;
	}

	public int getCountSteps() {
		return countSteps;
	}

	public void setPlayStrategy(PlayStrategy playStrategy) {
		for (ReplayItem item : items) {
			item.setPlayStrategy(playStrategy);
		}
	}

	public void play() throws ReplayException {
		
		reset();
		
		StatisticData.getInstance().setCountValues(countSteps);
		
		for (int i = 0; i < countSteps; ++i) {
			for (ReplayItem item : items) {
				item.step(countSteps, stepsize);
			}

			if(i > 0)
				StatisticData.getInstance().step(getNodes(),i-1);
			Time.getInstance().timePassed(stepsize);
		}

		// because powerConsumption can only read after utilization time period,
		// so the last value is missing otherwise
		for (ReplayItem item : items) {
			item.getReplayDevice().setPowerConsumptionForLastStep();
		}
		
		StatisticData.getInstance().step(getNodes(),countSteps-1);
		
		finalizeReplay();
	}
	
	public Set<Node> getNodes() {
		
		if(nodes == null) {
			nodes = new HashSet<Node>();
			
			for (ReplayItem item : items) {
				Node node = item.getReplayDevice().getACPIDevice().getNode();
				
				// devices without a node can exists
				if(node != null)
					nodes.add(node);
			}
		}
		
		return nodes;
	}

	public void finalizeReplay() {
		for (ReplayItem item : items) {
			item.getReplayDevice().getACPIDevice().finalizeComponent();
		}
	}

	public void reset() {

		Time.getInstance().reset();
		StatisticData.getInstance().reset();
		ACPIStateChangesHistory.getInstance().reset();
		
		for (ReplayItem item : items) {
			item.reset();
		}
		
		for(ACPIDevice device : ACPIDevice.getDevices()) {
			if(device.getNode() != null)
				device.getNode().reset();
		}
		
		nodes = null;
	}

	public List<ReplayItem> getReplayItems() {
		return items;
	}

	public void setReplayItems(List<ReplayItem> items) {
		this.items = items;
	}
	
	public void addReplayItem(ReplayItem item) {
		this.items.add(item);
	}

	public void visualize(Visualizer visualizer) throws VisualizerException {

		
		String xAxisTitle = "Time in " + stepsize + " ms";
		
		if(stepsize >= 1000) {
			if(stepsize % 1000 == 0)
				xAxisTitle = "Time in " + (stepsize/1000) + " sec";
		}
		
		visualizer.setXAxisTitle(xAxisTitle);
		
		visualizer.setUtilizationYAxisTitle("Utilization in percent");
		visualizer.setPowerConsumptionYAxisTitle("Power consumption in watt");
		
		visualizer.copyReplayItems(items);

		visualizer.visualize();
		
		visualizer.reset();
	}

}

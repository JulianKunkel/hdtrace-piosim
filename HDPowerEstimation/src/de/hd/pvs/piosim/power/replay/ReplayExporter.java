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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hd.pvs.piosim.power.acpi.ACPIComponent;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.history.ACPIStateChangesHistory;
import de.hd.pvs.piosim.power.acpi.history.TraceHistoryExporter;
import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.data.StatisticData;
import de.hd.pvs.piosim.power.data.visualizer.Visualizer;
import de.hd.pvs.piosim.power.data.visualizer.VisualizerException;
import de.hd.pvs.piosim.power.replay.strategy.PlayStrategy;
import de.hd.pvs.piosim.power.trace.HDTraceExporter;
import de.hd.pvs.piosim.power.trace.HDTraceExporterException;

public class ReplayExporter {
	
	private ComponentConsumption componentConsumption = new ComponentConsumption();
	private Set<Node> nodes = null;
	private List<ACPIComponent> componentList = new ArrayList<ACPIComponent>();
	private int stepsize = 0;
	private int countSteps = 0;
	private ACPIStateChangesHistory history = null;
	private List<ACPIStateChangesHistory> histories = new ArrayList<ACPIStateChangesHistory>();
	private List<String> replayNames = new ArrayList<String>();
	
	public void setACPIStateChangesHistory(ACPIStateChangesHistory history) {
		this.history = history;
	}
	
	public void addACPIStateChangesHistory(ACPIStateChangesHistory history) {
		history.setName(replayNames.get(replayNames.size()-1));
		this.histories.add(history);
	}
	
	public void add(Replay replay, String name) throws ReplayExporterException {
		if(componentConsumption.getSize() == 0) {
			stepsize = replay.getStepsize();
			countSteps = replay.getCountSteps();
			nodes = replay.getNodes();
		} else {
			if(stepsize != replay.getStepsize())
				throw new ReplayExporterException("Stepsize must be equal for all replays");
			
			if(countSteps != replay.getCountSteps())
				throw new ReplayExporterException("CountSteps must be equal for all replays");
		}
		
		replayNames.add(name);
		
		for(ReplayItem item : replay.getReplayItems()) {
			
			BigDecimal[] powerConsumption = new BigDecimal[item.getReplayDevice().getPowerConsumption().length];
			System.arraycopy(item.getReplayDevice().getPowerConsumption(), 0, powerConsumption, 0, powerConsumption.length);
			componentConsumption.add(name, item.getReplayDevice().getACPIDevice().getACPIComponent(), powerConsumption);
			
			if(componentConsumption.getSize() == 0) {
				componentList.add(item.getReplayDevice().getACPIDevice().getACPIComponent());
			}
		}
		
		// add node consumption for all nodes
		
		for(Node node : nodes) {
			componentConsumption.add(name, node, StatisticData.getInstance().getSumPowerConsumptionForEachStep(node));
		}

	}
	
	public void export(HDTraceExporter exporter) throws HDTraceExporterException {
				
		if(history == null && histories.size() == 0) {
			// start and end states with acpi 0 for the whole timeline for each component
			for(int k=0; k<componentList.size(); ++k) {
				exporter.startState(componentList.get(k), "","ACPI0", new BigDecimal("0")); 
				exporter.endState(componentList.get(k), "","ACPI0", new BigDecimal(countSteps*stepsize)); 
			}
		} else {
			// use the history to start and end states
			TraceHistoryExporter historyExporter = new TraceHistoryExporter();
			
			if(history != null)
				historyExporter.exportStates(exporter, history);
			else {
				historyExporter.export(exporter, histories);
			}
		}
		
		for(int i=0; i<countSteps; ++i) {
			BigDecimal time = new BigDecimal((i+1)*stepsize);

			for(Node node : nodes) {

				for(String replayName : replayNames) {
					exporter.writeTimestamp(node, replayName, time);

					for(ACPIDevice device : node.getNodeDevices()) {
						exporter.writeStatistics(device.getACPIComponent(), replayName, componentConsumption.get(replayName,device.getACPIComponent(),i));
					}
					
					exporter.writeStatistics(node, replayName, componentConsumption.get(replayName,node,i));
				}
			}

		}
	}

	/**
	 * resets the replay, plays all strategies and add these to exporter. After method replay is reseted
	 * 
	 * @param replay
	 * @param playStrategies
	 * @param visualizer
	 * @throws ReplayException
	 */
	public void play(Replay replay, Map<String, PlayStrategy> playStrategies, Visualizer visualizer) throws ReplayException {
		
		replay.reset();
		
		for(String strategyName : playStrategies.keySet()) {
			replay.setPlayStrategy(playStrategies.get(strategyName));
			replay.play();
			
			if(visualizer != null)
				try {
					visualizer.setChartTitle(strategyName);
					replay.visualize(visualizer);
				} catch (VisualizerException e) {
					throw new ReplayException(e.getMessage(),e.getStackTrace());
				}

			add(replay, strategyName);
			addACPIStateChangesHistory(ACPIStateChangesHistory.getCopy());

			replay.reset();
		}
	}
	
	/**
	 * resets the replay, plays all strategies and add these to exporter. After method replay is reseted
	 * 
	 * @param replay
	 * @param playStrategies
	 * @throws ReplayException
	 */
	public void play(Replay replay, Map<String, PlayStrategy> playStrategies) throws ReplayException {
		
		play(replay,playStrategies,null);
	}

}

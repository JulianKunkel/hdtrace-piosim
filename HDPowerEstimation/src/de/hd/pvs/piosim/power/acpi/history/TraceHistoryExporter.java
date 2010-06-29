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
package de.hd.pvs.piosim.power.acpi.history;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.hd.pvs.TraceFormat.TraceFormatWriter;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.power.acpi.ACPIComponent;
import de.hd.pvs.piosim.power.calculation.ACPICalculation;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;
import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.trace.HDTraceExporter;
import de.hd.pvs.piosim.power.trace.HDTraceExporterException;
import de.hd.pvs.piosim.power.trace.TopologyNodeSet;

public class TraceHistoryExporter {

	private Logger logger = Logger.getLogger(TraceHistoryExporter.class);
	private Map<String, ACPIStateChange> startedStates;
	private Map<ACPIComponent, BigDecimal> componentTotalPowerConsumption = new HashMap<ACPIComponent, BigDecimal>();
	private Map<ACPIComponent, BigDecimal> componentLastChangeTime = new HashMap<ACPIComponent, BigDecimal>();
	private TopologyNodeSet set = new TopologyNodeSet();
	private TraceFormatWriter writer;
	private BigDecimal offset = new BigDecimal("0");

	public void exportStates(HDTraceExporter exporter,
			ACPIStateChangesHistory history) throws HDTraceExporterException {
		startedStates = new HashMap<String, ACPIStateChange>();

		logger.info("Exporting " + history.getSize()
				+ " ACPI changes (only states)");

		for (int i = 0; i < history.getSize(); ++i) {
			export(exporter, history.getName(), history.get(i));
		}
	}

	public void export(String path, String description, String application,
			List<ACPIStateChangesHistory> histories, List<Node> nodes) {
		startedStates = new HashMap<String, ACPIStateChange>();

		List<String> replayNames = new ArrayList<String>();

		for (ACPIStateChangesHistory history : histories)
			replayNames.add(history.getName());

		writer = set.buildInitializedWriter(path, description, application,
				nodes, replayNames, offset);

		ACPIStateChangesHistoryIterator iterator = new ACPIStateChangesHistoryIterator(
				histories);

		while (iterator.hasEntries()) {
			String replayName = histories.get(iterator.getNextHistoryIndex())
					.getName();
			export(iterator.getNextEntry(), replayName);
		}
	}

	public void export(String path, String description, String application,
			List<ACPIStateChangesHistory> histories, List<Node> nodes,
			BigDecimal offset) {

		this.offset = offset;

		export(path, description, application, histories, nodes);
	}

	public void setOffset(BigDecimal offset) {
		this.offset = offset;
	}

	private void export(ACPIStateChange entry, String replayName) {

		try {

			String stateName = entry.getACPIComponent().getStateName(
					entry.getState());
			TopologyNode stateNode = set.getStateNode(entry.getACPIComponent(),
					replayName);
			if (entry.getState() == ACPIStateChangesHistory.STATE_END) {
				writer.writeStateEnd(stateNode, stateName, buildEpoch(entry
						.getTime()));
			} else {
				writer.writeStateStart(stateNode, stateName, buildEpoch(entry
						.getTime()));
			}

			TopologyNode statisticNode = set.getStatisticNode(entry
					.getACPIComponent(), replayName);

			writer.writeStatisticsTimestamp(statisticNode, set
					.getStatisticsGroupDescription(), buildEpoch(entry
					.getTime()));

			BigDecimal totalPowerConsumption = componentTotalPowerConsumption
					.get(entry.getACPIComponent());
			BigDecimal lastChangeTime = componentLastChangeTime.get(entry
					.getACPIComponent());

			if (totalPowerConsumption == null)
				totalPowerConsumption = new BigDecimal("0");

			if (lastChangeTime == null)
				lastChangeTime = new BigDecimal("0");

			BigDecimal powerConsumptionInWattH = BaseCalculation.substract(
					entry.getPowerConsumption(), totalPowerConsumption);
			BigDecimal durationInMs = BaseCalculation.substract(
					entry.getTime(), lastChangeTime);

			BigDecimal stepPowerConsumption = new BigDecimal("0");

			if (durationInMs.compareTo(BigDecimal.ZERO) > 0)
				ACPICalculation.calculateInWatt(powerConsumptionInWattH,
						durationInMs);

			writer.writeStatisticValue(statisticNode, set
					.getStatisticsDescription(entry.getACPIComponent()),
					stepPowerConsumption.floatValue());

			totalPowerConsumption = entry.getPowerConsumption();
			lastChangeTime = entry.getTime();

			componentTotalPowerConsumption.put(entry.getACPIComponent(),
					totalPowerConsumption);
			componentLastChangeTime.put(entry.getACPIComponent(),
					lastChangeTime);

		} catch (IOException ex) {

		}
	}

	private Epoch buildEpoch(BigDecimal time) {
		return new Epoch(BaseCalculation
				.toNs(BaseCalculation.sum(time, offset)).longValue());
	}

	private void export(HDTraceExporter exporter, String replayName,
			ACPIStateChange entry) throws HDTraceExporterException {

		if (entry.getState() == ACPIStateChangesHistory.STATE_END) {

			// check if valid state end
			ACPIStateChange startedEntry = startedStates.get(encode(entry,
					replayName));
			if (startedEntry == null)
				logger
						.error("No start state for corresponding end state for component "
								+ entry.getACPIComponent().getName());
			else {
				String state = entry.getACPIComponent().getStateName(
						startedEntry.getState());
				logger.debug("(" + entry.getTime() + ") "
						+ entry.getACPIComponent().getName() + ": end state: "
						+ state);
				exporter.endState(entry.getACPIComponent(), replayName, state,
						entry.getTime());
			}

		} else {
			String state = entry.getACPIComponent().getStateName(
					entry.getState());
			logger.debug("(" + entry.getTime() + ") "
					+ entry.getACPIComponent().getName() + ": start state: "
					+ state);
			exporter.startState(entry.getACPIComponent(), replayName, state,
					entry.getTime());
			startedStates.put(encode(entry, replayName), entry);
		}
	}

	private String encode(ACPIStateChange entry, String replayName) {
		return entry.getACPIComponent().getNode().getName()
				+ entry.getACPIComponent().getName() + replayName;
	}

	public void export(HDTraceExporter exporter,
			List<ACPIStateChangesHistory> histories)
			throws HDTraceExporterException {

		ACPIStateChangesHistoryIterator iterator = new ACPIStateChangesHistoryIterator(
				histories);
		startedStates = new HashMap<String, ACPIStateChange>();

		while (iterator.hasEntries()) {

			// for current implementation only this way
			String replayName = histories.get(iterator.getNextHistoryIndex())
					.getName();
			ACPIStateChange entry = iterator.getNextEntry();

			export(exporter, replayName, entry);
		}
	}

}

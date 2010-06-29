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
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.hd.pvs.TraceFormat.TraceFormatWriter;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.power.acpi.ACPIComponent;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;
import de.hd.pvs.piosim.power.cluster.Node;

public class HDTraceExporter {
	private TraceFormatWriter writer;
	private StatisticsGroupDescription groupDescription;
	private Logger logger = Logger.getLogger(HDTraceExporter.class);
	private BigDecimal offset = new BigDecimal("0");
	private TopologyNodeSet set = new TopologyNodeSet();
	
	public Map<String,String> getComponentGrouping() {
		return set.getGrouping();
	}
	
	public HDTraceExporter(String projectPath, String traceDescription, String applicationName, List<Node> nodes, List<String> replayNames) {
	
		writer = set.buildInitializedWriter(projectPath, traceDescription, applicationName, nodes, replayNames, offset);
		
		groupDescription = set.getStatisticsGroupDescription();
	}

	public HDTraceExporter(String projectPath, String traceDescription, String applicationName, List<Node> nodes, List<String> replayNames, Map<String,String> grouping, BigDecimal offset) {
	
		this.offset = offset;
		
		set.setGrouping(grouping);
		
		writer = set.buildInitializedWriter(projectPath, traceDescription, applicationName, nodes, replayNames, offset);
		
		groupDescription = set.getStatisticsGroupDescription();
	}	
	
	public void startState(ACPIComponent component, String replayName,String state, BigDecimal time) throws HDTraceExporterException {
		try {
			writer.writeStateStart(set.getStateNode(component, replayName), state, buildEpoch(time));
		} catch (IOException e) {
			throw new HDTraceExporterException("Unable to start state: " + e.getMessage(),e.getStackTrace());
		}
	}

	public void endState(ACPIComponent component, String replayName, String state, BigDecimal time) throws HDTraceExporterException {
		try {
			writer.writeStateEnd(set.getStateNode(component, replayName), state,buildEpoch(time));
		} catch (IOException e) {
			throw new HDTraceExporterException("Unable to end state: " + e.getMessage(),e.getStackTrace());
		}
	}

	public void writeTimestamp(ACPIComponent component, String replayName, BigDecimal time) throws HDTraceExporterException {
		try {
			logger.debug("Writing timestamp for component " + component.getName() + "(" + replayName + "): " + time);
			writer.writeStatisticsTimestamp(set.getStatisticNode(component, replayName), groupDescription, buildEpoch(time));
		} catch (Exception e) {
			throw new HDTraceExporterException("Unable to write Statistics Timestamp: " + e.getMessage(),e.getStackTrace());
		}
	}
	
	public void writeTimestamp(Node node, String replayName, BigDecimal time) throws HDTraceExporterException {
		try {
			logger.debug("Writing timestamp for node " + node.getName() + "(" + replayName + "): " + time);
			writer.writeStatisticsTimestamp(set.getStatisticNode(node, replayName), groupDescription, buildEpoch(time));
		} catch (Exception e) {
			throw new HDTraceExporterException("Unable to write Statistics Timestamp: " + e.getMessage(),e.getStackTrace());
		}
	}
	
	public void writeStatistics(ACPIComponent component, String replayName, BigDecimal value) throws HDTraceExporterException {
		try {
			logger.debug("Writing statistics for component " + component.getName() + "(" + replayName + ")");
			writer.writeStatisticValue(set.getStatisticNode(component, replayName), set.getStatisticsDescription(component), value.floatValue());
		} catch (Exception e) {
			throw new HDTraceExporterException("Unable to write Statistics : " + e.getMessage(),e.getStackTrace());
		}
	}
	
	public void writeStatistics(Node node, String replayName, BigDecimal value) throws HDTraceExporterException {
		try {
			logger.debug("Writing statistics for node " + node.getName() + "(" + replayName + ")");
			writer.writeStatisticValue(set.getStatisticNode(node, replayName), set.getStatisticsDescription(node), value.floatValue());
		} catch (Exception e) {
			throw new HDTraceExporterException("Unable to write Statistics : " + e.getMessage(),e.getStackTrace());
		}
	}
	
	public void finalize() throws HDTraceExporterException {
		try {
			writer.finalizeTrace();
		} catch (IOException e) {
			throw new HDTraceExporterException("Unable to finalize exporter: " + e.getMessage(),e.getStackTrace());
		}
	}

	private Epoch buildEpoch(BigDecimal time) {
		return new Epoch(BaseCalculation.toNs(BaseCalculation.sum(time,offset)).longValue());
	}
}

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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsReader;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.StAXTraceFileReader;

public class ExternalStatisticReader {
	
	private Logger logger = Logger.getLogger(ExternalStatisticReader.class);
	
	private String statisticName;
	private String hostname;
	private String filename;
	private List<StatisticsGroupEntry> statisticGroupEntries;
	private List<StatisticsDescription> statisticsDescriptions;
	private List<String> hostnames = null;
	
	public List<StatisticsGroupEntry> getStatisticGroupEntries() {
		return statisticGroupEntries;
	}

	public List<StatisticsDescription> getStatisticsDescriptions() {
		return statisticsDescriptions;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void readStatistic(String hostname, String statisticName) throws HDTraceImporterException {
		statisticGroupEntries = new ArrayList<StatisticsGroupEntry>();
		statisticsDescriptions = null;
		this.statisticName = statisticName;
		this.hostname = hostname;
		setStatistics();		
	}
	
	public List<String> getHostnames() throws HDTraceImporterException {
		try {
			
			hostnames = new LinkedList<String>();

			logger.info("Opening File: " + filename);

			TraceFormatFileOpener traceReader = new TraceFormatFileOpener(
					filename, false, StatisticsReader.class,
					StAXTraceFileReader.class, null);
			
			traverseHostnames(traceReader.getProjectDescription().getTopologyRoot());
			
			return hostnames;

		} catch (Exception e) {
			throw new HDTraceImporterException(e);
		}
	}
	
	private void setStatistics() throws HDTraceImporterException {

		try {

			logger.info("Opening File: " + filename);

			TraceFormatFileOpener traceReader = new TraceFormatFileOpener(
					filename, false, StatisticsReader.class,
					StAXTraceFileReader.class, null);
			
			traverse(traceReader.getProjectDescription().getTopologyRoot());

		} catch (Exception e) {
			throw new HDTraceImporterException(e);
		}
	}
	
	private void traverseHostnames(TopologyNode topologyNode) {

		if (topologyNode == null)
			return;
		
		if(topologyNode.getType().equals("Hostname")) {
			
			logger.debug("Found Hostname topology node: " + topologyNode.getName());
			
			if(!hostnames.contains(topologyNode.getName())) {
				hostnames.add(topologyNode.getName());
				logger.debug("Added hostname: " + topologyNode.getName());
			} else if (topologyNode.getChildElements().size() > 0) {
				for (TopologyNode childNode : topologyNode.getChildElements()
						.values()) {
					traverseHostnames(childNode);
				}
			}
		} else if (topologyNode.getChildElements().size() > 0) {
			for (TopologyNode childNode : topologyNode.getChildElements()
					.values()) {
				traverseHostnames(childNode);
			}
		}
				
	}


	private void traverse(TopologyNode topologyNode)
			throws HDTraceImporterException {

		if (topologyNode == null)
			return;

		if (topologyNode.getStatisticsSource(statisticName) != null) {
			
			if (topologyNode.getName().equals(hostname) && topologyNode.getType().equals("Hostname")) {

				logger.info("Reading " + statisticName + " statistic from file: "
						+ topologyNode.getStatisticFileName(statisticName));

				StatisticsReader reader = (StatisticsReader) topologyNode
						.getStatisticsSource(statisticName);

				statisticsDescriptions = reader.getGroup().getStatisticsOrdered();

				try {
					StatisticsGroupEntry inputEntry = reader
							.getNextInputEntry();

					while (inputEntry != null) {

						statisticGroupEntries.add(inputEntry);

						inputEntry = reader.getNextInputEntry();
					}
					
					return;

				} catch (Exception e) {
					throw new HDTraceImporterException(e);
				}
			}

		} else if (topologyNode.getChildElements().size() > 0) {
			for (TopologyNode childNode : topologyNode.getChildElements()
					.values()) {
				traverse(childNode);
			}
		}

	}
}

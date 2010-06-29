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
package de.hd.pvs.piosim.power.tests;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.hd.pvs.piosim.power.Time;
import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.cluster.NodeFactory;
import de.hd.pvs.piosim.power.devices.MockDevice;
import de.hd.pvs.piosim.power.trace.HDTraceExporter;
import de.hd.pvs.piosim.power.trace.HDTraceExporterException;

public class HDTraceExporterTest extends AbstractTestCase {
	
	@Test
	public void testWithNewGivenExample() {
		
		String pathToExporterOutput = outputFolder.getAbsolutePath()+ "/exportOutWithNewGivenExample";

		String simple = "Simple";
		String lookAhead = "Look Ahead";
		
		MockDevice disk = new MockDevice();
		disk.setName("Disk");

		List<Node> nodes = new ArrayList<Node>();
		Node host1 = NodeFactory.createEmptySimpleNode("host01");
		host1.add(disk);
		nodes.add(host1);
		
		List<String> strategies = new ArrayList<String>();
		strategies.add(simple);
		strategies.add(lookAhead);
		
//		TraceFormatWriter writer = new TraceFormatWriter("/tmp/test", "test descrpition", "test application",  
//				new String []{"Host","Energy","Component"});
//	
//		
//		final TopologyNode host1 = writer.createInitalizeTopology(new String[]{"host01"});	
//		
//		final TopologyNode host1EnergyStates = writer.createInitalizeTopology(new String[]{"host01","Energy States"});
//		final TopologyNode host1EstimatedEnergy = writer.createInitalizeTopology(new String[]{"host01","Energy"});
//		final TopologyNode diskEnergyStates = writer.createInitalizeTopology(new String[]{"host01","Energy States","Disk"});
//		final TopologyNode diskEstimatedEnergy = writer.createInitalizeTopology(new String[]{"host01","Energy","Disk"});
//		StatisticsGroupDescription estimatedEnergy = new StatisticsGroupDescription("EstimatedEnergyConsumption");
//		
//		StatisticsDescription statEstimatedSimple = new StatisticsDescription(estimatedEnergy, "Simple", StatisticsEntryType.FLOAT, 0, "Watt", "Energy");
//		StatisticsDescription statEstimatedLookAhead = new StatisticsDescription(estimatedEnergy, "Look Ahead", StatisticsEntryType.FLOAT, 0, "Watt", "Energy");
//
//		estimatedEnergy.addStatistic(statEstimatedLookAhead);
//		estimatedEnergy.addStatistic(statEstimatedSimple);
//		// init the trace file, the first entry will start at the given time:
//		writer.initStatisticsTopology(diskEstimatedEnergy, estimatedEnergy, new Epoch(0.0));
//		writer.initStatisticsTopology(host1EstimatedEnergy, estimatedEnergy, new Epoch(0.0));	
		
		HDTraceExporter exporter = new HDTraceExporter(pathToExporterOutput, "test descrpition",
				"test application", nodes, strategies);		
		
//		writer.writeStateStart(diskEnergyStates, "ACPI0", new Epoch(0.0));
//		writer.writeStateEnd(diskEnergyStates, "ACPI0", new Epoch(3.0));
//		writer.writeStateStart(diskEnergyStates, "ACPI3", new Epoch(3.0));
//		
//		writer.writeStateEnd(diskEnergyStates, "ACPI3", new Epoch(4.0));

		try {
			exporter.startState(disk.getACPIComponent(), lookAhead, "ACPI0", Time.getInstance().getCurrentTimeInMillis());
			
			Time.getInstance().timePassed(3000);

			exporter.endState(disk.getACPIComponent(), lookAhead, "ACPI0", Time.getInstance().getCurrentTimeInMillis());
			exporter.startState(disk.getACPIComponent(), lookAhead, "ACPI3", Time.getInstance().getCurrentTimeInMillis());
			
			Time.getInstance().timePassed(1000);
			
			exporter.endState(disk.getACPIComponent(), lookAhead, "ACPI3", Time.getInstance().getCurrentTimeInMillis());
		} catch (HDTraceExporterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
//		writer.writeStatisticsTimestamp(diskEstimatedEnergy, estimatedEnergy, new Epoch(3.0));
//		writer.writeStatisticValue(diskEstimatedEnergy, statEstimatedLookAhead, new Float(2.0));
//		writer.writeStatisticValue(diskEstimatedEnergy, statEstimatedSimple, new Float(2.0));
//		
//		
//		writer.writeStatisticsTimestamp(diskEstimatedEnergy, estimatedEnergy, new Epoch(4.0));
//		writer.writeStatisticValue(diskEstimatedEnergy, statEstimatedLookAhead, new Float(0.5));
//		writer.writeStatisticValue(diskEstimatedEnergy, statEstimatedSimple, new Float(2.0));
//		
		
		Time.getInstance().reset();
		
		disk.reset();
		host1.reset();
		
		Time.getInstance().timePassed(3000);
		
		try {
			exporter.writeTimestamp(disk.getACPIComponent(), simple, Time.getInstance().getCurrentTimeInMillis());
			exporter.writeStatistics(disk.getACPIComponent(), simple, new BigDecimal("2.0"));
			
			exporter.writeTimestamp(disk.getACPIComponent(), lookAhead, Time.getInstance().getCurrentTimeInMillis());
			exporter.writeStatistics(disk.getACPIComponent(), lookAhead, new BigDecimal("2.0"));
			
			Time.getInstance().timePassed(1000);
			
			exporter.writeTimestamp(disk.getACPIComponent(), lookAhead, Time.getInstance().getCurrentTimeInMillis());			
			exporter.writeStatistics(disk.getACPIComponent(), lookAhead, new BigDecimal("2.0"));
			exporter.writeTimestamp(disk.getACPIComponent(), simple, Time.getInstance().getCurrentTimeInMillis());		
			exporter.writeStatistics(disk.getACPIComponent(), simple, new BigDecimal("0.5"));
			
		} catch (HDTraceExporterException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
//		writer.finalizeTrace();
		
		try {
			exporter.finalize();
		} catch (HDTraceExporterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

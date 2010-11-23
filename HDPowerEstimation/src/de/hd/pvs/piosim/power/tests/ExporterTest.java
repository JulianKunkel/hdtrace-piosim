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

import de.hd.pvs.piosim.power.acpi.history.ACPIStateChangesHistory;
import de.hd.pvs.piosim.power.calculation.ACPICalculation;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;
import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.cluster.SimpleNode;
import de.hd.pvs.piosim.power.data.DeviceData;
import de.hd.pvs.piosim.power.data.visualizer.VisualizerException;
import de.hd.pvs.piosim.power.devices.MockDevice;
import de.hd.pvs.piosim.power.replay.Replay;
import de.hd.pvs.piosim.power.replay.ReplayDevice;
import de.hd.pvs.piosim.power.replay.ReplayException;
import de.hd.pvs.piosim.power.replay.ReplayExporter;
import de.hd.pvs.piosim.power.replay.ReplayExporterException;
import de.hd.pvs.piosim.power.replay.ReplayItem;
import de.hd.pvs.piosim.power.replay.strategy.OptimalPlayStrategy;
import de.hd.pvs.piosim.power.tests.util.TestObjectCreator;
import de.hd.pvs.piosim.power.trace.HDTraceExporter;
import de.hd.pvs.piosim.power.trace.HDTraceExporterException;

public class ExporterTest extends AbstractTestCase {
	
	private Replay replay = null;
	private MockDevice mock = null;
	private Node node = null;

	@Test
	public void testAcpiStateChangeTimes() {
		
		buildItems();	
		
		try {
			replay.play();
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	
		
		// 6500 ms in state 3 = 3600/3600000 * 6500 = 6.5 watt-h 
		// + state changes consumptions
		// + 1 watt-h + 1 watt-h + 1 watt-h = 9.5 watt-h
		
		assertEquals(9.5,mock.getEnergyConsumption().doubleValue());
		
		BigDecimal[] powerConsumption = replay.getReplayItems().get(0).getReplayDevice().getPowerConsumption();
		
		BigDecimal sum = new BigDecimal("0");
		
		for(int i=0; i<powerConsumption.length; ++i) {
			BigDecimal stepPowerConsumption= ACPICalculation.calculateInWattH(powerConsumption[i],new BigDecimal(TestObjectCreator.stepsize));
			sum = BaseCalculation.sum(sum, stepPowerConsumption);
			if(i == 0 || i == 1)
				assertEquals(0.5,stepPowerConsumption.doubleValue());
			else if (i == 3)
				assertEquals(1.5, stepPowerConsumption.doubleValue());
			else
				assertEquals(1.0, stepPowerConsumption.doubleValue());
		}
		
		assertEquals(mock.getEnergyConsumption().doubleValue(), sum.doubleValue());
		
		try {
			replay.visualize(testVisualizer);
		} catch (VisualizerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		List<Node> nodes = new ArrayList<Node>();
		nodes.add(node);
		
		List<String> strategies = new ArrayList<String>();
		strategies.add("Optimal");
		HDTraceExporter traceExporter = new HDTraceExporter(outputFolder.getAbsolutePath()+ "/exportOut","description","application",nodes,strategies);
		ReplayExporter replayExporter = new ReplayExporter();
		
		try {
			replayExporter.add(replay, strategies.get(0));
			ACPIStateChangesHistory.getInstance().setName(strategies.get(0));
			replayExporter.setACPIStateChangesHistory(ACPIStateChangesHistory.getInstance());
			replayExporter.export(traceExporter);
			traceExporter.finalize();
		} catch (ReplayExporterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (HDTraceExporterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	private void buildItems() {
		DeviceData deviceData = TestObjectCreator.createZeroUtilizationDeviceData(new String[]{"device"}).get("device");
		mock = new MockDevice();
		mock.setName("device");
		node = new SimpleNode();
		node.setName("node");
		node.add(mock);
		
		mock.setStateEnergyConsumption(1, 10000);
		mock.setStateEnergyConsumption(2, 10000);
		mock.setStateEnergyConsumption(3, 3600);
		
		mock.setLoadPowerConsumption(10000);
		mock.setIdlePowerConsumption(10000);
		
		mock.setIncStateDuration(0, 2000);
		mock.setIncStateDuration(1, 1000);
		mock.setIncStateDuration(2, 500);
		
		mock.setIncStateEnergyConsumption(0, 1);
		mock.setIncStateEnergyConsumption(1, 1);
		mock.setIncStateEnergyConsumption(2, 1);
		
		ReplayDevice replayDevice = new ReplayDevice();
		replayDevice.setACPIDevice(mock);
		replayDevice.setDeviceData(deviceData);
		ReplayItem item = new ReplayItem();
		item.setPlayStrategy(new OptimalPlayStrategy());
		item.setReplayDevice(replayDevice);
		
		replay = new Replay();
		replay.setCountSteps(TestObjectCreator.countSteps);
		replay.setStepsize(TestObjectCreator.stepsize);
		ArrayList<ReplayItem> items = new ArrayList<ReplayItem>();
		items.add(item);
		replay.setReplayItems(items);
	}
}

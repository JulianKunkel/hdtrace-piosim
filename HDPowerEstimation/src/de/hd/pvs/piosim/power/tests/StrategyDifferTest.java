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
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.cluster.BuildException;
import de.hd.pvs.piosim.power.data.DeviceData;
import de.hd.pvs.piosim.power.data.visualizer.VisualizerException;
import de.hd.pvs.piosim.power.replay.Replay;
import de.hd.pvs.piosim.power.replay.ReplayDevice;
import de.hd.pvs.piosim.power.replay.ReplayException;
import de.hd.pvs.piosim.power.replay.ReplayItem;
import de.hd.pvs.piosim.power.replay.StrategyDiffer;
import de.hd.pvs.piosim.power.replay.strategy.OptimalPlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.PlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.SimplePlayStrategy;
import de.hd.pvs.piosim.power.tests.util.TestObjectCreator;

public class StrategyDifferTest extends AbstractTestCase {
	private String firstComponentName = "CPU";
	private Map<String,String> mapping = new HashMap<String,String>();
	int countSteps = 100;

	@Test
	public void testStrategyDiffer() {
		
		mapping.put(firstComponentName, "SimpleCPU");

		List<PlayStrategy> strategyList = new ArrayList<PlayStrategy>();

		strategyList.add(new SimplePlayStrategy());
		strategyList.add(new OptimalPlayStrategy());
		
		Replay replay = new Replay();
		replay.setCountSteps(countSteps);
		replay.setStepsize(1000);
		
		ReplayDevice replayDevice = new ReplayDevice();
		ACPIDevice device = null;
		try {
			device = TestObjectCreator.buildACPIDevice(firstComponentName, mapping);
		} catch (BuildException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		replayDevice.setACPIDevice(device);
		
		DeviceData deviceData = createDeviceData();
		
		replayDevice.setDeviceData(deviceData);
		
		List<ReplayItem> items = new ArrayList<ReplayItem>();
		ReplayItem item = new ReplayItem();
		item.setReplayDevice(replayDevice);
		items.add(item);	
		replay.setReplayItems(items);

		try {
			StrategyDiffer differ = new StrategyDiffer();
			differ.setReplay(replay);
			differ.setStrategyList(strategyList);
			differ.playAndVisualize(countSteps, testVisualizer);
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (VisualizerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	private DeviceData createDeviceData() {
		
		DeviceData deviceData = new DeviceData();

		// data for components
		BigDecimal[] utilization = new BigDecimal[countSteps];

		// linear ascending utilization, 0-99 percent
		for (int i = 0; i < countSteps; ++i) {
			MathContext mc = new MathContext(2);

			utilization[i] = new BigDecimal(0.01 * i, mc);

		}
		
		// add some (potential) sleeping phases for device
		for(int i=10; i<20; ++i) {
			utilization[i] = BigDecimal.ZERO;
		}
		for(int i=70; i<90; ++i) {
			utilization[i] = BigDecimal.ZERO;
		}

		deviceData.setUtilization(utilization);
		
		return deviceData;
	}

}

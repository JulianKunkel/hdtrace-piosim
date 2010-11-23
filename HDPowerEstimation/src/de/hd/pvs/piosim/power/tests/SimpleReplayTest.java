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

import org.apache.log4j.Logger;
import org.junit.Test;

import de.hd.pvs.piosim.power.DeviceBuilder;
import de.hd.pvs.piosim.power.Time;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.cluster.BuildException;
import de.hd.pvs.piosim.power.cluster.ExtendedNode;
import de.hd.pvs.piosim.power.cluster.NodeFactory;
import de.hd.pvs.piosim.power.cluster.SimpleNode;
import de.hd.pvs.piosim.power.data.DeviceData;
import de.hd.pvs.piosim.power.data.visualizer.VisualizerException;
import de.hd.pvs.piosim.power.replay.Replay;
import de.hd.pvs.piosim.power.replay.ReplayDevice;
import de.hd.pvs.piosim.power.replay.ReplayException;
import de.hd.pvs.piosim.power.replay.ReplayItem;
import de.hd.pvs.piosim.power.replay.strategy.PlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.SimplePlayStrategy;

public class SimpleReplayTest extends AbstractTestCase {
	
	
	private String[] deviceNames = {"CPU1","CPU2","DISK"};
	private Map<String,String> mapping = new HashMap<String,String>();
	private int countSteps = 10;
	private DeviceData[] deviceData = new DeviceData[deviceNames.length];
	private Logger logger = Logger.getLogger(SimpleReplayTest.class);
	
	
	@Test
	public void testPlay() {
		
		SimpleNode node = NodeFactory.createEmptySimpleNode("simpleNode");
		
		mapping.put("CPU1", "SimpleCPU");
		mapping.put("CPU2", "SimpleCPU");
		mapping.put("DISK", "SimpleDisk");
		
		initDeviceData();
		
		List<ReplayItem> items = new ArrayList<ReplayItem>();
		for(int i=0; i<deviceNames.length; ++i) {
			ReplayDevice replayDevice = null;
			try {
				replayDevice = buildReplayDevice(deviceData[i], deviceNames[i]);
				node.add(replayDevice.getACPIDevice());
			} catch (BuildException e) {
				e.printStackTrace();
				fail(e.getMessage());
			} 
			ReplayItem item = buildReplayItem(replayDevice, new SimplePlayStrategy());
			items.add(item);
		}
		
		Replay replay = new Replay();
		replay.setReplayItems(items);
		replay.setCountSteps(countSteps);
		replay.setStepsize(1000);
		
		try {
			replay.play();
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		try {
			replay.visualize(testVisualizer);
		} catch (VisualizerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		logger.info("Time passed: " + Time.getInstance().getCurrentTimeInMillis() + " ms");
		logger.info("Simple Node consumption: " + node.getEnergyConsumption());
		
	}
	
	@Test
	public void testNodeConsumption() {
		
		ExtendedNode node = NodeFactory.createEmptyExtendedNode("extendedNode",new BigDecimal("80"));
		
		mapping.put("CPU1", "SimpleCPU");
		mapping.put("CPU2", "SimpleCPU");
		mapping.put("DISK", "SimpleDisk");
		
		initDeviceData();
		
		List<ReplayItem> items = new ArrayList<ReplayItem>();
		for(int i=0; i<deviceNames.length; ++i) {
			ReplayDevice replayDevice = null;
			try {
				replayDevice = buildReplayDevice(deviceData[i], deviceNames[i]);
				node.add(replayDevice.getACPIDevice());
			} catch (BuildException e) {
				e.printStackTrace();
				fail(e.getMessage());
			} 
			ReplayItem item = buildReplayItem(replayDevice, new SimplePlayStrategy());
			items.add(item);
		}
		
		Replay replay = new Replay();
		replay.setReplayItems(items);
		replay.setCountSteps(countSteps);
		replay.setStepsize(1000);
		
		try {
			replay.play();
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		try {
			replay.visualize(testVisualizer);
		} catch (VisualizerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		logger.info("Time passed: " + Time.getInstance().getCurrentTimeInMillis() + " ms");
		logger.info("Extended Node consumption: " + node.getEnergyConsumption());

	}
	
	private void initDeviceData() {
		
		// data for components
		BigDecimal[] data = new BigDecimal[countSteps];
		BigDecimal[] data2 = new BigDecimal[countSteps];
		BigDecimal[] data3 = new BigDecimal[countSteps];
		
		for(int i=0; i<countSteps; ++i) {
			MathContext mc = new MathContext(2);
			if(i % 2 == 0) {
				data[i] = new BigDecimal(0.1 * i,mc);
				data2[i] = new BigDecimal(1 - (0.1 * i),mc);
				data3[i] = new BigDecimal(1 - (0.1 * i),mc);
			} else {
				data[i] = data[i-1];
				data2[i] = data2[i-1];
				data3[i] = data[i-1];
			}
		}

		deviceData[0] = new DeviceData();
		deviceData[0].setUtilization(data);
		deviceData[1] = new DeviceData();
		deviceData[1].setUtilization(data2);
		deviceData[2] = new DeviceData();
		deviceData[2].setUtilization(data3);
	}
	
	private ReplayDevice buildReplayDevice(DeviceData deviceData, String name) throws BuildException {
		
		ACPIDevice device = DeviceBuilder.createACPIDevice(mapping.get(name));
		device.setName(name);
		ReplayDevice replayDevice = new ReplayDevice();
		replayDevice.setACPIDevice(device);
		replayDevice.setDeviceData(deviceData);
		
		return replayDevice;
	}
	
	private ReplayItem buildReplayItem(ReplayDevice replayDevice, PlayStrategy playStrategy) {
		
		ReplayItem item = new ReplayItem();
		item.setPlayStrategy(playStrategy);
		item.setReplayDevice(replayDevice);
		return item;
	}

}

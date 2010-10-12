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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.hd.pvs.piosim.power.cluster.BuildException;
import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.data.DeviceData;
import de.hd.pvs.piosim.power.data.visualizer.VisualizerException;
import de.hd.pvs.piosim.power.replay.Replay;
import de.hd.pvs.piosim.power.replay.ReplayException;
import de.hd.pvs.piosim.power.replay.ReplayExporter;
import de.hd.pvs.piosim.power.replay.ReplayExporterException;
import de.hd.pvs.piosim.power.replay.ReplayItem;
import de.hd.pvs.piosim.power.replay.strategy.ApproachPlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.PlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.SimplePlayStrategy;
import de.hd.pvs.piosim.power.tests.util.TestObjectCreator;
import de.hd.pvs.piosim.power.trace.HDTraceExporter;
import de.hd.pvs.piosim.power.trace.HDTraceExporterException;

public class ReplayExporterTest extends AbstractTestCase {
	
	private List<String> strategies;
	
	@Override
	public void setUp() {
		super.setUp();
		strategies = new ArrayList<String>();
		strategies.add("strategy name");
	}
 
	@Test
	public void testExportOfTwoReplaysWithDifferentStrategies() {
			
		String[] deviceNames = {"CPU1"};
		Map<String,String> mapping = new HashMap<String,String>();
		mapping.put("CPU1", "SimpleCPU");
		Map<String,PlayStrategy> playStrategies = new HashMap<String,PlayStrategy>();
		playStrategies.put("CPU1", new SimplePlayStrategy());
		Map<String,DeviceData> deviceData = new HashMap<String,DeviceData>();
		
		Node node = null;
		Replay replay = null;
		try {
			node = TestObjectCreator.createSimpleNode(deviceNames, mapping);
			node.setName("timobile");
			deviceData = TestObjectCreator.createApproachDeviceData(deviceNames);
			replay = TestObjectCreator.createReplay(node, playStrategies, deviceData);
		} catch (BuildException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}
		
		try {
			replay.play();
			replay.visualize(testVisualizer);
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (VisualizerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
		ReplayExporter replayExporter = new ReplayExporter();
		
		try {
			replayExporter.add(replay, strategies.get(0));
		} catch (ReplayExporterException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}

		replay.reset();
		
		
		
		// 2. Replay
		playStrategies = new HashMap<String,PlayStrategy>();
		playStrategies.put("CPU1", new ApproachPlayStrategy());
		
		replay = TestObjectCreator.createReplay(node, playStrategies, deviceData);
		
		try {
			replay.play();
			replay.visualize(testVisualizer);
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (VisualizerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
		try {
			replayExporter.add(replay, strategies.get(0));
		} catch (ReplayExporterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
			

		List<Node> nodes = new ArrayList<Node>();
		nodes.add(node);
		HDTraceExporter traceExporter = new HDTraceExporter(outputFolder.getAbsolutePath() + "/export","description","application",nodes,strategies);
			
		try {
			replayExporter.export(traceExporter);
		} catch (HDTraceExporterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		try {
			traceExporter.finalize();
		} catch (HDTraceExporterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testNodeExport() {
		String[] deviceNames = {"CPU1","CPU2","DISK"};
		Map<String,String> mapping = new HashMap<String,String>();
		mapping.put("CPU1", "SimpleCPU");
		mapping.put("CPU2", "SimpleCPU");
		mapping.put("DISK", "SimpleDisk");
		Map<String,PlayStrategy> playStrategies = new HashMap<String,PlayStrategy>();
		playStrategies.put("CPU1", new SimplePlayStrategy());
		playStrategies.put("CPU2", new SimplePlayStrategy());
		playStrategies.put("DISK", new SimplePlayStrategy());
		Map<String,DeviceData> deviceDataTimobile = new HashMap<String,DeviceData>();
		
		Node timobile = null;
		Replay replay = null;
		try {
			timobile = TestObjectCreator.createSimpleNode(deviceNames, mapping);
			timobile.setName("timobile");
			deviceDataTimobile = TestObjectCreator.createApproachDeviceData(deviceNames);
			replay = TestObjectCreator.createReplay(timobile, playStrategies, deviceDataTimobile);
		} catch (BuildException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		try {
			replay.play();
			replay.visualize(testVisualizer);
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (VisualizerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
		ReplayExporter replayExporter = new ReplayExporter();
		
		try {
			replayExporter.add(replay, strategies.get(0));
		} catch (ReplayExporterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		replay.reset();
		
		
		
		// 2. Replay
		playStrategies = new HashMap<String,PlayStrategy>();
		playStrategies.put("CPU1", new ApproachPlayStrategy());
		playStrategies.put("CPU2", new ApproachPlayStrategy());
		playStrategies.put("DISK", new ApproachPlayStrategy());
		
		replay = TestObjectCreator.createReplay(timobile, playStrategies, deviceDataTimobile);
		
		try {
			replay.play();
			replay.visualize(testVisualizer);
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (VisualizerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
		try {
			replayExporter.add(replay, strategies.get(0));
		} catch (ReplayExporterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
			

		List<Node> nodes = new ArrayList<Node>();
		nodes.add(timobile);
		HDTraceExporter traceExporter = new HDTraceExporter(outputFolder.getAbsolutePath() + "/exportNode","description","application",nodes,strategies);
			
		try {
			replayExporter.export(traceExporter);
		} catch (HDTraceExporterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		try {
			traceExporter.finalize();
		} catch (HDTraceExporterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testNodeExportWithMultipleNodes() {
		String[] deviceNames = {"CPU","DISK"};
		Map<String,String> mapping = new HashMap<String,String>();
		mapping.put("CPU", "SimpleCPU");
		mapping.put("DISK", "SimpleDisk");
		Map<String,PlayStrategy> playStrategies = new HashMap<String,PlayStrategy>();
		playStrategies.put("CPU", new SimplePlayStrategy());
		playStrategies.put("DISK", new SimplePlayStrategy());
		Map<String,DeviceData> deviceDataTimobile = new HashMap<String,DeviceData>();
		Map<String,DeviceData> deviceDataCopy = new HashMap<String,DeviceData>();
		
		Node timobile = null;
		Node timobileExtended = null;
		Replay replay = null;
		Replay replayCopy = null;
		try {
			timobile = TestObjectCreator.createSimpleNode(deviceNames, mapping);
			timobile.setName("timobile");
			timobileExtended = TestObjectCreator.createExtendedNode(deviceNames, mapping);
			timobileExtended.setName("extended");
			deviceDataTimobile = TestObjectCreator.createApproachDeviceData(deviceNames);
			deviceDataCopy = TestObjectCreator.createApproachDeviceData(deviceNames);
			replay = TestObjectCreator.createReplay(timobile, playStrategies, deviceDataTimobile);
			replayCopy = TestObjectCreator.createReplay(timobileExtended, playStrategies, deviceDataCopy);
			
			for(ReplayItem item : replayCopy.getReplayItems()) {
				replay.addReplayItem(item);
			}
		} catch (BuildException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		try {
			replay.play();
			replay.visualize(testVisualizer);
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (VisualizerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
		ReplayExporter replayExporter = new ReplayExporter();
		
		try {
			replayExporter.add(replay, strategies.get(0));
		} catch (ReplayExporterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		replay.reset();
		
		
		
		// 2. Replay
		playStrategies = new HashMap<String,PlayStrategy>();
		playStrategies.put("CPU", new ApproachPlayStrategy());
		playStrategies.put("DISK", new ApproachPlayStrategy());
		
		replay = TestObjectCreator.createReplay(timobile, playStrategies, deviceDataTimobile);
		replayCopy = TestObjectCreator.createReplay(timobileExtended, playStrategies, deviceDataCopy);
		
		for(ReplayItem item : replayCopy.getReplayItems()) {
			replay.addReplayItem(item);
		}
		
		try {
			replay.play();
			replay.visualize(testVisualizer);
		} catch (ReplayException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (VisualizerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
		try {
			strategies.add("approach");
			replayExporter.add(replay, strategies.get(1));
		} catch (ReplayExporterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
			

		List<Node> nodes = new ArrayList<Node>();
		nodes.add(timobile);
		nodes.add(timobileExtended);
		HDTraceExporter traceExporter = new HDTraceExporter(outputFolder.getAbsolutePath() + "/exportMultipleNodes","description","application",nodes,strategies);
			
		try {
			replayExporter.export(traceExporter);
		} catch (HDTraceExporterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		try {
			traceExporter.finalize();
		} catch (HDTraceExporterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}

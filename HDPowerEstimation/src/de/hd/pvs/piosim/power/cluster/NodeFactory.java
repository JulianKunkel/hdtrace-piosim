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
package de.hd.pvs.piosim.power.cluster;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.hd.pvs.piosim.power.DeviceBuilder;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.devices.SimpleCPU;
import de.hd.pvs.piosim.power.devices.SimpleDisk;
import de.hd.pvs.piosim.power.devices.SimpleMemory;
import de.hd.pvs.piosim.power.devices.SimpleNIC;
import de.hd.pvs.piosim.power.devices.SimpleVGA;
import de.hd.pvs.piosim.power.replay.ReplayItem;

public class NodeFactory {
	
	private static Logger logger = Logger.getLogger(NodeFactory.class);
	
	private NodeFactory() {
		
	}
	
	public static SimpleNode createSimpleNode() {
		SimpleNode node = new SimpleNode();
		ACPIDevice disk = new SimpleDisk();
		disk.setName("SimpleDisk");
		node.add(disk);
		ACPIDevice cpu = new SimpleCPU();
		cpu.setName("SimpleCPU");
		node.add(cpu);
		ACPIDevice vga = new SimpleVGA();
		vga.setName("SimpleVGA");
		node.add(vga);
		ACPIDevice ram = new SimpleMemory();
		ram.setName("SimpleMemory");
		node.add(ram);
		ACPIDevice nic = new SimpleNIC();
		nic.setName("SimpleNIC");
		node.add(nic);
		return node;
	}
	
	public static SimpleNode createSimpleNode(String nodeName) {
		SimpleNode node = createSimpleNode();
		node.setName(nodeName);
		return node;
	}
	
	public static ExtendedNode createExtendedNode(Map<String,String> mapping, String nodeName) throws BuildException {
		return createExtendedNode(mapping, nodeName, new BigDecimal("0"));
	}
	
	public static ExtendedNode createExtendedNode(Map<String,String> mapping, String nodeName, BigDecimal overhead) throws BuildException {
		ExtendedNode node = new ExtendedNode();
		
		for(String name : mapping.keySet()) {
			ACPIDevice nodeDevice = DeviceBuilder.createACPIDevice(mapping.get(name));
			nodeDevice.setName(name);
			node.add(nodeDevice);
		}
		
		node.setName(nodeName);
		
		node.setOverhead(overhead);
		logger.debug("Created Node " + nodeName + " with " + node.getNodeDevices().size() + " device(s) and an overhead of " + overhead);
		return node;
	}
	
	public static ExtendedNode createExtendedNode() {
		return createExtendedNode("",new BigDecimal("0"));
	}
	
	public static ExtendedNode createExtendedNode(String name) {
		return createExtendedNode(name,new BigDecimal("0"));
	}
	
	public static ExtendedNode createExtendedNode(BigDecimal overhead) {
		return createExtendedNode("",overhead);
	}
	
	public static ExtendedNode createExtendedNode(String name, BigDecimal overhead) {
		ExtendedNode node = new ExtendedNode();
		
		ACPIDevice disk = new SimpleDisk();
		disk.setName("SimpleDisk");
		node.add(disk);
		ACPIDevice cpu = new SimpleCPU();
		cpu.setName("SimpleCPU");
		node.add(cpu);
		ACPIDevice vga = new SimpleVGA();
		vga.setName("SimpleVGA");
		node.add(vga);
		ACPIDevice ram = new SimpleMemory();
		ram.setName("SimpleMemory");
		node.add(ram);
		ACPIDevice nic = new SimpleNIC();
		nic.setName("SimpleNIC");
		node.add(nic);
		
		node.setOverhead(overhead);
		node.setName(name);
		logger.debug("Created ExtendedNode " + name + " with " + node.getNodeDevices().size() + " device(s) and an overhead of " + overhead);
		return node;
	}
	
	/**
	 * Creates a node with devices specified in the mapping
	 * @param mapping <NameForDevice,ClassName>
	 * @return
	 * @throws BuildException
	 */
	public static SimpleNode createSimpleNode(Map<String,String> mapping, String nodeName) throws BuildException {
		SimpleNode node = new SimpleNode();
		
		for(String name : mapping.keySet()) {
			ACPIDevice nodeDevice = DeviceBuilder.createACPIDevice(mapping.get(name));
			nodeDevice.setName(name);
			node.add(nodeDevice);
		}
		
		node.setName(nodeName);
		
		return node;
	}

	public static Node createSimpleNode(List<ReplayItem> replayItems, String nodeName) {
		SimpleNode node = new SimpleNode();
		
		for(ReplayItem item : replayItems) {
			node.add(item.getReplayDevice().getACPIDevice());
		}
		
		node.setName(nodeName);
		
		logger.debug("Created Node " + nodeName + " with " + node.getNodeDevices().size() + " device(s)");
		
		return node;
	}

	public static SimpleNode createEmptySimpleNode(String nodeName) {
		SimpleNode node = new SimpleNode();
		node.setName(nodeName);
		logger.debug("Created Node " + nodeName + " with " + node.getNodeDevices().size() + " device(s)");
		return node;
	}

	public static ExtendedNode createEmptyExtendedNode(String name, BigDecimal overhead) {
		ExtendedNode node = new ExtendedNode();
		node.setName(name);
		node.setOverhead(overhead);
		logger.debug("Created ExtendedNode " + name + " with " + node.getNodeDevices().size() + " device(s) and an overhead of " + overhead);
		return node;
	}
	
	public static ExtendedNode createEmptyExtendedNode(String name) {
		return createEmptyExtendedNode(name,new BigDecimal("0"));
	}

}

package de.hd.pvs.piosim.power.tests;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.cluster.BuildException;
import de.hd.pvs.piosim.power.cluster.ExtendedNode;
import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.cluster.NodeFactory;
import de.hd.pvs.piosim.power.cluster.PowerSupply;
import de.hd.pvs.piosim.power.data.DeviceData;
import de.hd.pvs.piosim.power.trace.ExternalStatisticReader;
import de.hd.pvs.piosim.power.trace.HDTraceImporter;
import de.hd.pvs.piosim.power.trace.HDTraceImporterException;
import de.hd.pvs.piosim.power.trace.TopologyNodeSet;

public class HDTraceImporterTest extends AbstractTestCase {
	
	private Map<String, String> createNameToACPIDeviceMapping() {
		Map<String, String> nameToACPIDeviceMapping = new HashMap<String, String>();

		nameToACPIDeviceMapping.put("CPU_TOTAL", "pvscluster.CPU");
		
		for(int i=0; i<4; ++i) {
			nameToACPIDeviceMapping.put("CPU_TOTAL_" + i, "pvscluster.CPU");
		}
		
		nameToACPIDeviceMapping.put("MEM_USED", "eeclust.Memory");
		nameToACPIDeviceMapping.put("NET_OUT_eth0", "pvscluster.NIC");
		nameToACPIDeviceMapping.put("NET_IN_eth0", "pvscluster.NIC");
		nameToACPIDeviceMapping.put("HDD_WRITE", "eeclust.Disk");
		
		return nameToACPIDeviceMapping;
	}
	
	private List<Node> createEEclustNodes(List<String> hostnames, Map<String, String> nameToACPIDeviceMapping) {
		
		PowerSupply powerSupply = new PowerSupply();

		powerSupply.setProcentualOverhead(new BigDecimal("0.35"));

		BigDecimal overhead = new BigDecimal("6.305");
		
		List<Node> nodes = new ArrayList<Node>();

		for (String hostname : hostnames) {

			try {
				ExtendedNode node = NodeFactory.createExtendedNode(
						nameToACPIDeviceMapping, hostname);
				node.setPowerSupply(powerSupply);
				node.setOverhead(overhead);
				nodes.add(node);
			} catch (BuildException e) {
				e.printStackTrace();
				return null;
			}
			
		}
		
		return nodes;
	}

	@Test
	public void testWithEEclustExample() {
		
		Logger.getRootLogger().setLevel(Level.OFF);
		Logger.getLogger(TopologyNodeSet.class).setLevel(Level.DEBUG);
		Logger.getLogger(ExternalStatisticReader.class).setLevel(Level.DEBUG);
		Logger.getLogger(HDTraceImporter.class).setLevel(Level.DEBUG);
		
		String inputProject = this.inputFolder + "/partdiff-par.proj";
		
		Map<String, String> nameToACPIDeviceMapping = createNameToACPIDeviceMapping();
		
		String[] componentNames = new String[nameToACPIDeviceMapping.size()];
		componentNames = nameToACPIDeviceMapping.keySet().toArray(componentNames);
		
		HDTraceImporter reader = new HDTraceImporter();
		
		reader.setFilename(inputProject);
		List<String> hostnames = reader.getHostnames();
		
		assertEquals(1, hostnames.size());
		
		List<Node> nodes = createEEclustNodes(hostnames, nameToACPIDeviceMapping);
		
		for(Node node : nodes)
			reader.addNode(node);
		
		assertEquals(1, nodes.size());
		
		assertEquals("eeclust", nodes.get(0).getName());
		
		try {
			reader.setUtilization(componentNames);
		} catch (HDTraceImporterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		assertEquals(2.0, reader.getMaxStepsize(), 00.1);
		assertEquals(2.0, reader.getMinStepsize(), 00.1);
		
		Map<ACPIDevice, DeviceData> data = reader.getDeviceData();
		
		for (ACPIDevice device : data.keySet()) {
			DeviceData deviceData = data.get(device);
			int countValues = deviceData.getCountValues();
			System.out.println("Device " + device.getNode() + "." + device.getName() + ": " + countValues);
		}
		
	}
}

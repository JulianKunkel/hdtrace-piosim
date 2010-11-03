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

import org.apache.log4j.Logger;
import org.junit.Test;

import de.hd.pvs.piosim.power.ComponentException;
import de.hd.pvs.piosim.power.Time;
import de.hd.pvs.piosim.power.acpi.ACPIAnalyzer;
import de.hd.pvs.piosim.power.acpi.ACPIDeviceException;
import de.hd.pvs.piosim.power.cluster.ExtendedNode;
import de.hd.pvs.piosim.power.cluster.NodeFactory;
import de.hd.pvs.piosim.power.cluster.PowerSupply;
import de.hd.pvs.piosim.power.cluster.SimpleNode;
import de.hd.pvs.piosim.power.devices.MockDevice;

public class ExtendedNodeTest extends AbstractTestCase {
	
	Logger logger = Logger.getLogger(ExtendedNodeTest.class);
	
	@Test
	public void testWithEfficiencyOne() {
		
		SimpleNode node = NodeFactory.createSimpleNode("simpleNode");
		ExtendedNode extNode = NodeFactory.createExtendedNode("extNode",new BigDecimal("0"));
		Time t = Time.getInstance();
		t.reset();
		
		try {
			node.run();
			extNode.run();
			t.timePassed(40000);
			node.stop();
			node.toSleep();
			extNode.stop();
			extNode.toSleep();
			t.timePassed(100000);
			node.run();
			extNode.run();
			t.timePassed(8000);
			node.stop();
			node.toSuspend();
			extNode.stop();
			extNode.toSuspend();
			t.timePassed(2000);
		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ACPIDeviceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		

		ACPIAnalyzer nodeAnalyzer = new ACPIAnalyzer();
		ACPIAnalyzer extNodeAnalyzer = new ACPIAnalyzer();
		nodeAnalyzer.setIACPIAnalyzable(node);
		extNodeAnalyzer.setIACPIAnalyzable(extNode);
		
		assertAllEquals(nodeAnalyzer, extNodeAnalyzer);
	}
	
	@Test
	public void testWithEfficiencyLowerOne() {
		
		SimpleNode node = NodeFactory.createSimpleNode("simpleNode");
		ExtendedNode extNode = NodeFactory.createExtendedNode("extNode", new BigDecimal("20"));
		
		assertEquals(20.0, extNode.getOverhead().doubleValue());
		
		Time t = Time.getInstance();
		t.reset();
		
		try {
			node.run();
			extNode.run();
			t.timePassed(40000);
			node.stop();
			node.toSleep();
			extNode.stop();
			extNode.toSleep();
			t.timePassed(100000);
			node.run();
			extNode.run();
			t.timePassed(8000);
			node.stop();
			node.toSuspend();
			extNode.stop();
			extNode.toSuspend();
			t.timePassed(2000);
		} catch (ComponentException e) {
			fail(e.getMessage());
		} catch (ACPIDeviceException e) {
			fail(e.getMessage());
		}
		
		assertEquals(true, node.getEnergyConsumption().doubleValue() < extNode.getEnergyConsumption().doubleValue());
	}
		

	public void testNodeOverhead() {
		
		ExtendedNode node = NodeFactory.createEmptyExtendedNode("node", new BigDecimal("120"));
		
		MockDevice mock = createMock();
		
		node.add(mock);
		
		try {
			mock.changeUtilization(new BigDecimal("0"));
			
			Time.getInstance().timePassed(60000); // 1 min
			
			// mock consumption = 0.6 * 60 * 60 / 3600 = 0.6 watt-h
			
			assertEquals(0.6, mock.getEnergyConsumption().doubleValue());
			
			// node consumption = 120 * 60 / 3600 + 0.6 watt-h = 2.6 watt-h
			
			assertEquals(2.6, node.getEnergyConsumption().doubleValue());

		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testNodePowerSupplyWithProcentualOverhead() {
		
		ExtendedNode node = NodeFactory.createEmptyExtendedNode("node");
		
		PowerSupply powerSupply = new PowerSupply();
		
		powerSupply.setProcentualOverhead(new BigDecimal("0.1"));
		
		node.setPowerSupply(powerSupply);
		
		MockDevice mock = createMock();
		
		node.add(mock);
		
		try {
			mock.changeUtilization(new BigDecimal("0"));
			
			Time.getInstance().timePassed(60000); // 1 min
			
			// mock consumption = 0.6 * 60 * 60 / 3600 = 0.6 watt-h
			
			assertEquals(0.6, mock.getEnergyConsumption().doubleValue());
			
			// node consumption = 0.6 * 1.1 = 0.66
			
			assertEquals(0.66, node.getEnergyConsumption().doubleValue());

		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testNodePowerSupplyWithUtilizationBasedOverhead() {
		
		ExtendedNode node = NodeFactory.createEmptyExtendedNode("node");
		
		PowerSupply powerSupply = new PowerSupply();
		
		powerSupply.setMaxEfficiency(new BigDecimal("1"));
		powerSupply.setMinEfficiency(new BigDecimal("0"));
		powerSupply.setMaxPower(new BigDecimal("100"));
		powerSupply.setProcentualOverhead(new BigDecimal("0.1"));
		
		node.setPowerSupply(powerSupply);
		
		MockDevice mock = createMock();
		
		node.add(mock);
		
		try {
			mock.changeUtilization(new BigDecimal("0"));
			
			Time.getInstance().timePassed(60000); // 1 min
			
			// mock consumption = 0.6 * 60 * 60 / 3600 = 0.6 watt-h
			
			assertEquals(0.6, mock.getEnergyConsumption().doubleValue());
			
			// 36 watt => 36 % utilization for the power supply => 64 % overhead => 36 * 1.64 * 60 / 3600 * 1.1 = 0.984 * 1.1 = 1.0824
			
			assertEquals(1.0824, node.getEnergyConsumption().doubleValue());

		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testNodePowerSupplyWithUtilizationBasedAndProcentualOverhead() {
		
		ExtendedNode node = NodeFactory.createEmptyExtendedNode("node");
		
		PowerSupply powerSupply = new PowerSupply();
		
		powerSupply.setMaxEfficiency(new BigDecimal("1"));
		powerSupply.setMinEfficiency(new BigDecimal("0"));
		powerSupply.setMaxPower(new BigDecimal("100"));
		
		node.setPowerSupply(powerSupply);
		
		MockDevice mock = createMock();
		
		node.add(mock);
		
		try {
			mock.changeUtilization(new BigDecimal("0"));
			
			Time.getInstance().timePassed(60000); // 1 min
			
			// mock consumption = 0.6 * 60 * 60 / 3600 = 0.6 watt-h
			
			assertEquals(0.6, mock.getEnergyConsumption().doubleValue());
			
			// 36 watt => 36 % utilization for the power supply => 64 % overhead => 36 * 1.64 * 60 / 3600 = 0.984
			
			assertEquals(0.984, node.getEnergyConsumption().doubleValue());

		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	private MockDevice createMock() {
		MockDevice mock = new MockDevice();
		
		mock.setIdlePowerConsumption(36);
		mock.setLoadPowerConsumption(60);
		
		return mock;
	}

}

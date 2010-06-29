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

import de.hd.pvs.piosim.power.ComponentException;
import de.hd.pvs.piosim.power.Time;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.ACPIDeviceException;
import de.hd.pvs.piosim.power.acpi.IACPIAnalyzable;
import de.hd.pvs.piosim.power.calculation.ACPICalculation;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;
import de.hd.pvs.piosim.power.calculation.CalculationException;
import de.hd.pvs.piosim.power.devices.MockDevice;

public class ACPICalculationTest extends AbstractTestCase {
	
	@Test
	public void testCalculateSumDurationACPIStateChanges() {
		
		List<IACPIAnalyzable> list = new ArrayList<IACPIAnalyzable>();
		
		MockDevice mock1 = new MockDevice();
		list.add(mock1);
		MockDevice mock2 = new MockDevice();
		list.add(mock2);
		MockDevice mock3 = new MockDevice();
		list.add(mock3);
		
		BigDecimal sum = ACPICalculation.calculateSumACPIStateChangesTimeOverhead(list);
		
		assertEquals(0.0, sum.doubleValue());
		
		mock1.setIncStateDuration(0, new BigDecimal("1000"));
		mock1.stop();
		try {
			mock1.toSuspend();
			mock1.run();
		} catch (ACPIDeviceException e) {
			fail(e.getMessage());
		} catch (ComponentException e) {
			fail(e.getMessage());
		}
		Time.getInstance().timePassed(2000);
		
		assertEquals(1000.0, mock1.getACPIStateChangesTimeOverhead().doubleValue());
		
		sum = ACPICalculation.calculateSumACPIStateChangesTimeOverhead(list);
		
		assertEquals(1000.0, sum.doubleValue());
		
		ACPIDevice.deregisterAllDevices();
	}
	
	@Test
	public void testCalculateMinimalStateTimeForEfficiency() {
		
		MockDevice mock = new MockDevice();
		mock.setIdlePowerConsumption(mock.getLoadPowerConsumption());
		
		BigDecimal duration = new BigDecimal("0.0");
		try {
			duration = ACPICalculation.calculateMinimalStateTimeForEfficiency(mock, 0, 3);
			
			fail("Calculation should fail because no energy can be saved");
		} catch (Exception e) {}	
		
		assertEquals(0.0, duration.doubleValue());
		
		mock.setStatePowerConsumption(3, new BigDecimal("1"));
		
		try {
			duration = ACPICalculation.calculateMinimalStateTimeForEfficiency(mock, 0, 3);
			
			fail("Calculation should fail because no energy can be saved");
		} catch (Exception e) {}	
		
		assertEquals(0.0, duration.doubleValue());
		
		mock.setLoadPowerConsumption(10);
		mock.setIdlePowerConsumption(10);
		mock.setStatePowerConsumption(3, new BigDecimal("1"));
		
		try {
			duration = ACPICalculation.calculateMinimalStateTimeForEfficiency(mock, 0, 3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertEquals(0.0, duration.doubleValue());
		
		mock.setIncStatePowerConsumption(0, new BigDecimal("9"));
		
		try {
			duration = ACPICalculation.calculateMinimalStateTimeForEfficiency(mock, 0, 3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// after one hour
		assertEquals(1.0, BaseCalculation.divide(duration, new BigDecimal("3600000")).doubleValue());
		
		mock.setIncStatePowerConsumption(0, new BigDecimal("4.5"));
		mock.setDecStatePowerConsumption(3, new BigDecimal("4.5"));
		
		try {
			duration = ACPICalculation.calculateMinimalStateTimeForEfficiency(mock, 0, 3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// also after one hour
		assertEquals(1.0, BaseCalculation.divide(duration, new BigDecimal("3600000")).doubleValue());
		
		mock.setIncStatePowerConsumption(0, new BigDecimal("0"));
		mock.setDecStatePowerConsumption(3, new BigDecimal("0"));
		
		mock.setIncStateDuration(0, new BigDecimal("3600000"));
		
		try {
			duration = ACPICalculation.calculateMinimalStateTimeForEfficiency(mock, 0, 3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// after one hour
		assertEquals(1.0, BaseCalculation.divide(duration, new BigDecimal("3600000")).doubleValue());
		
		mock.setIncStateDuration(0, new BigDecimal("400000"));
		mock.setIncStateDuration(2, new BigDecimal("600000"));
		mock.setDecStateDuration(1, new BigDecimal("600000"));
		mock.setDecStateDuration(3, new BigDecimal("2000000"));
		
		try {
			duration = ACPICalculation.calculateMinimalStateTimeForEfficiency(mock, 0, 3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// after one hour
		assertEquals(1.0, BaseCalculation.divide(duration, new BigDecimal("3600000")).doubleValue());
		
		mock.setIncStatePowerConsumption(0, new BigDecimal("9"));
		
		try {
			duration = ACPICalculation.calculateMinimalStateTimeForEfficiency(mock, 0, 3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// after two hours
		assertEquals(2.0, BaseCalculation.divide(duration, new BigDecimal("3600000")).doubleValue());		
		
		
	}
	
	@Test
	public void testWithoutEfficientChangePossible() {
		MockDevice mock = new MockDevice();
		
		mock.setLoadPowerConsumption(100);
		mock.setIdlePowerConsumption(80);
		mock.setStatePowerConsumption(3, 85);
		
		try {
			ACPICalculation.calculateMinimalStateTimeForEfficiency(mock, 0, 3);
			
			// should throw and exception because no efficient change possible
			
			fail("Efficient change should not be possible!");
		} catch (CalculationException e) {}
	}

}

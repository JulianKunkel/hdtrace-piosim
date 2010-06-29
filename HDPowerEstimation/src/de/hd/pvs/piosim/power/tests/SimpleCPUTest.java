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

import org.junit.Test;

import de.hd.pvs.piosim.power.ComponentException;
import de.hd.pvs.piosim.power.Time;
import de.hd.pvs.piosim.power.acpi.ACPIAnalyzer;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.ACPIDeviceException;
import de.hd.pvs.piosim.power.calculation.ACPICalculation;
import de.hd.pvs.piosim.power.devices.SimpleCPU;

public class SimpleCPUTest extends AbstractTestCase {

	@Test
	public void testRunWithoutACPI() {

		Time t = Time.getInstance();
		t.reset();

		ACPIDevice cpu = new SimpleCPU();

		// testset: cpu 100 watt
		// cpu run for 40 sec => power consumption = 100 watt / 60 / 60 * 40 =
		// 1.11111111 watt-h
		// cpu stop for 10 sec => power consumption = 100 watt / 60 / 60 * 10 =
		// 0.27777777 watt-h
		// cpu run for 8 sec => power consumption = 100 watt / 60 / 60 * 8 =
		// 0.22222222 watt-h
		// cpu stop for 2 sec => power consumption = 100 watt / 60 / 60 * 2 =
		// 0.05555555 watt-h
		//
		// total power = 1.66666666 watt-h

		try {
			cpu.run();
			t.timePassed(40000);
			cpu.stop();
			t.timePassed(10000);
			cpu.run();
			t.timePassed(8000);
			cpu.stop();
			t.timePassed(2000);
		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		ACPIAnalyzer analyzer = new ACPIAnalyzer();

		analyzer.setIACPIAnalyzable(cpu);

		assertEquals(analyzer.getAbsoluteACPITime().doubleValue(), 60000.0);
		assertEquals(analyzer.getTimePassed(), analyzer.getAbsoluteACPITime());
		assertEquals(analyzer.getPowerConsumption().doubleValue(), ACPICalculation.calculateInWattH(analyzer.getMaxPowerConsumption(),new BigDecimal("60000")).doubleValue());
		assertEquals(analyzer.getACPITimeOverhead().doubleValue(), 0.0);
		assertEquals(0.0,analyzer.getAbsoluteACPIPowerSaving().doubleValue());

		analyzer.printIACPIAnalyzableStatistics();

		ACPIDevice.deregister(cpu);
	}

	@Test
	public void testRunWithACPI() {

		Time t = Time.getInstance();
		t.reset();

		ACPIDevice cpu = new SimpleCPU();

		// testset: @see testRunWithoutACPI()
		// difference: acpi modes sleep and suspend are used
		try {
			cpu.run();
			t.timePassed(40000);
			cpu.stop();
			cpu.toSleep();
			t.timePassed(10000);
			cpu.toWork();
			cpu.run();
			t.timePassed(8000);
			cpu.stop();
			cpu.toSuspend();
			t.timePassed(2000);
		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ACPIDeviceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		ACPIAnalyzer analyzer = new ACPIAnalyzer();

		analyzer.setIACPIAnalyzable(cpu);

		assertEquals(60015.0, analyzer.getAbsoluteACPITime().doubleValue());
		assertTrue(analyzer.getTimePassed().doubleValue() < analyzer
				.getAbsoluteACPITime().doubleValue());
		assertTrue(analyzer.getPowerConsumption().doubleValue() < analyzer
				.getMaxPowerConsumption().doubleValue());
		assertEquals(15.0, analyzer.getACPITimeOverhead().doubleValue());
		assertTrue(analyzer.getAbsoluteACPIPowerSaving().doubleValue() > 0);

		analyzer.printIACPIAnalyzableStatistics();

		ACPIDevice.deregister(cpu);
	}

	@Test
	public void testRunWithFoxyACPI() {

		Time t = Time.getInstance();
		t.reset();

		ACPIDevice cpu = new SimpleCPU();

		try {
			cpu.run();
			t.timePassed(40000);
			cpu.stop();
			cpu.toSleep();
			t.timePassed(9985);
			cpu.toWork();
			t.timePassed(15);
			cpu.run();
			t.timePassed(8000);
			cpu.stop();
			cpu.toSuspend();
			t.timePassed(2000);
		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ACPIDeviceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		ACPIAnalyzer analyzer = new ACPIAnalyzer();

		analyzer.setIACPIAnalyzable(cpu);

		assertEquals(analyzer.getAbsoluteACPITime().doubleValue(), 60000.0);
		assertEquals(analyzer.getTimePassed().doubleValue(), analyzer
				.getAbsoluteACPITime().doubleValue());
		assertEquals(analyzer.getACPITimeOverhead().doubleValue(), 0.0);
		assertTrue(analyzer.getPowerConsumption().doubleValue() < analyzer
				.getMaxPowerConsumption().doubleValue());
		assertTrue(analyzer.getAbsoluteACPIPowerSaving().doubleValue() > 0);

		analyzer.printIACPIAnalyzableStatistics();

		ACPIDevice.deregister(cpu);
	}

}

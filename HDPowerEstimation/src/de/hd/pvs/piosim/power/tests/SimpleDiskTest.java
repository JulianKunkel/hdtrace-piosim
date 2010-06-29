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

import org.junit.Test;

import de.hd.pvs.piosim.power.ComponentException;
import de.hd.pvs.piosim.power.Time;
import de.hd.pvs.piosim.power.acpi.ACPIAnalyzer;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.ACPIDeviceException;
import de.hd.pvs.piosim.power.calculation.ACPICalculation;
import de.hd.pvs.piosim.power.devices.SimpleDisk;


public class SimpleDiskTest extends AbstractTestCase {
	
	@Test
	public void testRunWithoutACPI() {

		Time t = Time.getInstance();
		t.reset();

		ACPIDevice disk = new SimpleDisk();

		// testset: disk 100 watt/h
		// disk run for 40 sec  => power consumption = 80 watt / 60 / 60 * 40 = 0.88888888 watt
		// disk stop for 10 sec => power consumption = 80 watt / 60 / 60 * 10 = 0.22222222 watt
		// disk run for 8 sec   => power consumption = 80 watt / 60 / 60 * 8  = 0.17777777 watt
		// disk stop for 2 sec  => power consumption = 80 watt / 60 / 60 * 2  = 0.04444444 watt
		//
		// total power = 1.33333333 watt in 60 sec
		
		try {
			disk.run();
			t.timePassed(40000);
			disk.stop();
			t.timePassed(10000);
			disk.run();
			t.timePassed(8000);
			disk.stop();
			t.timePassed(2000);
		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
		ACPIAnalyzer analyzer = new ACPIAnalyzer();

		analyzer.setIACPIAnalyzable(disk);
		
		assertEquals(60000.0, analyzer.getAbsoluteACPITime().doubleValue());
		assertEquals(analyzer.getTimePassed(),analyzer.getAbsoluteACPITime());
		assertEquals(ACPICalculation.calculateInWattH(analyzer.getMaxPowerConsumption(),Time.getInstance().getCurrentTimeInMillis()), analyzer.getPowerConsumption());
		assertEquals(0.0, analyzer.getACPITimeOverhead().doubleValue());
		assertEquals(0.0, analyzer.getAbsoluteACPIPowerSaving().doubleValue());

		
		analyzer.printIACPIAnalyzableStatistics();
		
		ACPIDevice.deregister(disk);
	}
	
	@Test
	public void testRunWithACPI() {

		Time t = Time.getInstance();
		t.reset();

		ACPIDevice disk = new SimpleDisk();
		
		// testset: @see testRunWithoutACPI()
		// difference: acpi modes sleep and suspend are used
		try {
			disk.run();
			t.timePassed(40000);
			disk.stop();
			disk.toSleep(); // let disk sleep
			t.timePassed(10000);
			disk.toWork(); // let disk wake up, but here is not wakeuptime before calculating -> time overhead
			disk.run();
			t.timePassed(8000);
			disk.stop();
			disk.toSuspend();
			t.timePassed(2000);
		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ACPIDeviceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
		ACPIAnalyzer analyzer = new ACPIAnalyzer();

		analyzer.setIACPIAnalyzable(disk);
		
		assertEquals(analyzer.getAbsoluteACPITime().doubleValue(),62220.0);
		assertTrue(analyzer.getTimePassed().doubleValue() < analyzer.getAbsoluteACPITime().doubleValue());
		assertTrue(analyzer.getPowerConsumption().doubleValue() < analyzer.getMaxPowerConsumption().doubleValue());
		assertEquals(analyzer.getACPITimeOverhead().doubleValue(), 2220.0);
		assertTrue(analyzer.getAbsoluteACPIPowerSaving().doubleValue() > 0);	
		
		analyzer.printIACPIAnalyzableStatistics();
		
		ACPIDevice.deregister(disk);
	}
	
	@Test
	public void testRunWithFoxyACPI() {

		Time t = Time.getInstance();
		t.reset();

		ACPIDevice disk = new SimpleDisk();

		try {
			disk.run();
			t.timePassed(40000);
			disk.stop();
			disk.toSleep();
			t.timePassed(7780);
			disk.toWork(); // let disk wake up before calculating
			t.timePassed(2220);
			disk.run();
			t.timePassed(8000);
			disk.stop();
			disk.toSuspend();
			t.timePassed(2000);
		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ACPIDeviceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		
		ACPIAnalyzer analyzer = new ACPIAnalyzer();

		analyzer.setIACPIAnalyzable(disk);
		
		assertEquals(analyzer.getAbsoluteACPITime().doubleValue(),60000.0);
		assertEquals(analyzer.getTimePassed().doubleValue(),analyzer.getAbsoluteACPITime().doubleValue());
		assertEquals(analyzer.getACPITimeOverhead().doubleValue(), 0.0);
		assertTrue(analyzer.getPowerConsumption().doubleValue() < analyzer.getMaxPowerConsumption().doubleValue());
		assertTrue(analyzer.getAbsoluteACPIPowerSaving().doubleValue() > 0);

		analyzer.printIACPIAnalyzableStatistics();
		
		ACPIDevice.deregister(disk);
	}

}

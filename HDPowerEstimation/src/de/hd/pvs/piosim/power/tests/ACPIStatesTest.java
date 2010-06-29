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
import de.hd.pvs.piosim.power.acpi.ACPIComponentException;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.ACPIDeviceException;
import de.hd.pvs.piosim.power.devices.MockDevice;

public class ACPIStatesTest extends AbstractTestCase {
	private MockDevice mock;
	private Time time;
	private ACPIAnalyzer analyzer;

	private void doIncJob() {
		
		time.reset();

		try {
			mock.run();
			time.timePassed(3600000);
			mock.stop();
			mock.toSuspend(); // ACPI state 2
			time.timePassed(7200000);
			mock.toWork(); // ACPI state 0
		} catch (ACPIDeviceException e) {
			fail(e.getMessage());
		} catch (ComponentException e) {
			fail(e.getMessage());
		}
	}

	private void doDecJob() {

		time.reset();

		mock.stop();
		try {
			mock.toSuspend(); // ACPI state 2
			time.timePassed(3600000);
			mock.run(); // ACPI state 0;
			time.timePassed(7200000);
		} catch (ComponentException e) {
			fail(e.getMessage());
		} catch (ACPIDeviceException e) {
			fail(e.getMessage());
		}

	}

	private void doJobWithUtilization() {
		time.reset();

		// 1h acpi 0, 100%; 1h acpi 0, 70%; change to acpi 1,2; 1h acpi 2, 70%;
		// 1h acpi 2, 40%; change to acpi 1,0; 1h acpi 0, 40%

		try {
			mock.changeUtilization(new BigDecimal("1.0"));
			mock.run(); // ACPI state 0, utilization 100 %
			time.timePassed(3600000);
			mock.changeUtilization(new BigDecimal("0.7")); // ACPI state 0,
			// utilization 70 %
			time.timePassed(3600000);
			mock.stop();
			mock.toSuspend(); // ACPI state 2, utilization 70 %
			time.timePassed(3600000);
			mock.changeUtilization(new BigDecimal("0.4")); // ACPI state 2,
			// utilization 40 %
			time.timePassed(3600000);
			mock.toWork(); // ACPI state 0, utilization 40 %
			mock.run();
			time.timePassed(3600000);

		} catch (ACPIComponentException e) {
			fail(e.getMessage());
		} catch (ComponentException e) {
			fail(e.getMessage());
		} catch (ACPIDeviceException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testIncACPIState() {

		mock = new MockDevice();
		time = Time.getInstance();
		analyzer = new ACPIAnalyzer();
		analyzer.setIACPIAnalyzable(mock);

		mock.setStatePowerConsumption(0, new BigDecimal("100"));
		mock.setStatePowerConsumption(1, new BigDecimal("10000000"));
		mock.setStatePowerConsumption(2, new BigDecimal("10"));
		mock.setStatePowerConsumption(3, new BigDecimal("10000000"));

		// 1h acpi 0; change to acpi 1,2; 2h acpi 2
		// 100 + 0 + 0 +20
		doIncJob();

		assertEquals(120.0, analyzer.getPowerConsumption().doubleValue());

		ACPIDevice.deregister(mock);

		mock = new MockDevice();
		analyzer.setIACPIAnalyzable(mock);

		mock.setStatePowerConsumption(0, new BigDecimal("100"));
		mock.setStatePowerConsumption(1, new BigDecimal("10000000"));
		mock.setStatePowerConsumption(2, new BigDecimal("10"));
		mock.setStatePowerConsumption(3, new BigDecimal("10000000"));
		mock.setIncStateDuration(0, new BigDecimal("3600000"));

		// 1h acpi 0, 1h change to acpi 1; change to acpi 2; 1h acpi 2
		// 100 + 0 + 0 + 10
		doIncJob();

		assertEquals(110.0, analyzer.getPowerConsumption().doubleValue());

		ACPIDevice.deregister(mock);

		mock = new MockDevice();
		analyzer.setIACPIAnalyzable(mock);

		mock.setStatePowerConsumption(0, new BigDecimal("100"));
		mock.setStatePowerConsumption(1, new BigDecimal("10000000"));
		mock.setStatePowerConsumption(2, new BigDecimal("10"));
		mock.setStatePowerConsumption(3, new BigDecimal("10000000"));
		mock.setIncStateDuration(0, new BigDecimal("3600000"));
		mock.setIncStatePowerConsumption(0, new BigDecimal("1"));

		// 1h acpi 0, 1h change to acpi 1; change to acpi 2; 1h acpi 2
		// 100 + 1 + 0 + 10
		doIncJob();

		assertEquals(111.0, analyzer.getPowerConsumption().doubleValue());

		ACPIDevice.deregister(mock);

		mock = new MockDevice();
		analyzer.setIACPIAnalyzable(mock);

		mock.setStatePowerConsumption(0, new BigDecimal("100"));
		mock.setStatePowerConsumption(1, new BigDecimal("10000000"));
		mock.setStatePowerConsumption(2, new BigDecimal("10"));
		mock.setStatePowerConsumption(3, new BigDecimal("10000000"));
		mock.setIncStateDuration(0, new BigDecimal("3600000"));
		mock.setIncStatePowerConsumption(0, new BigDecimal("1"));
		mock.setIncStatePowerConsumption(1, new BigDecimal("5"));

		// 1h acpi 0, 1h change to acpi 1; change to acpi 2; 1h acpi 2
		// 100 + 1 + 5 + 10
		doIncJob();

		assertEquals(116.0, analyzer.getPowerConsumption().doubleValue());

		ACPIDevice.deregister(mock);

		mock = new MockDevice();
		analyzer.setIACPIAnalyzable(mock);

		mock.setStatePowerConsumption(0, new BigDecimal("100"));
		mock.setStatePowerConsumption(1, new BigDecimal("10000000"));
		mock.setStatePowerConsumption(2, new BigDecimal("10"));
		mock.setStatePowerConsumption(3, new BigDecimal("10000000"));
		mock.setIncStateDuration(0, new BigDecimal("3600000"));
		mock.setIncStateDuration(1, new BigDecimal("3600000"));
		mock.setIncStatePowerConsumption(0, new BigDecimal("1"));
		mock.setIncStatePowerConsumption(1, new BigDecimal("5"));

		// 1h acpi 0, 1h change to acpi 1; 1h change to acpi 2; 0h acpi 2
		// 100 + 1 + 5 + 0
		doIncJob();

		assertEquals(106.0, analyzer.getPowerConsumption().doubleValue());

		ACPIDevice.deregister(mock);
	}

	@Test
	public void testDecACPIState() {

		mock = new MockDevice();
		time = Time.getInstance();
		analyzer = new ACPIAnalyzer();
		analyzer.setIACPIAnalyzable(mock);

		mock.setStatePowerConsumption(0, new BigDecimal("100"));
		mock.setStatePowerConsumption(1, new BigDecimal("10000000"));
		mock.setStatePowerConsumption(2, new BigDecimal("10"));
		mock.setStatePowerConsumption(3, new BigDecimal("10000000"));

		// change to acpi 1,2; 1h acpi 2; change to acpi 1,0; 2h acpi 0
		// 0 + 10 + 0 + 200
		doDecJob();

		assertEquals(210.0, analyzer.getPowerConsumption().doubleValue());

		ACPIDevice.deregister(mock);

		mock = new MockDevice();
		analyzer.setIACPIAnalyzable(mock);

		mock.setStatePowerConsumption(0, new BigDecimal("100"));
		mock.setStatePowerConsumption(1, new BigDecimal("10000000"));
		mock.setStatePowerConsumption(2, new BigDecimal("10"));
		mock.setStatePowerConsumption(3, new BigDecimal("10000000"));
		mock.setDecStateDuration(1, new BigDecimal("3600000"));

		// change to acpi 1,2; 1h acpi 2; 1h change to acpi 1; change to acpi 0;
		// 1h acpi 0
		// 0 + 10 + 0 + 0 + 100
		doDecJob();

		assertEquals(110.0, analyzer.getPowerConsumption().doubleValue());

		ACPIDevice.deregister(mock);

		mock = new MockDevice();
		analyzer.setIACPIAnalyzable(mock);

		mock.setStatePowerConsumption(0, new BigDecimal("100"));
		mock.setStatePowerConsumption(1, new BigDecimal("10000000"));
		mock.setStatePowerConsumption(2, new BigDecimal("10"));
		mock.setStatePowerConsumption(3, new BigDecimal("10000000"));
		mock.setDecStateDuration(1, new BigDecimal("3600000"));
		mock.setDecStatePowerConsumption(1, new BigDecimal("1"));

		// change to acpi 1,2; 1h acpi 2; 1h change to acpi 1; change to acpi 0;
		// 1h acpi 0
		// 0 + 10 + 0 + 1 + 100
		doDecJob();

		assertEquals(111.0, analyzer.getPowerConsumption().doubleValue());

		ACPIDevice.deregister(mock);

		mock = new MockDevice();
		analyzer.setIACPIAnalyzable(mock);

		mock.setStatePowerConsumption(0, new BigDecimal("100"));
		mock.setStatePowerConsumption(1, new BigDecimal("10000000"));
		mock.setStatePowerConsumption(2, new BigDecimal("10"));
		mock.setStatePowerConsumption(3, new BigDecimal("10000000"));
		mock.setDecStateDuration(1, new BigDecimal("3600000"));
		mock.setDecStatePowerConsumption(1, new BigDecimal("1"));
		mock.setDecStatePowerConsumption(2, new BigDecimal("5"));

		// change to acpi 1,2; 1h acpi 2; 1h change to acpi 1; change to acpi 0;
		// 1h acpi 0
		// 0 + 10 + 5 + 1 + 100
		doDecJob();

		assertEquals(116.0, analyzer.getPowerConsumption().doubleValue());

		ACPIDevice.deregister(mock);

		mock = new MockDevice();
		analyzer.setIACPIAnalyzable(mock);

		mock.setStatePowerConsumption(0, new BigDecimal("100"));
		mock.setStatePowerConsumption(1, new BigDecimal("10000000"));
		mock.setStatePowerConsumption(2, new BigDecimal("10"));
		mock.setStatePowerConsumption(3, new BigDecimal("10000000"));
		mock.setDecStateDuration(1, new BigDecimal("3600000"));
		mock.setDecStateDuration(2, new BigDecimal("3600000"));
		mock.setDecStatePowerConsumption(1, new BigDecimal("1"));
		mock.setDecStatePowerConsumption(2, new BigDecimal("5"));

		// change to acpi 1,2; 1h acpi 2; 1h change to acpi 1; 1h change to acpi
		// 0; 0h acpi 0
		// 0 + 10 + 5 + 1 + 0
		doDecJob();

		assertEquals(16.0, analyzer.getPowerConsumption().doubleValue());

		ACPIDevice.deregister(mock);
	}

	@Test
	public void testACPIStatesWithUtilization() {
		mock = new MockDevice();
		time = Time.getInstance();
		analyzer = new ACPIAnalyzer();
		analyzer.setIACPIAnalyzable(mock);

		mock.setStatePowerConsumption(0, new BigDecimal("100"));
		mock.setStatePowerConsumption(1, new BigDecimal("10000000"));
		mock.setStatePowerConsumption(2, new BigDecimal("10"));
		mock.setStatePowerConsumption(3, new BigDecimal("10000000"));

		// 1h acpi 0, 100%; 1h acpi 0, 70%; change to acpi 1,2; 1h acpi 2, 70%;
		// 1h acpi 2, 40%; change to acpi 1,0; 1h acpi 0, 40%
		// 100 + 70 + 0 + 10 + 10 + 0 + 40
		doJobWithUtilization();

		assertEquals(230.0, analyzer.getPowerConsumption().doubleValue());

		ACPIDevice.deregister(mock);

		mock = new MockDevice();
		time = Time.getInstance();
		analyzer = new ACPIAnalyzer();
		analyzer.setIACPIAnalyzable(mock);

		mock.setStatePowerConsumption(0, 100);
		mock.setStatePowerConsumption(1, 10000000);
		mock.setStatePowerConsumption(2, 10);
		mock.setStatePowerConsumption(3, 10000000);

		mock.setIdlePowerConsumption(80);

		// 1h acpi 0, 100%; 1h acpi 0, 70%; change to acpi 1,2; 1h acpi 2, 70%;
		// 1h acpi 2, 40%; change to acpi 1,0; 1h acpi 0, 40%
		// 100 + 94 + 0 + 10 + 10 + 0 + 88
		doJobWithUtilization();

		assertEquals(302.0, analyzer.getPowerConsumption().doubleValue());

		ACPIDevice.deregister(mock);

		mock = new MockDevice();
		time = Time.getInstance();
		analyzer = new ACPIAnalyzer();
		analyzer.setIACPIAnalyzable(mock);

		mock.setStatePowerConsumption(0, new BigDecimal("100"));
		mock.setStatePowerConsumption(1, new BigDecimal("10000000"));
		mock.setStatePowerConsumption(2, new BigDecimal("10"));
		mock.setStatePowerConsumption(3, new BigDecimal("10000000"));
		mock.setIncStateDuration(0, new BigDecimal("3600000"));

		// 1h acpi 0, 100%; 1h acpi 0, 70%; 1h change to acpi 1,2; 0h acpi 2,
		// 70%; 1h acpi 2, 40%; change to acpi 1,0; 1h acpi 0, 40%
		// 100 + 70 + 0 + 0 + 10 + 0 + 40
		doJobWithUtilization();

		assertEquals(220.0, analyzer.getPowerConsumption().doubleValue());

		ACPIDevice.deregister(mock);
	}

	@Test
	public void testScheduledChangesWithNothingToDo() {

		mock = new MockDevice();
		time = Time.getInstance();
		analyzer = new ACPIAnalyzer();
		analyzer.setIACPIAnalyzable(mock);

		time.reset();

		mock.setStatePowerConsumption(0, new BigDecimal("0"));
		mock.setStatePowerConsumption(1, new BigDecimal("0"));
		mock.setIncStatePowerConsumption(0, new BigDecimal("100"));

		try {
			mock.run();
			time.timePassed(1000); // power consumption = 0 watt
			mock.stop();
			mock.toSuspend(); // power consumption = 100 watt
			time.timePassed(1000);
			mock.toSuspend(); // power consumption = 100 watt
			time.timePassed(1000);
			mock.toWork();
			mock.run();
		} catch (ComponentException e) {
			fail(e.getMessage());
		} catch (ACPIDeviceException e) {
			fail(e.getMessage());
		}

		assertEquals(new BigDecimal("100"), analyzer.getPowerConsumption());

		time.reset();

		assertEquals(new BigDecimal("0"), time.getCurrentTimeInMillis());

		mock.setDecStateDuration(1, new BigDecimal("100"));

		try {
			mock.run();
			time.timePassed(1000); // time = 1000 ms
			mock.stop();
			mock.toSuspend();
			time.timePassed(1000); // time = 2000 ms
			mock.toSuspend();
			mock.toWork(); // acpi overhead = 100
			mock.run();
			time.timePassed(100); // time = 2100 ms
			mock.toWork();
			time.timePassed(1000); // time = 3100 ms
		} catch (ComponentException e) {
			fail(e.getMessage());
		} catch (ACPIDeviceException e) {
			fail(e.getMessage());
		}

		assertEquals(new BigDecimal("100"), analyzer.getACPITimeOverhead());

	}
}

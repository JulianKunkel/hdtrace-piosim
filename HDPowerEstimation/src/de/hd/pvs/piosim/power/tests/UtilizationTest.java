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
import de.hd.pvs.piosim.power.devices.MockDevice;

public class UtilizationTest extends AbstractTestCase {

	private MockDevice mock;
	private Time time;
	private ACPIAnalyzer analyzer;

	@Test
	public void testUtilization() {
		mock = new MockDevice();
		time = Time.getInstance();
		time.reset();
		analyzer = new ACPIAnalyzer();
		analyzer.setIACPIAnalyzable(mock);

		// optimal power efficiency

		mock.setStatePowerConsumption(0, new BigDecimal("100"));
		mock.setStatePowerConsumption(1, new BigDecimal("10000000"));
		mock.setStatePowerConsumption(2, new BigDecimal("10000000"));
		mock.setStatePowerConsumption(3, new BigDecimal("10000000"));

		BigDecimal powerConsumption = new BigDecimal("0");
		for (int i = 100; i >= 0; i--) {
			changeUtilizationJob(new BigDecimal(i)
					.divide(new BigDecimal("100")));
			BigDecimal utilizationPowerConsumption = powerConsumption.subtract(
					analyzer.getPowerConsumption()).negate();
			powerConsumption = powerConsumption
					.add(utilizationPowerConsumption);
			assertEquals(new Double(i), utilizationPowerConsumption
					.doubleValue());
		}

		ACPIDevice.deregister(mock);

		// real power efficiency

		mock = new MockDevice();
		time = Time.getInstance();
		time.reset();
		analyzer = new ACPIAnalyzer();
		analyzer.setIACPIAnalyzable(mock);

		mock.setStatePowerConsumption(1, new BigDecimal("10000000"));
		mock.setStatePowerConsumption(2, new BigDecimal("10000000"));
		mock.setStatePowerConsumption(3, new BigDecimal("10000000"));

		mock.setIdlePowerConsumption(80);
		mock.setLoadPowerConsumption(100);

		powerConsumption = new BigDecimal("0.0");

		for (int i = 100; i >= 0; i--) {
			changeUtilizationJob(new BigDecimal(i)
					.divide(new BigDecimal("100")));
			BigDecimal utilizationPowerConsumption = powerConsumption.subtract(
					analyzer.getPowerConsumption()).negate();
			powerConsumption = powerConsumption
					.add(utilizationPowerConsumption);
			BigDecimal expected = new BigDecimal("0.2").multiply(
					new BigDecimal(i).divide(new BigDecimal("100"))).add(
					new BigDecimal("0.8")).multiply(new BigDecimal("100"));
			assertEquals(expected.doubleValue(), utilizationPowerConsumption
					.doubleValue());
		}

		ACPIDevice.deregister(mock);

	}

	private void changeUtilizationJob(BigDecimal utilization) {
		try {
			mock.run();
			mock.changeUtilization(utilization);
		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		time.timePassed(3600000);

	}
}

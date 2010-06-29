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

import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.devices.MockDevice;

public class MockDeviceTest extends AbstractTestCase {

	@Test
	public void testMock() {
		MockDevice mock = new MockDevice();
		
		for(int i=0; i<mock.getComponentPowerSchema().getCountStates(); ++i) {
			assertEquals(0.0, mock.getComponentPowerSchema().getDecStateDuration()[i].doubleValue());
			assertEquals(0.0, mock.getComponentPowerSchema().getDecStatePowerConsumption()[i].doubleValue());
			assertEquals(0.0, mock.getComponentPowerSchema().getIncStateDuration()[i].doubleValue());
			assertEquals(0.0, mock.getComponentPowerSchema().getIncStatePowerConsumption()[i].doubleValue());
			assertEquals(0.0, mock.getComponentPowerSchema().getStatePowerConsumption()[i].doubleValue());
		}
		
		mock.setStatePowerConsumption(3, new BigDecimal("10"));
		
		assertEquals(10.0, mock.getComponentPowerSchema().getStatePowerConsumption(3).doubleValue());
		
		ACPIDevice.deregister(mock);
		mock = new MockDevice();
		
		for(int i=0; i<mock.getComponentPowerSchema().getCountStates(); ++i) {
			assertEquals(0.0, mock.getComponentPowerSchema().getDecStateDuration()[i].doubleValue());
			assertEquals(0.0, mock.getComponentPowerSchema().getDecStatePowerConsumption()[i].doubleValue());
			assertEquals(0.0, mock.getComponentPowerSchema().getIncStateDuration()[i].doubleValue());
			assertEquals(0.0, mock.getComponentPowerSchema().getIncStatePowerConsumption()[i].doubleValue());
			assertEquals(0.0, mock.getComponentPowerSchema().getStatePowerConsumption()[i].doubleValue());
		}
		
		ACPIDevice.deregister(mock);

	}
}

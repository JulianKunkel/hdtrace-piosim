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

import de.hd.pvs.piosim.power.Time;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.ACPIDeviceException;
import de.hd.pvs.piosim.power.acpi.DevicePowerStates;
import de.hd.pvs.piosim.power.devices.SimpleMemory;

public class SimpleMemoryTest extends AbstractTestCase {
	
	@Test
	public void testMemoryBanks() {
		
		Time t = Time.getInstance();
		t.reset();
		
		SimpleMemory ram = new SimpleMemory();
		
		assertEquals(DevicePowerStates.DEVICE_POWER_STATE_0, ram.getDevicePowerState());
		
		try {
			ram.toSleep();
		} catch (ACPIDeviceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		t.timePassed(10000);
		ram.refresh();
				
		assertEquals(DevicePowerStates.DEVICE_POWER_STATE_3, ram.getDevicePowerState());
		
		ACPIDevice.deregister(ram);
	}
}

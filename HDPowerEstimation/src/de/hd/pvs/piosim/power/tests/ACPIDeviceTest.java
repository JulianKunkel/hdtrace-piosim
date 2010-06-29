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
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.ACPIDeviceException;
import de.hd.pvs.piosim.power.acpi.DevicePowerStates;
import de.hd.pvs.piosim.power.devices.SimpleCPU;
import de.hd.pvs.piosim.power.devices.SimpleDisk;

public class ACPIDeviceTest extends AbstractTestCase {
	
	@Test
	public void testRegistering() {
		
		assertEquals(0, ACPIDevice.getDevices().size());
		
		ACPIDevice dev1 = new SimpleCPU();
		
		assertEquals(dev1,ACPIDevice.getDevices().get(0));
		
		ACPIDevice.deregister(dev1);
		
		assertEquals(ACPIDevice.getDevices().size(), 0);
		
		ACPIDevice dev2 = new SimpleDisk();
		ACPIDevice dev3 = new SimpleDisk();
		ACPIDevice dev4 = new SimpleCPU();
		
		assertEquals(ACPIDevice.getDevices().size(), 3);
		
		ACPIDevice.deregister(dev3);
		
		assertEquals(ACPIDevice.getDevices().size(), 2);
		
		for(ACPIDevice device : ACPIDevice.getDevices()) {
			assertTrue(device.equals(dev2) || device.equals(dev4));
		}
		
		ACPIDevice.deregister(dev2);
		
		assertEquals(ACPIDevice.getDevices().size(), 1);
		
		assertFalse(ACPIDevice.deregister(dev2));
		
		assertTrue(ACPIDevice.deregister(dev4));
		
		assertEquals(ACPIDevice.getDevices().size(), 0);
		
		dev2 = new SimpleDisk();
		dev3 = new SimpleDisk();
		dev4 = new SimpleCPU();
		
		assertEquals(ACPIDevice.getDevices().size(), 3);
		
		ACPIDevice.deregisterAllDevices();
		
		assertEquals(ACPIDevice.getDevices().size(), 0);
					
	}
	
	@Test
	public void testDeviceStates() {
		ACPIDevice disk = new SimpleDisk();
		
		assertFalse(disk.isRunning());
		
		try {
			disk.run();
		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		assertTrue(disk.isRunning());
		
		disk.stop();
		
		assertFalse(disk.isRunning());
	}
	
	@Test
	public void testDeviceACPIStates() {
		
		Time t = Time.getInstance();
		t.reset();
		ACPIDevice disk = new SimpleDisk();
		
		assertFalse(disk.isRunning());
		
		assertEquals(disk.getDevicePowerState(), DevicePowerStates.DEVICE_POWER_STATE_0);
		
		try {
			disk.toSleep();
		} catch (ACPIDeviceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		t.timePassed(10000);
		
		disk.refresh();
		
		assertEquals(disk.getDevicePowerState(), DevicePowerStates.DEVICE_POWER_STATE_3);
		
		try {
			disk.toSuspend();
		} catch (ACPIDeviceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		t.timePassed(10000);
		
		disk.refresh();
		
		assertEquals(disk.getDevicePowerState(), DevicePowerStates.DEVICE_POWER_STATE_2);
		
		try {
			disk.toWork();
		} catch (ACPIDeviceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		t.timePassed(10000);
		
		disk.refresh();
		
		assertEquals(disk.getDevicePowerState(), DevicePowerStates.DEVICE_POWER_STATE_0);
		
		try {
			disk.toSuspend();
		} catch (ACPIDeviceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		t.timePassed(10000);
		
		disk.refresh();
		
		assertEquals(disk.getDevicePowerState(), DevicePowerStates.DEVICE_POWER_STATE_2);
		
		try {
			disk.run();
		} catch (ComponentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		t.timePassed(10000);
		
		disk.refresh();
		
		assertEquals(disk.getDevicePowerState(), DevicePowerStates.DEVICE_POWER_STATE_0);
		
		try {
			disk.toSleep();
			assertTrue(false);
		} catch (ACPIDeviceException e) {
			assertTrue(true);
		}
		
		t.timePassed(10000);
		
		disk.refresh();
		
		assertEquals(disk.getDevicePowerState(), DevicePowerStates.DEVICE_POWER_STATE_0);
		
		try {
			disk.toSuspend();
			assertTrue(false);
		} catch (ACPIDeviceException e) {
			assertTrue(true);
		}
		
		t.timePassed(10000);
		
		disk.refresh();
		
		assertEquals(disk.getDevicePowerState(), DevicePowerStates.DEVICE_POWER_STATE_0);		
		
	}

}

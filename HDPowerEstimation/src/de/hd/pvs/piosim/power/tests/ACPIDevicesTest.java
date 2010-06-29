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
import de.hd.pvs.piosim.power.devices.SimpleCPU;
import de.hd.pvs.piosim.power.devices.SimpleDisk;
import de.hd.pvs.piosim.power.devices.SimpleMemory;
import de.hd.pvs.piosim.power.devices.SimpleNIC;
import de.hd.pvs.piosim.power.devices.SimpleVGA;

public class ACPIDevicesTest extends AbstractTestCase {
	
	@Test
	public void testWithAllDevicesWithoutACPI() {
		Time t = Time.getInstance();
		t.reset();

		@SuppressWarnings("unused")
		ACPIDevice disk = new SimpleDisk();
		@SuppressWarnings("unused")
		ACPIDevice cpu = new SimpleCPU();
		@SuppressWarnings("unused")
		ACPIDevice vga = new SimpleVGA();
		@SuppressWarnings("unused")
		ACPIDevice ram = new SimpleMemory();
		@SuppressWarnings("unused")
		ACPIDevice nic = new SimpleNIC();

		for(ACPIDevice device : ACPIDevice.getDevices()) {
			try {
				device.run();
			} catch (ComponentException e) {
				fail(e.getMessage());
			}
		}
		t.timePassed(40000);
		for(ACPIDevice device : ACPIDevice.getDevices()) {
			device.stop();
		}
		t.timePassed(100000);
		for(ACPIDevice device : ACPIDevice.getDevices()) {
			try {
				device.run();
			} catch (ComponentException e) {
				fail(e.getMessage());
			}
		}
		t.timePassed(8000);
		for(ACPIDevice device : ACPIDevice.getDevices()) {
			device.stop();
		}
		t.timePassed(2000);
		
		ACPIAnalyzer analyzer = new ACPIAnalyzer();
		
		analyzer.printIACPIAnalyzableStatistics();	
		
		for(ACPIDevice device : ACPIDevice.getDevices()) {
			analyzer.setIACPIAnalyzable(device);
			analyzer.printIACPIAnalyzableStatistics();
		}
		
		ACPIDevice.deregisterAllDevices();
		
	}
	
	@Test
	public void testWithAllDevicesAndACPI() {
		
		Time t = Time.getInstance();
		t.reset();

		@SuppressWarnings("unused")
		ACPIDevice disk = new SimpleDisk();
		@SuppressWarnings("unused")
		ACPIDevice cpu = new SimpleCPU();
		@SuppressWarnings("unused")
		ACPIDevice vga = new SimpleVGA();
		@SuppressWarnings("unused")
		ACPIDevice ram = new SimpleMemory();
		@SuppressWarnings("unused")
		ACPIDevice nic = new SimpleNIC();

		for(ACPIDevice device : ACPIDevice.getDevices()) {
			try {
				device.run();
			} catch (ComponentException e) {
				fail(e.getMessage());
			}
		}
		t.timePassed(40000);
		for(ACPIDevice device : ACPIDevice.getDevices()) {
			device.stop();
			try {
				device.toSleep();
			} catch (ACPIDeviceException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		t.timePassed(100000);
		for(ACPIDevice device : ACPIDevice.getDevices()) {
			try {
				device.run();
			} catch (ComponentException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		t.timePassed(8000);
		for(ACPIDevice device : ACPIDevice.getDevices()) {
			device.stop();
			try {
				device.toSuspend();
			} catch (ACPIDeviceException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		t.timePassed(2000);
		
		ACPIAnalyzer analyzer = new ACPIAnalyzer();
		
		analyzer.printIACPIAnalyzableStatistics();	
		
		for(ACPIDevice device : ACPIDevice.getDevices()) {
			analyzer.setIACPIAnalyzable(device);
			analyzer.printIACPIAnalyzableStatistics();
		}
		
		ACPIDevice.deregisterAllDevices();
		
	}
}

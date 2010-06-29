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
package de.hd.pvs.piosim.power;

import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.cluster.BuildException;

/**
 * Class to dynamically create a new device by its class name
 * @author Timo Minartz
 *
 */
public class DeviceBuilder {
	
	public final static String DEVICE_PATH = "de.hd.pvs.piosim.power.devices."; 
	
	/**
	 * creates a new class instance depending on <code>name</code> and <code>DEVICE_PATH</code>
	 * @param name Class name of device to create
	 * @return corresponding device
	 * @throws BuildException if building failed
	 */
	public static ACPIDevice createACPIDevice(String name) throws BuildException {
		Class<?> clazz;
		ACPIDevice device = null;
		try {
			clazz = Class.forName(DEVICE_PATH + name);
			device = (ACPIDevice) clazz.newInstance();
		} catch (ClassNotFoundException e) {
			throw new BuildException("Class not found: " + DEVICE_PATH + name, e.getStackTrace());
		} catch (InstantiationException e) {
			throw new BuildException("Can not instantiate class: " + DEVICE_PATH + name, e.getStackTrace());
		} catch (IllegalAccessException e) {
			throw new BuildException("Do not have access to class: " + DEVICE_PATH + name, e.getStackTrace());
		}
			
		return device;
		
	}
}

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
package de.hd.pvs.piosim.power.acpi;

public class ACPIDeviceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4646994438795486454L;

	public ACPIDeviceException(String message, StackTraceElement[] stackTrace) {
		super(message);
		this.setStackTrace(stackTrace);
	}

	public ACPIDeviceException(String message) {
		super(message);
	}

}

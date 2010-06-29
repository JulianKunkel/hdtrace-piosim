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
package de.hd.pvs.piosim.power.devices;

import java.math.BigDecimal;

import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.ConvertingException;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;


public abstract class MemoryDevice extends ACPIDevice {

	protected BigDecimal memorySize;
	protected int countBanks;
	
	
	public BigDecimal getMemorySize() {
		return memorySize;
	}

	public int getCountBanks() {
		return countBanks;
	}
	
	@Override
	public BigDecimal convertToPercentualUtilization(double value, String unit)
			throws ConvertingException {
		BigDecimal proc = new BigDecimal("0");
		if (unit.equals("MB")) {
			proc = BaseCalculation.divide(
					new BigDecimal(value),memorySize);
		} else if(unit.equals("B")) {
			proc = BaseCalculation.divide(
					new BigDecimal(value),BaseCalculation.toByte(memorySize));
		} else {
			throw new ConvertingException("Undefined unit: " + unit);
		}
		
		return proc;
	}
	
}

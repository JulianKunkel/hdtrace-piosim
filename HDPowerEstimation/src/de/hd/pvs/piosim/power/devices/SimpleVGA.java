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

import de.hd.pvs.piosim.power.InvalidValueException;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.ConvertingException;

public class SimpleVGA extends ACPIDevice {

	@Override
	public void initComponentPowerConsumption() {
		BigDecimal[] statePowerConsumption = {new BigDecimal("150"), new BigDecimal("130"), new BigDecimal("60"), new BigDecimal("10")};
		BigDecimal[] decStateDuration = {new BigDecimal("0"), new BigDecimal("50"), new BigDecimal("50"), new BigDecimal("50")};
		BigDecimal[] decStateEnergyConsumption = {new BigDecimal("0"), new BigDecimal("0.1"), new BigDecimal("0.1"), new BigDecimal("0.1")};
		BigDecimal[] incStateDuration = {new BigDecimal("50"), new BigDecimal("50"), new BigDecimal("50"), new BigDecimal("0")};
		BigDecimal[] incStateEnergyConsumption = {new BigDecimal("0.1"), new BigDecimal("0.1"), new BigDecimal("0.1"), new BigDecimal("0")};
		
		BigDecimal idlePowerConsumption = new BigDecimal("120");
		BigDecimal loadPowerConsumption = new BigDecimal("150");

		try {
			this.getComponentPowerSchema().setStatePowerConsumption(statePowerConsumption);
			this.getComponentPowerSchema().setDecStateDuration(decStateDuration);
			this.getComponentPowerSchema().setDecStateEnergyConsumption(decStateEnergyConsumption);
			this.getComponentPowerSchema().setIncStateDuration(incStateDuration);
			this.getComponentPowerSchema().setIncStateEnergyConsumption(incStateEnergyConsumption);
			this.getComponentPowerSchema().setLoadPowerConsumption(loadPowerConsumption);
			this.getComponentPowerSchema().setIdlePowerConsumption(idlePowerConsumption);
		} catch (InvalidValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public BigDecimal convertToPercentualUtilization(double value, String unit)
			throws ConvertingException {
		// TODO Auto-generated method stub
		return null;
	}

}

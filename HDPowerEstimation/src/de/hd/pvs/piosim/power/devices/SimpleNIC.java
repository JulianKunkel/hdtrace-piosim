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
import de.hd.pvs.piosim.power.acpi.DevicePowerStates;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;

public class SimpleNIC extends ACPIDevice {

	private BigDecimal maxSpeed = new BigDecimal("1000");

	@Override
	public void initComponentPowerConsumption() {
		BigDecimal[] statePowerConsumption = {new BigDecimal("50"),new BigDecimal("0"), new BigDecimal("10"), new BigDecimal("0.5")};
		BigDecimal[] decStateDuration = {new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("5"), new BigDecimal("15")};
		BigDecimal[] decStateEnergyConsumption = {new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0.05"), new BigDecimal("0.15")};
		BigDecimal[] incStateDuration = {new BigDecimal("15"), new BigDecimal("0"), new BigDecimal("15"), new BigDecimal("0")};
		BigDecimal[] incStateEnergyConsumption = {new BigDecimal("0.05"), new BigDecimal("0"), new BigDecimal("0.15"), new BigDecimal("0")};
		
		BigDecimal idlePowerConsumption = new BigDecimal("40");
		BigDecimal loadPowerConsumption = new BigDecimal("50");

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
	
	/*
	 * Use acpi level
	 * 0 = full working = 1000 MBit
	 * 1 = working = 100 MBit
	 * 2 = low working = 10 MBit
	 * 3 = sleep
	 */
	
	public int getSpeedForDevicePowerState(int state) {
		this.refresh();
		switch(state) {
		case DevicePowerStates.DEVICE_POWER_STATE_0: return 1000;
		case DevicePowerStates.DEVICE_POWER_STATE_1: return 100;
		case DevicePowerStates.DEVICE_POWER_STATE_2: return 10;
		case DevicePowerStates.DEVICE_POWER_STATE_3: return 0;
		default: return 0;
		}
	}

	@Override
	public BigDecimal convertToPercentualUtilization(double value, String unit) throws ConvertingException {
		if(unit.equals("B")) {
			return BaseCalculation.multiply(BaseCalculation.divide(new BigDecimal(value),maxSpeed),BaseCalculation.HUNDRED);
		}
		
		throw new ConvertingException("Undefined unit: " + unit);
	}

}

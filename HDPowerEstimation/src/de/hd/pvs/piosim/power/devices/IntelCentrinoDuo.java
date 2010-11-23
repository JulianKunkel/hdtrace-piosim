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
import de.hd.pvs.piosim.power.calculation.BaseCalculation;

public class IntelCentrinoDuo extends ACPIDevice {
	
	

	@Override
	public void initComponentPowerConsumption() {

		/* Values for Intel Centrino Core Duo 1.8 Ghz */
		
		/*
		 * from datasheet
		 * 
		 * TDP: 34 Watt
		 * HFM: 53.3 Watt
		 * Sleep: 12.4 Watt
		 * Deeper Sleep: 7.6 Watt
		 * Intel Enhanced Deeper Sleep: 1.2 Watt
		 */
		BigDecimal[] statePowerConsumption = { new BigDecimal("53.3"),
				new BigDecimal("12.4"), new BigDecimal("7.6"),
				new BigDecimal("1.2") };
		
		/* TDP / maxPowerConsumption = 34 Watt / 53.3 Watt ~= 64%*/
		BigDecimal loadPowerConsumption = new BigDecimal("53.3");
		BigDecimal idlePowerConsumption = new BigDecimal("34");
		
		
		/*
		 * from /proc/acpi/processor/CPU0/power
		 * 
		 * C0: (unused)
		 * C1: 1 microsecond to C0
		 * C2: 1 microsecond to C0
		 * C3: 17 microsecond to C0
		 */
		BigDecimal[] incStateDuration = { new BigDecimal("0"),
				new BigDecimal("0.000001"), new BigDecimal("0"), new BigDecimal("0.000016") };
		
		/*
		 * use incStateDuration[i]*statePowerConsumption[i-1]
		 */
		BigDecimal[] incStateEnergyConsumption = { new BigDecimal("0"),
				new BigDecimal("0.0000533"), new BigDecimal("0"), new BigDecimal("0.0001216") };
		
		/*
		 * use incState values because no source
		 */
		BigDecimal[] decStateDuration = new BigDecimal[incStateDuration.length];
		System.arraycopy(incStateDuration, 0, decStateDuration, 0, incStateDuration.length);
		BigDecimal[] decStateEnergyConsumption = new BigDecimal[incStateEnergyConsumption.length];
		System.arraycopy(incStateEnergyConsumption, 0, decStateEnergyConsumption, 0, incStateEnergyConsumption.length);

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
		if(unit.equals("%"))
			return BaseCalculation.divide(new BigDecimal(value),BaseCalculation.HUNDRED);
		
		throw new ConvertingException("Unknown unit: " + unit);
	}

}

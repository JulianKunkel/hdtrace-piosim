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
package de.hd.pvs.piosim.power.devices.pvscluster;

import java.math.BigDecimal;

import org.apache.log4j.Logger;

import de.hd.pvs.piosim.power.InvalidValueException;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.ConvertingException;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;

public class NIC extends ACPIDevice {
	
	private Logger logger = Logger.getLogger(NIC.class);
	
	// maxSpeed in MByte / sec
	private BigDecimal maxSpeed = new BigDecimal("128"); // = 1 GBit

	@Override
	public void initComponentPowerConsumption() {
		BigDecimal[] statePowerConsumption = { new BigDecimal("2"),
				new BigDecimal("0"), new BigDecimal("0"),
				new BigDecimal("0.2") };
		BigDecimal[] decStateDuration = { new BigDecimal("0"),
				new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0.1") };
		BigDecimal[] decStateEnergyConsumption = { new BigDecimal("0"),
				new BigDecimal("0"), new BigDecimal("0"),
				new BigDecimal("0.001") };
		BigDecimal[] incStateDuration = { new BigDecimal("0.1"),
				new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0") };
		BigDecimal[] incStateEnergyConsumption = { new BigDecimal("0.000001"),
				new BigDecimal("0"), new BigDecimal("0"),
				new BigDecimal("0") };
		
		BigDecimal idlePowerConsumption = new BigDecimal("0.78");

		try {
			this.getComponentPowerSchema().setStatePowerConsumption(
					statePowerConsumption);
			this.getComponentPowerSchema()
					.setDecStateDuration(decStateDuration);
			this.getComponentPowerSchema().setDecStateEnergyConsumption(
					decStateEnergyConsumption);
			this.getComponentPowerSchema()
					.setIncStateDuration(incStateDuration);
			this.getComponentPowerSchema().setIncStateEnergyConsumption(
					incStateEnergyConsumption);
			this.getComponentPowerSchema().setIdlePowerConsumption(idlePowerConsumption);
		} catch (InvalidValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public BigDecimal convertToPercentualUtilization(double value, String unit)
			throws ConvertingException {
		if (unit.equals("B")) {
			BigDecimal percent = BaseCalculation.divide(
					convertToMB(value),maxSpeed);
			
			if(percent.compareTo(BigDecimal.ONE) > 0) {
				logger.info("Workaround: " + percent + " > 1!! Using percent = 1");
				percent = BigDecimal.ONE;
			}
				
			
			return percent;
		}

		throw new ConvertingException("Undefined unit: " + unit);
	}
	
	private BigDecimal convertToMB(double byteValue) {
		return BaseCalculation.toMegaByte(new BigDecimal(byteValue));
	}

}

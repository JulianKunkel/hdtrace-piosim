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

package de.hd.pvs.piosim.power.devices.eeclust;

import java.math.BigDecimal;

import de.hd.pvs.piosim.power.InvalidValueException;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.ConvertingException;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;

/**
 * @author Timo Minartz
 *
 */
public class Disk extends ACPIDevice {
	
	// throughput in bytes/sec
	private BigDecimal throughput = new BigDecimal("200000000");

	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.power.acpi.ACPIDevice#initComponentPowerConsumption()
	 */
	@Override
	public void initComponentPowerConsumption() {
		
		BigDecimal[] statePowerConsumption = {new BigDecimal("80"), new BigDecimal("60"), new BigDecimal("10"), new BigDecimal("0.1")};
		BigDecimal[] decStateDuration = {new BigDecimal("0"), new BigDecimal("20"), new BigDecimal("200"), new BigDecimal("2000")};
		BigDecimal[] decStatePowerConsumption = {new BigDecimal("0"), new BigDecimal("0.01"), new BigDecimal("0.02"), new BigDecimal("0.03")};
		BigDecimal[] incStateDuration = {new BigDecimal("10"), new BigDecimal("100"), new BigDecimal("1000"), new BigDecimal("0")};
		BigDecimal[] incStatePowerConsumption = {new BigDecimal("0.05"), new BigDecimal("0.015"), new BigDecimal("0.025"), new BigDecimal("0")};
		
		BigDecimal idlePowerConsumption = new BigDecimal("64");
		BigDecimal loadPowerConsumption = new BigDecimal("80");

		try {
			this.getComponentPowerSchema().setStatePowerConsumption(statePowerConsumption);
			this.getComponentPowerSchema().setDecStateDuration(decStateDuration);
			this.getComponentPowerSchema().setDecStatePowerConsumption(decStatePowerConsumption);
			this.getComponentPowerSchema().setIncStateDuration(incStateDuration);
			this.getComponentPowerSchema().setIncStatePowerConsumption(incStatePowerConsumption);
			this.getComponentPowerSchema().setLoadPowerConsumption(loadPowerConsumption);
			this.getComponentPowerSchema().setIdlePowerConsumption(idlePowerConsumption);
		} catch (InvalidValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see de.hd.pvs.piosim.power.acpi.ACPIDevice#convertToPercentualUtilization(double, java.lang.String)
	 */
	@Override
	public BigDecimal convertToPercentualUtilization(double value, String unit)
			throws ConvertingException {
		if(unit.equals("%"))
			return BaseCalculation.divide(new BigDecimal(value),BaseCalculation.HUNDRED);
		else if(unit.equals("B"))
			return BaseCalculation.divide(
					new BigDecimal(value),throughput);
	
		throw new ConvertingException("Unknown unit: " + unit);
	}

}

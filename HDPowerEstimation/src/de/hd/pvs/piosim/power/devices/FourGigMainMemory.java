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

public class FourGigMainMemory extends MemoryDevice {

	@Override
	public void initComponentPowerConsumption() {
		
		memorySize = new BigDecimal("4096");
		countBanks = 2;

		BigDecimal[] statePowerConsumption = {new BigDecimal("40"), new BigDecimal("20"), new BigDecimal("0"), new BigDecimal("0")};
		BigDecimal[] decStateDuration = {new BigDecimal("0"),new BigDecimal("5"),new BigDecimal("5"),new BigDecimal("0")};
		BigDecimal[] decStatePowerConsumption = {new BigDecimal("0"),new BigDecimal("0.01"),new BigDecimal("0.01"),new BigDecimal("0")};
		BigDecimal[] incStateDuration = {new BigDecimal("5"),new BigDecimal("5"),new BigDecimal("0"),new BigDecimal("0")};
		BigDecimal[] incStatePowerConsumption = {new BigDecimal("0.01"), new BigDecimal("0.01"), new BigDecimal("0"), new BigDecimal("0")};
		
		BigDecimal idlePowerConsumption = new BigDecimal("32");
		BigDecimal loadPowerConsumption = new BigDecimal("40");

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
		
		this.getACPIComponent().setStateName(0, "TWO_BANKS_ACTIVE");
		this.getACPIComponent().setStateName(1, "ONE_BANK_ACTIVE");
		this.getACPIComponent().setStateName(2, "ZERO_BANKS_ACTIVE");
		this.getACPIComponent().setStateName(3, "ZERO_BANKS_ACTIVE");
	}	

}

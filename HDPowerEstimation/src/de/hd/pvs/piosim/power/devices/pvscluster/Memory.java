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

import de.hd.pvs.piosim.power.InvalidValueException;
import de.hd.pvs.piosim.power.devices.MemoryDevice;

public class Memory extends MemoryDevice {
	
	@Override
	public void initComponentPowerConsumption() {
		
		memorySize = new BigDecimal("1024");
		countBanks = 2;

		BigDecimal[] statePowerConsumption = {new BigDecimal("13.585"), new BigDecimal("7.5"), new BigDecimal("5"), new BigDecimal("2.5")};
		BigDecimal[] decStateDuration = {new BigDecimal("0"),new BigDecimal("0"),new BigDecimal("0"),new BigDecimal("0.006")};
		BigDecimal[] decStateEnergyConsumption = {new BigDecimal("0"),new BigDecimal("0"),new BigDecimal("0"),new BigDecimal("0.000000011")};
		BigDecimal[] incStateDuration = {new BigDecimal("0.006"),new BigDecimal("0"),new BigDecimal("0"),new BigDecimal("0")};
		BigDecimal[] incStateEnergyConsumption = {new BigDecimal("0.000000011"), new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0")};
		BigDecimal idlePowerConsumption = new BigDecimal("11");
		
		
		try {
			this.getComponentPowerSchema().setStatePowerConsumption(statePowerConsumption);
			this.getComponentPowerSchema().setDecStateDuration(decStateDuration);
			this.getComponentPowerSchema().setDecStateEnergyConsumption(decStateEnergyConsumption);
			this.getComponentPowerSchema().setIncStateDuration(incStateDuration);
			this.getComponentPowerSchema().setIncStateEnergyConsumption(incStateEnergyConsumption);
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

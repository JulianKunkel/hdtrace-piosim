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
package de.hd.pvs.piosim.power.data;

import java.math.BigDecimal;

public class DeviceData {

	private ComponentData componentData = new ComponentData();
	
	// total energy consumption for this device in watt-h
	private BigDecimal energyConsumption = new BigDecimal("0");
	
	public DeviceData() {
	}

	public DeviceData(DeviceData deviceData) {
		BigDecimal[] utilization = new BigDecimal[deviceData.getUtilization().length];
		System.arraycopy(deviceData.getUtilization(), 0, utilization, 0, utilization.length);
		BigDecimal[] powerConsumption = new BigDecimal[deviceData.getConsumption().length];
		System.arraycopy(deviceData.getConsumption(), 0, powerConsumption, 0, powerConsumption.length);
		
		this.setConsumption(powerConsumption);
		this.setUtilization(utilization);
	}

	public void setUtilization(BigDecimal[] utilization) {
		componentData.setUtilization(utilization);
	}

	public void addUtiliziation(BigDecimal stepUtilization) {
		componentData.addUtilization(stepUtilization);
	}

	public BigDecimal[] getUtilization() {
		return componentData.getUtilization();
	}

	public BigDecimal getUtiliziation(int step) {
		return componentData.getUtilization(step);
	}

	public BigDecimal getEnergyConsumption() {
		return energyConsumption;
	}

	public void setConsumption(BigDecimal[] powerConsumption) {
		componentData.setPowerConsumption(powerConsumption);
	}

	public BigDecimal[] getConsumption() {
		return componentData.getPowerConsumption();
	}

	public BigDecimal getConsumption(int step) {
		return componentData.getPowerConsumption(step);
	}

	public void addConsumption(BigDecimal stepPowerConsumption) {
		componentData.addPowerConsumption(stepPowerConsumption);
	}

	public int getCountValues() {
		return componentData.getSize();
	}

	public void setEnergyConsumption(BigDecimal energyConsumption) {
		this.energyConsumption = energyConsumption;
	}

}

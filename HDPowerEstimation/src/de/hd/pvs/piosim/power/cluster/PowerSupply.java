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
package de.hd.pvs.piosim.power.cluster;

import java.math.BigDecimal;

import de.hd.pvs.piosim.power.calculation.ACPICalculation;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;

public class PowerSupply {
	
	private BigDecimal procentualOverhead = null;
	private BigDecimal minEfficiency = null;
	private BigDecimal maxEfficiency = null;
	private BigDecimal maxPower = null;

	public BigDecimal getProcentualOverhead() {
		return procentualOverhead;
	}

	public void setProcentualOverhead(BigDecimal procentualOverhead) {
		this.procentualOverhead = procentualOverhead;
	}

	public BigDecimal getMinEfficiency() {
		return minEfficiency;
	}

	public void setMinEfficiency(BigDecimal minEfficiency) {
		this.minEfficiency = minEfficiency;
	}

	public BigDecimal getMaxEfficiency() {
		return maxEfficiency;
	}

	public void setMaxEfficiency(BigDecimal maxEfficiency) {
		this.maxEfficiency = maxEfficiency;
	}

	public BigDecimal getMaxPower() {
		return maxPower;
	}

	public void setMaxPower(BigDecimal maxPower) {
		this.maxPower = maxPower;
	}

	/**
	 * 
	 * @param currentPowerConsumption
	 * @param lastChangeTime
	 * @param time
	 * @return energy consumption in watt-h
	 */
	public BigDecimal getEnergyConsumption(BigDecimal currentPowerConsumption,
			BigDecimal lastChangeTime, BigDecimal time) {
		
		BigDecimal powerConsumption = currentPowerConsumption;
		
		BigDecimal duration = BaseCalculation.substract(time, lastChangeTime);
		
		if(minEfficiency != null && maxEfficiency != null && maxPower != null) {
			BigDecimal utilization = BaseCalculation.divide(currentPowerConsumption, maxPower);
			BigDecimal efficiency = BaseCalculation.substract(BaseCalculation.ONE, BaseCalculation.interpolatePowerEfficiency(maxEfficiency, minEfficiency, utilization));
			powerConsumption = BaseCalculation.multiply(currentPowerConsumption, BaseCalculation.sum(efficiency, BaseCalculation.ONE));
		} 
		
		BigDecimal energyConsumption = ACPICalculation.calculateInWattH(powerConsumption,duration);
	
		if(procentualOverhead != null) {
			energyConsumption = BaseCalculation.multiply(energyConsumption, BaseCalculation.sum(procentualOverhead, BaseCalculation.ONE));
		}	
		
		return energyConsumption;
	}

}

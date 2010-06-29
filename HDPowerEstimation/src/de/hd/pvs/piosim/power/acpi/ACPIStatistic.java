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
package de.hd.pvs.piosim.power.acpi;

import java.math.BigDecimal;

import de.hd.pvs.piosim.power.calculation.BaseCalculation;

public class ACPIStatistic {
	
	// contains all duration times for all states 
	private BigDecimal[] acpiStateTimes = new BigDecimal[DevicePowerStates.DEVICE_POWER_STATE_COUNT];
	private int[] acpiStateCount = new int[DevicePowerStates.DEVICE_POWER_STATE_COUNT];
	
	private BigDecimal acpiStateChangeTimes = new BigDecimal("0");
	
	// global time from last state change
	private BigDecimal lastACPIChangeTime = new BigDecimal("0");
	
	public ACPIStatistic() {
		for(int i=0; i<acpiStateTimes.length; ++i) {
			acpiStateTimes[i] = new BigDecimal("0");
			acpiStateCount[i] = 0;
		}
	}

	public void addChangeTime(BigDecimal durationForDevicePowerStateChange) {
		acpiStateChangeTimes = BaseCalculation.sum(acpiStateChangeTimes, durationForDevicePowerStateChange);		
	}

	public void addStateTime(int state,
			BigDecimal stateTime) {
		acpiStateTimes[state] = BaseCalculation.sum(acpiStateTimes[state], stateTime);	
		acpiStateCount[state] = acpiStateCount[state] + 1;
	}

	public void setLastChangeTime(BigDecimal lastACPIChangeTime) {
		this.lastACPIChangeTime = lastACPIChangeTime;		
	}

	public BigDecimal getLastChangeTime() {
		return lastACPIChangeTime;		
	}

	public BigDecimal getDurationStateTimes() {
		return acpiStateChangeTimes;
	}
	
	public BigDecimal getAcpiStateTimes(int state) {
		return acpiStateTimes[state];
	}
	
	public int getAcpiStateCount(int state) {
		return acpiStateCount[state];
	}

}

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

import de.hd.pvs.piosim.power.acpi.ACPIComponent;
import de.hd.pvs.piosim.power.acpi.DevicePowerStates;
import de.hd.pvs.piosim.power.acpi.history.ACPIStateChangesHistory;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;

public class StatisticComponentData {
	
	private int countACPIchanges = 0;
	private ACPIComponent component;
	private BigDecimal[] stateDuration;
	private int currentState;
	
	private BigDecimal lastChangeTime = new BigDecimal("0");
	
	public StatisticComponentData(ACPIComponent component) {
		
		this.component = component;
		
		stateDuration = new BigDecimal[DevicePowerStates.DEVICE_POWER_STATE_COUNT + 1];
		for(int i=0; i<stateDuration.length; ++i) 
			stateDuration[i] = new BigDecimal("0");
		
		currentState = DevicePowerStates.DEVICE_POWER_STATE_0;
	}
	
	public void startState(BigDecimal time, int state) {
		lastChangeTime = time;
		
		if(state == ACPIStateChangesHistory.STATE_CHANGE)
			state = DevicePowerStates.DEVICE_POWER_STATE_COUNT;
		
		currentState = state;
		
		if(state == DevicePowerStates.DEVICE_POWER_STATE_3)
			countACPIchanges++;
	}
	
	public void endState(BigDecimal time) {
		if(time == null)
			System.out.println("time");
		else if(lastChangeTime == null)
			System.out.println("lastChangeTime");
		
		BigDecimal duration = BaseCalculation.substract(time, lastChangeTime);
		stateDuration[currentState] = BaseCalculation.sum(stateDuration[currentState], duration);
	}
	
	public int getCountACPIChanges() {
		return countACPIchanges;
	}
	
	public ACPIComponent getACPIComponent() {
		return component;
	}
	
	public void print() {
		System.out.print("changes: " + countACPIchanges + " [");
		for(int i=0; i<stateDuration.length; ++i) {
			System.out.print(" " + stateDuration[i]);
		}
		System.out.println(" ]");
	}

}

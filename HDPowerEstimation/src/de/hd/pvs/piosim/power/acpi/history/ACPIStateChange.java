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
package de.hd.pvs.piosim.power.acpi.history;

import java.math.BigDecimal;

import de.hd.pvs.piosim.power.acpi.ACPIComponent;
import de.hd.pvs.piosim.power.data.StatisticData;

public class ACPIStateChange {

	private BigDecimal time;
	private int state;
	private ACPIComponent component;
	private BigDecimal powerConsumption;
	
	
	public BigDecimal getTime() {
		return time;
	}

	public int getState() {
		return state;
	}

	public ACPIComponent getACPIComponent() {
		return component;
	}
	
	public BigDecimal getPowerConsumption() {
		return powerConsumption;
	}

	public ACPIStateChange(BigDecimal time, ACPIComponent component, int state, BigDecimal powerConsumption) {
		this.time = time;
		this.state = state;
		this.component = component;
		this.powerConsumption = powerConsumption;
		
		if(state == ACPIStateChangesHistory.STATE_END) {
			StatisticData.getInstance().endState(time, component);
		} else  {
			StatisticData.getInstance().startState(time, state, component);
		} 
	}

	public void print() {
		if(state == ACPIStateChangesHistory.STATE_END) {
			System.out.println(time + ": " + component.getName() + " State finished. powerConsumption: " + powerConsumption);
		} else if(state == ACPIStateChangesHistory.STATE_CHANGE) {
			System.out.println(time + ": " + component.getName() + " new StateChange. powerConsumption: " + powerConsumption);
		} else {
			System.out.println(time + ": " + component.getName() + " new State " + state + ". powerConsumption: " + powerConsumption);
		}
	}


}

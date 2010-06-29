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

import de.hd.pvs.piosim.power.ComponentException;
import de.hd.pvs.piosim.power.ComponentPowerSchema;
import de.hd.pvs.piosim.power.cluster.Node;

public abstract class ACPIComponentFacade implements IACPIAnalyzable {
	
	private ACPIComponent component;
	
	protected ACPIComponentFacade() {
		component = new ACPIComponent();
	}
	
	//TODO: change to protected here would be fine ;)
	public ACPIComponent getACPIComponent() {
		return component;
	}
 	
	public BigDecimal getDurationForDevicePowerStateChange(int fromState,
			int toState) {
		return component.getDurationForDevicePowerStateChange(fromState, toState);
	}
	
	/**
	 * 
	 * @param toState
	 * @return power consumption for change in watt-h
	 */
	protected BigDecimal getPowerConsumptionForDevicePowerStateChange(int toState) {
		return component.getPowerConsumptionForDevicePowerStateChange(toState);
	}

	public BigDecimal getPowerConsumptionForDevicePowerStateChange(
			int fromState, int toState) {
		return component.getPowerConsumptionForDevicePowerStateChange(fromState, toState);
	}

	public ComponentPowerSchema getComponentPowerSchema() {
		return component.getComponentPowerSchema();
	}

	public Node getNode() {
		return component.getNode();
	}

	public void setNode(Node node) {
		component.setNode(node);
	}

	public String getName() {
		return component.getName();
	}

	public void reset() {
		component.reset();
	}

	public void setName(String name) {
		component.setName(name);
	}

	public void changeUtilization(BigDecimal utilization) throws ComponentException {
		component.changeUtilization(utilization);		
	}

	public int getDevicePowerState() {
		return component.getDevicePowerState();
	}

	public BigDecimal getDurationForDevicePowerStateChange(int state) {
		return component.getDurationForDevicePowerStateChange(state);
	}
	
	protected BigDecimal getLastChangeTime() {
		return component.getLastChangeTime();
	}
	
	protected BigDecimal getCurrentPowerConsumption() {
		return component.getCurrentPowerConsumption();
	}
		
	public void finalizeComponent() {
		component.finalizeComponent();
	}
	
	/* inherited from interface IACPIAnalyzable */
	
	@Override
	public BigDecimal getPowerConsumption() {
		return component.getPowerConsumption();
	}

	/**
	 * Almost every device state change needs a specified
	 * time (see class ComponentPowerSchema). This value
	 * contains the aggregate sum for all device state changing
	 * durations in ms
	 * 
	 * @return the aggregate sum of all state changing times in ms
	 */
	@Override
	public BigDecimal getACPIStateChangesTime() {
		return component.getDurationACPIStateChangeTimes();
	}

	@Override
	public abstract BigDecimal getACPIStateChangesTimeOverhead();

	@Override
	public abstract BigDecimal getMaxPowerConsumption();

	@Override
	public abstract void refresh();
}

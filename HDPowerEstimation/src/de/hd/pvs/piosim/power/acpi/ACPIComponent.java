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

import de.hd.pvs.piosim.power.Component;
import de.hd.pvs.piosim.power.ComponentPowerSchema;
import de.hd.pvs.piosim.power.Time;
import de.hd.pvs.piosim.power.acpi.history.ACPIStateChangesHistory;
import de.hd.pvs.piosim.power.calculation.ACPICalculation;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;

public class ACPIComponent extends Component implements DevicePowerStates {

	private ACPIStateChangesHistory history = ACPIStateChangesHistory.getInstance();
	private String[] acpiStateDescription = new String[DEVICE_POWER_STATE_COUNT];
	private ACPIStatistic statistic = new ACPIStatistic();
	
	// current acpi state
	private int currentACPIDevicePowerState = 0; // working mode

	public ACPIComponent() {
		componentPowerSchema = new ComponentPowerSchema(DEVICE_POWER_STATE_COUNT);
		history.add(Time.getInstance().getCurrentTimeInMillis(), this, currentACPIDevicePowerState, componentEnergyConsumption);
		for(int i=0; i<acpiStateDescription.length; ++i) {
			acpiStateDescription[i] = "DEVICE_POWER_STATE_" + i;
		}
	}

	public ACPIStateChangesHistory getACPIStateChangesHistory() {
		return history;
	}
	
	public void endDevicePowerState(BigDecimal endTime) {
		// use later time from both changing times
		BigDecimal lastChangeTime = getLastChangeTime();

		// add energy consumption for the last period
		componentEnergyConsumption = ACPICalculation.sumEnergyConsumptionTillNow(componentEnergyConsumption, getCurrentPowerConsumption(), endTime, lastChangeTime);

		// end state for last period 
		history.add(endTime, this,ACPIStateChangesHistory.STATE_END, componentEnergyConsumption);
		
		// add time for last period to statistic
		statistic.addStateTime(currentACPIDevicePowerState, BaseCalculation.substract(endTime, lastChangeTime));
		
		// start state for the change 
		history.add(endTime, this,ACPIStateChangesHistory.STATE_CHANGE, componentEnergyConsumption);
		
		// remember changing times
		setLastACPIChangeTime(setLastUtilizationChangeTime(endTime));
		
		if(this.getNode() != null)
			this.getNode().updateEnergyConsumption(endTime);

	}
	
	public void startDevicePowerState(int state, BigDecimal startTime) throws ACPIComponentException {
		
		if (state >= DEVICE_POWER_STATE_COUNT) {
			throw new ACPIComponentException("Invalid device state: " + state);
		}
		
		// add time for this change to statistic
		statistic.addChangeTime(getDurationForDevicePowerStateChange(state));
		
		// add energy consumption for the change
		componentEnergyConsumption = BaseCalculation.sum(componentEnergyConsumption, getEnergyConsumptionForDevicePowerStateChange(state));
		
		// update the acpi state
		currentACPIDevicePowerState = state;
		
		// end state for the change
		history.add(startTime, this,ACPIStateChangesHistory.STATE_END, componentEnergyConsumption);
		
		// start state for the next period
		history.add(startTime,this,currentACPIDevicePowerState, componentEnergyConsumption);
		
		// remember changing times
		setLastACPIChangeTime(setLastUtilizationChangeTime(startTime));
		
		if(this.getNode() != null)
			this.getNode().updateEnergyConsumption(startTime);

	}
	
	public BigDecimal getLastChangeTime() {
		return getLastUtilizationChangeTime().max(getLastACPIChangeTime());
	}

	/**
	 * Returns energy consumption in watt
	 * 
	 * @return energy consumption for this component in watt-h
	 */
	public BigDecimal getEnergyConsumption() {	
		return componentEnergyConsumption;
	}
	
	public void finalizeComponent() {
		BigDecimal lastChangeTime = getLastUtilizationChangeTime().max(getLastACPIChangeTime());

		componentEnergyConsumption = ACPICalculation.sumEnergyConsumptionTillNow(componentEnergyConsumption, getCurrentPowerConsumption(), Time.getInstance().getCurrentTimeInMillis(), lastChangeTime);
		
		history.add(Time.getInstance().getCurrentTimeInMillis(), this,ACPIStateChangesHistory.STATE_END, componentEnergyConsumption);
		
		setLastACPIChangeTime(setLastUtilizationChangeTime(Time.getInstance().getCurrentTimeInMillis()));
	}

	/**
	 * Return time needed for the acpi state change based on the components
	 * power schema. Calls <code>getDurationForACPIStateChange(int fromState, int toState)</code> with
	 * fromState = actualState
	 * 
	 * @param toState
	 *            acpi state to switch to
	 * @return time needed for the state change
	 */
	public BigDecimal getDurationForDevicePowerStateChange(int toState) {
		return getDurationForDevicePowerStateChange(this.currentACPIDevicePowerState, toState);
	}
	
	/**
	 * Return time needed for the acpi state change based on the components
	 * power schema. 
	 * 
	 * @param fromState acpi state to start from
	 * @param toState
	 *            acpi state to switch to
	 * @return time needed for the state change in ms
	 */
	@Override
	public BigDecimal getDurationForDevicePowerStateChange(int fromState, int toState) {
		BigDecimal duration = new BigDecimal("0");

		if (toState == fromState)
			return duration;

		if (toState < fromState) {
			// decrement state
			for (int i = fromState; i > toState; i--) {
				duration = BaseCalculation.sum(duration,componentPowerSchema
						.getDecStateDuration()[i]);
			}
		} else {
			// increment state
			for (int i = fromState; i < toState; i++) {
				duration = BaseCalculation.sum(duration,componentPowerSchema
						.getIncStateDuration()[i]);
			}
		}

		return duration;
	}
	
	/**
	 * Return energy consumption for the acpi state change based on the components
	 * power schema. Calls <code>getEnergyConsumptionForACPIStateChange(int fromState, int toState)</code> with
	 * fromState = actualState
	 * 
	 * @param toState
	 *            acpi state to switch to
	 * @return energy consumption for the state change in watt-h
	 */
	public BigDecimal getEnergyConsumptionForDevicePowerStateChange(int toState) {
		return getEnergyConsumptionForDevicePowerStateChange(this.currentACPIDevicePowerState, toState);
	}
	
	@Override
	public BigDecimal getEnergyConsumptionForDevicePowerStateChange(int fromState, int toState) {
		BigDecimal energyConsumption = new BigDecimal("0");

		if (toState == fromState)
			return energyConsumption;

		if (toState < fromState) {
			// decrement state
			for (int i = fromState; i > toState; i--) {
				energyConsumption = BaseCalculation.sum(energyConsumption,componentPowerSchema
						.getDecStateEnergyConsumption()[i]);
			}
		} else {
			// increment state
			for (int i = fromState; i < toState; i++) {
				energyConsumption = BaseCalculation.sum(energyConsumption,componentPowerSchema
						.getIncStateEnergyConsumption()[i]);
			}
		}

		return energyConsumption;
	}

	/**
	 * @return current acpi state of this component
	 */
	@Override
	public int getDevicePowerState() {
		return currentACPIDevicePowerState;
	}
	
	/**
	 * Returns components power consumption in the actual acpi state and utilization
	 * 
	 * @return current power consumption of this component in watt
	 */
	@Override
	public BigDecimal getCurrentPowerConsumption() {
		return componentPowerSchema.getStatePowerConsumption(currentACPIDevicePowerState,
				currentUtilization);
	}
	
	@Override
	public void reset() {
		super.reset();
		currentACPIDevicePowerState = 0; // working mode
		setLastACPIChangeTime(new BigDecimal("0"));	
		history.add(Time.getInstance().getCurrentTimeInMillis(), this, currentACPIDevicePowerState, componentEnergyConsumption);
	}
	
	public String getStateName(int state) {
		
		if(state == ACPIStateChangesHistory.STATE_CHANGE)
			return "STATE_CHANGE";
		
		if(state >= 0 && state < acpiStateDescription.length)
			return acpiStateDescription[state];
		
		return "UNKNOWN_DEVICE_POWER_STATE";
	}
	
	public void setStateName(int state, String name) {
		if(state >= 0 && state < acpiStateDescription.length)
			acpiStateDescription[state] = name;
	}

	public void setLastACPIChangeTime(BigDecimal lastACPIChangeTime) {
		statistic.setLastChangeTime(lastACPIChangeTime);
	}

	public BigDecimal getLastACPIChangeTime() {
		return statistic.getLastChangeTime();
	}
	
	public BigDecimal getDurationACPIStateChangeTimes() {
		return statistic.getDurationStateTimes();
	}
	
	public int getACPIStateCount(int state) {
		return statistic.getAcpiStateCount(state);
	}
	
	public BigDecimal getACPIStateTimes(int state) {
		return statistic.getAcpiStateTimes(state);
	}

}

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
package de.hd.pvs.piosim.power;

import java.math.BigDecimal;

/**
 * This class encapsulates the power schema for a component containing
 * values for power consumption for each acpi state, duration for state
 * changes and power consumption for state changes
 * @author Timo Minartz
 *
 */
public class ComponentPowerSchema {
	
	private int countStates;
	
	// states and power consumption for each state in watt, first index (state 0) has to be specified extra
	private BigDecimal[] statePowerConsumption;
	
	// power consumption for component at 0 percent utilization in watt
	private BigDecimal idlePowerConsumption = new BigDecimal("0");
	
	//power consumption for component at 100 percent utilization in watt
	private BigDecimal loadPowerConsumption = null;
	
	// energy consumption for incrementing the state in watt-h, last index not used
	private BigDecimal[] incStateEnergyConsumption;
	
	// energy consumption for decrementing the state in watt-h, first index not used
	private BigDecimal[] decStateEnergyConsumption;
	
	// time for incrementing the state in ms, last index not used
	private BigDecimal[] incStateDuration;
	
	// time for decrementing the state in ms, first index not used
	private BigDecimal[] decStateDuration;
	
	// interpolation strategy for component power consumption at device state 0
	private InterpolationStrategy interpolationStrategy;
	
	/**
	 * constructs new power schema with power and time consumption for state uses and changes
	 * @param countStates count states to use
	 */
	public ComponentPowerSchema(int countStates) {
		this.countStates = countStates;
		statePowerConsumption = new BigDecimal[countStates];
		incStateEnergyConsumption = new BigDecimal[countStates];
		decStateEnergyConsumption = new BigDecimal[countStates];
		incStateDuration = new BigDecimal[countStates];
		decStateDuration = new BigDecimal[countStates];	
		
		// only linear interpolation is available
		interpolationStrategy = new LinearInterpolation();
	}
	
	/**
	 * Power consumption for component under load (100 percent utilization)
	 * @param loadPowerConsumption power consumption for component at 100 percent utilization in watt
	 */
	public void setLoadPowerConsumption(BigDecimal loadPowerConsumption) {
		this.loadPowerConsumption = loadPowerConsumption;
	}
	
	/**
	 * Power consumption for idle component (0 percent utilization)
	 * @param idlePowerConsumption power consumption for component at 0 percent utilization in watt
	 */
	public void setIdlePowerConsumption(BigDecimal idlePowerConsumption) {
		this.idlePowerConsumption = idlePowerConsumption;
	}
	
	/**
	 * 
	 * @return array with all states power consumption in watt
	 */
	public BigDecimal[] getStatePowerConsumption() {
		return statePowerConsumption;
	}

	/**
	 * 
	 * @param state state to get power consumption for
	 * @return power consumption in watt for state at 0 percent utilization
	 */
	public BigDecimal getStatePowerConsumption(int state) {
		return getStatePowerConsumption(state, new BigDecimal("0"));
	}
	
	/**
	 * Power consumption for state for a specific utilization.
	 * The power efficiency is linear interpolated from <code>minEfficiency</code>
	 * and <code>maxEfficiency</code>
	 * @param state state to get power consumption for
	 * @param utilization utilization of component in percent
	 * @return power consumption in watt for state depending on utilization
	 */
	public BigDecimal getStatePowerConsumption(int state, BigDecimal utilization) {

		if(state == 0) {
			return getUtilizationBasedPowerEfficiency(utilization);
		}
		return statePowerConsumption[state];
	}

	private BigDecimal getUtilizationBasedPowerEfficiency(BigDecimal utilization) {
		return interpolationStrategy.interpolate(idlePowerConsumption, loadPowerConsumption, utilization);
	}

	public BigDecimal getIdlePowerConsumption() {
		return idlePowerConsumption;
	}

	public BigDecimal getLoadPowerConsumption() {
		return loadPowerConsumption;
	}

	/**
	 * power consumption for each state in watt, index 1 corresponds with state 1 and so on
	 * @param deviceStatePowerConsumption power consumption in watt for each state
	 * @throws InvalidValueException if array size doesn't correspond to countStates
	 */
	public void setStatePowerConsumption(BigDecimal[] deviceStatePowerConsumption) throws InvalidValueException {
		if(deviceStatePowerConsumption.length != countStates)
			throw new InvalidValueException("Invalid size. Array size has to be " + countStates);
		this.statePowerConsumption = deviceStatePowerConsumption;
		if(this.loadPowerConsumption == null)
			this.loadPowerConsumption= this.statePowerConsumption[0];
	}

	/**
	 * energy consumption for incrementing the state, last index not used
	 * @return energy consumption in watt-h for incrementing the state
	 */
	public BigDecimal[] getIncStateEnergyConsumption() {
		return incStateEnergyConsumption;
	}

	/**
	 * energy consumption for incrementing the state, last index not used
	 * @param incStateEnergyConsumption energy consumption in watt-h for incrementing the state
	 * @throws InvalidValueException if array size doesn't correspond to countStates
	 */
	public void setIncStateEnergyConsumption(BigDecimal[] incStateEnergyConsumption) throws InvalidValueException {
		if(incStateEnergyConsumption.length != countStates)
			throw new InvalidValueException("Invalid size. Array size has to be " + countStates);
		this.incStateEnergyConsumption = incStateEnergyConsumption;
	}

	/**
	 * energy consumption for decrementing the state, last index not used
	 * @return energy consumption in watt-h for decrementing the state
	 */
	public BigDecimal[] getDecStateEnergyConsumption() {
		return decStateEnergyConsumption;
	}

	/**
	 * energy consumption for decrementing the state, first index not used
	 * @param decStateEnergyConsumption energy consumption in watt-h for decrementing the state
	 * @throws InvalidValueException if array size doesn't correspond to countStates
	 */
	public void setDecStateEnergyConsumption(BigDecimal[] decStateEnergyConsumption) throws InvalidValueException {
		if(decStateEnergyConsumption.length != countStates)
			throw new InvalidValueException("Invalid size. Array size has to be " + countStates);
		this.decStateEnergyConsumption = decStateEnergyConsumption;
	}

	/**
	 * time for incrementing the state, last index not used
	 * @return incStateDuration time in ms for incrementing the state
	 */
	public BigDecimal[] getIncStateDuration() {
		return incStateDuration;
	}

	/**
	 * time for incrementing the state, last index not used
	 * @param incStateDuration time in ms for incrementing the state
	 * @throws InvalidValueException if array size doesn't correspond to countStates
	 */
	public void setIncStateDuration(BigDecimal[] incStateDuration) throws InvalidValueException {
		if(incStateDuration.length != countStates)
			throw new InvalidValueException("Invalid size. Array size has to be " + countStates);
		this.incStateDuration = incStateDuration;
	}

	/**
	 * time for decrementing the state, first index not used
	 * @return time in ms for incrementing the state
	 */
	public BigDecimal[] getDecStateDuration() {
		return decStateDuration;
	}

	/**
	 * time for decrementing the state, first index not used
	 * @param decStateDuration time in ms for incrementing the state
	 * @throws InvalidValueException Exception if array size doesn't correspond to countStates
	 */
	public void setDecStateDuration(BigDecimal[] decStateDuration) throws InvalidValueException {
		if(decStateDuration.length != countStates)
			throw new InvalidValueException("Invalid size. Array size has to be " + countStates);
		this.decStateDuration = decStateDuration;
	}
	
	/**
	 * count different states corresponds to intern array size
	 * @return count different states
	 */
	public int getCountStates() {
		return countStates;
	}

	public void setInterpolationStrategy(InterpolationStrategy interpolationStrategy) {
		this.interpolationStrategy = interpolationStrategy;
	}

	public InterpolationStrategy getInterpolationStrategy() {
		return interpolationStrategy;
	}
 }

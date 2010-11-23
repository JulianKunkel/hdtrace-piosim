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

import de.hd.pvs.piosim.power.calculation.ACPICalculation;
import de.hd.pvs.piosim.power.cluster.Node;

/**
 * This class encapsulates the component properties such as utilization, power
 * schema and name. It also provides a method to change the utilization of this
 * component.
 * 
 * @author Timo Minartz
 * 
 */
public abstract class Component {

	protected ComponentPowerSchema componentPowerSchema;

	private String name;

	// current component utilization
	protected BigDecimal currentUtilization = new BigDecimal("1");

	// global time from last utilization change
	private BigDecimal lastUtilizationChangeTime = new BigDecimal("0");

	// energy consumption for device till time(0) in watt-h
	protected BigDecimal componentEnergyConsumption = new BigDecimal("0");
	
	private Node node = null;
	
	public Node getNode() {
		return node;
	}
	
	public void setNode(Node node) {
		this.node = node;
	}

	/**
	 * Returns current components power consumption
	 * 
	 * @return current power consumption of this component in watt
	 */
	public abstract BigDecimal getCurrentPowerConsumption();

	/**
	 * Changes utilization of this component to <code>utilization</code>. If
	 * <code>utilization</code> equals current utilization, only the refresh()
	 * Method is called. Otherwise, utilization is updated and so the power
	 * consumption for the last block.
	 * 
	 * @param utilization
	 *            to change to between 0.0 and 1.0
	 * @throws ComponentException
	 *             if utilization is no valid percent value
	 */
	public void changeUtilization(BigDecimal utilization)
			throws ComponentException {

		//refresh();

		BigDecimal time = Time.getInstance().getCurrentTimeInMillis();

		if (utilization.doubleValue() < 0 || utilization.doubleValue() > 1)
			throw new ComponentException(this.getName() + ": Invalid utilization (in percent): "
					+ utilization);

		if (currentUtilization.doubleValue() == utilization.doubleValue())
			return;

		componentEnergyConsumption = ACPICalculation.sumEnergyConsumptionTillNow(
				componentEnergyConsumption, getCurrentPowerConsumption(), time,
				getLastUtilizationChangeTime());

		this.currentUtilization = utilization;
		setLastUtilizationChangeTime(time);
		
		if(this.getNode() != null)
			this.getNode().updateEnergyConsumption(time);
	}

	/**
	 * Returns the components power schema
	 * 
	 * @return components power schema
	 */
	public ComponentPowerSchema getComponentPowerSchema() {
		return componentPowerSchema;
	}

	/**
	 * 
	 * @param name
	 *            for this component
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return components name
	 */
	public String getName() {
		return name;
	}

	public void reset() {
		currentUtilization = new BigDecimal("1");
		setLastUtilizationChangeTime(new BigDecimal("0"));
		componentEnergyConsumption = new BigDecimal("0");
	}

	public BigDecimal setLastUtilizationChangeTime(
			BigDecimal lastUtilizationChangeTime) {
		this.lastUtilizationChangeTime = lastUtilizationChangeTime;
		return lastUtilizationChangeTime;
	}

	public BigDecimal getLastUtilizationChangeTime() {
		return lastUtilizationChangeTime;
	}
}

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
import java.util.ArrayList;
import java.util.List;

import de.hd.pvs.piosim.power.ComponentException;
import de.hd.pvs.piosim.power.Time;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.ACPIDeviceException;
import de.hd.pvs.piosim.power.acpi.GlobalSystemStates;
import de.hd.pvs.piosim.power.acpi.IACPIAnalyzable;
import de.hd.pvs.piosim.power.calculation.ACPICalculation;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;

/**
 * This class encapsulates a node with a list of all its devices
 * @author Timo Minartz
 *
 */
public abstract class Node implements IACPIAnalyzable, GlobalSystemStates {
	
	protected List<ACPIDevice> nodeDevices;
	protected int globalSystemState = 0;
	private String name;
	private BigDecimal lastChangeTime = new BigDecimal("0");
	
	// overhead in watt per node (mainboard etc.)
	protected BigDecimal overhead = new BigDecimal("0");
	
	// energy consumption of this node in watt-h
	protected BigDecimal energyConsumption = new BigDecimal("0");
	
	// current power consumption in watt of this node
	protected BigDecimal currentPowerConsumption = new BigDecimal("0");
	
	protected PowerSupply powerSupply = new PowerSupply();

	/**
	 * 
	 * @return List with all devices belonging to this node
	 */
	public List<ACPIDevice> getNodeDevices() {
		return nodeDevices;
	}

	/**
	 * Resets intern list
	 * @param nodeDevices List with all devices belonging to this node
	 */
	public void setNodeDevices(List<ACPIDevice> nodeDevices) {
		this.nodeDevices = nodeDevices;
	}
	
	/**
	 * Add a single device to this node if no node with this name exists
	 * @param nodeDevice device to add to node
	 */
	public void add(ACPIDevice nodeDevice) {
		if(nodeDevice.getNode() != this) {
			nodeDevices.add(nodeDevice);
			nodeDevice.setNode(this);
		}
	}
	
	/**
	 * @param deviceName Device name to search for
	 * @return ACPIDevice with this name if in this node
	 */
	public ACPIDevice getDevice(String deviceName) {
		for (ACPIDevice device : nodeDevices) {
			if(device.getName().equals(deviceName))
				return device;
		}			
		return null;
	}
	
	@Override
	public BigDecimal getACPIStateChangesTimeOverhead() {
		return ACPICalculation.calculateSumACPIStateChangesTimeOverhead(new ArrayList<IACPIAnalyzable>(nodeDevices));
	}
	
	@Override
	public BigDecimal getACPIStateChangesTime() {
		return ACPICalculation.calculateSumDurationACPIStateChanges(new ArrayList<IACPIAnalyzable>(nodeDevices));
	}

	@Override
	public BigDecimal getMaxPowerConsumption() {
		return ACPICalculation.calculateSumMaxPowerConsumption(new ArrayList<IACPIAnalyzable>(nodeDevices));
	}
	
	@Override
	public void refresh() {
		for (ACPIDevice nodeDevice : nodeDevices) {
			nodeDevice.refresh();
		}
	}

	@Override
	public BigDecimal getDurationForGlobalSystemStateChange(int fromState,
			int toState) {
		return ACPICalculation.calculateSumDurationForDevicePowerStateChange(nodeDevices, fromState, toState);
	}

	@Override
	public int getGlobalSystemState() {
		return this.globalSystemState;
	}
	
	@Override
	public void switchToGlobalSystemState(int state) throws ACPIDeviceException {
		for(ACPIDevice device : nodeDevices) {
			device.toACPIState(state);
		}
		
		this.globalSystemState = state;
	}

	@Override
	public BigDecimal getEnergyConsumptionForGlobalSystemStateChange(
			int fromState, int toState) {
		return ACPICalculation.calculateSumEnergyConsumptionForDevicePowerStateChange(nodeDevices, fromState, toState);
	}

	public void run() throws ComponentException {
		for(ACPIDevice nodeDevice : nodeDevices) {
			nodeDevice.run();
		}
	}

	public void stop() {
		for(ACPIDevice nodeDevice : nodeDevices) {
			nodeDevice.stop();
		}
	}

	public void toSleep() throws ACPIDeviceException {
		for(ACPIDevice nodeDevice : nodeDevices) {
			nodeDevice.toSleep();
		}
	}

	public void toWork() throws ACPIDeviceException {
		for(ACPIDevice nodeDevice : nodeDevices) {
			nodeDevice.toWork();
		}
	}

	public void toSuspend() throws ACPIDeviceException {
		for(ACPIDevice nodeDevice : nodeDevices) {
			nodeDevice.toSuspend();
		}
	}

	public void reset() {
		lastChangeTime = new BigDecimal("0");
		energyConsumption = new BigDecimal("0");
		currentPowerConsumption = new BigDecimal("0");
			
		for(ACPIDevice device : getNodeDevices())
			currentPowerConsumption = BaseCalculation.sum(currentPowerConsumption, device.getACPIComponent().getCurrentPowerConsumption());	
		
		currentPowerConsumption = BaseCalculation.sum(currentPowerConsumption,this.getOverhead());
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 * @return Overhead for this node in watt
	 */
	public BigDecimal getOverhead() {
		return overhead;
	}

	/**
	 * update the total energy consumption for the node
	 * @param time actual time in ms
	 */
	public void updateEnergyConsumption(BigDecimal time) {
		
		energyConsumption = BaseCalculation.sum(energyConsumption, powerSupply.getEnergyConsumption(currentPowerConsumption,lastChangeTime, time));
		
		currentPowerConsumption = new BigDecimal("0");
		
		for(ACPIDevice device : getNodeDevices())
			currentPowerConsumption = BaseCalculation.sum(currentPowerConsumption, device.getACPIComponent().getCurrentPowerConsumption());	
		
		currentPowerConsumption = BaseCalculation.sum(currentPowerConsumption,this.getOverhead());
		
		lastChangeTime = time;
	}
	
	@Override
	public BigDecimal getEnergyConsumption() {
		refresh();
		BigDecimal tmp = BaseCalculation.sum(energyConsumption, powerSupply.getEnergyConsumption(currentPowerConsumption,lastChangeTime, Time.getInstance().getCurrentTimeInMillis()));
		return tmp;
	}
	
	@Override 
	public String toString() {
		return this.getName();
	}
	

}

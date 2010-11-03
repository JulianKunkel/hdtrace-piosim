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

import de.hd.pvs.piosim.power.ComponentException;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.ACPIDeviceException;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;


public class ExtendedCluster extends Cluster {

	private ACPIDevice simpleSwitch;
	
	public ExtendedCluster() {
		this.nodes = new ArrayList<Node>();
	}

	public ACPIDevice getSimpleSwitch() {
		return simpleSwitch;
	}

	public void setSimpleSwitch(ACPIDevice simpleSwitch) {
		this.simpleSwitch = simpleSwitch;
	}

	@Override
	public BigDecimal getEnergyConsumption() {
		return BaseCalculation.sum(super.getEnergyConsumption(),simpleSwitch.getEnergyConsumption());
	}

	@Override
	public BigDecimal getACPIStateChangesTimeOverhead() {
		return BaseCalculation.sum(super.getACPIStateChangesTimeOverhead(),simpleSwitch.getACPIStateChangesTimeOverhead());
	}
	
	@Override
	public void refresh() {
		super.refresh();
		simpleSwitch.refresh();
	}

	@Override
	public void run() throws ComponentException {
		super.run();
		simpleSwitch.run();
	}

	@Override
	public void stop() {
		super.stop();
		simpleSwitch.stop();
	}

	@Override
	public void toSleep() throws ACPIDeviceException {
		super.toSleep();
		simpleSwitch.toSleep();
	}

	@Override
	public void toWork() throws ACPIDeviceException {
		super.toWork();
		simpleSwitch.toWork();
	}

	@Override
	public void toSuspend() throws ACPIDeviceException {
		super.toSuspend();
		simpleSwitch.toSuspend();
	}
}

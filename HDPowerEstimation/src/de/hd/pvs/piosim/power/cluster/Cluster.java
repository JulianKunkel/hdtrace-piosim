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
import de.hd.pvs.piosim.power.acpi.ACPIDeviceException;
import de.hd.pvs.piosim.power.acpi.IACPIAnalyzable;
import de.hd.pvs.piosim.power.calculation.ACPICalculation;

public class Cluster implements IACPIAnalyzable {
	protected List<Node> nodes;

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public void add(Node node) {
		this.nodes.add(node);
	}

	@Override
	public BigDecimal getPowerConsumption() {
		return ACPICalculation.calculateSumPowerConsumption(new ArrayList<IACPIAnalyzable>(nodes));
	}

	@Override
	public BigDecimal getACPIStateChangesTimeOverhead() {
		return ACPICalculation.calculateSumACPIStateChangesTimeOverhead(new ArrayList<IACPIAnalyzable>(nodes));
	}
	
	@Override
	public BigDecimal getACPIStateChangesTime() {
		return ACPICalculation.calculateSumDurationACPIStateChanges(new ArrayList<IACPIAnalyzable>(nodes));
	}

	@Override
	public BigDecimal getMaxPowerConsumption() {
		return ACPICalculation.calculateSumMaxPowerConsumption(new ArrayList<IACPIAnalyzable>(nodes));
	}
	
	@Override
	public void refresh() {
		for (Node node : nodes) {
			node.refresh();
		}
	}

	public void run() throws ComponentException {
		for (Node node : nodes) {
			node.run();
		}
	}

	public void stop() {
		for (Node node : nodes) {
			node.stop();
		}
	}

	public void toSleep() throws ACPIDeviceException {
		for (Node node : nodes) {
			node.toSleep();
		}
	}

	public void toWork() throws ACPIDeviceException {
		for (Node node : nodes) {
			node.toWork();
		}
	}

	public void toSuspend() throws ACPIDeviceException {
		for (Node node : nodes) {
			node.toSuspend();
		}
	}
}

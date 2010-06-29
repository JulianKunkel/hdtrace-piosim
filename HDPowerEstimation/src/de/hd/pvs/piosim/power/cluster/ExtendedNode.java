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

import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;

/**
 * This class encapsulates a Node with a overhead of 70 watt
 * @author Timo Minartz
 *
 */
public class ExtendedNode extends Node {
	
	public ExtendedNode() {
		this.nodeDevices = new ArrayList<ACPIDevice>();
//		this.setOverhead(new BigDecimal("6.305"));
//		this.powerSupply.setProcentualOverhead(new BigDecimal("0.35"));
	}

	/**
	 * 
	 * @param overhead The overhead for this node in watt
	 */
	public void setOverhead(BigDecimal overhead) {
		this.overhead = overhead;
	}
	
	public void setPowerSupply(PowerSupply powerSupply) {
		this.powerSupply = powerSupply;
	}
	
	@Override
	public BigDecimal getMaxPowerConsumption() {
		return BaseCalculation.sum(super.getMaxPowerConsumption(), this.getOverhead());
	}	
	
	
	
}

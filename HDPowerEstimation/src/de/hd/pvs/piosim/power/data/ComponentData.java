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
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains all data belonging to a component for a replay:
 * utilization and power consumption
 * 
 * @author Timo Minartz
 *
 */
public class ComponentData {

	String name;
	List<BigDecimal> utilization = new ArrayList<BigDecimal>();
	List<BigDecimal> powerConsumption = new ArrayList<BigDecimal>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal[] getUtilization() {
		BigDecimal[] array = new BigDecimal[utilization.size()];
		array = utilization.toArray(array);
		return array;
	}
	
	public BigDecimal getUtilization(int index) {
		if(utilization == null)
			return new BigDecimal("0");
		return utilization.get(index);
	}

	public void setUtilization(BigDecimal[] utilization) {
		this.utilization.clear();
		for(int i=0; i<utilization.length; ++i) {
			this.utilization.add(utilization[i]);
		}
	}
	
	public void addUtilization(BigDecimal utilization) {
		this.utilization.add(utilization);
	}
	
	public BigDecimal[] getPowerConsumption() {
		BigDecimal[] array = new BigDecimal[powerConsumption.size()];
		array = powerConsumption.toArray(array);
		return array;
	}
	
	public BigDecimal getPowerConsumption(int index) {	
		return powerConsumption.get(index);
	}

	public void setPowerConsumption(BigDecimal[] powerConsumption) {
		this.powerConsumption.clear();
		for(int i=0; i<powerConsumption.length; ++i) {
			this.powerConsumption.add(powerConsumption[i]);
		}
	}
	
	public void addPowerConsumption(BigDecimal powerConsumption) {
		this.powerConsumption.add(powerConsumption);
	}

	public int getSize() {
		return utilization.size();
	}

	public void clearPowerConsumption() {
		this.powerConsumption.clear();		
	}
	
}

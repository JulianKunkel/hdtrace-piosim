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
package de.hd.pvs.piosim.power.tests;

import java.math.BigDecimal;

import org.junit.Test;

import de.hd.pvs.piosim.power.cluster.PowerSupply;

public class PowerSupplyTest extends AbstractTestCase {
	
	@Test
	public void testPowerSupply() {
		PowerSupply powerSupply = new PowerSupply();
		
		assertEquals(null, powerSupply.getMaxEfficiency());
		assertEquals(null, powerSupply.getMinEfficiency());
		assertEquals(null, powerSupply.getProcentualOverhead());
		assertEquals(null, powerSupply.getMaxPower());
		
		BigDecimal currentPowerConsumption = new BigDecimal("200");
		BigDecimal lastChangeTime = new BigDecimal("0");
		BigDecimal time = new BigDecimal("3600000");
		
		assertEquals(200.0, powerSupply.getEnergyConsumption(currentPowerConsumption, lastChangeTime, time).doubleValue());
		
		time = new BigDecimal("1800000");
		
		assertEquals(100.0, powerSupply.getEnergyConsumption(currentPowerConsumption, lastChangeTime, time).doubleValue());
		
		powerSupply.setProcentualOverhead(new BigDecimal("0.5"));
		
		assertEquals(150.0, powerSupply.getEnergyConsumption(currentPowerConsumption, lastChangeTime, time).doubleValue());
		
		powerSupply.setProcentualOverhead(new BigDecimal("0"));
		
		assertEquals(100.0, powerSupply.getEnergyConsumption(currentPowerConsumption, lastChangeTime, time).doubleValue());
		
		powerSupply.setMaxEfficiency(new BigDecimal("1"));
		powerSupply.setMinEfficiency(new BigDecimal("0"));
		powerSupply.setMaxPower(new BigDecimal("200"));
		
		assertEquals(100.0, powerSupply.getEnergyConsumption(currentPowerConsumption, lastChangeTime, time).doubleValue());
		
		powerSupply.setMaxPower(new BigDecimal("400"));
		
		assertEquals(150.0, powerSupply.getEnergyConsumption(currentPowerConsumption, lastChangeTime, time).doubleValue());
		
		powerSupply.setMinEfficiency(new BigDecimal("1"));
		
		assertEquals(100.0, powerSupply.getEnergyConsumption(currentPowerConsumption, lastChangeTime, time).doubleValue());
		
	}
}

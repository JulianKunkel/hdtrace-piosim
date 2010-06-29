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
package de.hd.pvs.piosim.power.devices;

import java.math.BigDecimal;

import de.hd.pvs.piosim.power.InvalidValueException;
import de.hd.pvs.piosim.power.acpi.ConvertingException;



public class MockDevice extends MemoryDevice {
	
	public MockDevice() {
		super();
	}

	public MockDevice(String name) {
		this.setName(name);
	}

	@Override
	public void initComponentPowerConsumption() {
		
		BigDecimal[] statePowerConsumption = {new BigDecimal("0"), new BigDecimal("0"),new BigDecimal("0"),new BigDecimal("0")};
		BigDecimal[] decStateDuration = {new BigDecimal("0"), new BigDecimal("0"),new BigDecimal("0"),new BigDecimal("0")};
		BigDecimal[] decStatePowerConsumption = {new BigDecimal("0"), new BigDecimal("0"),new BigDecimal("0"),new BigDecimal("0")};
		BigDecimal[] incStateDuration = {new BigDecimal("0"), new BigDecimal("0"),new BigDecimal("0"),new BigDecimal("0")};
		BigDecimal[] incStatePowerConsumption = {new BigDecimal("0"), new BigDecimal("0"),new BigDecimal("0"),new BigDecimal("0")};
		BigDecimal idlePowerConsumption = new BigDecimal("0");
		BigDecimal loadPowerConsumption = new BigDecimal("0");
		
		try {
			this.getComponentPowerSchema().setStatePowerConsumption(statePowerConsumption);
			this.getComponentPowerSchema().setDecStateDuration(decStateDuration);
			this.getComponentPowerSchema().setDecStatePowerConsumption(decStatePowerConsumption);
			this.getComponentPowerSchema().setIncStateDuration(incStateDuration);
			this.getComponentPowerSchema().setIncStatePowerConsumption(incStatePowerConsumption);
			this.getComponentPowerSchema().setLoadPowerConsumption(loadPowerConsumption);
			this.getComponentPowerSchema().setIdlePowerConsumption(idlePowerConsumption);
		} catch (InvalidValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setStatePowerConsumption(int state, double wattPerHour) {
		setStatePowerConsumption(state, new BigDecimal(Double.toString(wattPerHour)));
	}
	public void setDecStateDuration(int state, double durationInMs) {
		setDecStateDuration(state, new BigDecimal(Double.toString(durationInMs)));
	}
	
	public void setIncStateDuration(int state, double durationInMs) {
		setIncStateDuration(state, new BigDecimal(Double.toString(durationInMs)));
	}
	
	public void setDecStatePowerConsumption(int state, double wattPerHour) {
		setDecStatePowerConsumption(state, new BigDecimal(Double.toString(wattPerHour)));
	}
	
	public void setIncStatePowerConsumption(int state, double wattPerHour) {
		setIncStatePowerConsumption(state, new BigDecimal(Double.toString(wattPerHour)));
	}
	
	public void setStatePowerConsumption(int state, BigDecimal watt) {
		if(state == 0)
			this.getComponentPowerSchema().setLoadPowerConsumption(watt);
		
		this.getComponentPowerSchema().getStatePowerConsumption()[state] = watt;
	}
	
	public void setDecStateDuration(int state, BigDecimal durationInMs) {
		this.getComponentPowerSchema().getDecStateDuration()[state] = durationInMs;
	}
	
	public void setIncStateDuration(int state, BigDecimal durationInMs) {
		this.getComponentPowerSchema().getIncStateDuration()[state] = durationInMs;
	}
	
	public void setDecStatePowerConsumption(int state, BigDecimal wattPerHour) {
		this.getComponentPowerSchema().getDecStatePowerConsumption()[state] = wattPerHour;
	}
	
	public void setIncStatePowerConsumption(int state, BigDecimal wattPerHour) {
		this.getComponentPowerSchema().getIncStatePowerConsumption()[state] = wattPerHour;
	}
	
	
	public void setLoadPowerConsumption(BigDecimal loadPowerConsumption) {
		this.getACPIComponent().getComponentPowerSchema().setLoadPowerConsumption(loadPowerConsumption);
	}
	
	
	public void setIdlePowerConsumption(BigDecimal idlePowerConsumption) {
		this.getACPIComponent().getComponentPowerSchema().setIdlePowerConsumption(idlePowerConsumption);
	}

	@Override
	public BigDecimal convertToPercentualUtilization(double value, String unit)
			throws ConvertingException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setMemorySize(BigDecimal memorySize) {
		this.memorySize = memorySize;
	}

	public void setCountBanks(int countBanks) {
		this.countBanks = countBanks;
	}

	public void setIdlePowerConsumption(double idlePowerConsumption) {
		setIdlePowerConsumption(new BigDecimal(Double.toString(idlePowerConsumption)));
		
	}
	
	public void setLoadPowerConsumption(double loadPowerConsumption) {
		setLoadPowerConsumption(new BigDecimal(Double.toString(loadPowerConsumption)));
		
	}

	public BigDecimal getLoadPowerConsumption() {
		return this.getACPIComponent().getComponentPowerSchema().getLoadPowerConsumption();
	}

	
}

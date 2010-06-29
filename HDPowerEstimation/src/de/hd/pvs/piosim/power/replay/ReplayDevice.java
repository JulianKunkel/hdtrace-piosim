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
package de.hd.pvs.piosim.power.replay;

import java.math.BigDecimal;

import de.hd.pvs.piosim.power.ComponentException;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.calculation.ACPICalculation;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;
import de.hd.pvs.piosim.power.data.DeviceData;

public class ReplayDevice {
	
	private ACPIDevice device;
	private DeviceData replayData;
	private DeviceData naturalData;
	private int stepsize;
//	private Logger logger = Logger.getLogger(ReplayDevice.class);
	
	public void setStepsize(int stepsize) {
		this.stepsize = stepsize;
	}
	
	public int getStepsize() {
		return stepsize;
	}
	
	private int currentStep = 0;

	/**
	 * Performs a step with size <code>stepsize</code> in timeline and
	 * calculates power consumption for step based on utilization for step 
	 * 
	 * @throws ComponentException
	 */
	public void step() throws ComponentException {
		setPowerConsumptionForStep(currentStep); // sets the power consumption for the last step
		setUtilizationForStep(currentStep); // sets the utilization for the current step
		currentStep++;
	}

	public BigDecimal[] getFutureUtilization(int futureSteps) {

		BigDecimal[] utilization = new BigDecimal[futureSteps];

		for (int i = 0; i < futureSteps; ++i) {
			utilization[i] = replayData.getUtiliziation(currentStep + i);
		}

		return utilization;
	}

	public BigDecimal getPowerConsumptionForStep(int step) {

		return ACPICalculation.calculateInWatt(BaseCalculation.substract(device.getPowerConsumption(),
				replayData.getTotalConsumption()), new BigDecimal(stepsize));

	}

	public void setPowerConsumptionForStep(int step) {
		
		if(step == 0)
			return;

		BigDecimal stepPowerConsumption = getPowerConsumptionForStep(step);

		replayData.addConsumption(stepPowerConsumption);
		replayData.setTotalConsumption(device.getPowerConsumption());
		
//		StatisticData.getInstance().addValues(device, step, replayData.getUtiliziation(step), stepPowerConsumption);

	}
	
	public void setPowerConsumptionForLastStep(int lastStep) {

		BigDecimal stepPowerConsumption = getPowerConsumptionForStep(lastStep);

		replayData.addConsumption(stepPowerConsumption);
		replayData.setTotalConsumption(device.getPowerConsumption());
		
//		StatisticData.getInstance().addValues(device, lastStep+1, replayData.getUtiliziation(lastStep), stepPowerConsumption);

	}

	public void setUtilizationForStep(int step) throws ComponentException {

		device.changeUtilization(replayData.getUtiliziation(step));

	}

	public void reset() {
		device.reset();
		replayData = new DeviceData(naturalData);
		currentStep = 0;
	}

	public ACPIDevice getACPIDevice() {
		return device;
	}

	public int getCurrentStep() {
		return currentStep;
	}
	
	public BigDecimal[] getPowerConsumption() {
		return replayData.getConsumption();
	}

	public BigDecimal[] getUtilization() {
		return replayData.getUtilization();
	}

	public void setUtilization(BigDecimal[] utilization) {
		replayData.setUtilization(utilization);
	}

	public void setACPIDevice(ACPIDevice device) {
		this.device = device;
	}

	public void setDeviceData(DeviceData deviceData) {
		this.naturalData = deviceData;
		this.replayData = new DeviceData(naturalData);
	}

	public BigDecimal getSumUtilizationForNextSteps(int countSteps) {
		BigDecimal utilization[] = getUtilization();
		BigDecimal sum = new BigDecimal("0");
		
		for(int i=currentStep; i<currentStep + countSteps; ++i) {
			BaseCalculation.sum(utilization[i], sum);
		}
		
		return sum;
	}

}

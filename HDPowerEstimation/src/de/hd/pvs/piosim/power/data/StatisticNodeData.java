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

import de.hd.pvs.piosim.power.calculation.BaseCalculation;

public class StatisticNodeData {
	
	private BigDecimal[] meanUtilization;
	private BigDecimal[] sumPowerConsumption;
	private BigDecimal[] currentStepPowerConsumption;
	private BigDecimal[] currentStepUtilization;
	private int currentStep = 0;
	private int currentDevice = 0;
	private int countSteps;
	
	public StatisticNodeData(int countDevices, int countSteps) {
		this.countSteps = countSteps;
		currentStepUtilization = new BigDecimal[countDevices];
		currentStepPowerConsumption = new BigDecimal[countDevices];
		
		meanUtilization = new BigDecimal[countSteps];
		sumPowerConsumption = new BigDecimal[countSteps];
	}

	public void finishStep() {
		
		// save mean value
		meanUtilization[currentStep] = BaseCalculation.divide(BaseCalculation.sum(currentStepUtilization), new BigDecimal(currentStepUtilization.length));
		
		// save sum
		sumPowerConsumption[currentStep] = BaseCalculation.sum(currentStepPowerConsumption);
		
		// reset working arrays
		currentStepUtilization = new BigDecimal[currentStepUtilization.length];
		currentStepPowerConsumption = new BigDecimal[currentStepPowerConsumption.length];
		
		currentStep++;
		currentDevice = 0;
	}

	public void addValues(BigDecimal stepUtilization,
			BigDecimal stepPowerConsumption) {
		currentStepUtilization[currentDevice] = stepUtilization;		
		currentStepPowerConsumption[currentDevice] = stepPowerConsumption;
		currentDevice++;
	}

	public BigDecimal[] getMeanUtilization() {
		if(currentStep < countSteps)
			finishStep();
		
		return meanUtilization;
	}

	public BigDecimal[] getSumPowerConsumption() {
		if(currentStep < countSteps)
			finishStep();
		
		return sumPowerConsumption;
	}
	
//	private BigDecimal calculateMeanValue(BigDecimal[] values) {
//		BigDecimal sum = new BigDecimal("0");
//		int countValues = 0;
//		for(int i=0; i<values.length; ++i) {
//			if(values[i] == null) {
//				countValues = i;
//				break;
//			}
//			sum = BaseCalculation.sum(sum,values[i]);
//		}
//		
//		if(countValues <= 0)
//			return null;
//		
//		return BaseCalculation.divide(sum, new BigDecimal(countValues));
//	}


}

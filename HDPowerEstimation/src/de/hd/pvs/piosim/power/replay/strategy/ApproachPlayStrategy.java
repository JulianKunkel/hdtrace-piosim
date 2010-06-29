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
package de.hd.pvs.piosim.power.replay.strategy;

import java.math.BigDecimal;

import org.apache.log4j.Logger;

import de.hd.pvs.piosim.power.ComponentException;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.ACPIDeviceException;
import de.hd.pvs.piosim.power.acpi.DevicePowerStates;
import de.hd.pvs.piosim.power.calculation.ACPICalculation;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;
import de.hd.pvs.piosim.power.calculation.CalculationException;
import de.hd.pvs.piosim.power.replay.ReplayDevice;
import de.hd.pvs.piosim.power.replay.ReplayException;

/**
 * This strategy extends OptimalPlayStrategy but asserts
 * utilization to be zero if under a specified tolerance.
 * Default tolerance is 10.0 percent
 * 
 * @author Timo Minartz
 *
 */
public class ApproachPlayStrategy implements PlayStrategy {
	
	// tolerance to be accepted (inclusive)
	private double tolerance = 0.1;
	private static Logger logger = Logger.getLogger(ApproachPlayStrategy.class);

	public double getTolerance() {
		return tolerance;
	}

	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}

	@Override
	public void step(ReplayDevice replayDevice, int countSteps) throws ReplayException {
		
		int stepsize = replayDevice.getStepsize();
		ACPIDevice device = replayDevice.getACPIDevice();
		device.refresh();
		if (device.getDevicePowerState() == DevicePowerStates.DEVICE_POWER_STATE_0) {
			int steps;
			steps = getCountStepsForEfficientStateChange(replayDevice,
					DevicePowerStates.DEVICE_POWER_STATE_0,
					DevicePowerStates.DEVICE_POWER_STATE_3, stepsize);
			
			logger.debug("Step " + replayDevice.getCurrentStep() + " - Count steps for efficient state change: " + steps);

			if (steps + replayDevice.getCurrentStep() < countSteps) {
				if (underToleranceUtilization(replayDevice, steps,this.tolerance) && !device.hasEnqueuedStateChanges()) {
					try {
						logger.info("Step " + replayDevice.getCurrentStep() + " - Putting device " + device + " to sleep");
						device.toSleep();
						if(rearrangeLoad(replayDevice)) {
							logger.info("Rearranged Load");
						}
					} catch (ACPIDeviceException e) {
						throw new ReplayException(e.getMessage(), e.getStackTrace());
					}
				}
			}
		} else {
			int steps = getCountStepsForStateChange(replayDevice,
					DevicePowerStates.DEVICE_POWER_STATE_0, stepsize);
			
			if (steps + replayDevice.getCurrentStep() < countSteps + 1) {
				if (!zeroUtilization(replayDevice, steps) && !device.hasEnqueuedStateChanges()) {
					try {
						device.toWork();
					} catch (ACPIDeviceException e) {
						throw new ReplayException(e.getMessage(), e.getStackTrace());
					}
				}
			}
		}
		
		try {
			replayDevice.step();
		} catch (ComponentException e) {
			throw new ReplayException(e.getMessage(), e.getStackTrace());
		}
	}
	
	/**
	 * Returns count steps for an efficient state change for this device.
	 * Meaning the steps spending in new <code>state</code> including state
	 * changes (to <code>state</code> and back again) to reduce power
	 * consumption
	 * 
	 * @param device
	 *            ACPIDevice for eventually state change
	 * @param state
	 *            ACPI state to change to
	 * @return count steps for an efficient state change
	 * @throws ReplayException
	 *             if calculation failed
	 */
	private int getCountStepsForEfficientStateChange(ReplayDevice replayDevice,
			int fromState, int toState, int stepsize) throws ReplayException {
		
		int currentStep = replayDevice.getCurrentStep();

		try {
				
			// time for efficient change, independently from the actual tolerance
			BigDecimal minimalStateTimeInMs = ACPICalculation.calculateMinimalStateTimeForEfficiency(
					replayDevice.getACPIDevice(), fromState, toState);
			
			// if a tolerance is specified, the load has to be done after the minimalStateTime.
			// Hence, the load time has to be calculated, dependent on the devices utilization for the minimalStateTime
	
			int stepsSupposedToBeZeroUtilized = (minimalStateTimeInMs.equals(BigDecimal.ZERO)) ? 0 : minimalStateTimeInMs.intValue() / stepsize + 1;

			if(stepsSupposedToBeZeroUtilized + currentStep >= replayDevice.getUtilization().length)
				return replayDevice.getUtilization().length + 1;
			
			BigDecimal load = BaseCalculation.sum(replayDevice.getFutureUtilization(stepsSupposedToBeZeroUtilized));
			
			// now try to add the load to the next steps, every next step is fully utilized till the load is rearranged
			for(int step = currentStep + stepsSupposedToBeZeroUtilized; ;++step) {
				
				if(step >= replayDevice.getUtilization().length)
					return step + 1; // the load cannot be rearranged
				
				load = BaseCalculation.substract(load, BaseCalculation.substract(BigDecimal.ONE,replayDevice.getUtilization()[step]));
				
				if(load.compareTo(BigDecimal.ZERO) <= 0)
					return step - currentStep;
			}
			
		} catch (CalculationException e) {
			throw new ReplayException(e.getMessage(), e.getStackTrace());
		}
	}

	private boolean underToleranceUtilization(ReplayDevice replayDevice,
			int steps, double tolerance) {
		
		BigDecimal[] utilization = replayDevice.getFutureUtilization(steps);
		
		for (BigDecimal bigDecimal : utilization) {
			if (bigDecimal == null || bigDecimal.doubleValue() > tolerance) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean zeroUtilization(ReplayDevice replayDevice, int steps) {

		return underToleranceUtilization(replayDevice,steps,0.0);
		
	}
	
	private int getCountStepsForStateChange(ReplayDevice replayDevice, int state, int stepsize) {
		int countSteps = replayDevice.getACPIDevice().getDurationForDevicePowerStateChange(state).intValue()
		/ stepsize + 1;
		
		return countSteps;
	}
	
	private boolean rearrangeLoad(ReplayDevice replayDevice) {
		
		BigDecimal[] utilization = replayDevice.getUtilization();
		BigDecimal load = new BigDecimal("0");
		int lastModifiedIndex;
		for(lastModifiedIndex=replayDevice.getCurrentStep(); lastModifiedIndex<utilization.length; ++lastModifiedIndex) {
			if(utilization[lastModifiedIndex].doubleValue() <= tolerance) {
				load = BaseCalculation.sum(load, utilization[lastModifiedIndex]);
				utilization[lastModifiedIndex] = BigDecimal.ZERO;
			} else {
				break;
			}
		}

		int countModifiedValues = lastModifiedIndex - replayDevice.getCurrentStep();
		
		if(countModifiedValues == 0)
			return false;

		int countFullUtilizationValues = load.intValue();

		// one value has not full utilization based on the values smaller than the tolerance
		BigDecimal moduloUtilization = BaseCalculation.substract(load, new BigDecimal(countFullUtilizationValues));

		// set this value
		if(lastModifiedIndex - countFullUtilizationValues -1 >= 0) {
			logger.debug("utilization on index " + (lastModifiedIndex - countFullUtilizationValues -1) + " set to " + moduloUtilization);
			utilization[lastModifiedIndex - countFullUtilizationValues -1] = moduloUtilization;
		}

		// set the full utilization values
		for(int j=lastModifiedIndex - countFullUtilizationValues; j< lastModifiedIndex; ++j) {
			logger.debug("utilization on index " + j + " set to 1");
			utilization[j] = BigDecimal.ONE;
		}
			
		replayDevice.setUtilization(utilization);
		
		return true;
	}
	
}

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
import de.hd.pvs.piosim.power.InvalidValueException;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.ACPIDeviceException;
import de.hd.pvs.piosim.power.acpi.DevicePowerStates;
import de.hd.pvs.piosim.power.calculation.ACPICalculation;
import de.hd.pvs.piosim.power.calculation.CalculationException;
import de.hd.pvs.piosim.power.replay.ReplayDevice;
import de.hd.pvs.piosim.power.replay.ReplayException;

public class MultipleStatePlayStrategy implements PlayStrategy {

	private double[] utilizationStates = { 0.75, 0.5, 0.25, 0 };
	private static Logger logger = Logger.getLogger(MultipleStatePlayStrategy.class);

	public void setUtilizationStates(double[] utilizationStates)
			throws InvalidValueException {
		if (utilizationStates.length != DevicePowerStates.DEVICE_POWER_STATE_COUNT)
			throw new InvalidValueException("count states has to be "
					+ DevicePowerStates.DEVICE_POWER_STATE_COUNT);

		this.utilizationStates = utilizationStates;
	}

	@Override
	public void step(ReplayDevice replayDevice, int countSteps)
			throws ReplayException {
		ACPIDevice device = replayDevice.getACPIDevice();
		device.refresh();

		int steps = -1;
		int stepsize = replayDevice.getStepsize();

		// check for increasing state
		if (device.getDevicePowerState() < DevicePowerStates.DEVICE_POWER_STATE_COUNT - 1) {
			try {
				steps = ACPICalculation.calculateMinimalStateTimeForEfficiency(
						device, device.getDevicePowerState(),
						device.getDevicePowerState() + 1).intValue()
						/ stepsize + 1;
			} catch (CalculationException e) {
				// no energy saving possible because of device power schema
				steps = countSteps;
			}

			if (steps + replayDevice.getCurrentStep() < countSteps
					&& !device.hasEnqueuedStateChanges()) {
				BigDecimal[] futureUtilization = replayDevice
						.getFutureUtilization(steps);
				int i;
				
				for (i = 0; i < steps; ++i) {
					if (futureUtilization[i].doubleValue() > utilizationStates[device.getDevicePowerState()])
						break;
				}
				if (i == steps) {
					// increase acpi state

					logger.info("Step " + replayDevice.getCurrentStep()
							+ ": Putting device " + device
							+ " to device state "
							+ (device.getDevicePowerState() + 1));
					try {
						device.toACPIState(device.getDevicePowerState() + 1);
					} catch (ACPIDeviceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		// check for decreasing state

		for (int state = DevicePowerStates.DEVICE_POWER_STATE_0; state < device
				.getDevicePowerState(); ++state) {
			steps = device.getDurationForDevicePowerStateChange(state)
					.intValue()
					/ stepsize + 1;

			if (steps + replayDevice.getCurrentStep() < countSteps
					&& !device.hasEnqueuedStateChanges()) {
				BigDecimal[] futureUtilization = replayDevice
						.getFutureUtilization(steps);
				int i;
				for (i = 0; i < steps; ++i) {
					if (futureUtilization[i].doubleValue() > utilizationStates[state])
						break;
				}

				if (i != steps) {
					// decrease acpi state

					logger.info("Step " + replayDevice.getCurrentStep()
							+ ": Putting device " + device
							+ " to device state " + state);
					try {
						device.toACPIState(state);
					} catch (ACPIDeviceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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

}

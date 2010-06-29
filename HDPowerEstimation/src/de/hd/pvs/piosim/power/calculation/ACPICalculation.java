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
package de.hd.pvs.piosim.power.calculation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.IACPIAnalyzable;

/**
 * Class to encapsulate all acpi relevant calculations
 * @author Timo Minartz
 *
 */
public class ACPICalculation {
	
	private static BigDecimal msInHour = new BigDecimal("3600000");
	private static BigDecimal secInHour = new BigDecimal("3600");
	
	public static BigDecimal calculateSumDurationForDevicePowerStateChange(List<ACPIDevice> list, int fromState, int toState) {
		BigDecimal sum = new BigDecimal("0");
		for (ACPIDevice device : list) {
			sum = BaseCalculation.sum(sum, device.getDurationForDevicePowerStateChange(fromState, toState));
		}

		return sum;
	}
	
	public static BigDecimal calculateSumPowerConsumptionForDevicePowerStateChange(List<ACPIDevice> list, int fromState,int toState) {
		BigDecimal sum = new BigDecimal("0");
		for (ACPIDevice device : list) {
			sum = BaseCalculation.sum(sum, device.getPowerConsumptionForDevicePowerStateChange(fromState, toState));
		}

		return sum;
	}

	public static BigDecimal calculateSumACPIStateChangesTimeOverhead(
			List<IACPIAnalyzable> list) {
		BigDecimal sum = new BigDecimal("0");
		for (IACPIAnalyzable acpiAnalyzable : list) {
			sum = BaseCalculation.sum(sum, acpiAnalyzable
					.getACPIStateChangesTimeOverhead());
		}

		return sum;
	}

	public static BigDecimal calculateSumMaxPowerConsumption(
			List<IACPIAnalyzable> list) {
		BigDecimal sum = new BigDecimal("0");
		for (IACPIAnalyzable acpiAnalyzable : list) {
			sum = BaseCalculation.sum(sum, acpiAnalyzable.getMaxPowerConsumption());
		}

		return sum;
	}

	public static BigDecimal calculateSumPowerConsumption(
			List<IACPIAnalyzable> list) {
		BigDecimal sum = new BigDecimal("0");
		for (IACPIAnalyzable acpiAnalyzable : list) {
			sum = BaseCalculation.sum(sum, acpiAnalyzable.getPowerConsumption());
		}

		return sum;
	}

	/**
	 * 
	 * @param maxPowerConsumption maxPowerConsumption in watt
	 * @param durationInMs duration in watt
	 * @param powerConsumption powerConsumption in watt-h
	 * @return absolute savings in watt-h
	 */
	public static BigDecimal calculateAbsoluteACPIPowerSaving(
			BigDecimal maxPowerConsumption, BigDecimal durationInMs, BigDecimal powerConsumption) {
		return BaseCalculation.substract(calculateInWattH(maxPowerConsumption, durationInMs), powerConsumption);
	}

	public static BigDecimal calculateAbsoluteACPITime(BigDecimal timePassed,
			BigDecimal ACPITimeOverhead) {
		return BaseCalculation.sum(timePassed, ACPITimeOverhead);
	}

	public static BigDecimal calculateRelativeACPITimeInPercent(
			BigDecimal absoluteACPITime, BigDecimal timePassed) {
		try {
		return BaseCalculation.multiply(BaseCalculation.divide(BaseCalculation
				.substract(absoluteACPITime, timePassed), timePassed),
				BaseCalculation.HUNDRED);
		} catch (ArithmeticException ex) {
			// timePassed == 0
			return new BigDecimal("0");
		}
	}

	public static BigDecimal calculateRelativeACPIPowerSavingInPercent(
			BigDecimal maxPowerConsumption, BigDecimal powerConsumption) {
		try {
			return BaseCalculation.multiply(BaseCalculation.divide(
					BaseCalculation.substract(maxPowerConsumption,
							powerConsumption), maxPowerConsumption),
							BaseCalculation.HUNDRED);
		} catch (ArithmeticException ex) {
			// maxPowerConsumption == 0
			return new BigDecimal("0");
		}
	}
	

	/**
	 * 
	 * @param powerConsumptionInWattH
	 * @param currentPowerConsumption
	 * @param startTimeInMs
	 * @param lastChangeTimeInMs
	 * @return power consumption in watt-h
	 */
	public static BigDecimal sumPowerConsumptionTillNow(BigDecimal powerConsumptionInWattH, BigDecimal currentPowerConsumption, BigDecimal startTimeInMs, BigDecimal lastChangeTimeInMs) {	
		// powerConsumption + ((startTimeInMs - lastChangeTimeInMs) * currentPowerConsumption / msInHour);
		//return BaseCalculation.sum(powerConsumption, BaseCalculation.divide(BaseCalculation.multiply(BaseCalculation.substract(startTimeInMs, lastChangeTimeInMs),currentPowerConsumption),msInHour));
		
		// powerConsumption + ((startTimeInSec - lastChangeTimeInSec) * currentPowerConsumption) / 3600;
		return BaseCalculation.sum(powerConsumptionInWattH, BaseCalculation.divide(BaseCalculation.multiply(BaseCalculation.toSec(BaseCalculation.substract(startTimeInMs, lastChangeTimeInMs)),currentPowerConsumption),secInHour));
	}
	
	public static BigDecimal calculateMaxPowerConsumption(BigDecimal time, BigDecimal powerConsumptionInStateZeroWithFullUtilization) {
		
		return BaseCalculation.divide(BaseCalculation.multiply(time, powerConsumptionInStateZeroWithFullUtilization),msInHour);
	}

	/**
	 * 
	 * @param powerConsumption power consumption without overhead in watt-h
	 * @param overhead Overhead in watt 
	 * @param time Time in ms for the overhead
	 * @return powerConsumption + (overhead * (time/1000));
	 */
	public static BigDecimal calculateExtendedNotePowerConsumption(
			BigDecimal powerConsumption, BigDecimal overhead, BigDecimal time) {
		return BaseCalculation.sum(powerConsumption, calculateInWattH(overhead, time));
	}
	
	/**
	 * calculates the minimal time in ms for an efficient state change based on the following equation:
	 * 
	 * t_min = P_change / P_saving + t_change
	 * 
	 * An efficient change is based on the power consumption and duration for changing to another state and back again.
	 * 
	 * @param device to calculate for
	 * @param fromState starting and ending state
	 * @param toState in-between state
	 * @return minimal time in ms for which a state change is efficient
	 * @throws CalculationException
	 */
	public static BigDecimal calculateMinimalStateTimeForEfficiency(ACPIDevice device, int fromState, int toState) throws CalculationException {
		
		// duration for state change in ms
		BigDecimal duration = BaseCalculation.sum(device
				.getDurationForDevicePowerStateChange(fromState, toState), device
				.getDurationForDevicePowerStateChange(toState, fromState));
		
		// power consumption in watt-h for state change
		BigDecimal powerConsumption = BaseCalculation.sum(device
				.getPowerConsumptionForDevicePowerStateChange(fromState, toState),
				device
						.getPowerConsumptionForDevicePowerStateChange(toState,
								fromState));
		
		// power saving in watt
		BigDecimal powerSaving = BaseCalculation.substract(device.getComponentPowerSchema().getStatePowerConsumption(fromState),device.getComponentPowerSchema().getStatePowerConsumption(toState));
		
		if(powerSaving.doubleValue() <= 0.0)
			throw new CalculationException("Power saving is <= 0. No efficent change possible");
		
		
		// t_min = P_change / P_saving + t_change = watt-h / watt + ms
		return BaseCalculation.sum(BaseCalculation.multiply(BaseCalculation.divide(powerConsumption,powerSaving), msInHour), duration);
	}

	public static BigDecimal calculateSumDurationACPIStateChanges(
			ArrayList<IACPIAnalyzable> list) {
		BigDecimal sum = new BigDecimal("0");
		for (IACPIAnalyzable acpiAnalyzable : list) {
			sum = BaseCalculation.sum(sum, acpiAnalyzable
					.getACPIStateChangesTime());
		}

		return sum;
	}

	public static BigDecimal calculateInWatt(
			BigDecimal powerConsumptionInWattH, BigDecimal durationInMs) {
		return BaseCalculation.divide(BaseCalculation.multiply(powerConsumptionInWattH,secInHour),BaseCalculation.toSec(durationInMs));
	}
	
	/**
	 * 
	 * watt * durationInSec / 
	 * 
	 * @param powerConsumptionInWatt
	 * @param durationInMs
	 * @return power consumption in watt-h
	 */
	public static BigDecimal calculateInWattH(
			BigDecimal powerConsumptionInWatt, BigDecimal durationInMs) {
		return BaseCalculation.divide(BaseCalculation.multiply(powerConsumptionInWatt,BaseCalculation.toSec(durationInMs)),secInHour);
	}
	
	
}

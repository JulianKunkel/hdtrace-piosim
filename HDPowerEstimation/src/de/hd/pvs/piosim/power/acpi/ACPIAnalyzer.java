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
package de.hd.pvs.piosim.power.acpi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.hd.pvs.piosim.power.Time;
import de.hd.pvs.piosim.power.calculation.ACPICalculation;

public class ACPIAnalyzer {
	
	private static Logger logger = Logger.getLogger(ACPIAnalyzer.class);

	private List<IACPIAnalyzable> acpiAnalyzablesList = new ArrayList<IACPIAnalyzable>();

	/**
	 * 
	 * @return Time passed (without ACPI) in ms
	 */
	public BigDecimal getTimePassed() {
		refresh();
		return Time.getInstance().getCurrentTimeInMillis();
	}

	/**
	 * 
	 * @return Time overhead for ACPI in ms
	 */
	public BigDecimal getACPITimeOverhead() {
		refresh();
		
		return ACPICalculation.calculateSumACPIStateChangesTimeOverhead(acpiAnalyzablesList);
	}

	/**
	 * 
	 * @return Time passed inclusive ACPI overhead in ms
	 */
	public BigDecimal getAbsoluteACPITime() {
		refresh();

		return ACPICalculation.calculateAbsoluteACPITime(getTimePassed(), getACPITimeOverhead());
	}

	/**
	 * 
	 * @return Overhead for using ACPI in percent
	 */
	public BigDecimal getRelativeACPITime() {
		refresh();

		return ACPICalculation.calculateRelativeACPITimeInPercent(getAbsoluteACPITime(), getTimePassed());
	}

	/**
	 * 
	 * @return Power consumption in watt, based on running time and power
	 *         consumption in state 0 at 100 percent utilization
	 */
	public BigDecimal getMaxPowerConsumption() {
		refresh();
		
		return ACPICalculation.calculateSumMaxPowerConsumption(acpiAnalyzablesList);
	}

	/**
	 * 
	 * @return Power consumption in watt, based on value read from
	 *         acpiAnalyzable
	 */
	public BigDecimal getPowerConsumption() {
		refresh();
		
		return ACPICalculation.calculateSumEnergyConsumption(acpiAnalyzablesList);
	}

	/**
	 * 
	 * @return Power saved while using ACPI in watt
	 */
	public BigDecimal getAbsoluteACPIPowerSaving() {
		refresh();
		
		return ACPICalculation.calculateAbsoluteACPIEnergySaving(getMaxPowerConsumption(), getTimePassed(),getPowerConsumption());
	}

	/**
	 * 
	 * @return Power savings for using ACPI in percent
	 */
	public BigDecimal getRelativeACPIPowerSaving() {
		refresh();
		
		return ACPICalculation.calculateRelativeACPIPowerSavingInPercent(getMaxPowerConsumption(), getPowerConsumption());
	}

	public void setIACPIAnalyzable(IACPIAnalyzable acpiAnalyzable) {
		acpiAnalyzablesList.clear();
		acpiAnalyzablesList.add(acpiAnalyzable);
		refresh();
	}

	public void setIACPIAnalyzableList(List<IACPIAnalyzable> acpiAnalyzableList) {
		for (IACPIAnalyzable acpiAnalyzable : acpiAnalyzableList) {
			this.acpiAnalyzablesList.add(acpiAnalyzable);
		}
		refresh();
	}

	private void refresh() {
		for (IACPIAnalyzable acpiAnalyzable : acpiAnalyzablesList) {
			acpiAnalyzable.refresh();
		}
	}

	public void printIACPIAnalyzableStatistics() {
		refresh();
		for (int i = 0; i < 80; ++i)
			logger.info("=");

		if (this.acpiAnalyzablesList.size() == 1) {
			logger.info("ACPI statistics for component: "
					+ this.acpiAnalyzablesList.get(0).getClass()
							.getSimpleName());
		} else {
			logger.info("Global ACPI statistics");
			this.setIACPIAnalyzableList(new ArrayList<IACPIAnalyzable>(ACPIDevice.getDevices()));
		}

		logger.info("Total power: " + getPowerConsumption() + " watt");
		logger.info("Total time: " + getAbsoluteACPITime() + " ms");

		logger.info("saved power: " + getAbsoluteACPIPowerSaving()
				+ " watt = " + getRelativeACPIPowerSaving() + " %");
		logger.info("time overhead: " + getACPITimeOverhead() + " ms = "
				+ getRelativeACPITime() + " %");

		for (int i = 0; i < 80; ++i)
			logger.info("=");
	}

}

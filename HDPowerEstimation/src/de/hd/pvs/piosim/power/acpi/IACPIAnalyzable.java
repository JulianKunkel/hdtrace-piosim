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

/**
 * Interface encapsulates all methods needed to analyze the acpi values
 * like power consumption etc.
 * @author Timo Minartz
 *
 */
public interface IACPIAnalyzable {
	
	/**
	 * calculates the power consumption for all subcomponents
	 * @return total power consumption in watt
	 */
	public BigDecimal getPowerConsumption();
	
	/**
	 * calculates the time spend for acpi state changes
	 * @return time spend for acpi state changes in ms
	 */
	public BigDecimal getACPIStateChangesTime();
	
	
	/**
	 * The overhead in ms (should be zero)
	 * @return overhead in ms
	 */
	public BigDecimal getACPIStateChangesTimeOverhead();
	
	/**
	 * calculates the maximum power consumption if all subcomponents don't support acpi
	 * @return max power consumption in watt
	 */
	public BigDecimal getMaxPowerConsumption();
	
	/**
	 * calls the underlying refresh method to schedule all outstanding acpi operations
	 */
	public void refresh();

	
}

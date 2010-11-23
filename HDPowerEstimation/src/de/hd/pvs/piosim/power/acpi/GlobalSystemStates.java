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

public interface GlobalSystemStates {
	
	public static final int GLOBAL_SYSTEM_STATE_COUNT = 4;
	public static final int GLOBAL_SYSTEM_STATE_0 = 0;
	public static final int GLOBAL_SYSTEM_STATE_1 = 1;
	public static final int GLOBAL_SYSTEM_STATE_2 = 2;
	public static final int GLOBAL_SYSTEM_STATE_3 = 3;
	
	public int getGlobalSystemState();
	public void switchToGlobalSystemState(int state) throws ACPIDeviceException;
	public BigDecimal getDurationForGlobalSystemStateChange(int fromState, int toState);
	public BigDecimal getEnergyConsumptionForGlobalSystemStateChange(int fromState, int toState);

}

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
package de.hd.pvs.piosim.power;

import java.math.BigDecimal;

import de.hd.pvs.piosim.power.calculation.BaseCalculation;


public class Time {

	private BigDecimal time = new BigDecimal("0");
	private static Time instance = null;
	
	private Time() {}
	
	public static Time getInstance() {
		if(instance == null) {
			instance = new Time();
		}
		
		return instance;
	}
	
	public void timePassed(int millis) {
		time = BaseCalculation.sum(time, new BigDecimal(millis));
	}
	
	public BigDecimal getCurrentTimeInMillis() {
		return time;
	}
	
	public void reset() {
		time = new BigDecimal("0");
	}
}

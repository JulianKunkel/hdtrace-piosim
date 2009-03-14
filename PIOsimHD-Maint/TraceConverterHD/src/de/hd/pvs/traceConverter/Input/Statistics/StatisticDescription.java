
//	Copyright (C) 2008, 2009 Julian M. Kunkel
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

/**
 * 
 */
package de.hd.pvs.traceConverter.Input.Statistics;


public class StatisticDescription{
	final StatisticType type;
	final String name;
	
	final String unit;
	final int multiplier;
	
	public StatisticDescription(String name, StatisticType type, String unit, int multiplier) {
		this.name = name;
		this.type = type;
		this.multiplier = multiplier;
		this.unit = unit;
	}
	
	public String getName() {
		return name;
	}
	
	public StatisticType getType() {
		return type;
	}
	
	public int getMultiplier() {
		return multiplier;
	}
	
	public String getUnit() {
		return unit;
	}
}
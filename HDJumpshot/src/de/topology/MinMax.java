//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.

package de.topology;

/**
 * Manages just a min and a maximum value.
 * @author julian
 */
public class MinMax {
	private double maxValue = Double.MIN_NORMAL;
	private double minValue = Double.MAX_VALUE;
	
	public void updateMaxValue(double newValue) {
		if(newValue > this.maxValue)
			this.maxValue = newValue;
	}

	public void updateMinValue(double newValue) {
		if(newValue < this.minValue)
			this.minValue = newValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public double getMinValue() {
		return minValue;
	}
}
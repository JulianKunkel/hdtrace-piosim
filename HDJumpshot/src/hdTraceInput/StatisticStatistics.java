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

package hdTraceInput;

/**
 * Statistics for a single statistic
 * @author Julian M. Kunkel
 *
 */
public class StatisticStatistics {
	final double maxValue;
	final double minValue;
	final double averageValue;
	final double stddevValue;
	
	public StatisticStatistics( double maxNumericValue, double minNumericValue, double averageNumericValue, double stddev) {
		this.maxValue = maxNumericValue;
		this.minValue = minNumericValue;
		this.averageValue = averageNumericValue;
		this.stddevValue = stddev;
	}	
	
	public double getAverageValue() {
		return averageValue;
	}
	
	public double getMaxValue() {
		return maxValue;
	}
	
	public double getMinValue() {
		return minValue;
	}
	
	public double getStddevValue() {
		return stddevValue;
	}
}

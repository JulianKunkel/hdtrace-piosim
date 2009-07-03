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

package de.viewer.graph;

import java.awt.Color;
import java.math.BigDecimal;

/**
 * Contains the input data for the histogram 
 * @author Julian M. Kunkel
 */
public class HistogramDoubleData extends HistogramData{
	final double [] bins;
	/**
	 * Sum of all bin values:
	 */
	final double aggregatedValue;
	
	final double maxValueInBin;
	

	
	public HistogramDoubleData(String title, Color color, double [] bins, double xOffset, double xExtend) {
		super(title, color, xOffset, xExtend);
		
		this.bins = bins;
		
		// determine max value:
		BigDecimal aggr = new BigDecimal(0);
		double maxValue = 0;
		for(double binVal: bins){
			maxValue = binVal > maxValue ? binVal : maxValue;
			aggr = aggr.add(new BigDecimal(binVal));
		}
		this.maxValueInBin = maxValue;
		this.aggregatedValue = aggr.doubleValue();
	}
	
	public double getAggregatedBinValues() {
		return aggregatedValue;
	}
	
	@Override
	public int getBinCount() {			
		return bins.length;
	}
	
	@Override
	public double getMaxY() {		
		return maxValueInBin;
	}		
	
	
	@Override
	public ElementEnumeration getYValues() {
		return new HistogramBinDoubleEnumeration(bins);
	}
	
	public double[] getBins() {
		return bins;
	}
	
	private class HistogramBinDoubleEnumeration  implements ElementEnumeration{
		int pos = 0;
		final double [] values;
		
		public HistogramBinDoubleEnumeration(double[] values) {
			this.values = values;
		}
		
		@Override
		public boolean hasMoreElements() {
			return pos < values.length;
		}
		
		@Override
		public double nextElement() {
			return values[pos++];
		}			
	}
	
}
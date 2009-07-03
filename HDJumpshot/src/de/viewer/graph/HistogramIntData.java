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

/**
 * Contains the input data for the histogram 
 * @author Julian M. Kunkel
 */
public class HistogramIntData extends HistogramData{
	final int [] bins;
	
	final int maxValueInBin;
	
	public HistogramIntData(String title, Color color, int [] bins, double xOffset, double xExtend) {
		super(title, color, xOffset, xExtend);
		this.bins = bins;
		
		// determine max value:
		int maxValue = 0;
		for(int binVal: bins){
			maxValue = binVal > maxValue ? binVal : maxValue; 
		}
		this.maxValueInBin = maxValue;
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
		return new HistogramBinEnumeration(bins);
	}
	
	public int[] getBins() {
		return bins;
	}
		
	private class HistogramBinEnumeration  implements ElementEnumeration{
		int pos = 0;
		final int [] values;
		
		public HistogramBinEnumeration(int [] values) {
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
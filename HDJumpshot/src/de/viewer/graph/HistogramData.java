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
public abstract class HistogramData extends GraphData{
	
	final private double averageValue;
	final private double aggregatedValue;
	
	final private double xOffset;
	final private double xExtend;

	final public double getXExtend() {
		return xExtend;
	}
	
	final public double getDeltaPerBin() {
		return xExtend / getBinCount();
	}
		
	public HistogramData(String title, Color color, double xOffset, double xExtend, double avgValue, double aggregatedValue) {
		super(title, color);
		
		this.xOffset = xOffset;
		this.xExtend = xExtend;
		this.averageValue = avgValue;
		this.aggregatedValue = aggregatedValue;
	}
	
	abstract public int getBinCount();

	@Override
	abstract public double getMaxY();
	
	@Override
	abstract public ElementEnumeration getYValues();

	@Override
	final public double getMaxX() {
		return xOffset + xExtend;
	}
	
	@Override
	final public double getMinX() {
		return xOffset;
	}
	
	
	@Override
	final public double getMinY() {
		return 0;
	}	
	
	@Override
	final public ElementEnumeration getXValues() {			
		return new HistogramXAxisEnumeration();
	}
	
	public double getAggregatedValue() {
		return aggregatedValue;
	}
	
	public double getAverageValue() {
		return averageValue;
	}
	
	private class HistogramXAxisEnumeration  implements ElementEnumeration{
		int curPos = 0;
		
		@Override
		public boolean hasMoreElements() {
			return curPos < getBinCount();
		}
		
		@Override
		public double nextElement() {
			return xOffset + getDeltaPerBin() * curPos++;
		}
	}	
}
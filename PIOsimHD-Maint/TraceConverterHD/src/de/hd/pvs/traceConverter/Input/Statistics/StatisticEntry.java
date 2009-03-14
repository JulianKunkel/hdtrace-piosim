
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

package de.hd.pvs.traceConverter.Input.Statistics;

import de.hd.pvs.piosim.model.util.Epoch;

/**
 * Read values from a statistic file, aka Statistic Group. 
 * 
 * @author Julian M. Kunkel
 *
 */
public class StatisticEntry {
	
	/**
	 * Maps the statistic name to the measured value.
	 */
	private final Object [] values;
	
	private final Epoch timeStamp; 
		
	public StatisticEntry(Object [] values, Epoch timeStamp) {
		this.values = values;
		this.timeStamp = timeStamp;
	}
	
	public Object[] getValues() {
		return values;
	}
	
	public Epoch getTimeStamp() {
		return timeStamp;
	}
}

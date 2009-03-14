
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

package de.hd.pvs.piosim.model.components.IOSubsystem;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.AttributeGetters;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNull;

/**
 * An simple IOSubsystem contains information for a single disk or flash drive. 
 * 
 * @author Julian M. Kunkel
 */
public class SimpleDisk extends IOSubsystem {

	@Attribute
	@NotNull
	/**
	 * The access time which must be spent before an I/O operation can be performed.
	 */
	private Epoch avgAccessTime = Epoch.ZERO;
	
	/**
	 * The maximum sequential throughput. Once the Component spend the averageAccessTime it can
	 * access data with that speed.
	 */
	@Attribute
	@NotNegativeOrZero
	private long maxThroughput = -1;
	
	
	@AttributeGetters 
	public Epoch getAvgAccessTime(){
		return avgAccessTime;
	}

	/**
	 * @return the maxThroughput
	 */
	@AttributeGetters 
	public long getMaxThroughput() {
		return maxThroughput;
	}

	/**
	 * @param maxThroughput
	 *            the maxThroughput to set
	 */
	public void setMaxThroughput(long maxThroughput) {
		this.maxThroughput = maxThroughput;
	}

	/**
	 * @param avgAccessTime
	 *            the avgAccessTime to set
	 */
	public void setAvgAccessTime(Epoch avgAccessTime) {
		this.avgAccessTime = avgAccessTime;
	}
		
}

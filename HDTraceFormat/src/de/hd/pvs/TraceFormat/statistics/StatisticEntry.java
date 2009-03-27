
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
  */

//	Copyright (C) 2009 Julian M. Kunkel
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


package de.hd.pvs.TraceFormat.statistics;

import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * A single read statistic value.
 * 
 * @author Julian M. Kunkel
 */
public class StatisticEntry implements TraceObject {
	final StatisticGroupEntry parentGroupEntry;
	final StatisticDescription description;	
	
	final Object value;
	
	public StatisticEntry(Object value, StatisticDescription description, StatisticGroupEntry parentGroupEntry) {
			this.value = value;
			this.description = description;
			this.parentGroupEntry = parentGroupEntry;
	}
	
	public Object getValue() {
		return value;
	}
	
	public Number getNumericValue() {
		return (Number) value;
	}
	
	public StatisticDescription getDescription() {
		return description;
	}
	
	public StatisticGroupEntry getParentGroupEntry() {
		return parentGroupEntry;
	}	
	
	@Override
	public TraceObjectType getType() {
		return TraceObjectType.STATISTICENTRY;
	}
	
	@Override
	public Epoch getEarliestTime() {
		return parentGroupEntry.getEarliestTime();
	}	
	
	@Override
	public Epoch getLatestTime() {	
		return parentGroupEntry.getLatestTime();
	}
}


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

import de.hd.pvs.TraceFormat.ITracableObject;
import de.hd.pvs.TraceFormat.TracableObjectType;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * A single statistic value. It is part of a statistics group entry.
 * 
 * @author Julian M. Kunkel
 */
public class StatisticsEntry implements ITracableObject {
	final StatisticsGroupEntry parentGroupEntry;
	final StatisticsDescription description;	
	
	final Object value;
	
	public StatisticsEntry(Object value, StatisticsDescription description, StatisticsGroupEntry parentGroupEntry) {
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
	
	public StatisticsDescription getDescription() {
		return description;
	}
	
	public StatisticsGroupEntry getParentGroupEntry() {
		return parentGroupEntry;
	}	
	
	@Override
	public TracableObjectType getType() {
		return TracableObjectType.STATISTICENTRY;
	}
	
	@Override
	public Epoch getEarliestTime() {
		return parentGroupEntry.getEarliestTime();
	}	
	
	@Override
	public Epoch getLatestTime() {	
		return parentGroupEntry.getLatestTime();
	}
	
	@Override
	final public Epoch getDurationTime() {
		return getLatestTime().subtract(getEarliestTime());
	}
}

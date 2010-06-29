//	Copyright (C) 2010 Timo Minartz
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
package de.hd.pvs.piosim.power.acpi.history;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import de.hd.pvs.piosim.power.acpi.ACPIComponent;

public class ACPIStateChangesHistory {

	public static final int STATE_END = -1;
	public static final int STATE_CHANGE = -2;
	
	private List<ACPIStateChange> entries;
	
	private static ACPIStateChangesHistory instance = null;
	
	private String name;
	
	private ACPIStateChangesHistory() {
		entries = new ArrayList<ACPIStateChange>();
	}
	
	private ACPIStateChangesHistory(ACPIStateChangesHistory history) {
		entries = new ArrayList<ACPIStateChange>();
		for(int i=0; i<history.getSize(); ++i) {
			ACPIStateChange entry = history.get(i);
			entries.add(new ACPIStateChange(entry.getTime(),entry.getACPIComponent(),entry.getState(),entry.getPowerConsumption()));
		}
		name = history.getName();
	}
	
	public static ACPIStateChangesHistory getInstance() {
		if(instance == null)
			instance = new ACPIStateChangesHistory();
		
		return instance;
	}
	
	public static ACPIStateChangesHistory getCopy() {
		return new ACPIStateChangesHistory(ACPIStateChangesHistory.getInstance());
	}

	/**
	 * 
	 * @param time
	 * @param component
	 * @param state
	 * @param powerConsumption power consumption till now
	 */
	public void add(BigDecimal time, ACPIComponent component, int state, BigDecimal powerConsumption) {
		ACPIStateChange stateChange = new ACPIStateChange(time,component, state, powerConsumption);
		entries.add(stateChange);
	}
	
	public void print() {
		for(ACPIStateChange entry : entries) {
			entry.print();
		}
	}
	
	public void reset() {
		entries.clear();
	}

	public int getSize() {
		return entries.size();
	}

	public ACPIStateChange get(int i) {
		return entries.get(i);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}


}

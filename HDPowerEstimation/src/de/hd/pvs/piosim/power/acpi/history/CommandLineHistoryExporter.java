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

public class CommandLineHistoryExporter {
	
	public static void export(ACPIStateChangesHistory history) {
		for(int i=0; i<history.getSize(); ++i) {
			ACPIStateChange entry = history.get(i);
			
			if(entry.getState() == ACPIStateChangesHistory.STATE_END) {
				System.out.println(entry.getTime() + ": " + entry.getACPIComponent().getName() + " State finished");
			} else {
				System.out.println(entry.getTime() + ": " + entry.getACPIComponent().getName() + " new State " + entry.getACPIComponent().getStateName(entry.getState()));
			}
		}
	}

}

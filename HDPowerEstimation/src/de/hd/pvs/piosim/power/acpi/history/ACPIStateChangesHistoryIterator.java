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

import java.util.List;

public class ACPIStateChangesHistoryIterator {
	
	private final int INDEX_NOT_CALCULATED = -2;
	private final int NO_MORE_ENTRIES = -1;
	private List<ACPIStateChangesHistory> histories;
	private int[] indices;
	private int minIndex;

	public ACPIStateChangesHistoryIterator(
			List<ACPIStateChangesHistory> histories) {
		
		indices = new int[histories.size()];
		
		for(int i=0; i<indices.length; ++i)
			indices[i] = 0;
		
		this.histories = histories;
		minIndex = INDEX_NOT_CALCULATED;
		
	}

	public boolean hasEntries() {
		if(minIndex == INDEX_NOT_CALCULATED)
			minIndex = getNextHistoryIndex();
		
		return minIndex != NO_MORE_ENTRIES;
	}

	public ACPIStateChange getNextEntry() {
		
		if(minIndex == INDEX_NOT_CALCULATED)
			minIndex = getNextHistoryIndex();
		
		int listIndex = minIndex;
		
		if(listIndex == NO_MORE_ENTRIES)
			return null;
		
		int historyIndex = indices[listIndex];
		
		indices[listIndex] = historyIndex + 1;
		
		minIndex = INDEX_NOT_CALCULATED;
		
		return histories.get(listIndex).get(historyIndex);
	}

	public int getNextHistoryIndex() {
		
		if(minIndex == INDEX_NOT_CALCULATED) {
			minIndex = NO_MORE_ENTRIES;
			ACPIStateChange minEntry = null;

			for(int i=0; i<histories.size(); ++i) {
				if(indices[i] < histories.get(i).getSize()) {
					ACPIStateChange entry = histories.get(i).get(indices[i]);

					if(minEntry == null || entry.getTime().compareTo(minEntry.getTime()) < 0) {
						minEntry = entry;
						minIndex = i;
					}
				}
			}
		}
		
		return minIndex;
	}

}

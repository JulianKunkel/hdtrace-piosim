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

package arrow;

import hdTraceInput.TraceFormatBufferedFileReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.hd.pvs.TraceFormat.TraceFormatFileOpener;

/**
 * Manufacture arrows for relations
 * 
 * @author Julian M. Kunkel
 */
public class RelationArrowComputer implements ArrowComputer{

	final ArrowCategory category = new ArrowCategory("Relations", null);
	
	@Override
	public ArrowCategory getResponsibleCategory() {		
		return category;
	}
	
	@Override
	public ArrowsOrdered computeArrows(TraceFormatBufferedFileReader reader) {
		final ArrayList<Arrow> arrows = new ArrayList<Arrow>();

		for(int i=0 ; i < reader.getNumberOfFilesLoaded(); i++){
			// scan for rank label
			final TraceFormatFileOpener file = reader.getLoadedFile(i);

			
		}

		// sort the arrows by starting time:
		Collections.sort(arrows, new Comparator<Arrow>(){
			@Override
			public int compare(Arrow o1, Arrow o2) {				
				return o1.getStartTime().compareTo(o2.getStartTime());
			}
		});

		return new ArrowsOrdered(arrows);
	}
}

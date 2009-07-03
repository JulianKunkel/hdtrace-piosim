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

package de.arrow;

import de.hdTraceInput.TraceFormatBufferedFileReader;

/**
 * Abstract interface providing methods to compute a group of arrows with an dedicated task. 
 * 
 * @author Julian M. Kunkel
 */
public interface ArrowComputer {
	public ArrowsOrdered computeArrows(TraceFormatBufferedFileReader reader);
	public ArrowCategory getResponsibleCategory();
	
	/**
	 * Could be implemented to hide categories which are not applicable to the current reader.
	 */
	// public boolean isUseful(TraceFormatBufferedFileReader reader); 
}

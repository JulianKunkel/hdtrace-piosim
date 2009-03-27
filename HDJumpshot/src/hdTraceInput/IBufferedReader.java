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


package hdTraceInput;

import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.util.Epoch;

public interface IBufferedReader {
	
	/**
	 * Minimum time available in the file
	 * @return
	 */
	public Epoch getMinTime();
	
	/**
	 * Maximum time available in the file
	 * @return
	 */
	public Epoch getMaxTime();
	
	/**
	 * Return the trace entry which covers or the one which is closest to this time.
	 * 
	 * @param time
	 * @return
	 */
	public TraceObject getTraceEntryClosestToTime(Epoch dTime);
}

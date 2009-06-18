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

package viewer.timelines.topologyPlugins;

import de.hd.pvs.TraceFormat.ReservedTopologyNames;

public interface MPIConstants {
	public final String RANK_TOPOLOGY = ReservedTopologyNames.Rank.toString();
	
	public final String XML_FILEOPEN = "File_open";
	public final String XML_FILE_SETVIEW = "File_set_view";
	public final String XML_FILECLOSE = "File_close";
}


 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
  */

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


package topology;

import viewer.timelines.TimelineType;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.topology.TopologyEntry;
import de.hd.pvs.TraceFormat.trace.TraceSource;

public class TopologyTraceTreeNode extends TopologyTreeNode{

	private final String label;
	
	public TopologyTraceTreeNode(String label, TopologyEntry topNode,
			TraceFormatFileOpener file, TopologyManager manager) {
		super(topNode, file, manager);
		this.label = label;
	}

	public TraceSource getTraceSource(){
		return getTopology().getTraceSource();
	}
	
	@Override
	public TimelineType getType() {
		return TimelineType.TRACE;
	}
	
	@Override
	public String toString() {
		return label;
	}
}


 /** Version Control Information $Id: TopologyTraceTreeNode.java 242 2009-04-25 19:25:34Z kunkel $
  * @lastmodified    $Date: 2009-04-25 21:25:34 +0200 (Sa, 25 Apr 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 242 $ 
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

import hdTraceInput.BufferedRelationReader;
import viewer.timelines.TimelineType;
import de.hd.pvs.TraceFormat.ITracableObject;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.util.Epoch;

public class TopologyRelationTreeNode extends TopologyTreeNode
{
	private static final long serialVersionUID = 3518866075690297655L;
	
	private final String label;
	
	public TopologyRelationTreeNode(
			String label, 
			TopologyNode topNode,
			TraceFormatFileOpener file
	) {
		super(topNode, file);
		this.label = label;
	}

	public BufferedRelationReader getRelationSource(){
		return (BufferedRelationReader) getTopology().getRelationSource();
	}
		
	@Override
	public TimelineType getType() {
		return TimelineType.RELATION;
	}
	
	@Override
	public String toString() {
		return label;
	}
	
	public ITracableObject getTraceEntryClosestToTime(Epoch time) {		
		return getRelationSource().getTraceEntryClosestToTime(time);
	}
	
	public int getMaximumConcurrentRelationEntries(){
		return getRelationSource().getMaximumConcurrentRelationEntries();
	}	
}


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


package de.topology;


import java.util.ArrayList;
import java.util.Enumeration;

import de.hd.pvs.TraceFormat.ITracableObject;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.relation.RelationEntry;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hdTraceInput.ITraceElementEnumerator;
import de.viewer.timelines.TimelineType;

public class TopologyRelationExpandedTreeNode extends TopologyRelationTreeNode
{
	private static final long serialVersionUID = 3518866075690297655L;
	
	private final int line;
		
	public TopologyRelationExpandedTreeNode(
			String label, 
			TopologyNode topNode,
			TraceFormatFileOpener file
	) 
	{
		super(label, topNode, file);
		this.line = 0;		
	}
	
	public TopologyRelationExpandedTreeNode(
			int line, 
			TopologyNode topNode,
			TraceFormatFileOpener file
	) {
		super(Integer.toString(line), topNode, file);
		this.line = line;
	}
	
	public boolean isTheOnlyRelation() {
		return getRelationSource().getMaximumConcurrentRelationEntries() == 1;
	}

	@Override
	public TimelineType getType() {
		return TimelineType.RELATION_EXPANDED;
	}
	
	public int getLine() {
		return line;
	}
	
	public ArrayList<RelationEntry> getEntries() {
		return getRelationSource().getEntriesOnLine(line);
	}
	
	@Override
	public Enumeration<RelationEntry> enumerateEntries(Epoch start, Epoch end) {
		return getRelationSource().enumerateRelations(start, end, line);
	}
	
	@Override
	public Enumeration<RelationEntry> enumerateEntries() {
		return getRelationSource().enumerateRelations(line);
	}
	
	public ITracableObject getTraceEntryClosestToTime(Epoch time) {
		return getRelationSource().getTraceEntryClosestToTime(time, line);
	}
	
	public ITraceElementEnumerator enumerateTraceEntries(boolean nested,
			Epoch startTime, Epoch endTime) {	
		return getRelationSource().enumerateTraceEntries(nested, startTime, endTime, line);
	}	
	
	@Override
	public boolean equalTopology(TopologyTreeNode obj) {	
		return super.equalTopology(obj) && ((TopologyRelationExpandedTreeNode) obj).line == line;
	}
}

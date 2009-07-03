
 /** Version Control Information $Id: TopologyInnerNode.java 242 2009-04-25 19:25:34Z kunkel $
  * @lastmodified    $Date: 2009-04-25 21:25:34 +0200 (Sa, 25. Apr 2009) $
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

import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.viewer.timelines.TimelineType;

public class TopologyInnerNode extends TopologyTreeNode{
	private static final long serialVersionUID = 8019988454872650120L;

	public TopologyInnerNode(TopologyNode topNode,	TraceFormatFileOpener file) {
		super(topNode, file);
	}

	@Override
	public TimelineType getType() {
		return TimelineType.INNER_NODE;
	}	
}

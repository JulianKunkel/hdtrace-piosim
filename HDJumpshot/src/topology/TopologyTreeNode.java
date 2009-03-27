
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

import javax.swing.tree.DefaultMutableTreeNode;

import viewer.timelines.TimelineType;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.topology.TopologyInternalLevel;

/**
 * 
 * Tree Node object which encapsulates all the information about the file and traces.
 * 
 * @author Julian M. Kunkel
 */
abstract public class TopologyTreeNode extends DefaultMutableTreeNode{
	// topology belonging to this node:
	final TopologyInternalLevel topology;
	final TraceFormatFileOpener file;	
	final TopologyManager       manager;
	
	abstract public TimelineType getType();
	
	public TopologyTreeNode(TopologyInternalLevel topNode,  TraceFormatFileOpener file, TopologyManager manager) {
		this.topology = topNode;
		this.manager = manager;
		this.file = file;
	}
	
	public TopologyInternalLevel getTopology() {
		return topology;
	}

	public TraceFormatFileOpener getFile() {
		return file;
	}
	
	public TopologyManager getManager() {
		return manager;
	}
	
	@Override
	public String toString() {
		return topology.getLabel();
	}
}

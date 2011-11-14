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

import de.hd.pvs.TraceFormat.ITracableObject;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.topology.TopologyTreeNode;

/**
 * An arrow interconnects two topologies at a given time.
 * @author Julian M. Kunkel
 *
 */
public class Arrow{
	final private TopologyTreeNode startTreeNode;
	final private TopologyTreeNode endTreeNode;

	final private ITracableObject sourceObj; // startReason
	final private ITracableObject targetObj;

	final private Epoch startTime;
	final private Epoch endTime;
	final private ArrowCategory category;

	/**
	 * @param firstTopology
	 * @param firstTime The absolute time (not viewer time) for the start topology
	 * @param scndTopology
	 * @param scndTime The absolute time (not viewer time) for the end topology
	 * @param category
	 */
	public Arrow(TopologyTreeNode firstTopology, Epoch firstTime,  TopologyTreeNode scndTopology, Epoch scndTime, ArrowCategory category) {
		this.endTime = scndTime;
		this.startTime = firstTime;		
		this.endTreeNode = scndTopology;
		this.startTreeNode = firstTopology;

		this.targetObj = null;
		this.sourceObj = null;
		this.category = category;		
	}

	/**
	 * @param firstTopology
	 * @param firstTime The absolute time (not viewer time) for the start topology
	 * @param scndTopology
	 * @param scndTime The absolute time (not viewer time) for the end topology
	 * @param category
	 */
	public Arrow(TopologyTreeNode firstTopology, Epoch firstTime, ITracableObject srcObc, TopologyTreeNode scndTopology , 
			Epoch scndTime, ITracableObject tgtObj, ArrowCategory category) {
		this.endTime = scndTime;
		this.startTime = firstTime;		
		this.endTreeNode = scndTopology;
		this.startTreeNode = firstTopology;
		this.sourceObj = srcObc;
		this.targetObj = tgtObj;
		this.category = category;
	}

	public TopologyTreeNode getEndTreeNode() {
		return endTreeNode;
	}
	
	public TopologyTreeNode getStartTreeNode() {
		return startTreeNode;
	}
	
	public Epoch getEndTime() {
		return endTime;
	}

	public Epoch getStartTime() {
		return startTime;
	}

	public ArrowCategory getCategory() {
		return category;
	}
	
	public ITracableObject getSourceObj() {
		return sourceObj;
	}
	
	public ITracableObject getTargetObj() {
		return targetObj;
	}	
	
	@Override
	public String toString() {	
		return startTreeNode + "-" + endTreeNode;
	}
}

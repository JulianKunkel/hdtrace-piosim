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

import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * An arrow interconnects two topologies at a given time.
 * @author Julian M. Kunkel
 *
 */
public class Arrow{
	final private TopologyNode startTopology;
	final private TopologyNode endTopology;

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
	public Arrow(TopologyNode firstTopology, Epoch firstTime, TopologyNode scndTopology , 
			Epoch scndTime, ArrowCategory category) {
		if(firstTime.compareTo(scndTime) <= 0){
			this.endTime = scndTime;
			this.startTime = firstTime;		
			this.endTopology = scndTopology;
			this.startTopology = firstTopology;
		}else{
			this.endTime = firstTime;
			this.startTime = scndTime;		
			this.endTopology = firstTopology;
			this.startTopology = scndTopology;
		}
		this.category = category;
	}

	public Epoch getEndTime() {
		return endTime;
	}

	public Epoch getStartTime() {
		return startTime;
	}

	public TopologyNode getEndTopology() {
		return endTopology;
	}

	public TopologyNode getStartTopology() {
		return startTopology;
	}

	public ArrowCategory getCategory() {
		return category;
	}
}

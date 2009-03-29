
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
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticSource;
import de.hd.pvs.TraceFormat.topology.TopologyEntry;

public class TopologyStatisticTreeNode extends TopologyTreeNode {
	private static final long serialVersionUID = 7893694713193686328L;
	
	final StatisticSource statisticSource;
	final StatisticDescription statisticDescription;
	
	public TopologyStatisticTreeNode(StatisticDescription statDesc, StatisticsGroupDescription group, TopologyEntry topNode, TraceFormatFileOpener file, TopologyManager manager) {
		super(topNode, file, manager);
		
		this.statisticSource = topNode.getStatisticSource(group.getName());
		this.statisticDescription = statDesc;
		
	}
	
	public String getStatisticName() {
		return statisticDescription.getName();
	}
	
	public StatisticSource getStatisticSource() {
		return statisticSource;
	}
	
	@Override
	public TimelineType getType() {	
		return TimelineType.STATISTIC;
	}
	
	@Override
	public String toString() {
		return getStatisticName();
	}
	
	public int getNumberInGroup() {
		return statisticDescription.getNumberInGroup();
	}
}
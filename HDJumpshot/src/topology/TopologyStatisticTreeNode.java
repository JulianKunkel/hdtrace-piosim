
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

import hdTraceInput.BufferedStatisticsFileReader;
import viewer.timelines.TimelineType;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsSource;
import de.hd.pvs.TraceFormat.topology.TopologyNode;

public class TopologyStatisticTreeNode extends TopologyTreeNode {
	private static final long serialVersionUID = 7893694713193686328L;
	
	final StatisticsSource statisticSource;
	final StatisticsDescription statisticDescription;
	
	public TopologyStatisticTreeNode(StatisticsDescription statDesc, String group, TopologyNode topNode, TraceFormatFileOpener file) {
		super(topNode, file);
		
		this.statisticSource = topNode.getStatisticsSource(group);
		this.statisticDescription = statDesc;		
	}
	
	public StatisticsGroupDescription getStatisticGroup(){
		return ((BufferedStatisticsFileReader) statisticSource).getGroup();
	}
		
	public StatisticsDescription getStatisticDescription() {
		return statisticDescription;
	}
	
	public BufferedStatisticsFileReader getStatisticSource() {
		return (BufferedStatisticsFileReader) statisticSource;
	}
	
	@Override
	public TimelineType getType() {	
		return TimelineType.STATISTIC;
	}
	
	@Override
	public String toString() {
		return statisticDescription.getName();
	}
	
	public int getNumberInGroup() {
		return statisticDescription.getNumberInGroup();
	}
}

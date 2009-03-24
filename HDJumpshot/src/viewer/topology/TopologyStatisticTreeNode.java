package viewer.topology;

import viewer.timelines.TimelineType;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticSource;
import de.hd.pvs.TraceFormat.topology.TopologyInternalLevel;

public class TopologyStatisticTreeNode extends TopologyTreeNode {
	final StatisticSource statisticSource;
	final StatisticDescription statisticDescription;
	
	public TopologyStatisticTreeNode(StatisticDescription statDesc, ExternalStatisticsGroup group, TopologyInternalLevel topNode, TraceFormatFileOpener file, TopologyManager manager) {
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

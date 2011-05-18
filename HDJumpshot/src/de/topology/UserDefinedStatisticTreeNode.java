package de.topology;

import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsEntryType;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hdTraceInput.BufferedStatisticsFileReader;
import de.viewer.common.SortedJTreeNode;

/**
 * Permit a user defined function to aggregate upon multiple topologies
 * 
 * @author julian
 */
public class UserDefinedStatisticTreeNode extends TopologyStatisticTreeNode{	
	public UserDefinedStatisticTreeNode(TopologyTreeNode node) {
		//StatisticsGroupDescription group, String name, StatisticsEntryType datatype, int numberInGroup, String unit, String grouping
		super(new StatisticsDescription(new StatisticsGroupDescription("testgroup"), "test", StatisticsEntryType.DOUBLE, 0, "user", "grouping"), node.getTopology(), new IBufferedStatisticsReader(filename, expectedGroupName), null);
	}

	private static final long serialVersionUID = 1L;
	
	

}

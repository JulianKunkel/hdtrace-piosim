package de.topology;

import java.nio.IntBuffer;

import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsEntryType;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hdTraceInput.BufferedMemoryReader;
import de.hdTraceInput.UserDefinedStatisticsInMemory;

/**
 * Permit a user defined function to aggregate upon multiple topologies
 * 
 * @author julian
 */
public class UserDefinedStatisticTreeNode extends TopologyStatisticTreeNode{

	private static final long serialVersionUID = 1L;
	
	private final TopologyTreeNode treeNode;
	
	/**
	 * Start to recompute the statistics
	 */
	public void recomputeStatistics(){
		final UserDefinedStatisticsInMemory reader = (UserDefinedStatisticsInMemory) getStatisticSource();
		
		StatisticsGroupEntry[] entries = new StatisticsGroupEntry[2];

		final StatisticsGroupDescription group = reader.getGroup();
		
		for (int i=0; i < entries.length; i++){
			final Object [] values = new Object[1];
			values[0] = i;
			entries[i] = new StatisticsGroupEntry(values, new Epoch(i), new Epoch(i+1), group);
		}
		
		reader.setEntries(entries);
	}
	
	public UserDefinedStatisticTreeNode(TopologyTreeNode node) {
		//StatisticsGroupDescription group, String name, StatisticsEntryType datatype, int numberInGroup, String unit, String grouping
		super(new StatisticsDescription(new StatisticsGroupDescription("testgroup"), "test", StatisticsEntryType.DOUBLE, 0, "user", "grouping"), 
				node.getTopology(), new UserDefinedStatisticsInMemory(), node.getFile());

		treeNode = node;
		
		final UserDefinedStatisticsInMemory reader = (UserDefinedStatisticsInMemory) getStatisticSource();
		reader.setGroup(getStatisticGroup());
				
		recomputeStatistics();
	}


}

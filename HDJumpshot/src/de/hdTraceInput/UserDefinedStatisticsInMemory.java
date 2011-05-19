package de.hdTraceInput;

import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsSource;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.topology.TopologyTreeNode;

/**
 * Represents a user defined statistics function.
 * 
 * @author julian
 *
 */
public class UserDefinedStatisticsInMemory extends BufferedMemoryReader {
	final TopologyTreeNode parentNode;
	
	/**
	 * Start to recompute the statistics
	 */
	public void recomputeStatistics(){		
		StatisticsGroupEntry[] entries = new StatisticsGroupEntry[2];

		final StatisticsGroupDescription group = getGroup();
		
		for (int i=0; i < entries.length; i++){
			final Object [] values = new Object[1];
			values[0] = i;
			entries[i] = new StatisticsGroupEntry(values, new Epoch(i), new Epoch(i+1), group);
		}
		
		setEntries(entries);
	}
	
	
	public UserDefinedStatisticsInMemory(TopologyTreeNode parentNode, StatisticsGroupDescription group) {
		this.parentNode = parentNode;
		setGroup(group);
		
		recomputeStatistics();
	}
	
}

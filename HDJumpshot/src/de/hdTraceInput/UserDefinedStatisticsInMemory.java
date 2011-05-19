package de.hdTraceInput;

import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsSource;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.topology.TopologyManager;
import de.topology.TopologyStatisticTreeNode;
import de.topology.TopologyTreeNode;
import de.viewer.common.SortedJTreeModel;

/**
 * Represents a user defined statistics function.
 * 
 * @author julian
 *
 */
public class UserDefinedStatisticsInMemory extends BufferedMemoryReader {
	final TopologyManager  topologyManager;
	final TopologyNode parentNode;

	/**
	 * The user supplied function to compute, initialized to 1
	 */
	private String computeFunction = "1";

	public void setComputeFunction(String computeFunction) {
		this.computeFunction = computeFunction;
	}

	public String getComputeFunction() {
		return computeFunction;
	}

	/**
	 * Start to recompute the statistics based on the direct (!) children
	 */
	public void recomputeStatistics(){		
				
		final TopologyTreeNode pNode =  topologyManager.getTopologyTreeNode(parentNode);
		
		int childCount = pNode.getChildCount();		
		for(int c = 0; c < childCount; c++){
			final Object node = (Object) pNode.getChildAt(c);
			
			if(! TopologyStatisticTreeNode.class.isInstance(node) ){
				continue;
			}
			
			// must be a statNode
			final TopologyStatisticTreeNode statNode = ((TopologyStatisticTreeNode) node);
		
			if(statNode.getStatisticSource() == this) // we are child of the parent.
				continue;

			System.out.println(statNode.getStatisticSource().getGroup().getName());

		}

		StatisticsGroupEntry[] entries = new StatisticsGroupEntry[2];

		final StatisticsGroupDescription group = getGroup();

		for (int i=0; i < entries.length; i++){
			final Object [] values = new Object[1];
			values[0] = i;
			entries[i] = new StatisticsGroupEntry(values, new Epoch(i), new Epoch(i+1), group);
		}

		setEntries(entries);

		// trigger computation of user-defined statistics on parent nodes

		final Enumeration<TreeNode> pNodes = ((TreeNode) pNode.getParent()).children();
		while(pNodes.hasMoreElements()){
			
			TreeNode node = pNodes.nextElement();
			
			
			if( ! TopologyStatisticTreeNode.class.isInstance(node) ){
				continue;
			}
			final TopologyStatisticTreeNode statNode = ((TopologyStatisticTreeNode) node);

			if(UserDefinedStatisticsInMemory.class.isInstance(statNode.getStatisticSource() )){
				UserDefinedStatisticsInMemory u = (UserDefinedStatisticsInMemory) statNode.getStatisticSource();
				// TODO re-computation only needed if the parent uses this variable.
				u.recomputeStatistics();
			}
		}

	}


	public UserDefinedStatisticsInMemory(TopologyManager topologyManager, TopologyNode parentNode, StatisticsGroupDescription group) {
		this.topologyManager = topologyManager;
		this.parentNode = parentNode;
		setGroup(group);
		
		recomputeStatistics();
	}

}

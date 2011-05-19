package de.hdTraceInput;

import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.tree.TreeNode;

import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsSource;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.topology.TopologyInnerNode;
import de.topology.TopologyManager;
import de.topology.TopologyStatisticTreeNode;
import de.topology.TopologyStatisticsGroupFolder;
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

	/**
	 * The metrics which are required to compute the function
	 */
	private String [] requiredMetrics = {"BMI", "TROVE"};
	
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
		
		TopologyStatisticTreeNode [] statisticsFound = new TopologyStatisticTreeNode[requiredMetrics.length];
		/**
		 * Describe the position in the statisticsFoundArray we have
		 */
		int curStatFound = 0;
		
		
		for(int c = 0; c < childCount; c++){
			final Object node = (Object) pNode.getChildAt(c);
			
			if(! TopologyStatisticTreeNode.class.isInstance(node)  ){
				
				if( TopologyStatisticsGroupFolder.class.isInstance(node) ){
					// this is a folder for a statistics group!
					final TopologyStatisticsGroupFolder folderNode =  ((TopologyStatisticsGroupFolder) node);					
					
					for(int sc = 0; sc < folderNode.getChildCount(); sc++){
						TopologyStatisticTreeNode statN = (TopologyStatisticTreeNode) folderNode.getChildAt(sc);
						
						for(String s : requiredMetrics){
							if(s.equals(statN.toString())){
								// got one!
								statisticsFound[curStatFound++] = statN;
								break;
							}
						}
					}
				}
				continue;
			}
			
			// must be a statNode
			final TopologyStatisticTreeNode statNode = ((TopologyStatisticTreeNode) node);
		
			if(statNode.getStatisticSource() == this) // we are child of the parent.
				continue;
			
			for(String s : requiredMetrics){
				if(s.equals(statNode.toString())){
					// got one!
					statisticsFound[curStatFound++] = statNode;
					break;
				}
			}
		}
		
		if(curStatFound != requiredMetrics.length){
			System.err.println("Did not find all required metrics for the user defined statistics, keeping the old values " + this);
			return;
		}
		
		// compute the new values

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

package de.hdTraceInput;

import java.util.ArrayList;
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
import de.viewer.common.ModelTime;
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
	final ModelTime modelTime;

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
		
		final TopologyStatisticTreeNode [] statisticsFound = new TopologyStatisticTreeNode[requiredMetrics.length];
		/**
		 * Contains the offset to the metric we are looking for in the group
		 */
		final int [] groupPosition = new int[requiredMetrics.length];
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
								groupPosition[curStatFound] = statN.getStatisticDescription().getNumberInGroup();
								statisticsFound[curStatFound++] = statN;								
								break;
							}
						}
					}
				}
				continue;
			}
			
			// must be a statNode which means there is no other number
			final TopologyStatisticTreeNode statNode = ((TopologyStatisticTreeNode) node);
		
			if(statNode.getStatisticSource() == this) // we are child of the parent.
				continue;
			
			for(String s : requiredMetrics){
				if(s.equals(statNode.toString())){
					// got one!
					groupPosition[curStatFound] = statNode.getStatisticDescription().getNumberInGroup();
					statisticsFound[curStatFound++] = statNode;
					break;
				}
			}
		}
		
		if(curStatFound != requiredMetrics.length){
			System.err.println("Did not find all required metrics for the user defined statistics, keeping the old values " + this);
			return;
		}
		
		// compute the new values based on the equation and the statisticsFound
		// track the current positions of all statistics we need		
		// enumerate the statistics of the reader with the model time.
		Enumeration<StatisticsGroupEntry> [] currentpositions = new Enumeration[statisticsFound.length];
		// the last element retrieved by the enumeration
		StatisticsGroupEntry [] lastElement = new StatisticsGroupEntry[statisticsFound.length];

		// track the time for the last element
		final Epoch biggestTime = new Epoch(Integer.MAX_VALUE, Integer.MAX_VALUE);
		Epoch lastTime = biggestTime;
		
		for(int i = 0; i < currentpositions.length; i++){
			
			currentpositions[i] = statisticsFound[i].getStatisticSource().enumerateStatistics(modelTime.getViewPositionAdjusted(), modelTime.getViewEndAdjusted());
			lastElement[i] = currentpositions[i].nextElement();
			
			Epoch elemTime =statisticsFound[i].getStatisticSource().getMinTime();
			
			if (  elemTime.compareTo(lastTime) < 0 ){
				lastTime = elemTime;
			}
		}

		// the group we will create entries for is this group
		final StatisticsGroupDescription group = getGroup();

		
		
		// create the new elements
		ArrayList<StatisticsGroupEntry> entries = new ArrayList<StatisticsGroupEntry>();
		
		while(true){
			int minIndex = -1;

			/**
			 * The earliest element to handle
			 */
			Epoch curMinTime = biggestTime;
			
			// now we have to seek the minimum index of all current positions
			for(int i=0; i < currentpositions.length; i++){
				
				if ( lastElement[i] != null && lastElement[i].getEarliestTime().compareTo(curMinTime) < 0 ){
					curMinTime = lastElement[i].getEarliestTime();
					minIndex = i;
				}
			}
			
			if(minIndex == -1){
				// we have scanned all elements.
				break;
			}
			
			// we have the minimum element.
			final StatisticsGroupEntry lastElem = lastElement[minIndex];

			final Object [] values = new Object[1];
			values[0] = lastElem.getValues()[groupPosition[minIndex]];
						
			entries.add( new StatisticsGroupEntry(values, lastTime, curMinTime, group) );
			
			lastTime = curMinTime;
			
			
			// update the enumeration
			if( currentpositions[minIndex].hasMoreElements() ){
				lastElement[minIndex] = currentpositions[minIndex].nextElement();
			}else{
				lastElement[minIndex] = null;
			}
		}
		

		if (entries.size() == 0){
			System.err.println("WARNING did not find interesting values for user computed value, adding dummy values!");
			
			final Object [] values = new Object[1];
			values[0] = 0.0;
			entries.add( new StatisticsGroupEntry(values, lastTime, lastTime, group));
		}


		// set the new entries
		setEntries(entries.toArray(new StatisticsGroupEntry[0]));
		
		
		// update the global statistics
		topologyManager.getTraceFormatFileReader().setGlobalValuesOnStatistics(parentNode.getStatisticsSources().values());
		
		
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


	public UserDefinedStatisticsInMemory(TopologyManager topologyManager, TopologyNode parentNode, StatisticsGroupDescription group, ModelTime modelTime) {
		this.topologyManager = topologyManager;
		this.parentNode = parentNode;
		this.modelTime = modelTime;
		setGroup(group);
		
		recomputeStatistics();
	}

}

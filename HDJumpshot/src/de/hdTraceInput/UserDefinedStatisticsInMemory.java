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
	private MathematicalExpression expression = new MathematicalExpression("1");

	
	public void setComputeFunction(String computeFunction) throws IllegalArgumentException{
		this.expression = new MathematicalExpression(computeFunction);	
		
		System.out.println(this.expression.textualRepresentation());
	}

	public String getComputeFunction() {
		return expression.textualRepresentation();
	}
	

	
	private void checkStatisticsOfNode(Object node, ArrayList<TopologyStatisticTreeNode> statisticsFound, ArrayList<Integer> groupPosition ){
		System.out.println("Checking node " + node);
		
		final String [] requiredMetrics = expression.getRequiredMetrics();
		
		if( TopologyStatisticsGroupFolder.class.isInstance(node) ){
			// this is a folder for a statistics group!
			final TopologyStatisticsGroupFolder folderNode =  ((TopologyStatisticsGroupFolder) node);					
			
			for(int sc = 0; sc < folderNode.getChildCount(); sc++){
				TopologyStatisticTreeNode statN = (TopologyStatisticTreeNode) folderNode.getChildAt(sc);
				
				final String statName = statN.getStatisticDescription().getName();
				
				System.out.println("stat found: " + statName );
				
				for(String s : requiredMetrics){
					if(s.equals(statName)){
						// got one!
						groupPosition.add( statN.getStatisticDescription().getNumberInGroup() );
						statisticsFound.add( statN );								
						break;
					}
				}
			}
		}
		
		if(! TopologyStatisticTreeNode.class.isInstance(node)  ){				
			return;
		}
		
		// must be a statNode which means there is no other number
		final TopologyStatisticTreeNode statNode = ((TopologyStatisticTreeNode) node);
	
		if(statNode.getStatisticSource() == this) // we are child of the parent.
			return;
		
		for(String s : requiredMetrics){
			if(s.equals(statNode.toString())){
				// got one!
				groupPosition.add( statNode.getStatisticDescription().getNumberInGroup() );
				statisticsFound.add( statNode );	
				break;
			}
		}
		
		return;
	}

	private void  findStatistics(TopologyTreeNode pNode, ArrayList<TopologyStatisticTreeNode> statisticsFound, ArrayList<Integer> groupPositions){
		int childCount = pNode.getChildCount();
		
		for(int c = 0; c < childCount; c++){
			final Object node = (Object) pNode.getChildAt(c);

			if(TopologyInnerNode.class.isInstance(node)){
				// check if children have the metrics
				int cchildCount = ((TopologyInnerNode)node).getChildCount();

				final int oldSize = statisticsFound.size();
				
				for(int d = 0; d < cchildCount; d++){
					final Object childChild = (Object) ((TopologyInnerNode)node).getChildAt(d);
					
					checkStatisticsOfNode(childChild, statisticsFound, groupPositions);
				}
				
				if ( oldSize != statisticsFound.size()) {
					// we found sth.
					// check that we found all required metrics!
					if(oldSize  + expression.getRequiredMetrics().length != statisticsFound.size() ){
						System.err.println("Did not find all required metrics for the user defined statistics for the node topology:" + ((TopologyInnerNode)node).getTopology().toRecursiveString() );
						// remove the values.
						while(statisticsFound.size() != oldSize){
							statisticsFound.remove( oldSize );
							groupPositions.remove( oldSize );
						}
					}
				}
				
			}else{
				// normal node:
				checkStatisticsOfNode(node, statisticsFound, groupPositions);
			}
			
			
		}
		

	}
	
	/**
	 * Start to recompute the statistics based on the direct (!) children
	 */
	public void recomputeStatistics(){		
		final TopologyTreeNode pNode =  topologyManager.getTopologyTreeNode(parentNode);
		
		/**
		 * Contains the offset to the metric we are looking for in the group
		 */
		final ArrayList<Integer> groupPosition = new ArrayList<Integer>();
		
		final ArrayList<TopologyStatisticTreeNode> statisticsFound = new ArrayList<TopologyStatisticTreeNode>();

		findStatistics(pNode, statisticsFound, groupPosition);
				
		// compute the new values based on the equation and the statisticsFound
		// track the current positions of all statistics we need		
		// enumerate the statistics of the reader with the model time.
		final Enumeration<StatisticsGroupEntry> [] currentpositions = new Enumeration[statisticsFound.size()];
		// the last element retrieved by the enumeration
		final StatisticsGroupEntry [] lastElement = new StatisticsGroupEntry[statisticsFound.size()];

		// track the time for the last element
		final Epoch biggestTime = new Epoch(Integer.MAX_VALUE, Integer.MAX_VALUE);
		Epoch lastTime = biggestTime;
		
		for(int i = 0; i < currentpositions.length; i++){
			
			currentpositions[i] = statisticsFound.get(i).getStatisticSource().enumerateStatistics(modelTime.getViewPositionAdjusted(), modelTime.getViewEndAdjusted());
			lastElement[i] = currentpositions[i].nextElement();
			
			Epoch elemTime = statisticsFound.get(i).getStatisticSource().getMinTime();
			
			if (  elemTime.compareTo(lastTime) < 0 ){
				lastTime = elemTime;
			}
		}

		// the group we will create entries for is this group
		final StatisticsGroupDescription group = getGroup();
		
		// create the new elements
		ArrayList<StatisticsGroupEntry> entries = new ArrayList<StatisticsGroupEntry>();

		/** The last element we processed  */
		StatisticsGroupEntry lastElem = null;
		
		double lastComputedResult = 0;
		
		final double [] lastValues = new double[statisticsFound.size()];
		final String [] variableNames = new String[statisticsFound.size()];
		
		// initialize the array
		for(int i=0; i < lastValues.length; i++){
			lastValues[i] = Double.NEGATIVE_INFINITY;
			variableNames[i] = statisticsFound.get(i).getStatisticDescription().getName();
		}
		
		/*
		 * Flag signaling if we can start the computation
		 */
		boolean startedComputation = false;
		
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
			lastElem = lastElement[minIndex];
			
			if(startedComputation){			
				// start to compute when all values are available				
				// compute the result
				lastComputedResult = expression.computeFunction(lastValues, variableNames);
				
				final Object [] values = new Object[1];
				// apply the function onto the variables.

				values[0] = lastComputedResult;			
				// last element:						
				entries.add( new StatisticsGroupEntry(values, lastTime, curMinTime, group) );
			}


			lastValues[minIndex] = lastElem.getNumeric(groupPosition.get(minIndex));
			
			assert(lastValues[minIndex] != Double.NaN);
			
			if(! startedComputation){
				// check if all variables are available.
				int i;
				for(i=0; i < lastValues.length; i++){
					if (lastValues[i] == Double.NEGATIVE_INFINITY){
						break;
					}
				}
				
				if( i == lastValues.length){
					startedComputation = true;
				}
			}
			
			lastTime = curMinTime;			
			
			// update the enumeration
			if( currentpositions[minIndex].hasMoreElements() ){
				lastElement[minIndex] = currentpositions[minIndex].nextElement();
			}else{
				lastElement[minIndex] = null;
				// shall we break now as not all statistics are available any more
			}
		}


		if (entries.size() == 0){
			System.err.println("WARNING did not find interesting values for user computed value, adding dummy values!");
			
			final Object [] values = new Object[1];
			values[0] = 0.0;
			entries.add( new StatisticsGroupEntry(values, modelTime.getViewPositionAdjusted(), modelTime.getViewEndAdjusted(), group));
		}else{
			final Object [] values = new Object[1];		
			values[0] = lastComputedResult;				
			entries.add( new StatisticsGroupEntry(values, lastTime, lastElem.getLatestTime(), group) );			
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
	}

}

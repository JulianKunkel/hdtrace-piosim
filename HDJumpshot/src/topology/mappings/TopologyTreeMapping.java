package topology.mappings;

import hdTraceInput.TraceFormatBufferedFileReader;
import topology.TopologyManagerContents;
import topology.TopologyStatisticTreeNode;
import topology.TopologyTreeNode;
import viewer.common.SortedJTreeNode;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.topology.TopologyNode;

/**
 * Parent for all topology orders.
 * @author julian
 */
abstract public class TopologyTreeMapping {
	abstract public SortedJTreeNode createTopology(TraceFormatBufferedFileReader reader);
	abstract public boolean isAvailable(TraceFormatBufferedFileReader reader);
	
	private boolean addStatistics = true;
	
	public void setTopologyManagerContents(TopologyManagerContents type){
		this.addStatistics = !(type == TopologyManagerContents.TRACE_ONLY);
	}
	
	protected void addTopologyTreeNode(TopologyTreeNode node, SortedJTreeNode parent){
		if(parent != null)
			parent.add(node);
	}

	protected SortedJTreeNode addDummyTreeNode(String name, SortedJTreeNode parent){
		SortedJTreeNode node = new SortedJTreeNode(name);
		parent.add(node);

		return node;
	}


	protected void addStatisticsInTopology(int level, SortedJTreeNode node, TopologyNode topology, TraceFormatFileOpener file){	
		// add statistic nodes:
		for(String groupName: topology.getStatisticSources().keySet()){    		
			StatisticsGroupDescription group = file.getProjectDescription().getExternalStatisticsGroup(groupName);

			SortedJTreeNode statGroupNode = addDummyTreeNode(groupName, node);

			for(StatisticDescription statDesc: group.getStatisticsOrdered()){
				TopologyStatisticTreeNode statNode = new TopologyStatisticTreeNode(statDesc, group, topology, file );

				addTopologyTreeNode(statNode, statGroupNode);
			}
		}
	}
	
	public boolean isAddStatistics() {
		return addStatistics;
	}	
}

package de.topology;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.hamcrest.Description;

import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsSource;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.viewer.common.SortedJTreeNode;

/**
 * Contains multiple statistics
 * @author julian
 */
public class TopologyStatisticsGroupFolder extends SortedJTreeNode {
	
	public TopologyStatisticsGroupFolder(String name) {
		super(name);
	}
}

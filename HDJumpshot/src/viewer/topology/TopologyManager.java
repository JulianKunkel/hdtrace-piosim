
package viewer.topology;

import hdTraceInput.BufferedStatisticFileReader;
import hdTraceInput.BufferedTraceFileReader;
import hdTraceInput.TraceFormatBufferedFileReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import viewer.timelines.TimelineType;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.topology.TopologyInternalLevel;

public class TopologyManager extends JTree
{
	private DefaultMutableTreeNode  tree_root;

	final TraceFormatBufferedFileReader  reader;

	private ArrayList<TopologyTreeNode> topoToTimelineMapping = new ArrayList<TopologyTreeNode>();

	/**
	 * If set to true then listeners are not notified on a topology change, this allows mass update of topology
	 */
	private boolean                            changeListenerDisabled = false;
	private LinkedList<TopologyChangeListener> changeListener = new LinkedList<TopologyChangeListener>();
	
	private TreeExpansionListener treeExpansionListener = new TopologyTreeExpansionListener();
	



	private class TopologyTreeExpansionListener implements TreeExpansionListener{    
		// from tree expansion listener
		@Override
		public void treeCollapsed(TreeExpansionEvent event) {
			fireTopologyChanged();
		}

		@Override
		public void treeExpanded(TreeExpansionEvent event) {
			fireTopologyChanged();
		}
	}

	public void fireTopologyChanged(){
		if( changeListenerDisabled ) return;

		reloadTopologyMappingFromTree();

		for(TopologyChangeListener list: changeListener){
			list.topologyChanged();
		}
	}

	/**
	 * If set to true then listeners are not notified on a topology change, this allows mass update of topology.
	 * Don't forget to enable if after a mass update.
	 *  
	 * @param changeListenerDisabled
	 */
	public void setChangeListenerDisabled(boolean changeListenerDisabled) {
		this.changeListenerDisabled = changeListenerDisabled;
		
		if(changeListenerDisabled == true){
			this.removeTreeExpansionListener(treeExpansionListener);
		}else{
			this.addTreeExpansionListener( treeExpansionListener);			
		}
	}

	public void addTopologyChangedListener(TopologyChangeListener listener){
		changeListener.add(listener);
	}

	/**
	 * Recreate topology based on tree, i.e. not expanded nodes are not shown as timelines.
	 */
	private void reloadTopologyMappingFromTree(){
		topoToTimelineMapping.clear();
		for(int timeline = 0; timeline < getRowCount(); timeline++){
			final TreePath path = getPathForRow(timeline);
			final TreeNode node = (TreeNode) path.getLastPathComponent();

			if(TopologyTreeNode.class.isInstance(node)){
				topoToTimelineMapping.add((TopologyTreeNode) node);
			}else{
				topoToTimelineMapping.add(null);
			}
		}
	}

	/**
	 * Get the trace reader for a particular timeline
	 * 
	 * @param timeline
	 * @return
	 */
	public BufferedTraceFileReader getTraceReaderForTimeline(int timeline){
		return (BufferedTraceFileReader) ((TopologyTraceTreeNode) topoToTimelineMapping.get(timeline)).getTraceSource();
	}

	/**
	 * Get the statistic reader responsible for a particular timeline
	 * @param timeline
	 * @return
	 */
	public BufferedStatisticFileReader getStatisticReaderForTimeline(int timeline){
		return (BufferedStatisticFileReader) ((TopologyStatisticTreeNode) topoToTimelineMapping.get(timeline)).getStatisticSource();
	}

	public TopologyStatisticTreeNode getStatisticNodeForTimeline(int timeline){
		return ((TopologyStatisticTreeNode) topoToTimelineMapping.get(timeline));
	}




	/**
	 * Return the number of a statistic within a group
	 * @param timeline
	 * @return
	 */
	public int getStatisticNumberForTimeline(int timeline){
		return ((TopologyStatisticTreeNode) topoToTimelineMapping.get(timeline)).getNumberInGroup();
	}

	public int getTimelineNumber(){
		return topoToTimelineMapping.size();
	}    

	public TimelineType getType(int timeline){    	
		if(topoToTimelineMapping.size() <= timeline)
			return TimelineType.INVALID_TIMELINE;
		if(topoToTimelineMapping.get(timeline) == null)
			return TimelineType.INNER_NODE;    	
		return topoToTimelineMapping.get(timeline).getType();
	}

	private void addTopologyTreeNode(TopologyTreeNode node, DefaultMutableTreeNode parent){
		if(parent != null)
			parent.add(node);
	}

	private DefaultMutableTreeNode addDummyTreeNode(String name, DefaultMutableTreeNode parent){
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
		parent.add(node);

		return node;
	}


	private void addStatisticsInTopology(int level, DefaultMutableTreeNode node, TopologyInternalLevel topology, TraceFormatFileOpener file){
		// add statistic nodes:
		for(String groupName: topology.getStatisticSources().keySet()){    		
			ExternalStatisticsGroup group = file.getProjectDescription().getExternalStatisticsGroup(groupName);

			DefaultMutableTreeNode statGroupNode = addDummyTreeNode(groupName, node);

			for(StatisticDescription statDesc: group.getStatisticsOrdered()){
				TopologyStatisticTreeNode statNode = new TopologyStatisticTreeNode(statDesc, group, topology, file, this );

				addTopologyTreeNode(statNode, statGroupNode);
			}
		}
	}

	private void recursivlyAddTopology(int level, DefaultMutableTreeNode parentNode, TopologyInternalLevel topology, TraceFormatFileOpener file){
		final TopologyTreeNode node = new TopologyInnerNode(topology, file, this);

		addTopologyTreeNode(node, parentNode);    	

		if(topology.getChildElements().size() != 0){
			// handle leaf level == trace nodes differently:
			Collection<TopologyInternalLevel> children = topology.getChildElements().values();
			boolean leafLevel = children.iterator().next().isLeaf();
			if(leafLevel){
				final DefaultMutableTreeNode traceParent = addDummyTreeNode("Trace", node);

				for(TopologyInternalLevel child: topology.getChildElements().values()){					
					if (child.getStatisticSources().size() == 0){
						// no statistic on the leaf level:
						TopologyTreeNode childNode = new TopologyTraceTreeNode(child, file, this);
						addTopologyTreeNode(childNode, traceParent);						
					}else{
						// handles statistics on the leaf level:
						final DefaultMutableTreeNode extra = addDummyTreeNode(child.getLabel(), traceParent);

						TopologyTreeNode childNode = new TopologyTraceTreeNode(child, file, this);
						addTopologyTreeNode(childNode, extra);

						addStatisticsInTopology(level, extra, child, file);
					}
				}								
			}else{
				for(TopologyInternalLevel child: topology.getChildElements().values()){
					recursivlyAddTopology(level +1, node, child, file);
				}
			}
		}

		addStatisticsInTopology(level, node, topology, file);
	}

	public String getTopologyLabels(){
		StringBuffer buff = new StringBuffer();
		for(int i=0; i < reader.getNumberOfFilesLoaded(); i++ ){
			TraceFormatFileOpener file = reader.getLoadedFile(i);
			buff.append(file.getProjectDescription().getProjectFilename() + ": ");
			for(String label: file.getTopologyLabels().getLabels()){
				buff.append(label + " ");
			}
			buff.append("\n");
		}

		return buff.toString();
	}

	/**
	 * restore the timelines to the normal / selected topology 
	 */
	public void restoreTopology(){
		setChangeListenerDisabled(true);
		// TODO allow different topologies.
		topoToTimelineMapping.clear();

		this.tree_root = loadDefaultTopologyToTreeMapping();

		expandTreeInternal();		
		
		setChangeListenerDisabled(false);
		
		fireTopologyChanged();
	}

	/**
	 * Remove timelines marked in the tree from the view
	 */
	public void removeMarkedTimelines(){
		TreePath [] paths = getSelectionPaths();
		if(paths.length == 0)
			return;

		final DefaultTreeModel model = (DefaultTreeModel) getModel();

		for(TreePath path: paths){
			int depth = path.getPathCount(); 
			if(depth > 1){
				model.removeNodeFromParent((MutableTreeNode) path.getLastPathComponent());

				// recursivly remove empty timelines
				for(int curDepth = depth - 2; curDepth >= 1; curDepth-- ){
					final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getPathComponent(curDepth);
					if( TopologyInnerNode.class.isInstance(node) ){
						final TopologyInnerNode topNode = (TopologyInnerNode) node;
												
						if(topNode.getType() == TimelineType.INNER_NODE && node.getChildCount() == 0){
							if(topNode.getParent() != null){
								model.removeNodeFromParent(topNode);
							}
							continue;
						}
						break;
					}else{
						if(node.getChildCount() != 0){
							break;
						}
						//otherwise remove it
						if(node.getParent() != null)
							model.removeNodeFromParent(node);
					}
				}
			}
			//removeSelectionPaths(paths);

			// TODO unload loaded file if pathCount == 1!!!
		}

		reloadTopologyMappingFromTree();	
		fireTopologyChanged();
	}


	/**
	 * Load a default topology, filename => hierarchically print the children 
	 */
	public DefaultMutableTreeNode loadDefaultTopologyToTreeMapping(){
		DefaultMutableTreeNode tree_root = new DefaultMutableTreeNode("HDTrace");

		setModel(new DefaultTreeModel(tree_root));            

		for(int f = 0 ; f < reader.getNumberOfFilesLoaded() ; f++){
			recursivlyAddTopology(1, tree_root, reader.getLoadedFile(f).getTopology(), reader.getLoadedFile(f));
		}
		
		return tree_root;
	}

	public TopologyTreeNode getTreeNodeForTimeline(int timeline){
		return topoToTimelineMapping.get(timeline);
	}


	public TopologyManager( final TraceFormatBufferedFileReader  reader )
	{
		this.reader = reader;
		super.setEditable( true );
		super.putClientProperty("JTree.lineStyle", "Angled");

		restoreTopology();
	}

	public void expandTree()
	{
		setChangeListenerDisabled(true);

		expandTreeInternal();

		setChangeListenerDisabled(false);
		fireTopologyChanged();
	}
	
	private void expandTreeInternal(){

		Enumeration<DefaultMutableTreeNode> rootEnumeration = tree_root.depthFirstEnumeration();
		while(rootEnumeration.hasMoreElements()){
			DefaultMutableTreeNode node = rootEnumeration.nextElement();
			if(! node.isLeaf()){
				// construct path
				TreePath path = new TreePath(node.getPath());
				expandPath(path);
			}
		}

	}
}

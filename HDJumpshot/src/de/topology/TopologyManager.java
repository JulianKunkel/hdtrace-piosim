
/** Version Control Information $Id: TopologyManager.java 456 2009-06-29 16:54:44Z kunkel $
 * @lastmodified    $Date: 2009-06-29 18:54:44 +0200 (Mo, 29. Jun 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 456 $ 
 */

//Copyright (C) 2009 Julian M. Kunkel

//This file is part of HDJumpshot.

//HDJumpshot is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//HDJumpshot is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.



package de.topology;


import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.JOptionPane;

import de.drawable.CategoryStatistic;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsEntryType;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hdTraceInput.BufferedRelationReader;
import de.hdTraceInput.BufferedTraceFileReader;
import de.hdTraceInput.IBufferedStatisticsReader;
import de.hdTraceInput.TraceFormatBufferedFileReader;
import de.hdTraceInput.UserDefinedStatisticsInMemory;
import de.topology.mappings.ExistingTopologyMappings;
import de.topology.mappings.TopologyTreeMapping;
import de.viewer.common.Const;
import de.viewer.common.ModelTime;
import de.viewer.common.SortedJTreeModel;
import de.viewer.common.SortedJTreeNode;
import de.viewer.first.MainManager;
import de.viewer.histogram.StatisticHistogramFrame;
import de.viewer.histogram.StatisticTimeHistogramFrame;
import de.viewer.linegraph.StatisticLineGraphFrame;
import de.viewer.timelines.TimelineType;

public class TopologyManager 
{
	private static final long serialVersionUID = 362940508169891280L;

	private JTree tree = new JTree();
	private DefaultMutableTreeNode  tree_root;

	final TraceFormatBufferedFileReader  reader;
	ModelTime                      modelTime;

	ExistingTopologyMappings       usedTopologyMapping = ExistingTopologyMappings.TopologyDefault;

	/**
	 * Stores for each timeline the corresponding topology
	 */
	private ArrayList<TopologyTreeNode>      timelines = new ArrayList<TopologyTreeNode>();

	private static class TopologyTreeNodeWrapper{ // temporary wrapper, TODO rework model!
		final TopologyTreeNode node;
		
		public TopologyTreeNodeWrapper(TopologyTreeNode node) {
			this.node = node;
		}
		
		@Override
		public boolean equals(Object obj) {		
			return node.equalTopology(((TopologyTreeNodeWrapper) obj).node);
		}
		
		
		@Override
		public int hashCode() {		
			return node.getTopology().hashCode();
		}
	}
	
	/**
	 * Stores for each TreeNode entry the corresponding timeline
	 */
	private HashMap<TopologyTreeNodeWrapper, Integer>  treeNodeToTimelineMapping = new HashMap<TopologyTreeNodeWrapper, Integer>();
	
	/**
	 * Map the topology node to the representing topology tree node
	 */
	private HashMap<TopologyNode, TopologyTreeNode>  topologyNodeToTreeNodeMapping = new HashMap<TopologyNode, TopologyTreeNode>();


	/**
	 * If set to true then listeners are not notified on a topology change, this allows mass update of topology
	 */
	private boolean                            changeListenerDisabled = false;
	private LinkedList<TopologyChangeListener> changeListener = new LinkedList<TopologyChangeListener>();

	private TreeExpansionListener treeExpansionListener = new TopologyTreeExpansionListener();

	private TopologyManagerContents topologyManagerType = TopologyManagerContents.EVERYTHING;

	/**
	 * Manage plugins for the contained topology, some plugins might be applicable only
	 * on a subset of loaded files, so be careful!
	 */
	private static class TopoPluginStruct{
		final private TopologyInputPlugin plugin;
		final private HashMap<TopologyNode, ITopologyInputPluginObject> objsPerTopo = new HashMap<TopologyNode, ITopologyInputPluginObject>();

		public TopoPluginStruct(TopologyInputPlugin plugin) {
			this.plugin = plugin;
		}
	}

	/**
	 * Per plugin type and topology level enabled plugin.
	 */
	private HashMap<Class<? extends ITopologyInputPluginObject>, TopoPluginStruct> topoPlugins = new HashMap<Class<? extends ITopologyInputPluginObject>, TopoPluginStruct>();

	/**
	 * The following class allows to store information about removed nodes.
	 * @author julian
	 */
	private class RemovedNode{
		final SortedJTreeNode parent;
		final SortedJTreeNode child;

		public RemovedNode(SortedJTreeNode child) {
			this.parent = (SortedJTreeNode) child.getParent();
			this.child = child;
		}
	}

	/**
	 * Stores information per StatisticDescription to allow to remove timelines if a statistic is visible
	 * and to restore them if made visible.
	 */
	private HashMap<CategoryStatistic, LinkedList<RemovedNode>> removedNodesMap = new HashMap<CategoryStatistic, LinkedList<RemovedNode>>(); 


	private SortedJTreeNode clickedNode;
	
	private TopologyManager getThis(){
		return this;
	}

	private void addTopologyMenu(JPopupMenu popupMenu){
		// show available topologies:
		for(final ExistingTopologyMappings mapping: ExistingTopologyMappings.values()){

			if(! mapping.getInstance().isAvailable(reader)){
				continue;
			}

			final JMenuItem item = new JMenuItem(new AbstractAction(){
				private static final long serialVersionUID = 1L;

				{ // instance initalizer
					putValue(Action.NAME, mapping.toString()); 					

					if(usedTopologyMapping == mapping){
						// is selected right now
						putValue(Action.NAME, mapping.toString() + " [Selected]");						
					}
				}

				@Override
				public void actionPerformed(ActionEvent e) {
					setTopologyMapping(ExistingTopologyMappings.valueOf(e.getActionCommand().split(" \\[Selected\\]")[0]));
				}
			});


			popupMenu.add(item);						
		}
	}
	
	final static String userDefinedStatInput = "Enter the new compute function for a statistics timeline A...Z use <OP>(<EXPRESSION>) to aggregate across multiple" +
			"timelines\n or just <Expression> to compute the expression based on one child.\n" +
			"Examples: \"+(A*B*2.0)\" explaination: for each child node multiply the value of statistics A with B times 2.0, \n " +
			"then summarize across multiple timelines.\n"+
			"Operators are +,*,/,- (+,* are permitted to reduce across timelines\n" +
			", and ^ Minimum and maximum operators (can be used to determine min, max across multiple timelines)";

	/**
	 * used to detect clicks on the tree i.e. for expanding the menus
	 */
	private MouseListener treeMouseListener = new MouseAdapter(){
		public void mouseClicked(java.awt.event.MouseEvent evt) {
			if (SwingUtilities.isRightMouseButton( evt )){
				final TreePath path = tree.getClosestPathForLocation(evt.getX(), evt.getY());
				clickedNode = (SortedJTreeNode) path.getLastPathComponent();

				JPopupMenu popupMenu = new JPopupMenu();

				// return if we are not in the Boundary of the current path (i.e. not really clicked inside the path).
				if(! tree.getPathBounds(path).contains(evt.getPoint())){
					addTopologyMenu(popupMenu);
					popupMenu.show( evt.getComponent(), evt.getX(), evt.getY() );
					return;
				}

				// allow to insert new TreeNodes on folder nodes

				if( TopologyTreeNode.class.isInstance(clickedNode) ){
					
					final TopologyTreeNode treeNode = ((TopologyTreeNode) clickedNode);
					
					if(! clickedNode.isLeaf()){
						popupMenu.add(new AbstractAction(){
							private static final long serialVersionUID = 1L;

							{
								putValue(Action.NAME, "Generate a user defined statistic timeline with aggregates");							
							}

							@Override
							public void actionPerformed(ActionEvent e) {
								
								String str = JOptionPane.showInputDialog(null, "Enter the name of the user category", "test");
								if(str != null && str.length() > 0){
									
									// only one statistics group with a given name is permitted on a node
									if(treeNode.getTopology().getStatisticsSource(str) != null){
										System.err.println("Error, the statistics group with the name " + str + " already exists for this topology node.");
										return;
									}
									

									String compFunc = JOptionPane.showInputDialog(null, userDefinedStatInput, "");
									if(compFunc == null){
										System.err.println("No compute function specified, I will use 0.0 as a compute function");
										compFunc = "0.0";
									}

									
									// create a new statistics node
									StatisticsGroupDescription group = new StatisticsGroupDescription(str);
									group.addStatistic( new StatisticsDescription(group, str, StatisticsEntryType.DOUBLE, 0, "", str));				

									UserDefinedStatisticsInMemory userStats = new UserDefinedStatisticsInMemory(getThis(), treeNode.getTopology(), group, modelTime);

									treeNode.getTopology().setStatisticsReader(group.getName(), userStats );
									
									userStats.setComputeFunction(compFunc);
									userStats.recomputeStatistics();

									// reload topology.

									reader.addCategories(group);								
									reader.releadTopologyAndCategories();
								}
							}
						});
					}
					
					
					popupMenu.add(new AbstractAction(){
						private static final long serialVersionUID = 1L;

						{
							putValue(Action.NAME, "Adjust time offset of nested timelines ");							
						}

						@Override
						public void actionPerformed(ActionEvent e) {
							
							String str = JOptionPane.showInputDialog(null, "Adjust the time offset of all nested timelines by ", "0.0");
							if(str != null && str.length() > 0){
								double value = Double.parseDouble(str);
								
								treeNode.adjustTimeOffset(value, modelTime.getGlobalMinimum(), modelTime.getGlobalMaximum());
								// TODO adjust global min/max time
								
								fireTopologyChanged();
							}
						}
					});
				}
				
				
				if( TopologyStatisticTreeNode.class.isInstance(clickedNode) ){
					final TopologyStatisticTreeNode statNode = ((TopologyStatisticTreeNode) clickedNode);
					
					if(UserDefinedStatisticsInMemory.class.isInstance(statNode.getStatisticSource() )){
						// allow to refresh the node...
						// TODO re-factor this code into the node
						popupMenu.add(new AbstractAction(){
							private static final long serialVersionUID = 1L;

							{
								putValue(Action.NAME, "Recompute statistics based on the visible subtopologies");							
							}
							
							@Override
							public void actionPerformed(ActionEvent e) {
								UserDefinedStatisticsInMemory eNode = ((UserDefinedStatisticsInMemory) statNode.getStatisticSource());
								eNode.recomputeStatistics();
								// just fire the topology change listener right now, TODO a redraw of this line would suffice, though.
								fireTopologyChanged();
							}
						});
						
						popupMenu.add(new AbstractAction(){
							private static final long serialVersionUID = 1L;

							{
								putValue(Action.NAME, "Change compute function");							
							}
							
							@Override
							public void actionPerformed(ActionEvent e) {
								UserDefinedStatisticsInMemory eNode = ((UserDefinedStatisticsInMemory) statNode.getStatisticSource());
								
								String str = JOptionPane.showInputDialog(null, userDefinedStatInput, eNode.getComputeFunction());
								if(str != null){								
									eNode.setComputeFunction(str);
									eNode.recomputeStatistics();

									// just fire the topology change listener right now, TODO a redraw of this line would suffice, though.
									fireTopologyChanged();
								}
							}
						});
					}

					// Show statistic histogram:			
					popupMenu.add(new AbstractAction(){
						private static final long serialVersionUID = 1L;

						{
							putValue(Action.NAME, "Show histogram for " + clickedNode.toString());							
						}

						@Override
						public void actionPerformed(ActionEvent e) {
							StatisticHistogramFrame frame = new StatisticHistogramFrame(
									statNode.getStatisticSource(), 
									statNode.getStatisticDescription(), modelTime, 
									reader.getCategory(statNode.getStatisticDescription()));
							frame.show();
						}
					});				

					// Show statistic histogram for the times			
					popupMenu.add(new AbstractAction(){
						private static final long serialVersionUID = 1L;

						{
							putValue(Action.NAME, "Show duration/value histogram for " + clickedNode.toString());							
						}

						@Override
						public void actionPerformed(ActionEvent e) {
							StatisticTimeHistogramFrame frame = new StatisticTimeHistogramFrame(
									statNode.getStatisticSource(), 
									statNode.getStatisticDescription(), modelTime, 
									reader.getCategory(statNode.getStatisticDescription()));
							frame.show();
						}
					});		

					// allow to show aggregated histogram for the selected nodes with the same group(ing).
					final ArrayList<TopologyStatisticTreeNode> selectedStatNodes = new ArrayList<TopologyStatisticTreeNode>();
					if(tree.getSelectionPaths() != null){
						for(TreePath curPath: tree.getSelectionPaths()){
							if( TopologyStatisticTreeNode.class.isInstance(curPath.getLastPathComponent()) ){
								final TopologyStatisticTreeNode selStatNode = (TopologyStatisticTreeNode) curPath.getLastPathComponent();
								if(	statNode.getStatisticGroup().equals(selStatNode.getStatisticGroup()) && 
										statNode.getStatisticDescription().getGrouping().equals(selStatNode.getStatisticDescription().getGrouping())){
									if(statNode.getStatisticDescription().getGrouping().length() > 0 || 
											statNode.getStatisticDescription().getName().equals(selStatNode.getStatisticDescription().getName())){
										selectedStatNodes.add(selStatNode);
									}
								}
							}
						}

						if(selectedStatNodes.size() > 1){
							popupMenu.add(new AbstractAction(){
								private static final long serialVersionUID = 1L;

								{
									putValue(Action.NAME, "Show line graph for " + selectedStatNodes.size() + " stats");
								}

								@Override
								public void actionPerformed(ActionEvent e) {
									//for(TopologyStatisticTreeNode node: selectedStatNodes){
									//System.out.println("Selected: " + node.getStatisticDescription().getName());
									//}

									StatisticLineGraphFrame frame = new StatisticLineGraphFrame(selectedStatNodes, reader, modelTime);
									frame.show();
								}
							});			
						}
					}
				}


				if(popupMenu.getComponentCount() == 0){
					addTopologyMenu(popupMenu);
				}
				popupMenu.show( evt.getComponent(), evt.getX(), evt.getY() );
			}
		};	
	};

	/**
	 * Change the current topology mapping, i.e. the mapping from topology to timelines.
	 * @param mapping
	 */
	public void setTopologyMapping(ExistingTopologyMappings mapping){
		this.usedTopologyMapping = mapping;
		
		restoreTopology();
	}

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

		// avoid a cyclic update:	changeListenerDisabled = true;
		reloadTopologyMappingFromTree();

		for(TopologyChangeListener list: changeListener){
			list.topologyChanged();
		}
	}

	/**
	 * If set to true then listeners are not notified on a topology change, this allows mass update of topology.
	 * Don't forget to enable if after a mass update.
	 * Return the previous state of the changelistener
	 *  
	 * @param changeListenerDisabled
	 */
	public boolean setChangeListenerDisabled(boolean changeListenerDisabled) {
		boolean old = this.changeListenerDisabled;
		if(old == changeListenerDisabled)
			return old;

		this.changeListenerDisabled = changeListenerDisabled;

		if(changeListenerDisabled == true){
			tree.removeTreeExpansionListener(treeExpansionListener);
		}else{
			tree.addTreeExpansionListener( treeExpansionListener);			
		}

		return old;
	}

	public void addTopologyChangedListener(TopologyChangeListener listener){
		changeListener.add(listener);
	}

	/**
	 * Recreate topology based on tree, i.e. not expanded nodes are not shown as timelines.
	 */
	private void reloadTopologyMappingFromTree(){
		timelines.clear();
		treeNodeToTimelineMapping.clear();
		topologyNodeToTreeNodeMapping.clear();
		
		for(int timeline = 0; timeline < tree.getRowCount(); timeline++){
			final TreePath path = tree.getPathForRow(timeline);
			final TreeNode node = (TreeNode) path.getLastPathComponent();

			if(TopologyTreeNode.class.isInstance(node)){
				timelines.add((TopologyTreeNode) node);

				treeNodeToTimelineMapping.put((new TopologyTreeNodeWrapper((TopologyTreeNode) node)), timeline);
				
				final TopologyNode tnode = ((TopologyTreeNode) node).getTopology();
				
				if(! topologyNodeToTreeNodeMapping.containsKey(tnode)){
					// only add the topology node containing statistics etc. and not the statistic nodes etc. 
					topologyNodeToTreeNodeMapping.put(tnode, (TopologyTreeNode) node);
				}				
			}else{
				timelines.add(null);
			}
		}
	}
	
	public TopologyTreeNode getTopologyTreeNode(TopologyNode node){
		return topologyNodeToTreeNodeMapping.get(node);
	}

	/**
	 * Get the trace reader for a particular timeline
	 * 
	 * @param timeline
	 * @return
	 */
	public BufferedTraceFileReader getTraceReaderForTimeline(int timeline){
		return ((TopologyTraceTreeNode) timelines.get(timeline)).getTraceSource();
	}

	/**
	 * Get the trace reader for a particular timeline
	 * 
	 * @param timeline
	 * @return
	 */
	public BufferedRelationReader getRelationReaderForTimeline(int timeline){
		return ((TopologyRelationTreeNode) timelines.get(timeline)).getRelationSource();
	}


	/**
	 * Get the statistic reader responsible for a particular timeline
	 * @param timeline
	 * @return
	 */
	public IBufferedStatisticsReader getStatisticReaderForTimeline(int timeline){
		return (IBufferedStatisticsReader) ((TopologyStatisticTreeNode) timelines.get(timeline)).getStatisticSource();
	}

	public TopologyStatisticTreeNode getStatisticNodeForTimeline(int timeline){
		return ((TopologyStatisticTreeNode) timelines.get(timeline));
	}

	/**
	 * Return the number of a statistic within a group
	 * @param timeline
	 * @return
	 */
	public int getStatisticNumberForTimeline(int timeline){
		return ((TopologyStatisticTreeNode) timelines.get(timeline)).getNumberInGroup();
	}

	public int getTimelineNumber(){
		return timelines.size();
	}    

	public TimelineType getType(int timeline){    	
		if(timelines.size() <= timeline)
			return TimelineType.INVALID_TIMELINE;
		if(timelines.get(timeline) == null)
			return TimelineType.INNER_NODE;    	
		return timelines.get(timeline).getType();
	}

	public String getTopologyLabels(){
		StringBuffer buff = new StringBuffer();
		for(int i=0; i < reader.getNumberOfFilesLoaded(); i++ ){
			TraceFormatFileOpener file = reader.getLoadedFile(i);
			buff.append(file.getProjectDescription().getProjectFilename() + ": ");
			for(String label: file.getTopologyLabels().getTypes()){
				buff.append(label + " ");
			}
			buff.append("\n");
		}

		return buff.toString();
	}

	/**
	 * remove all topologies from the tree which have empty leafs.
	 */
	public void removeEmptyTopologies(){
		final SortedJTreeModel model = (SortedJTreeModel) tree.getModel();

		tree.clearSelection();
		for(int row = 0 ; row < tree.getRowCount() ; row++){
			tree.setSelectionRow(row);

			final TreePath path = tree.getSelectionPath(); 
			final int depth = path.getPathCount();

			if(depth > 1 && ((DefaultMutableTreeNode) path.getLastPathComponent()).isLeaf() ){				
				// recursivly remove empty timelines
				for(int curDepth = depth - 1; curDepth >= 1; curDepth-- ){
					final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getPathComponent(curDepth);
					if(node.getChildCount() != 0){
						break;
					}

					if( TopologyTreeNode.class.isInstance(node) ){
						final TopologyTreeNode topNode = (TopologyTreeNode) node;

						//System.out.println( depth + " " + node.toString() + " " + topNode.getType());

						if(topNode.getType() == TimelineType.INNER_NODE) {
							model.removeNodeFromParent(topNode);
							row--;
							continue;
						}
						break;
					}else{
						//otherwise remove it
						model.removeNodeFromParent(node);
						row--;

						continue;
					}
				}
			}

			tree.clearSelection();
		}
	}

	/**
	 * restore the timelines to the normal / selected topology 
	 */
	public void restoreTopology(){
		removedNodesMap.clear();

		final boolean old = setChangeListenerDisabled(true);
		try{
			TopologyTreeMapping mapping = usedTopologyMapping.getInstance();
			mapping.setTopologyManagerContents(topologyManagerType);
			this.tree_root = mapping.createTopology(reader);

			tree.setModel(new SortedJTreeModel(tree_root));

			expandTreeInternal();

			removeEmptyTopologies();

			setChangeListenerDisabled(old);

			updateTopologyPluginsIfNecessary();

			// update/remove invisible statistics:
			if(topologyManagerType == TopologyManagerContents.EVERYTHING || topologyManagerType == TopologyManagerContents.STATISTICS_ONLY){
				for (CategoryStatistic cat: reader.getCategoriesStatistics().values()){
					if(! cat.isVisible()){
						setStatisticCategoryVisiblity(cat, false);
					}
				}
			}

			fireTopologyChanged();			
		}catch(Exception e){
			throw new IllegalArgumentException(e);
		}
	}

	private void updateTopologyPluginsIfNecessary(){

	}

	public void setStatisticCategoryVisiblity(CategoryStatistic category, boolean visible){
		final SortedJTreeModel model = getTreeModel();

		if(visible == true){
			final LinkedList<RemovedNode> removedNodes = removedNodesMap.remove(category);

			if(removedNodes == null || removedNodes.size() == 0){
				return;
			}

			// now add the old values:
			for(RemovedNode rmNode: removedNodes){
				model.insertNodeInto(rmNode.child, rmNode.parent);
			}

			return;
		}

		if(removedNodesMap.containsKey(category)){
			// already invisible.
			return;
		}

		// walk through tree:
		final Enumeration<DefaultMutableTreeNode> nodes = tree_root.depthFirstEnumeration();
		final LinkedList<RemovedNode> removedNodes = new LinkedList<RemovedNode>();

		final StatisticsDescription desc = category.getStatisticDescription();

		while(nodes.hasMoreElements()){
			final DefaultMutableTreeNode node = nodes.nextElement();

			if( TopologyStatisticTreeNode.class.isInstance(node) ){
				final TopologyStatisticTreeNode statNode = (TopologyStatisticTreeNode) node;
				if(desc.equals(statNode.getStatisticDescription())){
					// remove that node:
					removedNodes.add( new RemovedNode(statNode));

					model.removeNodeFromParent(statNode);
				}
			}
		}
		removedNodesMap.put(category, removedNodes);

		reloadTopologyMappingFromTree();
	}

	/**
	 * Remove timelines marked in the tree from the view
	 */
	public void removeMarkedTimelines(){
		TreePath [] paths = tree.getSelectionPaths();
		if(paths == null || paths.length == 0)
			return;

		final SortedJTreeModel model = getTreeModel();

		for(TreePath path: paths){
			int depth = path.getPathCount(); 
			if(depth > 1){
				model.removeNodeFromParent((MutableTreeNode) path.getLastPathComponent());

				// recursively remove empty timelines
				for(int curDepth = depth - 2; curDepth >= 1; curDepth-- ){
					final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getPathComponent(curDepth);
					if( TopologyTreeNode.class.isInstance(node) ){
						final TopologyTreeNode topNode = (TopologyTreeNode) node;

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

		fireTopologyChanged();
	}



	public TopologyTreeNode getTreeNodeForTimeline(int timeline){
		return timelines.get(timeline);
	}

	/**
	 *  
	 * @param reader
	 * @param modelTime
	 * @param topologyManagerContents The initial topologies to show.
	 */
	public TopologyManager( final TraceFormatBufferedFileReader  reader, ModelTime modelTime,
			TopologyManagerContents topologyManagerContents)
	{		
		this.reader = reader;
		tree.setEditable( false );

		tree.setToolTipText("Real topology, right click enables special options.");
		//tree.putClientProperty("JTree.lineStyle", "Angled");

		tree.addMouseListener(treeMouseListener);
		tree.setFont(Const.FONT);

		tree.setCellRenderer(new TopologyTreeRenderer(MainManager.getIconManager()));

		this.modelTime = modelTime;
		this.topologyManagerType = topologyManagerContents;
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
				tree.expandPath(path);
			}
		}
	}

	public JTree getTree() {
		return tree;
	}

	public SortedJTreeModel getTreeModel(){
		return (SortedJTreeModel) tree.getModel();
	}

	public void scrollRowToVisible(int timeline) {
		tree.scrollRowToVisible(timeline);
	}

	public int getRowCount() {
		return tree.getRowCount();
	}

	public int getRowHeight() {
		return tree.getRowHeight();
	}

	public void setRowHeight(int rowHeight) {
		tree.setRowHeight(rowHeight);
	}

	public TopologyManagerContents getTopologyManagerContents() {
		return topologyManagerType;
	}

	public void setTopologyManagerContents(TopologyManagerContents topologyManagerContents) {
		this.topologyManagerType = topologyManagerContents;
	}

	/**
	 * Return the timeline for this topology or NULL if the topology is not mapped right now.
	 * @param entry
	 * @return
	 */
	public Integer getTimelineForTreeNode(TopologyTreeNode entry){
		return treeNodeToTimelineMapping.get(new TopologyTreeNodeWrapper(entry));		
	}

	/**
	 * Check new topologies if plugins can be applied to them.
	 * It applies the plugins only to currently visible topologies !!!
	 * 
	 * If a topology already exists, do not modify it. 
	 * @param pluginsToCheck
	 */
	public void tryToLoadPlugins(List<Class<? extends TopologyInputPlugin>> pluginsToCheck){
		for(Class<? extends TopologyInputPlugin> cls: pluginsToCheck){
			TopoPluginStruct handlerStruct = topoPlugins.get(cls);
			final TopologyInputPlugin plugin;

			final boolean isNew;

			if(handlerStruct != null){
				isNew = false;
				plugin = handlerStruct.plugin;			
			}else{
				isNew = true;
				// instantiate it temporarily			
				try{
					plugin = cls.newInstance();
					plugin.setTopologyManager(this);

					handlerStruct = new TopoPluginStruct(plugin);
				}catch(Exception e){
					System.err.println("Error on loading topology plugin " + cls.getCanonicalName());
					e.printStackTrace();
					continue;
				}

				boolean canBeEnabled = false;

				// check if it can be applied	
				for(int fileNum = 0 ; fileNum < reader.getNumberOfFilesLoaded() ; fileNum++){
					if( plugin.tryToActivate(reader.getLoadedFile(fileNum))){ 
						canBeEnabled = true;					
					}
				}
				if(! canBeEnabled){
					continue;
				}
			}


			// try to apply the plugin to each currently visible topology.
			final Enumeration<TreeNode> nodeEnum = tree_root.depthFirstEnumeration();
			while(nodeEnum.hasMoreElements()){ 
				final TreeNode node = nodeEnum.nextElement();

				if( TopologyTreeNode.class.isInstance(node) ){
					final TopologyTreeNode topNode = (TopologyTreeNode) node;

					// check if the object got already created:
					if(! handlerStruct.objsPerTopo.containsKey(topNode.topology)){					
						ITopologyInputPluginObject pluginObj = plugin.tryToInstantiateObjectFor(topNode.topology);
						if(pluginObj != null){
							handlerStruct.objsPerTopo.put(topNode.topology, pluginObj);
						}
					}
				}
			}


			if(isNew && ! handlerStruct.objsPerTopo.isEmpty()){
				topoPlugins.put(handlerStruct.plugin.getInstanciatedObjectsType(), handlerStruct);
			}
		}
	}

	/**
	 * Try to get the instantiated topology input plugin for a given topology.
	 * @param topology
	 * @param cls
	 * @return
	 */
	public <Type extends ITopologyInputPluginObject> Type getPluginObjectForTopology(TopologyNode topology, Class<Type> cls) {
		final TopoPluginStruct handlerStruct = topoPlugins.get(cls);
		if(handlerStruct == null)
			return null;
		return (Type) handlerStruct.objsPerTopo.get(topology);
	}
	
	public TraceFormatBufferedFileReader getTraceFormatFileReader() {
		return reader;
	}
}

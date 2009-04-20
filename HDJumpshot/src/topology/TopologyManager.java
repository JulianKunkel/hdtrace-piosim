
/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$ 
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



package topology;

import hdTraceInput.BufferedStatisticFileReader;
import hdTraceInput.BufferedTraceFileReader;
import hdTraceInput.TraceFormatBufferedFileReader;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;

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

import topology.mappings.ExistingTopologyMappings;
import topology.mappings.TopologyTreeMapping;
import viewer.common.Const;
import viewer.common.ModelTime;
import viewer.common.SortedJTreeModel;
import viewer.common.SortedJTreeNode;
import viewer.first.MainManager;
import viewer.histogram.StatisticHistogramFrame;
import viewer.timelines.TimelineType;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.topology.TopologyEntry;

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

	/**
	 * Stores for each topology entry the corresponding timeline
	 */
	private HashMap<TopologyEntry, Integer>  topoToTimelineMapping = new HashMap<TopologyEntry, Integer>();


	/**
	 * If set to true then listeners are not notified on a topology change, this allows mass update of topology
	 */
	private boolean                            changeListenerDisabled = false;
	private LinkedList<TopologyChangeListener> changeListener = new LinkedList<TopologyChangeListener>();

	private TreeExpansionListener treeExpansionListener = new TopologyTreeExpansionListener();

	private TopologyManagerContents topologyManagerType = TopologyManagerContents.EVERYTHING;

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
	private HashMap<StatisticDescription, LinkedList<RemovedNode>> removedNodesMap = new HashMap<StatisticDescription, LinkedList<RemovedNode>>(); 


	private SortedJTreeNode clickedNode;

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
				}

				@Override
				public void actionPerformed(ActionEvent e) {
					setTopologyMapping(ExistingTopologyMappings.valueOf(e.getActionCommand()));
				}
			});


			if(usedTopologyMapping == mapping){
				item.setEnabled(false);
			}
			popupMenu.add(item);						
		}
	}

	/**
	 * used to detect clicks on the tree i.e. for expanding the menus
	 */
	private MouseListener treeMouseListener = new MouseAdapter(){
		public void mouseClicked(java.awt.event.MouseEvent evt) {
			if (SwingUtilities.isRightMouseButton( evt )){
				final TreePath path = tree.getClosestPathForLocation(evt.getX(), evt.getY());
				clickedNode = (SortedJTreeNode) path.getLastPathComponent();

				JPopupMenu popupMenu = new JPopupMenu();

				// return if we are not in the Boundrary of the current path (i.e. not really clicked inside the path).
				if(! tree.getPathBounds(path).contains(evt.getPoint())){
					addTopologyMenu(popupMenu);
					popupMenu.show( evt.getComponent(), evt.getX(), evt.getY() );
					return;
				}

				if( TopologyStatisticTreeNode.class.isInstance(clickedNode) ){					
					// Show statistic histogram:			
					popupMenu.add(new AbstractAction(){
						private static final long serialVersionUID = 1L;

						{
							putValue(Action.NAME, "Show statistic histogram");
						}

						@Override
						public void actionPerformed(ActionEvent e) {
							final TopologyStatisticTreeNode statNode = (TopologyStatisticTreeNode) clickedNode;

							StatisticHistogramFrame frame = new StatisticHistogramFrame(
									(BufferedStatisticFileReader) statNode.getStatisticSource(), 
									statNode.getStatisticDescription(), modelTime, 
									reader.getCategory(((BufferedStatisticFileReader) statNode.getStatisticSource()).getGroup(), statNode.getStatisticDescription().getName()));
							frame.show();
						}
					});						
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
		topoToTimelineMapping.clear();
		for(int timeline = 0; timeline < tree.getRowCount(); timeline++){
			final TreePath path = tree.getPathForRow(timeline);
			final TreeNode node = (TreeNode) path.getLastPathComponent();

			if(TopologyTreeNode.class.isInstance(node)){
				timelines.add((TopologyTreeNode) node);

				topoToTimelineMapping.put(((TopologyTreeNode) node).getTopology(), timeline);
			}else{
				timelines.add(null);
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
		return (BufferedTraceFileReader) ((TopologyTraceTreeNode) timelines.get(timeline)).getTraceSource();
	}

	/**
	 * Get the statistic reader responsible for a particular timeline
	 * @param timeline
	 * @return
	 */
	public BufferedStatisticFileReader getStatisticReaderForTimeline(int timeline){
		return (BufferedStatisticFileReader) ((TopologyStatisticTreeNode) timelines.get(timeline)).getStatisticSource();
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
			for(String label: file.getTopologyLabels().getLabels()){
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

			fireTopologyChanged();			
		}catch(Exception e){
			throw new IllegalArgumentException(e);
		}
	}

	public void setStatisticVisiblity(StatisticDescription statistic, boolean visible){
		final SortedJTreeModel model = getTreeModel();

		if(visible == true){
			final LinkedList<RemovedNode> removedNodes = removedNodesMap.remove(statistic);
			if(removedNodes == null || removedNodes.size() == 0){
				return;
			}
			// now add the old values:
			for(RemovedNode rmNode: removedNodes){
				model.insertNodeInto(rmNode.child, rmNode.parent);
			}

			return;
		}

		// walk through tree:
		final Enumeration<DefaultMutableTreeNode> nodes = tree_root.depthFirstEnumeration();
		final LinkedList<RemovedNode> removedNodes = new LinkedList<RemovedNode>();

		while(nodes.hasMoreElements()){
			final DefaultMutableTreeNode node = nodes.nextElement();

			if( TopologyStatisticTreeNode.class.isInstance(node) ){
				final TopologyStatisticTreeNode statNode = (TopologyStatisticTreeNode) node;
				if(statistic == statNode.getStatisticDescription()){
					// remove that node:
					removedNodes.add( new RemovedNode(statNode));
					
					model.removeNodeFromParent(statNode);
				}
			}
		}
		removedNodesMap.put(statistic, removedNodes);

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
				tree.expandPath(path);
			}
		}
	}

	public JTree getTree() {
		return tree;
	}

	private SortedJTreeModel getTreeModel(){
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
	public Integer getTimelineForTopology(TopologyEntry entry){
		return topoToTimelineMapping.get(entry);		
	}
}

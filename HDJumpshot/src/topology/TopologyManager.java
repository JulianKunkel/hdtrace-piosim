
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
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import viewer.common.Const;
import viewer.common.ModelTime;
import viewer.histogram.StatisticHistogramFrame;
import viewer.timelines.TimelineType;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;

public class TopologyManager 
{
	private static final long serialVersionUID = 362940508169891280L;

	private JTree tree = new JTree();
	private DefaultMutableTreeNode  tree_root;

	final TraceFormatBufferedFileReader  reader;
	ModelTime                      modelTime;

	private ArrayList<TopologyTreeNode> topoToTimelineMapping = new ArrayList<TopologyTreeNode>();
	
	
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
		final DefaultMutableTreeNode parent;
		final DefaultMutableTreeNode child;
		
		public RemovedNode(DefaultMutableTreeNode child) {
			this.parent = (DefaultMutableTreeNode) child.getParent();
			this.child = child;
		}
	}
	
	/**
	 * Stores information per StatisticDescription to allow to remove timelines if a statistic is visible
	 * and to restore them if made visible.
	 */
	private HashMap<StatisticDescription, LinkedList<RemovedNode>> removedNodesMap = new HashMap<StatisticDescription, LinkedList<RemovedNode>>(); 
	
  
	/**
	 * used to detect clicks on the tree i.e. for expanding the menus
	 */
	private MouseListener treeMouseListener = new MouseAdapter(){
		public void mouseClicked(java.awt.event.MouseEvent evt) {
			if (SwingUtilities.isRightMouseButton( evt )){
				final TreePath path = tree.getClosestPathForLocation(evt.getX(), evt.getY());
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				
				// return if we are not in the Boundrary of the current path (i.e. not really clicked inside the path).
				if(! tree.getPathBounds(path).contains(evt.getPoint())){
					return;
				}
				
				if( TopologyStatisticTreeNode.class.isInstance(node) ){
					final TopologyStatisticTreeNode statNode = (TopologyStatisticTreeNode) node;

					JPopupMenu statisticPopupMenu = new JPopupMenu();
					statisticPopupMenu.add(new ShowStatisticHistogramAction(statNode));					
					statisticPopupMenu.show( evt.getComponent(), evt.getX(), evt.getY() );					
				}
			}
		};	
	};
	
	private class ShowStatisticHistogramAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		
		final TopologyStatisticTreeNode statNode;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			StatisticHistogramFrame frame = new StatisticHistogramFrame(
					(BufferedStatisticFileReader) statNode.getStatisticSource(), 
					statNode.getStatisticDescription(), modelTime, 
					reader.getCategory(((BufferedStatisticFileReader) statNode.getStatisticSource()).getGroup(), statNode.getStatisticDescription().getName()));
			frame.show();
		}
		
		public ShowStatisticHistogramAction(TopologyStatisticTreeNode statNode) {
			super("Show statistics for " + statNode.getStatisticName());
			this.statNode = statNode;
	        //putValue(SHORT_DESCRIPTION, desc);
		}
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
		topoToTimelineMapping.clear();
		for(int timeline = 0; timeline < tree.getRowCount(); timeline++){
			final TreePath path = tree.getPathForRow(timeline);
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
		final DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

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
	 * Call it once.
	 */
	public void init(ModelTime modelTime){
		this.modelTime = modelTime;
		restoreTopology();
	}
	
	/**
	 * restore the timelines to the normal / selected topology 
	 */
	public void restoreTopology(){
		final boolean old = setChangeListenerDisabled(true);
		topoToTimelineMapping.clear();

		this.tree_root = (new DefaultTopologyTreeMapping(topologyManagerType)).loadTopology(reader);

		tree.setModel(new DefaultTreeModel(tree_root));
		
		expandTreeInternal();

		removeEmptyTopologies();

		setChangeListenerDisabled(old);

		fireTopologyChanged();
	}
	
	public void setStatisticVisiblity(StatisticDescription statistic, boolean visible){
		final DefaultTreeModel model = getTreeModel();
		
		if(visible == true){
			final LinkedList<RemovedNode> removedNodes = removedNodesMap.remove(statistic);
			if(removedNodes == null || removedNodes.size() == 0){
				return;
			}
			// now add the old values:
			for(RemovedNode rmNode: removedNodes){
				model.insertNodeInto(rmNode.child, rmNode.parent, 0);
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
				if(statNode.getStatisticGroup() == statistic.getGroup() && statistic == statNode.getStatisticDescription()){
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

		final DefaultTreeModel model = getTreeModel();

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

		reloadTopologyMappingFromTree();	
		fireTopologyChanged();
	}



	public TopologyTreeNode getTreeNodeForTimeline(int timeline){
		return topoToTimelineMapping.get(timeline);
	}


	public TopologyManager( final TraceFormatBufferedFileReader  reader )
	{		
		this.reader = reader;
		tree.setEditable( true );		
		tree.putClientProperty("JTree.lineStyle", "Angled");
		
		tree.addMouseListener(treeMouseListener);
		tree.setFont(Const.FONT);
		
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
	
	private DefaultTreeModel getTreeModel(){
		return (DefaultTreeModel) tree.getModel();
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
}

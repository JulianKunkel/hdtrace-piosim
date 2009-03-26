/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.topology;

import hdTraceInput.BufferedStatisticFileReader;
import hdTraceInput.BufferedTraceFileReader;
import hdTraceInput.TraceFormatBufferedFileReader;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import viewer.timelines.TimelineType;
import viewer.zoomable.Debug;
import viewer.zoomable.named_vector;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.topology.TopologyInternalLevel;

public class TopologyManager extends JTree
{
	private DefaultMutableTreeNode  tree_root;

	final TraceFormatBufferedFileReader  reader;

	private List<TreePath>[]        leveled_paths;
	private int                     max_level;
	private int                     next_expanding_level;

	private List<TreePath>          cut_paste_buf;
	private int                     buf_level;

	private ArrayList<TopologyTreeNode> topoToTimelineMapping = new ArrayList<TopologyTreeNode>();

	private LinkedList<TopologyChangeListener> changeListener = new LinkedList<TopologyChangeListener>();

	private class TopologyTreeExpansionListener implements TreeExpansionListener{    
		// from tree expansion listener
		@Override
		public void treeCollapsed(TreeExpansionEvent event) {
			reloadTopologyMappingFromTree();
			for(TopologyChangeListener list: changeListener){
				list.topologyChanged();
			}
		}

		@Override
		public void treeExpanded(TreeExpansionEvent event) {
			reloadTopologyMappingFromTree();

			for(TopologyChangeListener list: changeListener){
				list.topologyChanged();
			}
		}
	}

	public void addTopologyChangedListener(TopologyChangeListener listener){
		changeListener.add(listener);
	}

	/**
	 * Recreate topology based on tree, i.e. not expanded nodes are not shown as timelines.
	 */
	public void reloadTopologyMappingFromTree(){
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
			return TimelineType.SPACER_NODE;    	
		return topoToTimelineMapping.get(timeline).getType();
	}

	private void clearTopologyToTimelineMapping(){
		topoToTimelineMapping.clear();
	}

	private void addTopologyTreeNode(TopologyTreeNode node, DefaultMutableTreeNode parent){
		if(node != null)
			node.setAssignedTimeline(topoToTimelineMapping.size());

		if(parent != null)
			parent.add(node);
	}

	private DefaultMutableTreeNode addDummyTreeNode(String name, DefaultMutableTreeNode parent){
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
		parent.add(node);

		return node;
	}


	private void recursivlyAddTopology(int level, DefaultMutableTreeNode parentNode, TopologyInternalLevel topology, TraceFormatFileOpener file){
		if(topology.isLeaf()){
			TopologyTreeNode node = new TopologyTraceTreeNode(topology, file, this); 

			addTopologyTreeNode(node, parentNode);    	
			return;
		}
		TopologyTreeNode node = new TopologyInnerNode(topology, file, this); 

		addTopologyTreeNode(node, parentNode);    	

		for(TopologyInternalLevel child: topology.getChildElements().values()){
			recursivlyAddTopology(level +1, node, child, file);
		}

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
	 * Load a default topology, filename => hierarchically print the children 
	 */
	public void loadDefaultTopology(){
		clearTopologyToTimelineMapping();

		tree_root = new DefaultMutableTreeNode("HDTrace");
		// add a dummy
		addTopologyTreeNode(null, null);

		setModel(new DefaultTreeModel(tree_root));            

		for(int f = 0 ; f < reader.getNumberOfFilesLoaded() ; f++){
			recursivlyAddTopology(1, tree_root, reader.getLoadedFile(f).getTopology(), reader.getLoadedFile(f));
		}

		this.update_leveled_paths();

		for(int i=0 ; i < 10; i++)
			expandLevel();
		
		reloadTopologyMappingFromTree();
	}

	public TopologyTreeNode getTreeNodeForTimeline(int timeline){
		return topoToTimelineMapping.get(timeline);
	}


	public TopologyManager( final TraceFormatBufferedFileReader  reader )
	{
		this.reader = reader;


		super.setEditable( true );

		super.putClientProperty("JTree.lineStyle", "Angled");
		
		loadDefaultTopology();   

		this.addTreeExpansionListener( new TopologyTreeExpansionListener());
	}

	private void getAllLeavesForNode( named_vector nvtr,
			DefaultMutableTreeNode node )
	{
		DefaultMutableTreeNode child;
		Enumeration children  = node.children();
		while ( children.hasMoreElements() ) {
			child = (DefaultMutableTreeNode) children.nextElement();
			if ( child.isLeaf() )
				nvtr.add( child.getUserObject() );
			else
				getAllLeavesForNode( nvtr, child );
		}
	}

	public named_vector getNamedVtr( TreePath node_path )
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		node_path.getLastPathComponent();
		named_vector nvtr = new named_vector( node.toString() );
		if ( ! super.isExpanded( node_path ) )
			getAllLeavesForNode( nvtr, node );
		return nvtr;
	}


	public void update_leveled_paths()
	{
		Iterator                paths;
		Enumeration             nodes;
		DefaultMutableTreeNode  node;
		TreePath                path;
		int                     ilevel;

		// Update the tree's Maximum allowed Level
		max_level = tree_root.getLastLeaf().getLevel();

		if ( Debug.isActive() ) {
			Debug.println( "tree_root(" + tree_root + ").level="
					+ tree_root.getLevel() );
			Debug.println( "last_leaf(" + tree_root.getLastLeaf() + ").level="
					+ max_level );
		}

		// Initialize the leveled_paths[] sizes
		leveled_paths = new ArrayList[ max_level + 1 ];
		leveled_paths[ 0 ] = new ArrayList( 1 );
		for ( ilevel = 1; ilevel <= max_level; ilevel++ )
			leveled_paths[ ilevel ] = new ArrayList();

		// Update the leveled_paths[]'s content
		nodes = tree_root.breadthFirstEnumeration();
		if ( nodes != null )
			while ( nodes.hasMoreElements() ) {
				node = (DefaultMutableTreeNode) nodes.nextElement();
				path = new TreePath( node.getPath() );
				leveled_paths[ node.getLevel() ].add( path );
			}

		// Update next_expanding_level
		boolean isAllExpanded = true;
		ilevel = 0;
		next_expanding_level = ilevel;
		while ( ilevel < max_level && isAllExpanded ) {
			paths = leveled_paths[ ilevel ].iterator();
			while ( paths.hasNext() && isAllExpanded ) {
				path = (TreePath) paths.next();
				isAllExpanded = isAllExpanded && super.isExpanded( path );
			}
			ilevel++;
		}
		if ( ilevel > max_level )
			next_expanding_level = max_level;
		else
			next_expanding_level = ilevel - 1;
	}

	public boolean isLevelExpandable()
	{
		return next_expanding_level < max_level;
	}

	public void expandLevel()
	{
		Iterator    paths;
		TreePath    path;

		if ( ! isLevelExpandable() )
			return;

		paths = leveled_paths[ next_expanding_level ].iterator();
		while ( paths.hasNext() ) {
			path = (TreePath) paths.next();
			if ( super.isCollapsed( path ) )
				super.expandPath( path );
		}
		if ( next_expanding_level < max_level )
			next_expanding_level++;
		else
			next_expanding_level = max_level;
	}

	public boolean isLevelCollapsable()
	{
		int next_collapsing_level = next_expanding_level - 1;
		return next_collapsing_level >= 0;
	}

	public void collapseLevel()
	{
		Iterator    paths;
		TreePath    path;
		int         next_collapsing_level;

		if ( ! isLevelCollapsable() )
			return;

		next_collapsing_level = next_expanding_level - 1;
		paths = leveled_paths[ next_collapsing_level ].iterator();
		while ( paths.hasNext() ) {
			path = (TreePath) paths.next();
			if ( super.isExpanded( path ) )
				super.collapsePath( path );
		}
		next_expanding_level = next_collapsing_level;
	}

	//  Manipulation of the Cut&Paste buffer
	public void renewCutAndPasteBuffer()
	{
		buf_level = -1;
		if ( cut_paste_buf != null )
			cut_paste_buf.clear();
		else
			cut_paste_buf = new ArrayList();
	}

	public boolean isPathLevelSameAsThatOfCutAndPasteBuffer( TreePath path )
	{
		return buf_level == this.getLastPathComponentLevel( path );
	}

	private int getLastPathComponentLevel( TreePath path )
	{
		DefaultMutableTreeNode node;
		node = (DefaultMutableTreeNode) path.getLastPathComponent();
		return node.getLevel();
	}

	public boolean isCutAndPasteBufferUniformlyLeveled( TreePath [] paths )
	{
		if ( paths != null && paths.length > 0 ) {
			int ilevel = this.getLastPathComponentLevel( paths[ 0 ] );
			for ( int idx = 1; idx < paths.length; idx++ ) {
				if ( ilevel != this.getLastPathComponentLevel( paths[ idx ] ) )
					return false;
			}
			buf_level = ilevel;
		}
		return true;
	}

	public void addToCutAndPasteBuffer( TreePath [] paths )
	{
		if ( cut_paste_buf != null ) {
			for ( int idx = 0; idx < paths.length; idx++ )
				cut_paste_buf.add( paths[ idx ] );
		}
	}

	public int getLevelOfCutAndPasteBuffer()
	{
		return buf_level;
	}

	public TreePath[] getFromCutAndPasteBuffer()
	{
		if ( cut_paste_buf != null ) {
			Object [] objs    = cut_paste_buf.toArray();
			TreePath [] paths = new TreePath[ objs.length ];
			for ( int idx = 0; idx < objs.length; idx++ )
				paths[ idx ] = (TreePath) objs[ idx ];
			return paths;
		}
		else
			return null;
	}

	public void clearCutAndPasteBuffer()
	{
		if ( cut_paste_buf != null )
			cut_paste_buf.clear();
	}
}

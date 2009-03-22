/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.zoomable;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import viewer.BufferedStatisticFileReader;
import viewer.BufferedTraceFileReader;
import viewer.TimelineType;
import viewer.TraceFormatBufferedFileReader;
import de.hd.pvs.TraceFormat.statistics.StatisticsReader;
import de.hd.pvs.TraceFormat.topology.HostnamePerProjectContainer;
import de.hd.pvs.TraceFormat.topology.RanksPerHostnameTraceContainer;
import de.hd.pvs.TraceFormat.topology.ThreadsPerRankTraceContainer;

// import viewer.common.Routines;
// import viewer.common.Parameters;

public class YaxisTree extends JTree
{
    private DefaultMutableTreeNode  tree_root;
    
    final TraceFormatBufferedFileReader  reader;

    private List<TreePath>[]                  leveled_paths;
    private int                     max_level;
    private int                     next_expanding_level;
 
    private List<TreePath>          cut_paste_buf;
    private int                     buf_level;
    
    /**
     * Get the trace reader for a particular timeline
     * 
     * @param timeline
     * @return
     */
    public BufferedTraceFileReader getTraceReaderForTimeline(int timeline){
    	return (BufferedTraceFileReader) reader.getFileOpener().getHostnameProcessMap().get("localhost").getTraceFilesPerRank().get(0).getFilesPerThread().get(0).getTraceFileReader();
    }
    
    /**
     * Get the statistic reader responsible for a particular timeline
     * @param timeline
     * @return
     */
    public BufferedStatisticFileReader getStatisticReaderForTimeline(int timeline){
    	return (BufferedStatisticFileReader) reader.getFileOpener().getHostnameProcessMap().get("localhost").getTraceFilesPerRank().get(0).getFilesPerThread().get(0).getStatisticReaders().get("Energy");
    }
    
    public int getStatisticNumberForTimeline(int timeline){
    	return 1;
    }
    
    public int getTimelines(){
    	return 3;
    }

    public TimelineType getType(int timeline){
    	return TimelineType.TRACE;
    }
    
    ////
    
    public YaxisTree( final TraceFormatBufferedFileReader  reader )
    {
        this.reader = reader;
        
        
        tree_root = new DefaultMutableTreeNode("Hosts");  
        
        // add all hosts
        // TODO generate PVFS2 stuff
        for (HostnamePerProjectContainer hostfiles: reader.getFileOpener().getHostnameProcessMap().values()){
        	final DefaultMutableTreeNode hostNode = new DefaultMutableTreeNode(hostfiles.getHostname());
        	tree_root.add(hostNode);
        	
        	for(RanksPerHostnameTraceContainer rankFiles: hostfiles.getTraceFilesPerRank().values()){
        		final DefaultMutableTreeNode rankNode = new DefaultMutableTreeNode(rankFiles.getRank());        		
        		hostNode.add(rankNode);
        		
        		for(ThreadsPerRankTraceContainer threadFiles: rankFiles.getFilesPerThread().values()){
        			final DefaultMutableTreeNode threadNode = new DefaultMutableTreeNode(threadFiles.getThread());        		
            		rankNode.add(threadNode);
            		            	
            		// add all statistics & trace            		
            		final DefaultMutableTreeNode traceNode = new DefaultMutableTreeNode("Trace");
            		threadNode.add(traceNode);
            		
            		for(String stats : threadFiles.getStatisticReaders().keySet()){
            			final DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(stats);
            			threadNode.add(groupNode);
            		}
            			
        		}
        	}
        }
        setModel(new DefaultTreeModel(tree_root));
        
        this.update_leveled_paths();
        super.setEditable( true );
        
        for(int i=0 ; i < 10; i++)
        	expandLevel();
        
        super.putClientProperty("JTree.lineStyle", "Angled");
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

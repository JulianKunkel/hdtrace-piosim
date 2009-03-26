/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.zoomable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import topology.TopologyManager;

public class ActionYaxisTreeExpand implements ActionListener
{
    private TopologyManager          tree_view;

    public ActionYaxisTreeExpand(TopologyManager       in_tree )
    {
        tree_view  = in_tree;
    }

    public void actionPerformed( ActionEvent event )
    {
    	tree_view.expandTree();
    }
}

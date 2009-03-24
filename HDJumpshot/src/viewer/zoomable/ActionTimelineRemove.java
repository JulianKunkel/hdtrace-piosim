/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.zoomable;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import javax.swing.tree.*;

import viewer.topology.TopologyManager;

public class ActionTimelineRemove implements ActionListener
{
    private YaxisList         list;
    private TopologyManager         tree;

    public ActionTimelineRemove( TopologyManager in_tree, YaxisList in_list )
    {
        tree = in_tree;
        list = in_list;
    }

    public void actionPerformed( ActionEvent event )
    {
        if ( Debug.isActive() )
            Debug.println( "Action for Remove Timeline button" );

        TreePath []  paths = tree.getSelectionPaths();
        /*
        for ( int idx = 0; idx < paths.length; idx++ )
            System.out.println( "Remove " + paths[ idx ] );
        */
        list.paintBlank( paths );
    }
}

/*
 *  (C) 2007 by Julian Kunkel
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Julian Kunkel
 */

package viewer.zoomable;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.tree.*;

import viewer.common.Dialogs;

public class ActionTimelineRestore implements ActionListener
{
	private class timeline_remove_operations{
		MutableTreeNode parent;
		MutableTreeNode child;
		int index;

		public timeline_remove_operations(MutableTreeNode parent, MutableTreeNode child, int index) {
			this.parent = parent;
			this.child = child;
			this.index = index;
		}
	};

    private Window             root_window;
    private ToolBarStatus      toolbar;
    private YaxisTree          tree;
    private DefaultTreeModel   tree_model;
    private ArrayList<timeline_remove_operations> operations =
    	new ArrayList<timeline_remove_operations>();

    public ActionTimelineRestore( Window           parent_window,
                                 ToolBarStatus    in_toolbar,
                                 YaxisTree        in_tree )
    {
        root_window  = parent_window;
        toolbar      = in_toolbar;
        tree         = in_tree;
        tree_model   = (DefaultTreeModel) tree.getModel();

    }

    public void actionPerformed( ActionEvent event )
    {
        if ( Debug.isActive() )
            Debug.println( "Action for Restore Timeline button" );

        int i;


        /* replay delete operations */
        for( i = operations.size()-1 ; i >= 0; i-- ){
            timeline_remove_operations op = operations.get(i);
            tree_model.insertNodeInto(op.child, op.parent, op.index);
        }
        operations.clear();

        // Update leveled_paths[]
        tree.update_leveled_paths();

        // Set toolbar buttons to reflect status
        toolbar.resetYaxisTreeButtons();
        
    }

    public void addUndoOperation(MutableTreeNode child){
    	MutableTreeNode parent = (MutableTreeNode) child.getParent();
    	operations.add( new timeline_remove_operations(
    			parent, child, parent.getIndex(child)) );
    }

}

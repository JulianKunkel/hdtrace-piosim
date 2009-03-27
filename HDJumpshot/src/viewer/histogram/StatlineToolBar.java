
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
  */

//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.


/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author Anthony Chan (Jumpshot 4), Julian M. Kunkel
 */

package viewer.histogram;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.JToolBar;

import topology.TopologyManager;
import viewer.common.Const;
import viewer.timelines.CanvasTimeline;
import viewer.zoomable.ActionPptyRefresh;
import viewer.zoomable.ActionPptyScreenshot;
import viewer.zoomable.ActionTimelineRestore;
import viewer.zoomable.ActionVportBackward;
import viewer.zoomable.ActionVportDown;
import viewer.zoomable.ActionVportForward;
import viewer.zoomable.ActionVportUp;
import viewer.zoomable.ActionYaxisTreeExpand;
import viewer.zoomable.ActionZoomHome;
import viewer.zoomable.ActionZoomIn;
import viewer.zoomable.ActionZoomOut;
import viewer.zoomable.ActionZoomRedo;
import viewer.zoomable.ActionZoomUndo;
import viewer.zoomable.ModelTime;
import viewer.zoomable.RulerTime;
import viewer.zoomable.ScrollbarTime;
import viewer.zoomable.ToolBarStatus;
import viewer.zoomable.ViewportTimeYaxis;

public class StatlineToolBar extends JToolBar
                             implements ToolBarStatus
{
    private Window                  root_window;
    private ViewportTimeYaxis       canvas_vport;
    private JScrollBar              y_scrollbar;
    private TopologyManager               y_tree;
    private ScrollbarTime           time_scrollbar;
    private ModelTime               time_model;

    private JButton                 mark_btn;
    private JButton                 move_btn;
    private JButton                 delete_btn;

    private JButton                 up_btn;
    private JButton                 down_btn;

    private JButton                 expand_btn;
    private JButton                 collapse_btn;

    private JButton                 backward_btn;
    private JButton                 forward_btn;

    private JButton                 zoomUndo_btn;
    private JButton                 zoomOut_btn;
    private JButton                 zoomHome_btn;
    private JButton                 zoomIn_btn;
    private JButton                 zoomRedo_btn;
    // private JButton                 zoomSet_btn;

    private JButton                 refresh_btn;
    private JButton                 print_btn;
    private CanvasTimeline          cnvas_timeline = null;
    private ActionTimelineRestore   restore;
    private RulerTime	            time_ruler;

    public StatlineToolBar( Window             parent_window,
    						CanvasTimeline  cnvas_timeline,
    						ActionTimelineRestore restore,
    						RulerTime	 time_ruler,
                            ViewportTimeYaxis  canvas_viewport,
                            JScrollBar         yaxis_scrollbar,
                            TopologyManager          yaxis_tree,
                            ScrollbarTime      a_time_scrollbar,
                            ModelTime          a_time_model )
    {
        super();
        this.restore = restore;
        this.canvas_vport = canvas_viewport;
        root_window      = parent_window;
        canvas_vport     = canvas_viewport;
        y_scrollbar      = yaxis_scrollbar;
        y_tree           = yaxis_tree;
        time_scrollbar   = a_time_scrollbar;
        time_model       = a_time_model;
        this.addButtons();
        canvas_vport.setToolBarStatus( this );
    }

    public void init()
    {
        this.initAllButtons();
    }

    private void addButtons()
    {
        Insets     btn_insets;
        Dimension  mini_separator_size;
        URL        icon_URL;

        btn_insets          = new Insets( 2, 2, 2, 2 );
        mini_separator_size = new Dimension( 5, 5 );

        up_btn = new JButton( new ImageIcon(  Const.IMG_PATH + "Up24.gif" ) );
        up_btn.setMargin( btn_insets );
        up_btn.setToolTipText( "Scroll Upward by half a screen" );
        // up_btn.setPreferredSize( btn_dim );
        up_btn.addActionListener( new ActionVportUp( y_scrollbar ) );
        up_btn.setMnemonic( KeyEvent.VK_UP );
        super.add( up_btn );

        down_btn = new JButton( new ImageIcon(  Const.IMG_PATH + "Down24.gif" ) );
        
        down_btn.setMargin( btn_insets );
        down_btn.setToolTipText( "Scroll Downward by half a screen" );
        down_btn.setMnemonic( KeyEvent.VK_DOWN );
        // down_btn.setPreferredSize( btn_dim );
        down_btn.addActionListener( new ActionVportDown( y_scrollbar ) );
        super.add( down_btn );

        super.addSeparator( mini_separator_size );

        mark_btn = new JButton( new ImageIcon(  Const.IMG_PATH + "Down24.gif" ) );
        
        mark_btn.setMargin( btn_insets );
        mark_btn.setToolTipText( "Mark the timelines" );
        // mark_btn.setPreferredSize( btn_dim );
        mark_btn.addActionListener(
                 new ActionTimelineMark( root_window, this, y_tree ) );
        super.add( mark_btn );

        icon_URL = getURL( Const.IMG_PATH + "Paste24.gif" );
        if ( icon_URL != null )
            move_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            move_btn = new JButton( "LabelMove" );
        move_btn.setMargin( btn_insets );
        move_btn.setToolTipText( "Move the marked timelines" );
        // move_btn.setPreferredSize( btn_dim );
        move_btn.addActionListener(
                 new ActionTimelineMove( root_window, this, y_tree ) );
        super.add( move_btn );

        icon_URL = getURL( Const.IMG_PATH + "Delete24.gif" );
        if ( icon_URL != null )
            delete_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            delete_btn = new JButton( "LabelDelete" );
        delete_btn.setMargin( btn_insets );
        delete_btn.setToolTipText( "Delete the marked timelines" );
        // delete_btn.setPreferredSize( btn_dim );
        delete_btn.addActionListener(
                   new ActionTimelineDelete( root_window, this, y_tree, restore ) );
        super.add( delete_btn );

        super.addSeparator( mini_separator_size );

        icon_URL = getURL( Const.IMG_PATH + "TreeExpand24.gif" );
        if ( icon_URL != null )
            expand_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            expand_btn = new JButton( "LabelExpand" );
        expand_btn.setMargin( btn_insets );
        expand_btn.setToolTipText(
                   "Expand the Y-axis tree label by 1 level" );
        expand_btn.setMnemonic( KeyEvent.VK_E );
        // expand_btn.setPreferredSize( btn_dim );
        expand_btn.addActionListener(
                   new ActionYaxisTreeExpand( this, y_tree ) );
        super.add( expand_btn );

        icon_URL = getURL( Const.IMG_PATH + "TreeCollapse24.gif" );
        if ( icon_URL != null )
            collapse_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            collapse_btn = new JButton( "LabelCollapse" );
        collapse_btn.setMargin( btn_insets );
        collapse_btn.setToolTipText(
                     "Collapse the Y-axis tree label by 1 level" );
        collapse_btn.setMnemonic( KeyEvent.VK_C );
        // collapse_btn.setPreferredSize( btn_dim );
        collapse_btn.addActionListener(
                     new ActionYaxisTreeCollapse( this, y_tree ) );
        super.add( collapse_btn );
        
        super.addSeparator();
        super.addSeparator();

        icon_URL = getURL( Const.IMG_PATH + "Backward24.gif" );
        if ( icon_URL != null )
            backward_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            backward_btn = new JButton( "Backward" );
        backward_btn.setMargin( btn_insets );
        backward_btn.setToolTipText( "Scroll Backward by half a screen" );
        backward_btn.setMnemonic( KeyEvent.VK_LEFT );
        // backward_btn.setPreferredSize( btn_dim );
        backward_btn.addActionListener(
                     new ActionVportBackward( time_scrollbar ) );
        super.add( backward_btn );

        icon_URL = getURL( Const.IMG_PATH + "Forward24.gif" );
        if ( icon_URL != null )
            forward_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            forward_btn = new JButton( "Forward" );
        forward_btn.setMargin( btn_insets );
        forward_btn.setToolTipText( "Scroll Forward by half a screen" );
        forward_btn.setMnemonic( KeyEvent.VK_RIGHT );
        // forward_btn.setPreferredSize( btn_dim );
        forward_btn.addActionListener(
                    new ActionVportForward( time_scrollbar ) );
        super.add( forward_btn );

        super.addSeparator( mini_separator_size );

        // icon_URL = getURL( Const.IMG_PATH + "Undo24.gif" );
        icon_URL = getURL( Const.IMG_PATH + "WinUndo.gif" );
        if ( icon_URL != null )
            zoomUndo_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            zoomUndo_btn = new JButton( "ZoomUndo" );
        zoomUndo_btn.setMargin( btn_insets );
        zoomUndo_btn.setToolTipText( "Undo the previous zoom operation" );
        zoomUndo_btn.setMnemonic( KeyEvent.VK_U );
        // zoomUndo_btn.setPreferredSize( btn_dim );
        zoomUndo_btn.addActionListener(
                     new ActionZoomUndo( this, time_model ) );
        super.add( zoomUndo_btn );

        super.addSeparator( mini_separator_size );

        icon_URL = getURL( Const.IMG_PATH + "ZoomOut24.gif" );
        if ( icon_URL != null )
            zoomOut_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            zoomOut_btn = new JButton( "ZoomOut" );
        zoomOut_btn.setMargin( btn_insets );
        zoomOut_btn.setToolTipText( "Zoom Out by 1 level in time" );
        zoomOut_btn.setMnemonic( KeyEvent.VK_O );
        // zoomOut_btn.setPreferredSize( btn_dim );
        zoomOut_btn.addActionListener(
                    new ActionZoomOut( this, time_model ) );
        super.add( zoomOut_btn );

        icon_URL = getURL( Const.IMG_PATH + "Home24.gif" );
        if ( icon_URL != null )
            zoomHome_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            zoomHome_btn = new JButton( "ZoomHome" );
        zoomHome_btn.setMargin( btn_insets );
        zoomHome_btn.setToolTipText(
                     "Reset zoom to the initial resolution in time" );
        zoomHome_btn.setMnemonic( KeyEvent.VK_H );
        // zoomHome_btn.setPreferredSize( btn_dim );
        zoomHome_btn.addActionListener(
                 new ActionZoomHome( this, time_model ) );
        super.add( zoomHome_btn );

        icon_URL = getURL( Const.IMG_PATH + "ZoomIn24.gif" );
        if ( icon_URL != null )
            zoomIn_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            zoomIn_btn = new JButton( "ZoomIn" );
        zoomIn_btn.setMargin( btn_insets );
        zoomIn_btn.setToolTipText( "Zoom In iby 1 level in time" );
        zoomIn_btn.setMnemonic( KeyEvent.VK_I );
        // zoomIn_btn.setPreferredSize( btn_dim );
        zoomIn_btn.addActionListener(
                   new ActionZoomIn( this, time_model ) );
        super.add( zoomIn_btn );

        super.addSeparator( mini_separator_size );

        // icon_URL = getURL( Const.IMG_PATH + "Redo24.gif" );
        icon_URL = getURL( Const.IMG_PATH + "WinRedo.gif" );
        if ( icon_URL != null )
            zoomRedo_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            zoomRedo_btn = new JButton( "ZoomRedo" );
        zoomRedo_btn.setMargin( btn_insets );
        zoomRedo_btn.setToolTipText( "Redo the previous zoom operation" );
        zoomRedo_btn.setMnemonic( KeyEvent.VK_R );
        // zoomRedo_btn.setPreferredSize( btn_dim );
        zoomRedo_btn.addActionListener(
                     new ActionZoomRedo( this, time_model ) );
        super.add( zoomRedo_btn );

        super.addSeparator( mini_separator_size );

        super.addSeparator();

        icon_URL = getURL( Const.IMG_PATH + "Refresh24.gif" );
        if ( icon_URL != null )
            refresh_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            refresh_btn = new JButton( "CanvasReDraw" );
        refresh_btn.setMargin( btn_insets );
        refresh_btn.setToolTipText(
                     "Redraw canvas to synchronize changes from "
                   + "Preference/Legend window or Yaxis label panel" );
        refresh_btn.setMnemonic( KeyEvent.VK_D );
        // refresh_btn.setPreferredSize( btn_dim );
        refresh_btn.addActionListener(
                   new ActionPptyRefresh( y_tree ) );
        super.add( refresh_btn );

        icon_URL = getURL( Const.IMG_PATH + "Print24.gif" );
        if ( icon_URL != null )
            print_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            print_btn = new JButton( "Print" );
        print_btn.setMargin( btn_insets );
        print_btn.setToolTipText( "Print the Statline window" );
        // print_btn.setPreferredSize( btn_dim );
        print_btn.addActionListener( new ActionPptyScreenshot(cnvas_timeline, time_ruler) );
        super.add( print_btn );
    }

    private void initAllButtons()
    {
        up_btn.setEnabled( true );
        down_btn.setEnabled( true );

        mark_btn.setEnabled( true );
        move_btn.setEnabled( false );
        delete_btn.setEnabled( false );
        // remove_btn.setEnabled( true );

        this.resetYaxisTreeButtons();

        backward_btn.setEnabled( true );
        forward_btn.setEnabled( true );
        this.resetZoomButtons();

        refresh_btn.setEnabled( true );
        print_btn.setEnabled( false );
    }

    //  Interface for ToolBarStatus
    public void resetZoomButtons()
    {
        double zoomlevel = time_model.getZoomFaktor();
        zoomIn_btn.setEnabled( zoomlevel < Const.MAX_ZOOM_FAKTOR );
        zoomHome_btn.setEnabled( zoomlevel != Const.MIN_ZOOM_FAKTOR );
        zoomOut_btn.setEnabled( zoomlevel > Const.MIN_ZOOM_FAKTOR );

        zoomUndo_btn.setEnabled( ! time_model.isZoomUndoStackEmpty() );
        zoomRedo_btn.setEnabled( ! time_model.isZoomRedoStackEmpty() );
    }

    //  Interface for ToolBarStatus
    public void resetYaxisTreeButtons()
    {
        expand_btn.setEnabled( y_tree.isLevelExpandable() );
        collapse_btn.setEnabled( y_tree.isLevelCollapsable() );
    }

    //  Interface for ToolBarStatus
    public JButton getPropertyRefreshButton()
    { return refresh_btn; }

    //  Interface for ToolBarStatus
    public JButton getTimelineMarkButton()
    { return mark_btn; }

    //  Interface for ToolBarStatus
    public JButton getTimelineMoveButton()
    { return move_btn; }
    //  Interface for ToolBarStatus
    public JButton getTimelineDeleteButton()
    { return delete_btn; }
}


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

package viewer.timelines;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.JToolBar;

import topology.TopologyManager;
import viewer.common.Const;
import viewer.zoomable.ActionPptyAutoRefresh;
import viewer.zoomable.ActionPptyRefresh;
import viewer.zoomable.ActionPptyScreenshot;
import viewer.zoomable.ActionSearchBackward;
import viewer.zoomable.ActionSearchForward;
import viewer.zoomable.ActionSearchInit;
import viewer.zoomable.ActionTimelineRemove;
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
import viewer.zoomable.RowAdjustments;
import viewer.zoomable.RulerTime;
import viewer.zoomable.ScrollbarTime;
import viewer.zoomable.ToolBarStatus;
import viewer.zoomable.ViewportTimeYaxis;


public class TimelineToolBar extends JToolBar
                             implements ToolBarStatus
{
    private Window                  root_window;
    private ViewportTimeYaxis       canvas_vport;
    private JScrollBar              y_scrollbar;
    private TopologyManager         topologyManager;
    private ScrollbarTime           time_scrollbar;
    private ModelTime               time_model;

    public  JButton                 timeline_delete_btn;
    public  JButton                 timelines_restore_btn;
    private JButton                 timelines_expand_btn;
    
    private JButton                 up_btn;
    private JButton                 down_btn;

    private JButton                 commit_btn;

    private JButton                 backward_btn;
    private JButton                 forward_btn;

    private JButton                 zoomUndo_btn;
    private JButton                 zoomOut_btn;
    private JButton                 zoomHome_btn;
    private JButton                 zoomIn_btn;
    private JButton                 zoomRedo_btn;

    private JButton                 searchBack_btn;
    private JButton                 searchInit_btn;
    private JButton                 searchFore_btn;

    private JButton                 refresh_btn;
    private JButton                 print_btn;
    private final CanvasTimeline          cnvas_timeline;
    private RulerTime	 			time_ruler = null;


    public ViewportTimeYaxis getCanvas_vport() {
		return canvas_vport;
	}

	public TimelineToolBar( Window             parent_window,
    						CanvasTimeline cnvas_timeline,
    						RulerTime	 time_ruler,
                            ViewportTimeYaxis  canvas_viewport,
                            JScrollBar         yaxis_scrollbar,
                            TopologyManager          yaxis_tree,
                            ScrollbarTime      a_time_scrollbar,
                            ModelTime          a_time_model,
                            RowAdjustments     a_row_adjs )
    {
        super();
        
        this.cnvas_timeline = cnvas_timeline;
		
        this.time_ruler = time_ruler;
        root_window      = parent_window;
        canvas_vport     = canvas_viewport;
        y_scrollbar      = yaxis_scrollbar;
        topologyManager           = yaxis_tree;
        time_scrollbar   = a_time_scrollbar;
        time_model       = a_time_model;
        this.addButtons();
        canvas_vport.setToolBarStatus( this );
        
        this.setFloatable(false);
        
        // orientate the toolbar left:
        addSeparator(new Dimension(1000,10));
    }

    public void init()
    {
        this.initAllButtons();
    }

    private void addButtons()
    {
        Insets     btn_insets;
        Dimension  mini_separator_size;

        btn_insets          = new Insets( 2, 2, 2, 2 );
        mini_separator_size = new Dimension( 5, 5 );

        up_btn = new JButton( new ImageIcon( Const.IMG_PATH + "Up24.gif" ) );
        up_btn.setMargin( btn_insets );
        up_btn.setToolTipText( "Scroll Upward by half a screen" );
        // up_btn.setPreferredSize( btn_dim );
        up_btn.addActionListener( new ActionVportUp( y_scrollbar ) );
        up_btn.setMnemonic( KeyEvent.VK_UP );
        super.add( up_btn );

        down_btn = new JButton( new ImageIcon( Const.IMG_PATH + "Down24.gif" ) );
        
        down_btn.setMargin( btn_insets );
        down_btn.setToolTipText( "Scroll Downward by half a screen" );
        down_btn.setMnemonic( KeyEvent.VK_DOWN );
        // down_btn.setPreferredSize( btn_dim );
        down_btn.addActionListener( new ActionVportDown( y_scrollbar ) );
        super.add( down_btn );

        super.addSeparator( mini_separator_size );

        timeline_delete_btn = new JButton( new ImageIcon( Const.IMG_PATH + "Delete24.gif" ) );
        timeline_delete_btn.setMargin( btn_insets );
        timeline_delete_btn.setToolTipText( "Delete the marked timelines" );
        // delete_btn.setPreferredSize( btn_dim );
        timeline_delete_btn.addActionListener( new ActionTimelineRemove( topologyManager) );
        super.add( timeline_delete_btn );

       	timelines_restore_btn = new JButton( new ImageIcon( Const.IMG_PATH + "WinUndo.gif"  ) );
        timelines_restore_btn.setMargin( btn_insets );
        timelines_restore_btn.setToolTipText( "Restore the timelines" );
        timelines_restore_btn.addActionListener( new ActionTimelineRestore(topologyManager) );
        super.add( timelines_restore_btn );

        timelines_expand_btn = new JButton( new ImageIcon( Const.IMG_PATH + "TreeExpand24.gif"  ) );
        timelines_expand_btn.setMargin( btn_insets );
        timelines_expand_btn.setToolTipText( "Expand the Y-axis tree label by 1 level" );
        timelines_expand_btn.setMnemonic( KeyEvent.VK_E );
        // expand_btn.setPreferredSize( btn_dim );
        timelines_expand_btn.addActionListener( new ActionYaxisTreeExpand( topologyManager ) );
        super.add( timelines_expand_btn );

        commit_btn = new JButton( new ImageIcon(  Const.IMG_PATH + "TreeCommit24.gif"  ) );
        
        commit_btn.setMargin( btn_insets );
        commit_btn.setToolTipText(
                   "Commit changes and Redraw the TimeLines Display" );
        commit_btn.setMnemonic( KeyEvent.VK_D );
        
        super.addSeparator();
        super.addSeparator();

        backward_btn = new JButton( new ImageIcon( Const.IMG_PATH + "Backward24.gif"  ) );
        backward_btn.setMargin( btn_insets );
        backward_btn.setToolTipText( "Scroll Backward by half a screen" );
        backward_btn.setMnemonic( KeyEvent.VK_LEFT );
        // backward_btn.setPreferredSize( btn_dim );
        backward_btn.addActionListener(
                     new ActionVportBackward( time_scrollbar ) );
        super.add( backward_btn );

        forward_btn = new JButton( new ImageIcon( Const.IMG_PATH + "Forward24.gif"  ) );
        forward_btn.setMargin( btn_insets );
        forward_btn.setToolTipText( "Scroll Forward by half a screen" );
        forward_btn.setMnemonic( KeyEvent.VK_RIGHT );
        // forward_btn.setPreferredSize( btn_dim );
        forward_btn.addActionListener(
                    new ActionVportForward( time_scrollbar ) );
        super.add( forward_btn );
        
        super.addSeparator( );

        zoomUndo_btn = new JButton( new ImageIcon( Const.IMG_PATH + "WinUndo.gif"  ) );
        zoomUndo_btn.setMargin( btn_insets );
        zoomUndo_btn.setToolTipText( "Undo the previous zoom operation" );
        zoomUndo_btn.setMnemonic( KeyEvent.VK_U );
        // zoomUndo_btn.setPreferredSize( btn_dim );
        zoomUndo_btn.addActionListener(
                     new ActionZoomUndo( this, time_model ) );
        super.add( zoomUndo_btn );

        zoomRedo_btn = new JButton( new ImageIcon( Const.IMG_PATH + "WinRedo.gif" ) );
        zoomRedo_btn.setMargin( btn_insets );
        zoomRedo_btn.setToolTipText( "Redo the previous zoom operation" );
        zoomRedo_btn.setMnemonic( KeyEvent.VK_R );
        zoomRedo_btn.addActionListener(
                     new ActionZoomRedo( this, time_model ) );
        super.add( zoomRedo_btn );
        
        super.addSeparator( mini_separator_size );
        
        zoomOut_btn = new JButton( new ImageIcon( Const.IMG_PATH + "ZoomOut24.gif"  ) );        
        zoomOut_btn.setMargin( btn_insets );
        zoomOut_btn.setToolTipText( "Zoom Out by 1 level in time" );
        zoomOut_btn.setMnemonic( KeyEvent.VK_O );
        // zoomOut_btn.setPreferredSize( btn_dim );
        zoomOut_btn.addActionListener(
                    new ActionZoomOut( this, time_model ) );
        super.add( zoomOut_btn );

        zoomHome_btn = new JButton( new ImageIcon( Const.IMG_PATH + "Home24.gif" ) );
        zoomHome_btn.setMargin( btn_insets );
        zoomHome_btn.setToolTipText(
                     "Reset zoom to the initial resolution in time" );
        zoomHome_btn.setMnemonic( KeyEvent.VK_H );
        // zoomHome_btn.setPreferredSize( btn_dim );
        zoomHome_btn.addActionListener(
                 new ActionZoomHome( this, time_model ) );
        super.add( zoomHome_btn );

        zoomIn_btn = new JButton( new ImageIcon( Const.IMG_PATH + "ZoomIn24.gif" ) );
        zoomIn_btn.setMargin( btn_insets );
        zoomIn_btn.setToolTipText( "Zoom In iby 1 level in time" );
        zoomIn_btn.setMnemonic( KeyEvent.VK_I );
        // zoomIn_btn.setPreferredSize( btn_dim );
        zoomIn_btn.addActionListener(
                   new ActionZoomIn( this, time_model ) );
        super.add( zoomIn_btn );

        super.addSeparator();

        searchBack_btn = new JButton( new ImageIcon( Const.IMG_PATH + "FindBack24.gif" ) );
        searchBack_btn.setMargin( btn_insets );
        searchBack_btn.setToolTipText( "Search Backward in time" );
        searchBack_btn.setMnemonic( KeyEvent.VK_B );
        // searchBack_btn.setPreferredSize( btn_dim );
        searchBack_btn.addActionListener(
                       new ActionSearchBackward( this, canvas_vport ) );
        super.add( searchBack_btn );

        searchInit_btn = new JButton( new ImageIcon( Const.IMG_PATH + "Find24.gif" ) );
        searchInit_btn.setMargin( btn_insets );
        searchInit_btn.setToolTipText(
                      "Search Initialization from last popup InfoBox's time" );
        searchInit_btn.setMnemonic( KeyEvent.VK_S );
        // searchInit_btn.setPreferredSize( btn_dim );
        searchInit_btn.addActionListener(
                       new ActionSearchInit( this, canvas_vport ) );
        super.add( searchInit_btn );

        searchFore_btn = new JButton( new ImageIcon( Const.IMG_PATH + "FindFore24.gif" ) );
        searchFore_btn.setMargin( btn_insets );
        searchFore_btn.setToolTipText( "Search Forward in time" );
        searchFore_btn.setMnemonic( KeyEvent.VK_F );
        // searchFore_btn.setPreferredSize( btn_dim );
        searchFore_btn.addActionListener(
                       new ActionSearchForward( this, canvas_vport ) );
        super.add( searchFore_btn );

        super.addSeparator();
        super.addSeparator();

        refresh_btn = new JButton( new ImageIcon( Const.IMG_PATH + "Refresh24.gif" ) );
        refresh_btn.setMargin( btn_insets );
        refresh_btn.setToolTipText(
                     "Redraw canvas to synchronize changes from "
                   + "Preference/Legend window or Yaxis label panel" );
        refresh_btn.setMnemonic( KeyEvent.VK_D );
        // refresh_btn.setPreferredSize( btn_dim );
        refresh_btn.addActionListener( new ActionPptyRefresh( topologyManager, cnvas_timeline ) );
        super.add( refresh_btn );

        
        print_btn = new JButton( new ImageIcon(  Const.IMG_PATH + "Print24.gif" ) );

        print_btn.setMargin( btn_insets );
        print_btn.setToolTipText( "Screenshot the Timeline window to /tmp/jumpshot*" );
        // print_btn.setPreferredSize( btn_dim );
        print_btn.addActionListener( new ActionPptyScreenshot(cnvas_timeline, time_ruler) );
        super.add( print_btn );        
        
        super.addSeparator();
        
        JButton autoRefresh_btn = new JButton( new ImageIcon( Const.IMG_PATH + "Refresh24.gif" ) );
        autoRefresh_btn.setMargin( btn_insets );
        autoRefresh_btn.setToolTipText( "Automatically redraw timeline on modification" );
        autoRefresh_btn.setMnemonic( KeyEvent.VK_A );
        autoRefresh_btn.addActionListener( new ActionPptyAutoRefresh( cnvas_timeline, autoRefresh_btn ) );
        super.add( autoRefresh_btn );        
    }

    private void initAllButtons()
    {
        up_btn.setEnabled( true );
        down_btn.setEnabled( true );
        
        backward_btn.setEnabled( true );
        forward_btn.setEnabled( true );
        this.resetZoomButtons();

        searchBack_btn.setEnabled( true );
        searchInit_btn.setEnabled( true );
        searchFore_btn.setEnabled( true );

        refresh_btn.setEnabled( true );
        print_btn.setEnabled( true );
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
    public JButton getYaxisTreeCommitButton()
    { return commit_btn; }

    //  Interface for ToolBarStatus
    public JButton getPropertyRefreshButton()
    { return refresh_btn; }

    //  Interface for ToolBarStatus
    public JButton getTimelineDeleteButton()
    { return timeline_delete_btn; }
}

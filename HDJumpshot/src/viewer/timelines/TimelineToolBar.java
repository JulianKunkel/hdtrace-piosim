/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.timelines;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.JToolBar;

import viewer.common.Const;
import viewer.topology.TopologyManager;
import viewer.zoomable.ActionPptyRefresh;
import viewer.zoomable.ActionPptyScreenshot;
import viewer.zoomable.ActionSearchBackward;
import viewer.zoomable.ActionSearchForward;
import viewer.zoomable.ActionSearchInit;
import viewer.zoomable.ActionTimelineDelete;
import viewer.zoomable.ActionTimelineMark;
import viewer.zoomable.ActionTimelineMove;
import viewer.zoomable.ActionTimelineRestore;
import viewer.zoomable.ActionVportBackward;
import viewer.zoomable.ActionVportDown;
import viewer.zoomable.ActionVportForward;
import viewer.zoomable.ActionVportUp;
import viewer.zoomable.ActionYaxisTreeCollapse;
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

    private JButton                 mark_btn;
    private JButton                 move_btn;
    public  JButton                 delete_btn;
    public  JButton                 restore_timelines_btn;

    private JButton                 up_btn;
    private JButton                 down_btn;

    private JButton                 expand_btn;
    private JButton                 collapse_btn;
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
    private ActionTimelineRestore   restore_timelines_listener;



    public ViewportTimeYaxis getCanvas_vport() {
		return canvas_vport;
	}

	public ActionTimelineRestore getRestore_timelines_listener() {
		return restore_timelines_listener;
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

    protected URL getURL( String filename )
    {
        URL url = null;
        url = getClass().getResource( filename );
        return url;
    }

    private void addButtons()
    {
        Insets     btn_insets;
        Dimension  mini_separator_size;
        URL        icon_URL;

        btn_insets          = new Insets( 2, 2, 2, 2 );
        mini_separator_size = new Dimension( 5, 5 );

        icon_URL = getURL( Const.IMG_PATH + "Up24.gif" );
        if ( icon_URL != null )
            up_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            up_btn = new JButton( "Up" );
        up_btn.setMargin( btn_insets );
        up_btn.setToolTipText( "Scroll Upward by half a screen" );
        // up_btn.setPreferredSize( btn_dim );
        up_btn.addActionListener( new ActionVportUp( y_scrollbar ) );
        up_btn.setMnemonic( KeyEvent.VK_UP );
        super.add( up_btn );

        icon_URL = getURL( Const.IMG_PATH + "Down24.gif" );
        if ( icon_URL != null )
            down_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            down_btn = new JButton( "Down" );
        down_btn.setMargin( btn_insets );
        down_btn.setToolTipText( "Scroll Downward by half a screen" );
        down_btn.setMnemonic( KeyEvent.VK_DOWN );
        // down_btn.setPreferredSize( btn_dim );
        down_btn.addActionListener( new ActionVportDown( y_scrollbar ) );
        super.add( down_btn );

        super.addSeparator( mini_separator_size );

        icon_URL = getURL( Const.IMG_PATH + "Edit24.gif" );
        if ( icon_URL != null )
            mark_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            mark_btn = new JButton( "LabelMark" );
        mark_btn.setMargin( btn_insets );
        mark_btn.setToolTipText( "Mark the timelines" );
        // mark_btn.setPreferredSize( btn_dim );
        mark_btn.addActionListener(
                 new ActionTimelineMark( root_window, this, topologyManager ) );
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
                 new ActionTimelineMove( root_window, this, topologyManager ) );
        super.add( move_btn );


        restore_timelines_listener = new ActionTimelineRestore( root_window,
        		this, topologyManager );

        icon_URL = getURL( Const.IMG_PATH + "Delete24.gif" );
        if ( icon_URL != null )
            delete_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            delete_btn = new JButton( "LabelDelete" );
        delete_btn.setMargin( btn_insets );
        delete_btn.setToolTipText( "Delete the marked timelines" );
        // delete_btn.setPreferredSize( btn_dim );
        delete_btn.addActionListener(
                   new ActionTimelineDelete( root_window, this, topologyManager, restore_timelines_listener ) );
        super.add( delete_btn );


        icon_URL = getURL(  Const.IMG_PATH + "WinUndo.gif"  );
        if ( icon_URL != null )
        	restore_timelines_btn = new JButton( new ImageIcon( icon_URL ) );
        else
        	restore_timelines_btn = new JButton( "LabelUndoDelete" );
        restore_timelines_btn.setMargin( btn_insets );
        restore_timelines_btn.setToolTipText( "Restore the timelines" );
        restore_timelines_btn.addActionListener( restore_timelines_listener );
        super.add( restore_timelines_btn );

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
                   new ActionYaxisTreeExpand( this, topologyManager ) );
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
                     new ActionYaxisTreeCollapse( this, topologyManager ) );
        super.add( collapse_btn );

        icon_URL = getURL( Const.IMG_PATH + "TreeCommit24.gif" );
        if ( icon_URL != null )
            commit_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            commit_btn = new JButton( "LabelCommit" );
        commit_btn.setMargin( btn_insets );
        commit_btn.setToolTipText(
                   "Commit changes and Redraw the TimeLines Display" );
        commit_btn.setMnemonic( KeyEvent.VK_D );
        // collapse_btn.setPreferredSize( btn_dim );
        
        // TODO Commit changes button
        //commit_btn.addActionListener(
         //          new ActionYaxisTreeCommit( root_window, this,
         //                                     canvas_vport, y_maps,
         //                                     row_adjs ) );
        
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
        
        super.addSeparator( );

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

        super.addSeparator( );

        /*
        icon_URL = getURL( Const.IMG_PATH + "ZoomSet24.gif" );
        if ( icon_URL != null )
            zoomSet_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            zoomSet_btn = new JButton( "ZoomSet" );
        zoomSet_btn.setMargin( btn_insets );
        zoomSet_btn.setToolTipText( "Set zoom paramter by a panel" );
        // zoomHome_btn.setPreferredSize( btn_dim );
        // zoomSet_btn.addActionListener(
        //         new ActionZoomSet( this, time_model ) );
        super.add( zoomSet_btn );
        */

        super.addSeparator();
        super.addSeparator();

        icon_URL = getURL( Const.IMG_PATH + "FindBack24.gif" );
        if ( icon_URL != null )
            searchBack_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            searchBack_btn = new JButton( "SearchBackward" );
        searchBack_btn.setMargin( btn_insets );
        searchBack_btn.setToolTipText( "Search Backward in time" );
        searchBack_btn.setMnemonic( KeyEvent.VK_B );
        // searchBack_btn.setPreferredSize( btn_dim );
        searchBack_btn.addActionListener(
                       new ActionSearchBackward( this, canvas_vport ) );
        super.add( searchBack_btn );

        icon_URL = getURL( Const.IMG_PATH + "Find24.gif" );
        if ( icon_URL != null )
            searchInit_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            searchInit_btn = new JButton( "SearchInitialize" );
        searchInit_btn.setMargin( btn_insets );
        searchInit_btn.setToolTipText(
                      "Search Initialization from last popup InfoBox's time" );
        searchInit_btn.setMnemonic( KeyEvent.VK_S );
        // searchInit_btn.setPreferredSize( btn_dim );
        searchInit_btn.addActionListener(
                       new ActionSearchInit( this, canvas_vport ) );
        super.add( searchInit_btn );

        icon_URL = getURL( Const.IMG_PATH + "FindFore24.gif" );
        if ( icon_URL != null )
            searchFore_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            searchFore_btn = new JButton( "SearchForward" );
        searchFore_btn.setMargin( btn_insets );
        searchFore_btn.setToolTipText( "Search Forward in time" );
        searchFore_btn.setMnemonic( KeyEvent.VK_F );
        // searchFore_btn.setPreferredSize( btn_dim );
        searchFore_btn.addActionListener(
                       new ActionSearchForward( this, canvas_vport ) );
        super.add( searchFore_btn );

        super.addSeparator();
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
                   new ActionPptyRefresh( topologyManager ) );
        super.add( refresh_btn );

        icon_URL = getURL( Const.IMG_PATH + "Print24.gif" );
        if ( icon_URL != null )
            print_btn = new JButton( new ImageIcon( icon_URL ) );
        else
            print_btn = new JButton( "Print" );

        print_btn.setMargin( btn_insets );
        print_btn.setToolTipText( "Screenshot the Timeline window to /tmp/jumpshot*" );
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
    public void resetYaxisTreeButtons()
    {
        expand_btn.setEnabled( topologyManager.isLevelExpandable() );
        collapse_btn.setEnabled( topologyManager.isLevelCollapsable() );
        commit_btn.setEnabled( true );
    }

    //  Interface for ToolBarStatus
    public JButton getYaxisTreeCommitButton()
    { return commit_btn; }

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


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

package viewer.common;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.JToolBar;

import topology.TopologyManager;
import viewer.common.IconManager.IconType;
import viewer.zoomable.ActionPptyRefresh;
import viewer.zoomable.ActionPptyScreenshot;
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
import viewer.zoomable.ScrollableObject;
import viewer.zoomable.ScrollbarTime;
import viewer.zoomable.ToolBarStatus;
import viewer.zoomable.ViewportTimeYaxis;


public class TimelineToolBar extends JToolBar
implements ToolBarStatus
{
	private static final long serialVersionUID = 3253826372881598652L;

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


	private JButton                 backward_btn;
	private JButton                 forward_btn;

	private JButton                 zoomUndo_btn;
	private JButton                 zoomOut_btn;
	private JButton                 zoomHome_btn;
	private JButton                 zoomIn_btn;
	private JButton                 zoomRedo_btn;

	private JButton                 refresh_btn;
	private JButton                 print_btn;
	private final ScrollableObject  cnvas_timeline;
	private RulerTime	 			time_ruler = null;
	
	private ButtonAutoRefresh autoRefresh_btn;


	public ViewportTimeYaxis getCanvas_vport() {
		return canvas_vport;
	}

	public TimelineToolBar( 
			ScrollableObject cnvas_timeline,
			RulerTime	 time_ruler,
			ViewportTimeYaxis  canvas_viewport,
			JScrollBar         yaxis_scrollbar,
			TopologyManager          yaxis_tree,
			ScrollbarTime      a_time_scrollbar,
			ModelTime          a_time_model,
			RowAdjustments     a_row_adjs,
			IconManager iconManager)
	{
		super();

		this.cnvas_timeline = cnvas_timeline;

		this.time_ruler = time_ruler;
		canvas_vport     = canvas_viewport;
		y_scrollbar      = yaxis_scrollbar;
		topologyManager           = yaxis_tree;
		time_scrollbar   = a_time_scrollbar;
		time_model       = a_time_model;
		this.addLeftButtons(iconManager);

		canvas_vport.setToolBarStatus( this );

		this.setFloatable(false);
	}

	/**
	 * Add all buttons which are on the right
	 */
	 public void addRightButtons(IconManager iconManager){
		Insets btn_insets          = getInsets();		
		btn_insets          = new Insets( 2, 2, 2, 2 );

		autoRefresh_btn.setMargin( btn_insets );
		autoRefresh_btn.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				refresh_btn.setEnabled(autoRefresh_btn.isAutoRefresh());				
			}
		});
		
		
		refresh_btn = new JButton( iconManager.getActiveToolbarIcon(IconType.Refresh) );
		refresh_btn.setMargin( btn_insets );
		refresh_btn.setToolTipText(	"Redraw canvas to synchronize changes from Preference/Legend window or Yaxis label panel" );
		refresh_btn.setMnemonic( KeyEvent.VK_D );
		refresh_btn.addActionListener( new ActionPptyRefresh( cnvas_timeline ) );
		
		refresh_btn.setEnabled(! autoRefresh_btn.isAutoRefresh());
		
		super.add( refresh_btn );


		print_btn = new JButton( iconManager.getActiveToolbarIcon(IconType.Screenshot) );

		print_btn.setMargin( btn_insets );
		print_btn.setToolTipText( "Screenshot the Timeline window to /tmp/jumpshot*" );
		print_btn.addActionListener( new ActionPptyScreenshot(cnvas_timeline, time_ruler) );
		super.add( print_btn );        

		super.addSeparator();
		
		super.add( autoRefresh_btn );      

		// orientate the toolbar left:
		addSeparator(new Dimension(1000,10));
	 }
	 
	 public Insets getInsets(){
		 return new Insets( 2, 2, 2, 2 );
	 }
	 

	 public void init()
	 {
		 this.initAllButtons();
	 }    

	 private void addLeftButtons(IconManager iconManager)
	 {
		 Insets     btn_insets;
		 Dimension  mini_separator_size;

		 btn_insets          = getInsets();
		 mini_separator_size = new Dimension( 5, 5 );

		 up_btn = new JButton( iconManager.getActiveToolbarIcon(IconType.Up) );
		 up_btn.setMargin( btn_insets );
		 up_btn.setToolTipText( "Scroll Upward by half a screen" );
		 up_btn.addActionListener( new ActionVportUp( y_scrollbar ) );
		 up_btn.setMnemonic( KeyEvent.VK_UP );
		 super.add( up_btn );

		 down_btn = new JButton( iconManager.getActiveToolbarIcon(IconType.Down) );

		 down_btn.setMargin( btn_insets );
		 down_btn.setToolTipText( "Scroll Downward by half a screen" );
		 down_btn.setMnemonic( KeyEvent.VK_DOWN );
		 down_btn.addActionListener( new ActionVportDown( y_scrollbar ) );
		 super.add( down_btn );

		 super.addSeparator( mini_separator_size );

		 timeline_delete_btn = new JButton( iconManager.getActiveToolbarIcon(IconType.RemoveTimeline) );
		 timeline_delete_btn.setMargin( btn_insets );
		 timeline_delete_btn.setToolTipText( "Delete the marked timelines" );
		 timeline_delete_btn.addActionListener( new ActionTimelineRemove( topologyManager) );
		 super.add( timeline_delete_btn );

		 timelines_restore_btn = new JButton( iconManager.getActiveToolbarIcon(IconType.Undo) );
		 timelines_restore_btn.setMargin( btn_insets );
		 timelines_restore_btn.setToolTipText( "Restore the timelines" );
		 timelines_restore_btn.addActionListener( new ActionTimelineRestore(topologyManager) );
		 super.add( timelines_restore_btn );

		 timelines_expand_btn = new JButton( iconManager.getActiveToolbarIcon(IconType.Expand) );
		 timelines_expand_btn.setMargin( btn_insets );
		 timelines_expand_btn.setToolTipText( "Expand the Y-axis tree label by 1 level" );
		 timelines_expand_btn.setMnemonic( KeyEvent.VK_E );
		 timelines_expand_btn.addActionListener( new ActionYaxisTreeExpand( topologyManager ) );
		 super.add( timelines_expand_btn );

		 super.addSeparator();
		 super.addSeparator();

		 backward_btn = new JButton( iconManager.getActiveToolbarIcon(IconType.Left) );
		 backward_btn.setMargin( btn_insets );
		 backward_btn.setToolTipText( "Scroll Backward by half a screen" );
		 backward_btn.setMnemonic( KeyEvent.VK_LEFT );
		 backward_btn.addActionListener(
				 new ActionVportBackward( time_scrollbar ) );
		 super.add( backward_btn );

		 forward_btn = new JButton( iconManager.getActiveToolbarIcon(IconType.Right) );
		 forward_btn.setMargin( btn_insets );
		 forward_btn.setToolTipText( "Scroll Forward by half a screen" );
		 forward_btn.setMnemonic( KeyEvent.VK_RIGHT );
		 forward_btn.addActionListener(
				 new ActionVportForward( time_scrollbar ) );
		 super.add( forward_btn );

		 super.addSeparator( );

		 zoomUndo_btn = new JButton( iconManager.getActiveToolbarIcon(IconType.Undo) );
		 zoomUndo_btn.setMargin( btn_insets );
		 zoomUndo_btn.setToolTipText( "Undo the previous zoom operation" );
		 zoomUndo_btn.setMnemonic( KeyEvent.VK_U );
		 zoomUndo_btn.addActionListener(
				 new ActionZoomUndo( this, time_model ) );
		 super.add( zoomUndo_btn );

		 zoomRedo_btn = new JButton( iconManager.getActiveToolbarIcon(IconType.Redo) );
		 zoomRedo_btn.setMargin( btn_insets );
		 zoomRedo_btn.setToolTipText( "Redo the previous zoom operation" );
		 zoomRedo_btn.setMnemonic( KeyEvent.VK_R );
		 zoomRedo_btn.addActionListener(
				 new ActionZoomRedo( this, time_model ) );
		 super.add( zoomRedo_btn );

		 super.addSeparator( mini_separator_size );

		 zoomOut_btn = new JButton( iconManager.getActiveToolbarIcon(IconType.ZoomOut) );        
		 zoomOut_btn.setMargin( btn_insets );
		 zoomOut_btn.setToolTipText( "Zoom Out by 1 level in time" );
		 zoomOut_btn.setMnemonic( KeyEvent.VK_O );
		 zoomOut_btn.addActionListener(
				 new ActionZoomOut( this, time_model ) );
		 super.add( zoomOut_btn );

		 zoomHome_btn = new JButton( iconManager.getActiveToolbarIcon(IconType.ZoomHome) );
		 zoomHome_btn.setMargin( btn_insets );
		 zoomHome_btn.setToolTipText(
				 "Reset zoom to the initial resolution in time" );
		 zoomHome_btn.setMnemonic( KeyEvent.VK_H );
		 zoomHome_btn.addActionListener(
				 new ActionZoomHome( this, time_model ) );
		 super.add( zoomHome_btn );

		 zoomIn_btn = new JButton( iconManager.getActiveToolbarIcon(IconType.ZoomIn) );
		 zoomIn_btn.setMargin( btn_insets );
		 zoomIn_btn.setToolTipText( "Zoom In iby 1 level in time" );
		 zoomIn_btn.setMnemonic( KeyEvent.VK_I );
		 zoomIn_btn.addActionListener(
				 new ActionZoomIn( this, time_model ) );
		 super.add( zoomIn_btn );

		 super.addSeparator();
		 
		 autoRefresh_btn = new ButtonAutoRefresh(cnvas_timeline);
	 }

	 protected void initAllButtons()
	 {
		 up_btn.setEnabled( true );
		 down_btn.setEnabled( true );

		 backward_btn.setEnabled( true );
		 forward_btn.setEnabled( true );
		 this.resetZoomButtons();

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
	 
	 public ButtonAutoRefresh getAutoRefreshBtn() {
		return autoRefresh_btn;
	}
}

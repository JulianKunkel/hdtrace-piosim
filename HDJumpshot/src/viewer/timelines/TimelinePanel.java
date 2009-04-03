
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

import hdTraceInput.TraceFormatBufferedFileReader;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import topology.TopologyManager;
import viewer.common.TimeEvent;
import viewer.common.TimeListener;
import viewer.legends.CategoryUpdatedListener;
import viewer.zoomable.ModelInfo;
import viewer.zoomable.ModelInfoPanel;
import viewer.zoomable.ModelTime;
import viewer.zoomable.ModelTimePanel;
import viewer.zoomable.RowAdjustments;
import viewer.zoomable.RowNumberChangedListener;
import viewer.zoomable.RulerTime;
import viewer.zoomable.ScrollbarTime;
import viewer.zoomable.ViewportTime;
import viewer.zoomable.ViewportTimePanel;
import viewer.zoomable.ViewportTimeYaxis;


/**
 * Panel arranged left of the timelines
 *
 */
public class TimelinePanel extends JPanel
{
	static final int MIN_LEFTPANEL_WIDTH = 150;	

	private Window                  root_window;

	private TimelineToolBar         toolbar;

	private BoundedRangeModel       y_model;
	private final TopologyManager   topologyManager;
	private JScrollPane             y_colpanel;
	private JScrollPane             y_scroller;
	private JScrollBar              y_scrollbar;


	private ModelTime               time_model;
	private ScrollbarTime           time_scrollbar;
	private ModelTimePanel          time_display_panel;

	private ModelInfo               info_model;
	private ModelInfoPanel          info_display_panel;
	private RulerTime               time_ruler;
	private ViewportTime            time_ruler_vport;
	private ViewportTimePanel       time_ruler_panel;

	private CanvasTimeline          time_canvas;
	private ViewportTimeYaxis       time_canvas_vport;
	private ViewportTimePanel       time_canvas_panel;

	private RowAdjustments          row_adjs;

	/** 
	 * This listener is invoked if the zoomlevel changes
	 */
	private TimeListener           timeUpdateListener = new TimeListener(){
		@Override
		public void timeChanged(TimeEvent evt) {
			// set zoom in/out button status.
			toolbar.resetZoomButtons();
		}
	};

	private class MyNumberOfRowsChangedListener implements RowNumberChangedListener{
		@Override
		public void rowNumberChanged() {
			time_canvas.redrawIfAutoRedraw();
		}
	}
		
	private class IOOptionsListener extends JComboBox implements ActionListener
	{
		public IOOptionsListener(){
			Insets canvas_panel_insets = time_canvas_panel.getInsets();

			this.setAlignmentX(
					Component.CENTER_ALIGNMENT );
			this.addActionListener( this );
		}

		public void actionPerformed( ActionEvent evt )
		{
			int selected;
			selected = getSelectedIndex();
		}
	}


	private final UpdateTableModelListener myTableLegendChangeListener = new UpdateTableModelListener();

	private class UpdateTableModelListener extends CategoryUpdatedListener{
		@Override
		public void categoriesAddedOrRemoved() {
			topologyManager.restoreTopology();
		}
	}  

	public TimelinePanel( final Window    parent_window, final TraceFormatBufferedFileReader  reader)
	{
		super();

		time_model    = new ModelTime( root_window,	reader.getGlobalMinTime(),	reader.getGlobalMaxTime());
		this.topologyManager = new TopologyManager(reader, time_model);

		reader.getLegendTraceModel().addCategoryUpdateListener(myTableLegendChangeListener);

		root_window  = parent_window;

		Dimension sb_minThumbSz = (Dimension)
		UIManager.get( "ScrollBar.minimumThumbSize" );
		sb_minThumbSz.width = 4;
		UIManager.put( "ScrollBar.minimumThumbSize", sb_minThumbSz );
		/*
           y_scroller for y_tree needs to be created before time_canvas, so
           y_model can be extracted to be used for the creation of time_canvas
		 */
		y_scroller  = new JScrollPane( topologyManager, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,	ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );
		y_scrollbar = y_scroller.getVerticalScrollBar();
		y_model     = y_scrollbar.getModel();

		/* Initialize the ModelTime slog.input.InputLog().getTreeRoot() */


		this.setLayout( new BorderLayout() );

		/* Setting up the CENTER panel to store various time-related GUIs */
		JPanel center_panel = new JPanel();
		center_panel.setLayout( new BoxLayout( center_panel,	BoxLayout.Y_AXIS ) );

		/* The Time Ruler */
		time_ruler        = new RulerTime( time_model );
		time_ruler_vport  = new ViewportTime( time_model );
		time_ruler_vport.setView( time_ruler );
		time_ruler_panel  = new ViewportTimePanel( time_ruler_vport );
		time_ruler_vport.initLeftMouseToZoom( false );
		/*
                   Propagation of AdjustmentEvent originating from scroller:

                   scroller -----> time_model -----> viewport -----> view
                             adj               time           paint
                   viewport is between time_model and view because
                   viewport is what user sees.
		 */
		/*
                   Since there is NOT a specific ViewportTime/ViewTimePanel
                   for RulerTime, so we need to set PreferredSize of RulerTime
                   here.  Since CanvasTimeline's has its MaximumSize set to MAX,
                   CanvasTimeline's ViewportTimePanel will become space hungary.
                   As we want RulerTime to be fixed height during resize
                   of the top level window, So it becomes CRUCIAL to set
                   Preferred Height of RulerTime's ViewportTimePanel equal
                   to its Minimum Height and Maximum Height.
		 */
		Insets   ruler_panel_insets = time_ruler_panel.getInsets();
		int      ruler_panel_height = ruler_panel_insets.top + time_ruler.getJComponentHeight()	+ ruler_panel_insets.bottom;
		time_ruler_panel.setPreferredSize(	new Dimension( 100, ruler_panel_height ) );

		/* The TimeLine Canvas */
		time_canvas       = new CanvasTimeline(  time_model, reader,  y_model, topologyManager);

		time_canvas_vport = new ViewportTimeYaxis( time_model, y_model, topologyManager );
		time_canvas_vport.setView( time_canvas );

		time_canvas_panel = new ViewportTimePanel( time_canvas_vport );
		time_canvas_vport.initLeftMouseToZoom( true );


		/* The View's Time Display Panel */
		time_display_panel = new ModelTimePanel( time_model );
		JPanel canvas_lmouse;
		canvas_lmouse = time_canvas_vport.createLeftMouseModePanel(	BoxLayout.X_AXIS );
		canvas_lmouse.setToolTipText("Operation for left mouse button click on Timeline canvas" );
		time_display_panel.add( canvas_lmouse );

		/* The Horizontal "Time" ScrollBar */
		time_scrollbar = new ScrollbarTime( time_model );
		time_scrollbar.setEnabled( true );
		time_model.setScrollBar( time_scrollbar );


		info_model     = new ModelInfo(reader);
		info_display_panel = new ModelInfoPanel( info_model );
		info_model.setParamDisplay( info_display_panel );
		time_canvas_vport.setInfoModel( info_model );


		center_panel.add( time_canvas_panel );
		center_panel.add( time_scrollbar );
		center_panel.add( time_ruler_panel );

		/* Setting up the LEFT panel to store various Y-axis related GUIs */
		JPanel left_panel = new JPanel();
		left_panel.setLayout( new BoxLayout( left_panel, BoxLayout.Y_AXIS ) );

		/* "VIEW" title */
		Insets canvas_panel_insets = time_canvas_panel.getInsets();

		/* YaxisTree View for SLOG-2 */
		y_scroller.setAlignmentX( Component.CENTER_ALIGNMENT );
		/* when y_scrollbar is changed, update time_canvas as well. */
		y_scrollbar.addAdjustmentListener( time_canvas_vport );

		/* YaxisTree's Column Labels */
		int       left_bottom_height = ruler_panel_height
		+ canvas_panel_insets.bottom;
		JLabel y_colarea   = new JLabel();
		// y_colarea.setFont( Const.FONT );

		y_colarea.setText( topologyManager.getTopologyLabels() );

		y_colpanel = new JScrollPane( y_colarea, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,	ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		/*
                   Since there is NOT a specific Top Level JPanel for
                   y_colpanel, so we need to set its PreferredSize here.
                   Since y_scroller(i.e. JScrollPane containing YaxisTree)
                   is the space hungary component here.  So it is CRUCIAL
                   to set the height PreferredSize equal to that of MinimumSize
                   and MaximumSize, hence y_colpanel will be fixed in height
                   during resizing of the top level frame.
		 */
		y_colpanel.setMinimumSize(
				new Dimension( 0, left_bottom_height ) );
		y_colpanel.setMaximumSize(
				new Dimension( Short.MAX_VALUE, left_bottom_height ) );
		y_colpanel.setPreferredSize(
				new Dimension( 20, left_bottom_height ) );

		// if the y scroller is changed, then update y_collpanel also.
		y_scroller.getHorizontalScrollBar().addAdjustmentListener( new toplogyManagerAxisValueChangedListener());

		left_panel.add( y_scroller );
		// left_panel.add( y_title_btm );
		left_panel.add( y_colpanel );

		left_panel.setMinimumSize( new Dimension(MIN_LEFTPANEL_WIDTH, 200));

		/* Setting up the RIGHT panel to store various time-related GUIs */		
		row_adjs = new RowAdjustments( time_canvas_vport, topologyManager );


		JPanel row_slider  = row_adjs.getSliderPanel();
		JScrollPane slider_scroller = new JScrollPane( row_slider,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );
		slider_scroller.setAlignmentX( Component.CENTER_ALIGNMENT );

		slider_scroller.setMinimumSize(
				new Dimension( 0, canvas_panel_insets.top ) );
		slider_scroller.setMaximumSize(
				new Dimension( Short.MAX_VALUE, canvas_panel_insets.top ) );
		slider_scroller.setPreferredSize(
				new Dimension( 20, canvas_panel_insets.top ) );

		/* Store the LEFT and CENTER panels in the JSplitPane */
		JSplitPane left_splitter, right_splitter;
		left_splitter  = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,	false, left_panel, center_panel );
		//  left_splitter.setResizeWeight( 0.0d );
		right_splitter = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,	false, left_splitter, row_adjs.getSliderPanel() );
		right_splitter.setOneTouchExpandable( true );
		left_splitter.setOneTouchExpandable( true );
		right_splitter.setResizeWeight( 1.0d );

		/* The ToolBar for various user controls */
		toolbar = new TimelineToolBar( root_window, time_canvas, time_ruler , time_canvas_vport,
				y_scrollbar, topologyManager, time_scrollbar, time_model, row_adjs );

		final JPanel top_panel = new JPanel();
		top_panel.setLayout( new BoxLayout( top_panel, BoxLayout.Y_AXIS ) );
		top_panel.add(toolbar);
		top_panel.add( time_display_panel);
		top_panel.add( info_display_panel);

		this.add( top_panel, BorderLayout.NORTH );

		this.add( right_splitter, BorderLayout.CENTER );

		/* Inform "time_canvas_vport" time has been changed */
		time_model.addTimeListener( time_canvas_vport );
		time_model.addTimeListener( time_ruler_vport );		
		time_model.addTimeListener( timeUpdateListener);

		// Initialize toolbar after creation of YaxisTree view
		toolbar.init();
		row_adjs.refreshSlidersAndTextFields();
		
		row_adjs.addRowChangedListener(new MyNumberOfRowsChangedListener());
	}

	/**
	 * Called if the y-axis scrollbar value changed 
	 * @author Julian M. Kunkel
	 *
	 */
	public class toplogyManagerAxisValueChangedListener implements AdjustmentListener{
		@Override
		public void adjustmentValueChanged(AdjustmentEvent e) {
			y_colpanel.getHorizontalScrollBar().setValue(y_scroller.getHorizontalScrollBar().getValue());
		}
	}
}

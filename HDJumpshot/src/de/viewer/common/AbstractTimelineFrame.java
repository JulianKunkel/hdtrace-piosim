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

package de.viewer.common;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hdTraceInput.FileLoadedListener;
import de.hdTraceInput.TraceFormatBufferedFileReader;
import de.topology.TopologyInputPlugin;
import de.topology.TopologyManager;
import de.topology.TopologyManagerContents;
import de.viewer.common.IconManager.IconType;
import de.viewer.first.MainManager;
import de.viewer.first.TopWindow;
import de.viewer.timelines.CanvasTimeline;
import de.viewer.zoomable.ModelTimePanel;
import de.viewer.zoomable.RowAdjustments;
import de.viewer.zoomable.RowNumberChangedListener;
import de.viewer.zoomable.RulerTime;
import de.viewer.zoomable.ScrollableObject;
import de.viewer.zoomable.ScrollbarTime;
import de.viewer.zoomable.ScrollbarTimeModel;
import de.viewer.zoomable.ViewportTime;
import de.viewer.zoomable.ViewportTimeYaxis;

/**
 * Implements a frame which consists of several sections, a linked in timeline manager,
 * a graphic object a ruler to change row height, capabilites to zoom in and scroll
 * and a common toolbar.
 * 
 * @author Julian M. Kunkel
 */
public abstract class AbstractTimelineFrame<InfoModelType> extends TopWindow{
	private static final long serialVersionUID = 7857561458577391709L;

	private TraceFormatBufferedFileReader reader;
	private ModelTime modelTime;


	static final int MIN_LEFTPANEL_WIDTH = 150;	

	private TimelineToolBar         toolbar;

	private BoundedRangeModel       y_model;
	private TopologyManager         topologyManager;
	private JScrollPane             y_colpanel;
	private JScrollPane             y_scroller;
	private JScrollBar              y_scrollbar;

	private ScrollbarTime           scrollbarTime;
	private ScrollbarTimeModel   		scrollbarTimeModel;

	private ModelTimePanel          time_display_panel;
	private ModelInfoPanel<InfoModelType> info_model;
	private RulerTime               timeRuler;
	private ViewportTime            time_ruler_vport;
	private JLabel                  yColarea   = new JLabel(); //below the topology manager 

	/**
	 * Text filter, allows to filter events with a specific attribute or allows to render them as a heat map 
	 */
	private JTextField              txtFilter;
	
	/**
	 * Drawing area:
	 */
	private ScrollableObject        canvasArea;
	private ViewportTimeYaxis       timeCanvasVport;

	private RowAdjustments          row_adjs;

	private   JRadioButton          zoom_btn;
	private   JRadioButton          hand_btn;
	
	private JCheckBox processNestedChkbox;	
	private JCheckBox processZeroTimeChckbox;	


	/**
	 * This function gets called, when the user changes the nested state in the toolbar
	 */
	abstract protected void fireNestedStateChanged();
	
	public class ZeroCallback{		
		/**
		 * Should the timeline start with 0 although a zoom is/has been performed.
		 * @return
		 */
		public boolean isStartWithZero(){
			return processZeroTimeChckbox.isSelected();
		}		
	}
	
	public void setProcessNested(boolean value){
		processNestedChkbox.setSelected(value);
	}
	
	public boolean isProcessNested(){
		return processNestedChkbox.isSelected();
	}


	/**
	 * Subclass can create its own menu panel.
	 * 
	 * @return
	 */
	abstract protected void addToToolbarMenu(TimelineToolBar toolbar, IconManager iconManager, Insets insets);

	/**
	 * Create your own model info panel.
	 * @return
	 */
	abstract protected ModelInfoPanel<InfoModelType> createModelInfoPanel();

	/**
	 * Subclass can add their own toolbars to the menu panel.
	 * @param target
	 */
	abstract protected void addOwnPanelsOrToolbars(JPanel menuPanel);

	/**
	 * Creates the main drawing area (which is scrollable).
	 * @return
	 */
	abstract protected ScrollableObject createCanvasArea();

	/**
	 * Return the available plugins for this view.
	 */
	abstract protected List<Class<? extends TopologyInputPlugin>> getAvailablePlugins();

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
			canvasArea.resized();
		}
	}

	private final FileLoadedListener myTableLegendChangeListener = new MyFileLoadedListener();

	private class MyFileLoadedListener implements FileLoadedListener{
		@Override
		public void additionalFileLoaded() {
			topologyManager.restoreTopology();
		}
	}  

	private JPanel createContentPane()
	{		
		Dimension sb_minThumbSz = (Dimension)
		UIManager.get( "ScrollBar.minimumThumbSize" );
		sb_minThumbSz.width = 4;
		UIManager.put( "ScrollBar.minimumThumbSize", sb_minThumbSz );
		/*
           y_scroller for y_tree needs to be created before time_canvas, so
           y_model can be extracted to be used for the creation of time_canvas
		 */
		y_scroller  = new JScrollPane( topologyManager.getTree(), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,	ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );
		y_scrollbar = y_scroller.getVerticalScrollBar();
		y_model     = y_scrollbar.getModel();

		/* Setting up the CENTER panel to store various time-related GUIs */
		JPanel center_panel = new JPanel();
		center_panel.setLayout( new BoxLayout( center_panel,	BoxLayout.Y_AXIS ) );

		scrollbarTimeModel = new ScrollbarTimeModel(modelTime);

		timeCanvasVport = new ViewportTimeYaxis( modelTime, y_model, topologyManager );

		/* The Time Ruler */
		timeRuler        = new RulerTime( scrollbarTimeModel, timeCanvasVport, new ZeroCallback() );
		time_ruler_vport  = new ViewportTime( modelTime );
		time_ruler_vport.setView( timeRuler );

		time_ruler_vport.setLeftMouseToZoom( Parameters.LEFTCLICK_INSTANT_ZOOM );
		/*
                   Propagation of AdjustmentEvent originating from scroller:

                   scroller -----> time_model -----> viewport -----> view
                             adj               time           paint
                   viewport is between time_model and view because
                   viewport is what user sees.
		 */

		/* The TimeLine Canvas */

		canvasArea       = createCanvasArea();
		timeCanvasVport.setView( canvasArea );

		timeCanvasVport.setLeftMouseToZoom( true );

		/* The View's Time Display Panel */
		time_display_panel = new ModelTimePanel( canvasArea );


		JPanel canvas_lmouse;

		final IconManager icons = MainManager.getIconManager();
		// allow only one button to be set:
		final ButtonGroup buttonGroup = new ButtonGroup();

		zoom_btn     = new JRadioButton( icons.getDisabledToolbarIcon(IconType.ZoomIn) );
		zoom_btn.setSelectedIcon( icons.getActiveToolbarIcon(IconType.ZoomIn) );
		zoom_btn.setBorderPainted( true );
		zoom_btn.setToolTipText( "Left mouse button click to Zoom" );
		zoom_btn.setSelected(true);
		zoom_btn.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt )
			{
				if ( zoom_btn.isSelected() )
					timeCanvasVport.setLeftMouseToZoom(true);
				time_ruler_vport.setLeftMouseToZoom(true);
			}
		} );

		hand_btn = new JRadioButton( icons.getDisabledToolbarIcon(IconType.Hand) );
		hand_btn.setSelectedIcon( icons.getActiveToolbarIcon(IconType.Hand) );
		hand_btn.setBorderPainted( true );
		hand_btn.setToolTipText( "Left mouse button click to Scroll" );
		hand_btn.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt )
			{
				if ( hand_btn.isSelected() )
					timeCanvasVport.setLeftMouseToZoom(false);
				time_ruler_vport.setLeftMouseToZoom(false);
			}
		} );

		
		buttonGroup.add(hand_btn);
		buttonGroup.add(zoom_btn);

		canvas_lmouse = new JPanel();
		canvas_lmouse.setLayout( new BoxLayout( canvas_lmouse, BoxLayout.X_AXIS ) );
		canvas_lmouse.add( zoom_btn );
		canvas_lmouse.add( hand_btn );
		canvas_lmouse.setBorder( BorderFactory.createEtchedBorder() );

		canvas_lmouse.setToolTipText("Operation for left mouse button click on Timeline canvas" );
		time_display_panel.add( canvas_lmouse );

		/* The Horizontal "Time" ScrollBar */
		scrollbarTime = new ScrollbarTime( scrollbarTimeModel );
		scrollbarTime.setEnabled( true );

		info_model     = createModelInfoPanel();
		info_model.init();
		timeCanvasVport.setInfoModel( info_model );

		center_panel.add( timeCanvasVport );
		center_panel.add( scrollbarTime );

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
		final int      ruler_panel_height = timeRuler.getRealImageHeight();
		time_ruler_vport.setMinimumSize(		new Dimension( 20, ruler_panel_height ) );
		time_ruler_vport.setMaximumSize(		new Dimension( Short.MAX_VALUE, ruler_panel_height ) );
		time_ruler_vport.setPreferredSize(		new Dimension( 20, ruler_panel_height ) );

		center_panel.add( time_ruler_vport );

		/* Setting up the LEFT panel to store various Y-axis related GUIs */
		JPanel left_panel = new JPanel();
		left_panel.setLayout( new BoxLayout( left_panel, BoxLayout.Y_AXIS ) );

		/* "VIEW" title */

		/* YaxisTree View for SLOG-2 */
		y_scroller.setAlignmentX( Component.CENTER_ALIGNMENT );
		/* when y_scrollbar is changed, update time_canvas as well. */
		y_scrollbar.addAdjustmentListener( timeCanvasVport );

		/* YaxisTree's Column Labels */
		yColarea.setFont( Const.FONT );

		yColarea.setText( topologyManager.getTopologyLabels() );

		y_colpanel = new JScrollPane( yColarea, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,	ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		/*
                   Since there is NOT a specific Top Level JPanel for
                   y_colpanel, so we need to set its PreferredSize here.
                   Since y_scroller(i.e. JScrollPane containing YaxisTree)
                   is the space hungary component here.  So it is CRUCIAL
                   to set the height PreferredSize equal to that of MinimumSize
                   and MaximumSize, hence y_colpanel will be fixed in height
                   during resizing of the top level frame.
		 */
		y_colpanel.setMinimumSize(		new Dimension( 0, ruler_panel_height ) );
		y_colpanel.setMaximumSize(		new Dimension( Short.MAX_VALUE, ruler_panel_height ) );
		y_colpanel.setPreferredSize(		new Dimension( 20, ruler_panel_height ) );

		// if the y scroller is changed, then update y_collpanel also.
		y_scroller.getHorizontalScrollBar().addAdjustmentListener( new toplogyManagerAxisValueChangedListener());

		left_panel.add( y_scroller );
		// left_panel.add( y_title_btm );
		left_panel.add( y_colpanel );

		left_panel.setMinimumSize( new Dimension(MIN_LEFTPANEL_WIDTH, 200));

		/* Setting up the RIGHT panel to store various time-related GUIs */		
		row_adjs = new RowAdjustments( timeCanvasVport, topologyManager );


		JPanel row_slider  = row_adjs.getSliderPanel();
		JScrollPane slider_scroller = new JScrollPane( row_slider,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );
		slider_scroller.setAlignmentX( Component.CENTER_ALIGNMENT );

		/* Store the LEFT and CENTER panels in the JSplitPane */
		JSplitPane left_splitter, right_splitter;
		left_splitter  = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,	false, left_panel, center_panel );
		//  left_splitter.setResizeWeight( 0.0d );
		right_splitter = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,	false, left_splitter, row_adjs.getSliderPanel() );
		right_splitter.setOneTouchExpandable( true );
		left_splitter.setOneTouchExpandable( true );
		right_splitter.setResizeWeight( 1.0d );

		/* The ToolBar for various user controls */
		final IconManager iconManager = MainManager.getIconManager(); 

		toolbar = new TimelineToolBar( canvasArea, timeRuler , timeCanvasVport,
				y_scrollbar, topologyManager, scrollbarTime, modelTime, row_adjs, iconManager );


		
		addToToolbarMenu(toolbar, iconManager, toolbar.getInsets());

		txtFilter = new JTextField(30);
		txtFilter.setToolTipText("Enter the attributes you want to filter with, e.g. use size > 100 & tag == 3 to filter events which match both entries AND: & and OR: | and () are supported.\nA HeatMap can be created based on an attribute by specifying heatmap: size");
		txtFilter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {				
				// color the text filter if it is invalid!
				if( getCanvasArea().applyFilter(txtFilter.getText()) ){
					txtFilter.setBackground(Color.WHITE);
				}else{
					txtFilter.setBackground(Color.RED);
				}
			}
		});
		toolbar.add(txtFilter);

		processNestedChkbox = new JCheckBox("Nested");	
		processNestedChkbox.setSelected(false);

		processNestedChkbox.setToolTipText("Are nested states used for the computation of the values?");
		processNestedChkbox.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {				
				fireNestedStateChanged();
			}
		});
		toolbar.add(processNestedChkbox);
		

		processZeroTimeChckbox = new JCheckBox("ZeroTime");	
		processZeroTimeChckbox.setSelected(false);

		processZeroTimeChckbox.setToolTipText("The timeline always starts at 0 after a zoom operation, scrolling changes the position though");
		toolbar.add(processZeroTimeChckbox);
		
		toolbar.addRightButtons(iconManager, getFrame());		
		toolbar.init();
		
		final JPanel top_panel = new JPanel();
		top_panel.setLayout( new BoxLayout( top_panel, BoxLayout.Y_AXIS ) );
		top_panel.add(toolbar);
		top_panel.add( time_display_panel);
		addOwnPanelsOrToolbars(top_panel);
		top_panel.add( info_model.getPanel() );

		// Initialize toolbar after creation of YaxisTree view
		row_adjs.refreshSlidersAndTextFields();

		row_adjs.addRowChangedListener(new MyNumberOfRowsChangedListener());


		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add( top_panel, BorderLayout.NORTH );
		contentPanel.add( right_splitter, BorderLayout.CENTER );
		
		return contentPanel;
	}

	public void forceRedraw(){
		canvasArea.forceRedraw();
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

	@Override
	protected void destroyWindow() {	
		// don't forget to remove modelTime listener (if autoupdate), otherwise resources are wasted
		modelTime.removeTimeListener( scrollbarTime );
		modelTime.removeTimeListener( timeCanvasVport );
		modelTime.removeTimeListener( time_ruler_vport );		
		modelTime.removeTimeListener( timeUpdateListener);
		modelTime.removeTimeListener( time_display_panel );
		
		reader.removeFileLoadListener(myTableLegendChangeListener);
	}
	
	/**
	 * Override this method to invoke special actions performed after/during the frame gets visible the
	 * first time. 
	 */
	@Override
	protected void initWindow() {
		modelTime.addTimeListener( scrollbarTime );
		modelTime.addTimeListener( timeCanvasVport );
		modelTime.addTimeListener( time_ruler_vport );		
		modelTime.addTimeListener( timeUpdateListener);
		modelTime.addTimeListener( time_display_panel );	

		reader.addFileLoadListener(myTableLegendChangeListener);
	}

	public AbstractTimelineFrame(final TraceFormatBufferedFileReader reader,  ModelTime modelTime) {
		this.reader = reader;				

		this.modelTime = modelTime;
		this.topologyManager = new TopologyManager(reader, modelTime, getTopologyManagerType());

		fileLoadedNotification();

		getFrame().setContentPane( createContentPane());		
	}

	/**
	 * When a file gets loaded this function shall be called:
	 * TODO create a listener for that reason.
	 */
	public void fileLoadedNotification(){
		this.topologyManager.restoreTopology();
		this.topologyManager.tryToLoadPlugins(getAvailablePlugins());		
	}

	protected TopologyManagerContents getTopologyManagerType(){
		return TopologyManagerContents.EVERYTHING;
	}

	protected ModelTime getModelTime() {
		return modelTime;
	}

	protected TraceFormatBufferedFileReader getReader() {
		return reader;
	}	

	protected TopologyManager getTopologyManager() {
		return topologyManager;
	}

	protected BoundedRangeModel getYModel() {
		return y_model;
	}

	protected ScrollableObject getCanvasArea() {
		return canvasArea;
	}

	protected ViewportTimeYaxis getTimeCanvasVport() {
		return timeCanvasVport;
	}

	/**
	 * Leftmost down area, below topology manager.
	 * @return
	 */
	protected JLabel getYColarea() {
		return yColarea;
	}

	public boolean isAutoRefresh(){
		return canvasArea.isAutoRefresh();
	}

	public ScrollbarTimeModel getScrollbarTimeModel() {
		return scrollbarTimeModel;
	}

	public RulerTime getTimeRuler() {
		return timeRuler;
	}
}

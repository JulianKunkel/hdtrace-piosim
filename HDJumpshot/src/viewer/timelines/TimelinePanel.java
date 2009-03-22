/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.timelines;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import viewer.TraceFormatBufferedFileReader;
import viewer.common.Const;
import viewer.common.Dialogs;
import viewer.zoomable.Debug;
import viewer.zoomable.ModelInfo;
import viewer.zoomable.ModelInfoPanel;
import viewer.zoomable.ModelTime;
import viewer.zoomable.ModelTimePanel;
import viewer.zoomable.RowAdjustments;
import viewer.zoomable.RulerTime;
import viewer.zoomable.ScrollbarTime;
import viewer.zoomable.TimeEvent;
import viewer.zoomable.TimeListener;
import viewer.zoomable.ViewportTime;
import viewer.zoomable.ViewportTimePanel;
import viewer.zoomable.ViewportTimeYaxis;
import viewer.zoomable.YaxisTree;
import de.hd.pvs.TraceFormat.SimpleConsoleLogger;


/**
 * Panel arranged left of the timelines
 *
 */
public class TimelinePanel extends JPanel
{
	static final int LEFTPANEL_WIDTH = 150;
	
	//depends on the height of the two toolbars inside the timeline windoe
	static final int DROPDOWN_HEIGHT = 88; 
	 
	
	private Window                  root_window;
	private final TraceFormatBufferedFileReader       reader;

	private TimelineToolBar         toolbar;
	
	private BoundedRangeModel       y_model;
	private final YaxisTree         y_tree;
	private JScrollPane             y_scroller;
	private JScrollBar              y_scrollbar;


	private ModelTime               time_model;
	private ScrollbarTime           time_scrollbar;
	private ModelTimePanel          time_display_panel;

	private ModelInfo               info_model;
	private ModelInfoPanel          info_display_panel;
	private JComboBox               io_options_pulldown_list;
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

	private class IOOptionsListener extends JComboBox implements ActionListener
	{
		public IOOptionsListener(){
			Insets canvas_panel_insets = time_canvas_panel.getInsets();
			this.setMinimumSize(
					new Dimension( LEFTPANEL_WIDTH, DROPDOWN_HEIGHT ) );
			this.setMaximumSize(
					new Dimension( Short.MAX_VALUE, DROPDOWN_HEIGHT ) );
			this.setPreferredSize(
					new Dimension( LEFTPANEL_WIDTH, DROPDOWN_HEIGHT ) );
			this.setAlignmentX(
					Component.CENTER_ALIGNMENT );
			this.addItem("PIOviz");
			this.addActionListener( this );
		}
		
		public void actionPerformed( ActionEvent evt )
		{
			int selected;
			selected = io_options_pulldown_list.getSelectedIndex();
		}
	}
	
	public TimelinePanel( final Window    parent_window,
			final TraceFormatBufferedFileReader  reader)
	{
		super();
		
		// TODO fixMe
		this.y_tree = new YaxisTree(reader);
		
		root_window  = parent_window;
		this.reader  = reader;

		Dimension sb_minThumbSz = (Dimension)
		UIManager.get( "ScrollBar.minimumThumbSize" );
		sb_minThumbSz.width = 4;
		UIManager.put( "ScrollBar.minimumThumbSize", sb_minThumbSz );
		/*
           y_scroller for y_tree needs to be created before time_canvas, so
           y_model can be extracted to be used for the creation of time_canvas
		 */
		y_scroller  = new JScrollPane( y_tree,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );
		y_scrollbar = y_scroller.getVerticalScrollBar();
		y_model     = y_scrollbar.getModel();

		/* Initialize the ModelTime slog.input.InputLog().getTreeRoot() */
		
		time_model    = new ModelTime( root_window,
				reader.getGlobalMinTime(),
				reader.getGlobalMaxTime());
				
		SimpleConsoleLogger.Debug( "tZoomFtr = " + time_model.getTimeZoomFactor() );

		this.setLayout( new BorderLayout() );

		/* Setting up the CENTER panel to store various time-related GUIs */
		JPanel center_panel = new JPanel();
		center_panel.setLayout( new BoxLayout( center_panel,
				BoxLayout.Y_AXIS ) );

		/* The Time Ruler */
		time_ruler        = new RulerTime( time_model );
		time_ruler_vport  = new ViewportTime( time_model );
		time_ruler_vport.setView( time_ruler );
		time_ruler_panel  = new ViewportTimePanel( time_ruler_vport );
		time_ruler_panel.setBorderTitle( " Time (seconds) ",
				TitledBorder.RIGHT,
				TitledBorder.BOTTOM,
				Const.FONT, Color.red );
		time_ruler_vport.initLeftMouseToZoom( false );
		/*
                   Propagation of AdjustmentEvent originating from scroller:

                   scroller -----> time_model -----> viewport -----> view
                             adj               time           paint
                   viewport is between time_model and view because
                   viewport is what user sees.
		 */
		time_model.addTimeListener( time_ruler_vport );
		
		time_model.addTimeListener( timeUpdateListener);
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
		int      ruler_panel_height = ruler_panel_insets.top
		+ time_ruler.getJComponentHeight()
		+ ruler_panel_insets.bottom;
		time_ruler_panel.setPreferredSize(
				new Dimension( 100, ruler_panel_height ) );

		/* The TimeLine Canvas */
		time_canvas       = new CanvasTimeline(  time_model, reader,  y_model, y_tree);
		
		time_canvas_vport = new ViewportTimeYaxis( time_model, y_model, y_tree );
		time_canvas_vport.setView( time_canvas );
		time_canvas_panel = new ViewportTimePanel( time_canvas_vport );
		time_canvas_panel.setBorderTitle( " TimeLines ",
				TitledBorder.RIGHT,
				TitledBorder.TOP,
				null, Color.blue );
		time_canvas_vport.initLeftMouseToZoom( true );
		/* Inform "time_canvas_vport" time has been changed */
		time_model.addTimeListener( time_canvas_vport );

		/* The View's Time Display Panel */
		time_display_panel = new ModelTimePanel( time_model );
		time_model.setParamDisplay( time_display_panel );
		JPanel canvas_lmouse;
		canvas_lmouse = time_canvas_vport.createLeftMouseModePanel(
				BoxLayout.X_AXIS );
		canvas_lmouse.setToolTipText(
				"Operation for left mouse button click on Timeline canvas" );
		time_display_panel.add( canvas_lmouse );

		/* The Horizontal "Time" ScrollBar */
		time_scrollbar = new ScrollbarTime( time_model );
		time_scrollbar.setEnabled( true );
		time_model.setScrollBar( time_scrollbar );


		info_model     = new ModelInfo(reader);
		info_display_panel = new ModelInfoPanel( info_model );
		info_model.setParamDisplay( info_display_panel );
		time_canvas_vport.setInfoModel( info_model );

		center_panel.add( time_display_panel );
		center_panel.add( info_display_panel );
		center_panel.add( time_canvas_panel );
		center_panel.add( time_scrollbar );
		center_panel.add( time_ruler_panel );

		/* Setting up the LEFT panel to store various Y-axis related GUIs */
		JPanel left_panel = new JPanel();
		left_panel.setLayout( new BoxLayout( left_panel,
				BoxLayout.Y_AXIS ) );

		/* "VIEW" title */
		Insets canvas_panel_insets = time_canvas_panel.getInsets();

		/* YaxisTree View for SLOG-2 */
		y_scroller.setAlignmentX( Component.CENTER_ALIGNMENT );
		/* when y_scrollbar is changed, update time_canvas as well. */
		y_scrollbar.addAdjustmentListener( time_canvas_vport );

		/* YaxisTree's Column Labels */
		int       left_bottom_height = ruler_panel_height
		+ canvas_panel_insets.bottom;
		JTextArea y_colarea   = new JTextArea();
		// y_colarea.setFont( Const.FONT );
		StringBuffer text_space  = new StringBuffer( " " );
		
		/*
		 * TODO what the heck?
		 * for ( int idx = 0; idx < y_colnames.length; idx++ ) {
			y_colarea.append( text_space.toString() + "@ "
					+ y_colnames[ idx ] + "\n" );
			text_space.append( "    " );
		}
		*/
		JScrollPane y_colpanel = new JScrollPane( y_colarea,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );
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


		io_options_pulldown_list = new IOOptionsListener();

		left_panel.add( io_options_pulldown_list );
		left_panel.add( y_scroller );
		// left_panel.add( y_title_btm );
		left_panel.add( y_colpanel );

		/* Setting up the RIGHT panel to store various time-related GUIs */
		JPanel right_panel = new JPanel();
		right_panel.setLayout( new BoxLayout( right_panel,
				BoxLayout.Y_AXIS ) );
		row_adjs = new RowAdjustments( time_canvas_vport, y_tree );

		JPanel row_resize  = row_adjs.getComboBoxPanel();
		row_resize.setMinimumSize(
				new Dimension( 0, canvas_panel_insets.top ) );
		row_resize.setMaximumSize(
				new Dimension( Short.MAX_VALUE, canvas_panel_insets.top ) );
		row_resize.setPreferredSize(
				new Dimension( 20, canvas_panel_insets.top ) );
		row_resize.setAlignmentX( Component.CENTER_ALIGNMENT );

		JPanel row_txtfld  = row_adjs.getTextFieldPanel();
		row_txtfld.setAlignmentX( Component.CENTER_ALIGNMENT );

		JPanel row_slider  = row_adjs.getSliderPanel();
		JScrollPane slider_scroller = new JScrollPane( row_slider,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );
		slider_scroller.setAlignmentX( Component.CENTER_ALIGNMENT );

		JPanel row_misc  = row_adjs.getMiscPanel();
		row_misc.setAlignmentX( Component.CENTER_ALIGNMENT );
		JPanel ruler_lmouse;
		ruler_lmouse = time_ruler_vport.createLeftMouseModePanel(
				BoxLayout.X_AXIS );
		ruler_lmouse.setToolTipText(
				"Operation for left mouse button click on Time Ruler" );
		ruler_lmouse.setAlignmentX( Component.LEFT_ALIGNMENT );
		row_misc.add( ruler_lmouse );

		JScrollPane row_colpanel = new JScrollPane( row_misc,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		/*
                   Since there is NOT a specific Top Level JPanel for
                   row_colpanel, so we need to set its PreferredSize here.
                   Since slider_scroller(i.e. JScrollPane containing JSlider)
                   is the space hungary component here.  So it is CRUCIAL
                   to set the height PreferredSize equal to that of MinimumSize
                   and MaximumSize, hence slider_scroller will be fixed in
                   height during resizing of the top level frame.
		 */
		row_colpanel.setMinimumSize(
				new Dimension( 0, left_bottom_height ) );
		row_colpanel.setMaximumSize(
				new Dimension( Short.MAX_VALUE, left_bottom_height ) );
		row_colpanel.setPreferredSize(
				new Dimension( 20, left_bottom_height ) );
		right_panel.add( row_resize );
		right_panel.add( row_txtfld );
		right_panel.add( slider_scroller );
		right_panel.add( row_colpanel );


		/* Store the LEFT and CENTER panels in the JSplitPane */
		JSplitPane left_splitter, right_splitter;
		left_splitter  = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
				false, left_panel, center_panel );
		//  left_splitter.setResizeWeight( 0.0d );
		right_splitter = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
				false, left_splitter, right_panel );
		right_splitter.setOneTouchExpandable( true );
		left_splitter.setOneTouchExpandable( true );
		right_splitter.setResizeWeight( 1.0d );

		this.add( right_splitter, BorderLayout.CENTER );

		/* The ToolBar for various user controls */
		toolbar = new TimelineToolBar( root_window, time_canvas, time_ruler , time_canvas_vport,
				y_scrollbar, y_tree, time_scrollbar, time_model, row_adjs );
		time_canvas.setRequired(toolbar.getRestore_timelines_listener(), time_canvas_vport);
		this.add( toolbar, BorderLayout.NORTH );

		row_adjs.initYLabelTreeSize();
	}

	public void init()
	{
		// Initialize toolbar after creation of YaxisTree view
		toolbar.init();
		row_adjs.initSlidersAndTextFields();

		if ( Debug.isActive() ) {
			Debug.println( "TimelinePanel.init(): time_model = "
					+ time_model );
			Debug.println( "TimelinePanel.init(): time_scrollbar = "
					+ time_scrollbar );
			Debug.println( "TimelinePanel.init(): time_ruler = "
					+ time_ruler );
		}
	}
}

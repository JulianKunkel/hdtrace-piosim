
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

package viewer.zoomable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import viewer.common.Const;
import viewer.common.CustomCursor;
import viewer.common.Debug;
import viewer.common.IconManager;
import viewer.common.Parameters;
import viewer.common.TimeEvent;
import viewer.common.TimeListener;
import viewer.common.IconManager.IconType;
import viewer.dialog.InfoDialog;
import viewer.dialog.InfoDialogForDuration;
import viewer.first.Jumpshot;
import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.statistics.StatisticEntry;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import drawable.TimeBoundingBox;

public class ViewportTime extends JViewport implements TimeListener, MouseInputListener, KeyListener
{
	private static final long serialVersionUID = -3752967463821381046L;
	
	private static final Color   INFO_LINE_COLOR  = Color.green;
	private static final Color   INFO_AREA_COLOR  = new Color(255,255,  0,64);
	private static final Color   ZOOM_LINE_COLOR  = Color.white;
	private static final Color   ZOOM_AREA_COLOR  = new Color(132,112,255,96);
	private static final Color   FOCUS_LINE_COLOR = Color.red;

	private   Point                     view_pt;
	// view_img is both a Component and ScrollableView object
	private   ScrollableView            view_img      = null;
	private   ModelTime                 time_model    = null;
	private   ModelInfo                 info_model    = null;    
	private   ToolBarStatus             toolbar       = null;

	private   TimeBoundingBox           vport_timebox = null;
	protected CoordPixelImage           coord_xform   = null;

	private   TimeBoundingBox           zoom_timebox  = null;
	private   TimeBoundingBox           info_timebox  = null;

	// info_dialogs list is used to keep track of all InfoDialog boxes.
	private   ArrayList<InfoDialog>          info_dialogs;

	private   InfoDialogActionListener  info_action_listener;
	private   InfoDialogWindowListener  info_window_listener;

	protected boolean                   isLeftMouseClick4Zoom;
	private   JRadioButton              zoom_btn;
	private   JRadioButton              hand_btn;
	
	private class MyComponentResizeListener extends ComponentAdapter{
		
		@Override
		public void componentResized(ComponentEvent e) {

			if ( view_img != null ) {
				/*
	               Instead of informing the view by ComponentEvent, i.e.
	               doing addComponentListener( (ComponentListener) view ),
	               ( (ComponentListener) view ).componentResized() is called
	               directly here to ensure that view is resized before 
	               super.setViewPosition() is called on view.  This is done
	               to ensure the correct sequence of componentResized().
	               This also means the "view" does NOT need to implement
	               ComponentListener interface.
				 */
				view_img.forceRedraw(getSize().width, getSize().height);
				/*
	               It is very IMPORTANT to do setPreferredSize() for JViewport
	               with custom JComponent view.  If PreferredSize is NOT set,
	               the top-level container, JFrame, will have difficulty to
	               compute the size final display window when calling
	               Window.pack().  The consequence will be the initial
	               view of JViewport has its getViewPosition() set to (0,0)
	               in view coordinates during program starts up.
	               Apparently, Window.pack() uses PreferredSize to compute
	               window size.
				 */
				getMe().setPreferredSize( getSize() );
				if ( Debug.isActive() )
					Debug.println( "ViewportTime: componentResized()'s view_img = "
							+ view_img );
				view_pt.x = view_img.getXaxisViewPosition();
				getMe().setViewPosition( view_pt );
				/*
	               calling view.repaint() to ensure the view is repainted
	               after setViewPosition is called.
	               -- apparently, this.repaint(), the RepaintManager, has invoked 
	                  ( (Component) view_img ).repaint();
	               -- JViewport.setViewPosition() may have invoked super.repaint()
				 */
				getMe().repaint();
			}
		}
	};	
	
	private ViewportTime getMe(){
		return this;
	}
	
	public ViewportTime( final ModelTime in_model )
	{
		time_model             = in_model;
		view_pt                = new Point( 0, 0 );
		isLeftMouseClick4Zoom  = false;   // default to Scroll with left mouse
		zoom_btn               = null;
		hand_btn               = null;
		/*
            HierarchyBoundsListener is for the case when this class
            is moved but NOT resized.  That it checks for situation
            to reinitialize the size of ScrollableView when the 
            scrollable image's size is reset for some obscure reason.

            However, defining getPreferredSize() of ScrollableView
            seems to make HierarchyBoundsListener of this class
            unnecessary.
		 */
		// addHierarchyBoundsListener( this );

		// setDebugGraphicsOptions( DebugGraphics.LOG_OPTION );
		vport_timebox       = new TimeBoundingBox();
		
		this.addComponentListener( new MyComponentResizeListener());
	}


	public void setInfoModel( ModelInfo in_model ) {
		this.info_model = in_model;
	}

	public ModelInfo getInfoModel() {
		return this.info_model;
	}


	public void setView( Component view )
	{
		super.setView( view );
		// Assume "view" has implemented the ComponentListener interface
		Dimension min_sz = view.getMinimumSize();
		if ( min_sz != null )
			setMinimumSize( min_sz );
		Dimension max_sz = view.getMaximumSize();
		if ( max_sz != null )
			setMaximumSize( max_sz );
		Dimension pref_sz = view.getPreferredSize();
		if ( pref_sz != null )
			setPreferredSize( pref_sz );
		view_img     = (ScrollableView) view;

		coord_xform  = new CoordPixelImage( (ScrollableObject) view_img );
		super.addMouseListener( this );
		super.addMouseMotionListener( this );
		super.addKeyListener( this );

		info_dialogs = new ArrayList<InfoDialog>();
		info_action_listener = new InfoDialogActionListener( );
		info_window_listener = new InfoDialogWindowListener( );
	}

	public void setToolBarStatus( ToolBarStatus  in_toolbar )
	{
		toolbar = in_toolbar;
	}

	//  For Debugging Profiling
	public Dimension getMinimumSize()
	{
		Dimension min_sz = super.getMinimumSize();
		if ( Debug.isActive() )
			Debug.println( "ViewportTime: min_size = " + min_sz );
		return min_sz;
	}

	//  For Debugging Profiling
	public Dimension getMaximumSize()
	{
		Dimension max_sz = super.getMaximumSize();
		if ( Debug.isActive() )
			Debug.println( "ViewportTime: max_size = " + max_sz );
		return max_sz;
	}

	//  For Debugging Profiling
	public Dimension getPreferredSize()
	{
		Dimension pref_sz = super.getPreferredSize();
		if ( Debug.isActive() )
			Debug.println( "ViewportTime: pref_size = " + pref_sz );
		return pref_sz;
	}

	protected void setYaxisViewPosition( int new_y_view_pos )
	{
		view_pt.y   = new_y_view_pos;
	}

	protected int  getXaxisViewPosition()
	{
		return view_pt.x;
	}

	/*
        timeChanged() is invoked by ModelTime's fireTimeChanged();

        Since ModelTime is the Model for the scroll_bar, timeChanged()
        will be called everytime when scroll_bar is moved/changed. 
	 */
	public void timeChanged( TimeEvent evt )
	{
		if ( Debug.isActive() ) {
			Debug.println( "ViewportTime: timeChanged()'s START: " );
			Debug.println( "time_evt = " + evt );
		}

		if ( view_img != null ) {
			// view_img.checkToXXXXView() assumes constant image size
			view_img.checkToZoomView();
			view_img.checkToScrollView();
			if ( Debug.isActive() )
				Debug.println( "ViewportTime:timeChanged()'s view_img = "
						+ view_img );
			view_pt.x = view_img.getXaxisViewPosition();
			super.setViewPosition( view_pt );
			/*
               calling view.repaint() to ensure the view is repainted
               after setViewPosition is called.
               -- apparently, super.repaint(), the RepaintManager, has invoked 
                  ( (Component) view_img ).repaint();
               -- JViewport.setViewPosition() may have invoked super.repaint()
			 */
			this.repaint();
		}
		if ( Debug.isActive() ) {
			if ( view_img != null ) {
				Debug.println( "ViewportTime: "
						+ "view_img.getXaxisViewPosition() = "
						+ view_pt.x );
				Debug.println( "ViewportTime: [after] getViewPosition() = "
						+ super.getViewPosition() );
			}
			Debug.println( "ViewportTime: timeChanged()'s END: " );
		}
	}
	
	private void drawShadyTimeBoundingBox( Graphics g,
			final TimeBoundingBox timebox,
			Color line_color, Color area_color )
	{
		double      line_time;
		int         x1_pos, x2_pos;

		if ( vport_timebox.overlaps( timebox ) ) {
			line_time = timebox.getEarliestTime();
			if ( coord_xform.contains( line_time ) ) {
				x1_pos = coord_xform.convertTimeToPixel( line_time );
				g.setColor( line_color );
				g.drawLine( x1_pos, 0, x1_pos, this.getHeight() );
			}
			else
				x1_pos = 0;

			line_time = timebox.getLatestTime();
			if ( coord_xform.contains( line_time ) ) {
				x2_pos = coord_xform.convertTimeToPixel( line_time );
				g.setColor( line_color );
				g.drawLine( x2_pos, 0, x2_pos, this.getHeight() );
			}
			else
				x2_pos = this.getWidth();

			if ( x2_pos > x1_pos ) {
				g.setColor( area_color );
				g.fillRect( x1_pos+1, 0, x2_pos-x1_pos-1, this.getHeight() );
			}
		}
	}

	public void paint( Graphics g )
	{
		Iterator<InfoDialog>    itr;
		InfoDialog  info_popup;
		double      popup_time;
		double      focus_time;
		int         x_pos;

		if ( Debug.isActive() )
			Debug.println( "ViewportTime: paint()'s START: " );

		// Need to get the FOCUS so KeyListener will respond.
		// requestFocus();

		//  "( (Component) view_img ).repaint()" may have been invoked
		//  in JComponent's paint() method's paintChildren() ?!
		super.paint( g );

		/*  Initialization  */
		vport_timebox.setEarliestTime( time_model.getTimeViewPosition() );
		vport_timebox.setLatestFromEarliest( time_model.getTimeViewExtent() );
		coord_xform.resetTimeBounds( vport_timebox );

		// Draw a line at time_model.getTimeZoomFocus() 
		if ( ! Parameters.LEFTCLICK_INSTANT_ZOOM ) {
			focus_time = time_model.getTimeZoomFocus();
			if ( coord_xform.contains( focus_time ) ) {
				x_pos = coord_xform.convertTimeToPixel( focus_time );
				g.setColor( FOCUS_LINE_COLOR );
				g.drawLine( x_pos, 0, x_pos, this.getHeight() );
			}
		}

		/*  Draw zoom boundary  */
		if ( zoom_timebox != null )
			this.drawShadyTimeBoundingBox( g, zoom_timebox,
					ZOOM_LINE_COLOR,
					ZOOM_AREA_COLOR );

		if ( info_timebox != null )
			this.drawShadyTimeBoundingBox( g, info_timebox,
					INFO_LINE_COLOR,
					INFO_AREA_COLOR );

		/*  Draw the InfoDialog marker  */
		itr = info_dialogs.iterator();
		while ( itr.hasNext() ) {
			info_popup = (InfoDialog) itr.next();
			if ( info_popup instanceof InfoDialogForDuration ) {
				InfoDialogForDuration  popup;
				popup = (InfoDialogForDuration) info_popup;
				this.drawShadyTimeBoundingBox( g, popup.getTimeBoundingBox(),
						INFO_LINE_COLOR,
						INFO_AREA_COLOR );
			}
			else {
				popup_time = info_popup.getClickedTime();
				if ( coord_xform.contains( popup_time ) ) {
					x_pos = coord_xform.convertTimeToPixel( popup_time );
					g.setColor( INFO_LINE_COLOR );
					g.drawLine( x_pos, 0, x_pos, this.getHeight() );
				}
			}
		}

		if ( Debug.isActive() )
			Debug.println( "ViewportTime: paint()'s END: " );
	}

	public void initLeftMouseToZoom( boolean in_isLeftMouseClick4Zoom )
	{
		ButtonGroup  btn_group;
		ImageIcon    icon, icon_shaded;
		final IconManager icons = Jumpshot.getIconManager();

		isLeftMouseClick4Zoom  = in_isLeftMouseClick4Zoom;
		btn_group              = new ButtonGroup();

		icon         = icons.getActiveToolbarIcon(IconType.ZoomIn);
		icon_shaded  = icons.getDisabledToolbarIcon(IconType.ZoomIn);

		zoom_btn     = new JRadioButton( icon_shaded );
		zoom_btn.setSelectedIcon( icon );
		zoom_btn.setBorderPainted( true );
		zoom_btn.setToolTipText( "Left mouse button click to Zoom" );
		zoom_btn.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt )
			{
				if ( zoom_btn.isSelected() )
					isLeftMouseClick4Zoom = true;
			}
		} );
		if ( isLeftMouseClick4Zoom )
			zoom_btn.doClick();
		btn_group.add( zoom_btn );

		icon         = icons.getActiveToolbarIcon(IconType.Hand);
		icon_shaded  = icons.getDisabledToolbarIcon(IconType.Hand);
		hand_btn = new JRadioButton( icon_shaded );
		hand_btn.setSelectedIcon( icon );
		hand_btn.setBorderPainted( true );
		hand_btn.setToolTipText( "Left mouse button click to Scroll" );
		hand_btn.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt )
			{
				if ( hand_btn.isSelected() )
					isLeftMouseClick4Zoom = false;
			}
		} );
		if ( !isLeftMouseClick4Zoom )
			hand_btn.doClick();
		btn_group.add( hand_btn );
	}

	public JPanel createLeftMouseModePanel( int boxlayout_mode )
	{
		JPanel   btn_panel = null;
		btn_panel = null;
		if ( zoom_btn != null && hand_btn != null ) {
			btn_panel = new JPanel();
			btn_panel.setLayout( new BoxLayout( btn_panel, boxlayout_mode ) );
			btn_panel.add( zoom_btn );
			btn_panel.add( hand_btn );
			btn_panel.setBorder( BorderFactory.createEtchedBorder() );
		}
		return btn_panel;
	}


	// Override the Component.setCursor()
	public void setCursor( Cursor new_cursor )
	{
		/*
               Replace the DEFAULT_CURSOR by ZoomPlus or HandOpen cursor.
               i.e. the default cursor for this class is either 
               ZoomPlus or HandOpen cursor depending on isLeftMouseClick4Zoom.
		 */
		if ( new_cursor == CustomCursor.Normal ) {
			if ( isLeftMouseClick4Zoom )
				super.setCursor( CustomCursor.ZoomPlus );
			else
				super.setCursor( CustomCursor.HandOpen );
		}
		else
			super.setCursor( new_cursor );
	}

	/*
            Interface to fulfill MouseInputListener()
	 */

	public void mouseMoved( MouseEvent mouse_evt ) {
		ScrollableObject  scrollable;
		Point             vport_click, view_click;
		TraceObject      dobj;

		vport_click = mouse_evt.getPoint();

		scrollable = (ScrollableObject) view_img;

		view_click = SwingUtilities.convertPoint( this,
				vport_click,
				scrollable );
		dobj = scrollable.getDrawableAt( view_click, vport_timebox );

		if(dobj != null){
			switch (dobj.getType()){
			case STATE:
				info_model.showInfo((StateTraceEntry) dobj);
				break;
			case EVENT:
				info_model.showInfo((EventTraceEntry) dobj);
				break;
			case STATISTICENTRY:
				info_model.showInfo((StatisticEntry) dobj);
				break;
			}
		}
	}    

	public void mouseEntered( MouseEvent mouse_evt )
	{
		super.requestFocus();
		if ( isLeftMouseClick4Zoom )
			super.setCursor( CustomCursor.ZoomPlus );
		else
			super.setCursor( CustomCursor.HandOpen );
	}

	public void mouseExited( MouseEvent mouse_evt )
	{
		/*
               useless to reset cursor here because of similarity of the
               overriden this.setCursor() above and mouseEntered().
		 */
		// super.setCursor( CustomCursor.Normal );
	}

	public void mouseClicked( MouseEvent mouse_evt )
	{
		
		if ( SwingUtilities.isLeftMouseButton( mouse_evt ) ) {
			if ( isLeftMouseClick4Zoom ) {  // Zoom Mode
				double focus_time;				
				Point  vport_click = mouse_evt.getPoint();
				
				focus_time  = coord_xform.convertPixelToTime( vport_click.x );				
				if ( Parameters.LEFTCLICK_INSTANT_ZOOM ) {
					// Left click with Shift to Zoom Out,
					// Left click to Zoom In.
					if ( mouse_evt.isShiftDown() ) {						
						time_model.zoomOut();
						super.setCursor( CustomCursor.ZoomMinus );
					}
					else {
						time_model.zoomIn(focus_time);
						super.setCursor( CustomCursor.ZoomPlus );
					}
					super.requestFocus();
				}
				else
					this.repaint();
			}
		}
		super.requestFocus();
	}

	/* 
            mouse_press_time is a temporary variable among
            mousePressed(), mouseDragged() & mouseReleased()
	 */
	private double                    mouse_pressed_time;
	private int                       mouse_pressed_Xloc;
	private int                       mouse_last_Xloc;
	private boolean                   hasControlKeyBeenPressed = false; 

	public void mousePressed( MouseEvent mouse_evt )
	{
		Point  vport_click;
		double click_time;

		// Ignore all mouse events when Control or Escape key is pressed
		if ( mouse_evt.isControlDown() ) {
			hasControlKeyBeenPressed  = true;
			return;
		}

		vport_timebox.setEarliestTime( time_model.getTimeViewPosition() );
		vport_timebox.setLatestFromEarliest(
				time_model.getTimeViewExtent() );
		coord_xform.resetTimeBounds( vport_timebox );
		vport_click = mouse_evt.getPoint();
		click_time  = coord_xform.convertPixelToTime( vport_click.x );
		if ( SwingUtilities.isLeftMouseButton( mouse_evt ) ) {
			if ( isLeftMouseClick4Zoom ) {  // Zoom Mode
				zoom_timebox = new TimeBoundingBox();
				zoom_timebox.setZeroDuration( click_time );
				this.repaint();
				super.setCursor( CustomCursor.ZoomPlus );
			}
			else  // Hand Mode
				super.setCursor( CustomCursor.HandClose );
		}
		else if ( SwingUtilities.isRightMouseButton( mouse_evt ) ) {
			info_timebox = new TimeBoundingBox();
			info_timebox.setZeroDuration( click_time );
			this.repaint();
		}
		mouse_pressed_time = click_time;
		mouse_pressed_Xloc = vport_click.x;
		mouse_last_Xloc    = vport_click.x;
	}

	public void mouseDragged( MouseEvent mouse_evt )
	{
		Point  vport_click;
		double click_time;

		// Ignore all mouse events when Control key is pressed
		if ( mouse_evt.isControlDown() || hasControlKeyBeenPressed ) {
			hasControlKeyBeenPressed  = true;
			return;
		}

		if ( mouse_evt.isShiftDown() )
			if ( super.getCursor() == CustomCursor.ZoomMinus )
				super.setCursor( CustomCursor.ZoomPlus );

		vport_click = mouse_evt.getPoint();
		click_time  = coord_xform.convertPixelToTime( vport_click.x );
		if ( SwingUtilities.isLeftMouseButton( mouse_evt ) ) {
			if ( isLeftMouseClick4Zoom ) {  // Zoom Mode
				if ( zoom_timebox != null ) { 
					// i.e., Zoom has NOT been cancelled yet
					if ( click_time > mouse_pressed_time )
						zoom_timebox.setLatestTime( click_time );
					else
						zoom_timebox.setEarliestTime( click_time );
					this.repaint();
					// super.setCursor( CustomCursor.ZoomPlus );
				}
			}
			else {  // Hand Mode
				if ( vport_click.x != mouse_last_Xloc ) {
					time_model.scroll( mouse_last_Xloc - vport_click.x );
					mouse_last_Xloc = vport_click.x;
					super.setCursor( CustomCursor.HandClose );
				}
			}
		}
		else if ( SwingUtilities.isRightMouseButton( mouse_evt ) ) {
			if ( click_time > mouse_pressed_time )
				info_timebox.setLatestTime( click_time );
			else
				info_timebox.setEarliestTime( click_time );
			this.repaint();
		}
	}

	public void mouseReleased( MouseEvent mouse_evt )
	{
		ScrollableObject  scrollable;
		InfoDialog        info_popup;
		Window            window;
		Point             vport_click, view_click, global_click;
		double            click_time;

		// Ignore all mouse events when Control key is pressed
		if ( mouse_evt.isControlDown() || hasControlKeyBeenPressed ) {
			hasControlKeyBeenPressed  = false;
			return;
		}

		vport_click = mouse_evt.getPoint();
		click_time  = coord_xform.convertPixelToTime( vport_click.x );
		if ( SwingUtilities.isLeftMouseButton( mouse_evt ) ) {
			if ( isLeftMouseClick4Zoom ) {  // Zoom Mode
				if ( zoom_timebox != null ) {
					// i.e., Zoom has NOT been cancelled yet
					if ( click_time > mouse_pressed_time )
						zoom_timebox.setLatestTime( click_time );
					else
						zoom_timebox.setEarliestTime( click_time );
					this.repaint();
					// if ( zoom_timebox.getDuration() > 0.0d ) {
					if (    Math.abs(vport_click.x - mouse_pressed_Xloc)
							>= Parameters.MIN_WIDTH_TO_DRAG ) {
						time_model.zoomRapidly(
								zoom_timebox.getEarliestTime(),
								zoom_timebox.getDuration() );
					}
					zoom_timebox = null;
					this.repaint();
					super.setCursor( CustomCursor.ZoomPlus );
				}
			}
			else {  // Hand Mode
				if ( vport_click.x != mouse_last_Xloc ) {
					time_model.scroll( mouse_last_Xloc - vport_click.x );
					mouse_last_Xloc = vport_click.x;
				}
				super.setCursor( CustomCursor.HandOpen );
			}
		}
		else if ( SwingUtilities.isRightMouseButton( mouse_evt ) ) {
			if ( click_time > mouse_pressed_time )
				info_timebox.setLatestTime( click_time );
			else
				info_timebox.setEarliestTime( click_time );
			scrollable = (ScrollableObject) view_img;
			// if ( info_timebox.getDuration() > 0.0d ) {
			if (    Math.abs(vport_click.x - mouse_pressed_Xloc)
					>= Parameters.MIN_WIDTH_TO_DRAG ) {
				window = SwingUtilities.windowForComponent( this );
				if ( window instanceof Frame )
					info_popup = new InfoDialogForDuration( (Frame) window,
							info_timebox,
							scrollable );
				else // if ( window instanceof Dialog )
					info_popup = new InfoDialogForDuration( (Dialog) window,
							info_timebox,
							scrollable );
			}
			else {
				view_click = SwingUtilities.convertPoint( this,
						vport_click,
						scrollable );
				info_popup = scrollable.getPropertyAt( view_click,
						vport_timebox );
			}
			global_click = new Point( vport_click );
			SwingUtilities.convertPointToScreen( global_click, this );
			info_popup.setVisibleAtLocation( global_click );
			info_popup.getCloseButton().addActionListener( 
					info_action_listener );
			info_popup.addWindowListener( info_window_listener );
			info_dialogs.add( info_popup );
			info_timebox = null;  // remove to avoid redundant drawing
			this.repaint();
		}
	}




	/*
            Interface to fulfill KeyListener()
	 */

	public void keyTyped( KeyEvent evt ) {}

	public void keyReleased( KeyEvent evt )
	{
		if ( evt.getKeyCode() == KeyEvent.VK_SHIFT ) {
			if ( super.getCursor() == CustomCursor.ZoomMinus )
				super.setCursor( CustomCursor.ZoomPlus );
		}
	}

	public void keyPressed( KeyEvent evt )
	{
		if ( evt.getKeyCode() == KeyEvent.VK_SHIFT ) {
			if ( super.getCursor() == CustomCursor.ZoomPlus )
				super.setCursor( CustomCursor.ZoomMinus );
		}
		else if ( evt.getKeyCode() == KeyEvent.VK_ESCAPE ) {
			if ( zoom_timebox != null ) {
				zoom_timebox = null;
				this.repaint();
			}
		}
	}



	public void resetToolBarZoomButtons()
	{
		if ( toolbar != null )
			toolbar.resetZoomButtons();
	}

	protected InfoDialog getLastInfoDialog()
	{
		int info_dialogs_size = info_dialogs.size();
		if ( info_dialogs_size > 0 )
			return  info_dialogs.get( info_dialogs_size - 1 );
		else
			return null;
	}


	private void forceRepaint(){
		this.repaint();
	}
	

	private class InfoDialogActionListener implements ActionListener
	{

		public void actionPerformed( ActionEvent evt )
		{
			InfoDialog  info_popup;
			Object      evt_src = evt.getSource();
			Iterator<InfoDialog> itr = info_dialogs.iterator();
			while ( itr.hasNext() ) {
				info_popup = itr.next();
				if ( evt_src == info_popup.getCloseButton() ) {
					info_dialogs.remove( info_popup );
					info_popup.dispose();
					forceRepaint();
					break;
				}
			}
		}
	}   // InfoDialogActionListener

	private class InfoDialogWindowListener extends WindowAdapter
	{
		public void windowClosing( WindowEvent evt )
		{
			InfoDialog  info_popup;
			Object      evt_src = evt.getSource();
			Iterator<InfoDialog> itr = info_dialogs.iterator();
			while ( itr.hasNext() ) {
				info_popup = (InfoDialog) itr.next();
				if ( evt_src == info_popup ) {
					info_dialogs.remove( info_popup );
					info_popup.dispose();
					forceRepaint();
					break;
				}
			}
		}
	}   // Class InfoDialogWindowListener

}

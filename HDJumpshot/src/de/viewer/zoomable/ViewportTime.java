
/** Version Control Information $Id: ViewportTime.java 264 2009-05-02 17:01:41Z kunkel $
 * @lastmodified    $Date: 2009-05-02 19:01:41 +0200 (Sa, 02. Mai 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 264 $ 
 */

//Copyright (C) 2009 Julian M. Kunkel

//This file is part of HDJumpshot.

//HDJumpshot is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//HDJumpshot is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.


/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author Anthony Chan (Jumpshot 4), Julian M. Kunkel
 */

package de.viewer.zoomable;


import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import de.drawable.TimeBoundingBox;
import de.viewer.common.CustomCursor;
import de.viewer.common.Debug;
import de.viewer.common.ModelInfoPanel;
import de.viewer.common.ModelTime;
import de.viewer.common.Parameters;
import de.viewer.common.Routines;
import de.viewer.common.TimeEvent;
import de.viewer.common.TimeListener;
import de.viewer.dialog.InfoDialog;
import de.viewer.dialog.InfoDialogForDuration;


public class ViewportTime extends JViewport implements TimeListener, MouseInputListener, KeyListener
{
	private static final long serialVersionUID = -3752967463821381046L;

	private static final Color   INFO_LINE_COLOR  = Color.green;
	private static final Color   INFO_AREA_COLOR  = new Color(255,255,  0,64);
	private static final Color   ZOOM_LINE_COLOR  = Color.white;
	private static final Color   ZOOM_AREA_COLOR  = new Color(132,112,255,96);
	private static final Color   FOCUS_LINE_COLOR = Color.red;

	// Current view position
	private   Point                     view_pt;
	// view_img is both a Component and ScrollableView object
	private   ScrollableObject            viewImage      = null;
	final private   ModelTime                 modelTime;

	// show information about the object the mouse is moved over
	private   ModelInfoPanel            info_model    = null;    

	// store the last object the mouse is moved over to avoid multiple invocations of the info_model
	private   Object                  lastMouseMoveObject = null;
	private   ToolBarStatus             toolbar       = null;

	private   TimeBoundingBox           vport_timebox = null;
	protected CoordPixelImage           coord_xform   = null;

	private   TimeBoundingBox           zoomTimebox  = null;
	private   TimeBoundingBox           infoTimebox  = null;
	// in which direction should the box (zoom or info) be modified on mouse move:
	private   boolean                   boxExtensionRight = true;

	/* 
            mouse_press_time is a temporary variable among
            mousePressed(), mouseDragged() & mouseReleased()
	 */
	private double                    mouse_pressed_time;
	private int                       mouse_pressed_Xloc;
	private int                       mouse_last_Xloc;
	private boolean                   hasControlKeyBeenPressed = false; 

	// info_dialogs list is used to keep track of all InfoDialog boxes.
	private   ArrayList<InfoDialog>          info_dialogs;

	private   InfoDialogActionListener  info_action_listener;
	private   InfoDialogWindowListener  info_window_listener;

	protected boolean                   isLeftMouseClick4Zoom;

	private class MyComponentResizeListener extends ComponentAdapter{		
		@Override
		public void componentResized(ComponentEvent e) {
			if ( viewImage != null && getSize().width > 30) {
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
				viewImage.resized();

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
							+ viewImage );
				view_pt.x = viewImage.getXaxisViewPosition();
				getMe().setViewPosition( view_pt );
				getMe().repaint();
			}
		}
	};	

	private ViewportTime getMe(){
		return this;
	}

	public ViewportTime( ModelTime modelTime)
	{
		this.modelTime         = modelTime;
		view_pt                = new Point( 0, 0 );
		isLeftMouseClick4Zoom  = false;   // default to Scroll with left mouse

		vport_timebox       = new TimeBoundingBox();
		this.addComponentListener( new MyComponentResizeListener());

		setOpaque(false);
	}

	public void setInfoModel( ModelInfoPanel in_model ) {
		this.info_model = in_model;
	}

	public void setView( ScrollableObject view )
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
		viewImage     = view;

		coord_xform  = new CoordPixelImage( viewImage );
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
	@Override
	public Dimension getMinimumSize()
	{
		Dimension min_sz = super.getMinimumSize();
		if ( Debug.isActive() )
			Debug.println( "ViewportTime: min_size = " + min_sz );
		return min_sz;
	}

	@Override
	public Dimension getMaximumSize()
	{
		Dimension max_sz = super.getMaximumSize();
		if ( Debug.isActive() )
			Debug.println( "ViewportTime: max_size = " + max_sz );
		return max_sz;
	}

	@Override
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
	@Override
	public void timeChanged( TimeEvent evt )
	{
		if ( Debug.isActive() ) {
			Debug.println( "ViewportTime: timeChanged()'s START: " );
			Debug.println( "time_evt = " + evt );
		}

		if ( viewImage != null ) {
			// view_img.checkToXXXXView() assumes constant image size
			viewImage.checkToZoomView();
			viewImage.checkToScrollView();

			if ( Debug.isActive() )
				Debug.println( "ViewportTime:timeChanged()'s view_img = "
						+ viewImage );
			view_pt.x = viewImage.getXaxisViewPosition();
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
			if ( viewImage != null ) {
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

	@Override
	public void paint(Graphics g) {		
		Iterator<InfoDialog>    itr;
		InfoDialog  info_popup;
		double      popup_time;
		double      focus_time;
		int         x_pos;

		super.paint(g);

		if ( Debug.isActive() )
			Debug.println( "ViewportTime: paint()'s START: " );

		/*  Initialization  */
		vport_timebox.setEarliestTime( modelTime.getViewPosition() );
		vport_timebox.setLatestFromEarliest( modelTime.getViewExtent() );
		coord_xform.resetTimeBounds( vport_timebox );

		// Draw a line at time_model.getTimeZoomFocus() 
		if ( ! Parameters.LEFTCLICK_INSTANT_ZOOM ) {
			focus_time = modelTime.getTimeZoomFocus();
			if ( coord_xform.contains( focus_time ) ) {
				x_pos = coord_xform.convertTimeToPixel( focus_time );
				g.setColor( FOCUS_LINE_COLOR );
				g.drawLine( x_pos, 0, x_pos, this.getHeight() );
			}
		}

		/*  Draw zoom boundary  */
		if ( zoomTimebox != null ){
			this.drawShadyTimeBoundingBox( g, zoomTimebox,
					ZOOM_LINE_COLOR,	ZOOM_AREA_COLOR );
		}

		if ( infoTimebox != null ){
			this.drawShadyTimeBoundingBox( g, infoTimebox,
					INFO_LINE_COLOR, INFO_AREA_COLOR );
		}

		/*  Draw the InfoDialog marker  */
		itr = info_dialogs.iterator();
		while ( itr.hasNext() ) {
			info_popup = itr.next();
			if ( info_popup instanceof InfoDialogForDuration ) {
				InfoDialogForDuration  popup;
				popup = (InfoDialogForDuration) info_popup;
				this.drawShadyTimeBoundingBox( g, popup.getTimeBoundingBox(),
						INFO_LINE_COLOR,
						INFO_AREA_COLOR );
			}	else {
				popup_time = info_popup.getClickedTime().getDouble();
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

	public void setLeftMouseToZoom( boolean in_isLeftMouseClick4Zoom )
	{
		isLeftMouseClick4Zoom  = in_isLeftMouseClick4Zoom;

		if ( isLeftMouseClick4Zoom )
			super.setCursor( CustomCursor.ZoomPlus );
		else
			super.setCursor( CustomCursor.HandOpen );
	}


	@Override
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
		else{
			super.setCursor( new_cursor );
		}
	}

	@Override
	public void mouseMoved( MouseEvent mouse_evt ) {
		ScrollableObject  scrollable;
		Point             vport_click, view_click;
		Object            dobj;

		vport_click = mouse_evt.getPoint();

		scrollable = (ScrollableObject) viewImage;

		view_click = SwingUtilities.convertPoint( this,	vport_click, scrollable );
		dobj = scrollable.getObjectAt( view_click  );

		if( dobj != lastMouseMoveObject ){
			info_model.showInfo(dobj);
		}

		lastMouseMoveObject = dobj;
	}    

	@Override
	public void mouseEntered( MouseEvent mouse_evt )
	{
		super.requestFocus();
		if ( isLeftMouseClick4Zoom )
			super.setCursor( CustomCursor.ZoomPlus );
		else
			super.setCursor( CustomCursor.HandOpen );
	}

	@Override
	public void mouseExited( MouseEvent mouse_evt )
	{
	}

	@Override
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
						modelTime.zoomOut();
						super.setCursor( CustomCursor.ZoomMinus );
					}
					else {
						modelTime.zoomIn(focus_time);
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


	@Override
	public void mousePressed( MouseEvent mouse_evt )
	{
		Point  vport_click;
		double click_time;

		// Ignore all mouse events when Control or Escape key is pressed
		if ( mouse_evt.isControlDown() ) {
			hasControlKeyBeenPressed  = true;
			return;
		}

		vport_timebox.setEarliestTime( modelTime.getViewPosition() );
		vport_timebox.setLatestFromEarliest(
				modelTime.getViewExtent() );
		coord_xform.resetTimeBounds( vport_timebox );
		vport_click = mouse_evt.getPoint();
		click_time  = coord_xform.convertPixelToTime( vport_click.x );

		boxExtensionRight = true;

		if ( SwingUtilities.isLeftMouseButton( mouse_evt ) ) {
			if ( isLeftMouseClick4Zoom ) {  // Zoom Mode
				zoomTimebox = new TimeBoundingBox();
				zoomTimebox.setZeroDuration( click_time );
			}
		}	else if ( SwingUtilities.isRightMouseButton( mouse_evt ) ) {
			infoTimebox = new TimeBoundingBox();
			infoTimebox.setZeroDuration( click_time );
			this.repaint();
		}
		mouse_pressed_time = click_time;
		mouse_pressed_Xloc = vport_click.x;
		mouse_last_Xloc    = vport_click.x;
	}

	@Override
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

		TimeBoundingBox currentBox = null;;
		if ( SwingUtilities.isLeftMouseButton( mouse_evt ) ) {
			if ( isLeftMouseClick4Zoom ) {  // Zoom Mode
				currentBox = zoomTimebox;
				super.setCursor( CustomCursor.ZoomPlus );
			}else { // Hand Mode
				if ( vport_click.x != mouse_last_Xloc ) {
					getModelTime().scroll( mouse_last_Xloc - vport_click.x );
					mouse_last_Xloc = vport_click.x;
					super.setCursor( CustomCursor.HandClose );

					return;
				}
			}
		}else if ( SwingUtilities.isRightMouseButton( mouse_evt ) ) {
			currentBox = infoTimebox;
		}

		if ( currentBox != null ) { 
			// i.e., Zoom has NOT been canceled yet, the following code 
			// ensures that the first clicked point is always the start or end.
			if ( click_time > currentBox.getLatestTime() ){
				if(! boxExtensionRight){							
					currentBox.setEarliestTime(currentBox.getLatestTime());
					boxExtensionRight = true;
				}					
				currentBox.setLatestTime( click_time );

			}else if(click_time < currentBox.getEarliestTime() ){
				if(boxExtensionRight){
					currentBox.setLatestTime(currentBox.getEarliestTime());
					boxExtensionRight = false;
				}
				currentBox.setEarliestTime( click_time );

			}else if(boxExtensionRight){
				currentBox.setLatestTime( click_time );
			}else{ // boxExtension Left
				currentBox.setEarliestTime( click_time );
			}
			this.repaint();
		}
	}


	@Override
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
				if ( zoomTimebox != null ) {
					// i.e., Zoom has NOT been cancelled yet
					if (    Math.abs(vport_click.x - mouse_pressed_Xloc) >= Parameters.MIN_WIDTH_TO_DRAG ) {
						modelTime.zoomRapidly(
								zoomTimebox.getEarliestTime(),
								zoomTimebox.getDuration() );
					}
					zoomTimebox = null;
					this.repaint();
					super.setCursor( CustomCursor.ZoomPlus );
				}
			}
			else {  // Hand Mode
				if ( vport_click.x != mouse_last_Xloc ) {
					getModelTime().scroll( mouse_last_Xloc - vport_click.x );
					mouse_last_Xloc = vport_click.x;
				}
				super.setCursor( CustomCursor.HandOpen );
			}
		}
		else if ( SwingUtilities.isRightMouseButton( mouse_evt ) && infoTimebox != null ) {			
			if ( click_time > mouse_pressed_time )
				infoTimebox.setLatestTime( click_time );
			else
				infoTimebox.setEarliestTime( click_time );
			scrollable = (ScrollableObject) viewImage;
			// if ( info_timebox.getDuration() > 0.0d ) {
			if (    Math.abs(vport_click.x - mouse_pressed_Xloc)
					>= Parameters.MIN_WIDTH_TO_DRAG ) {
				window = SwingUtilities.windowForComponent( this );
				info_popup = new InfoDialogForDuration( (Frame) window,
						infoTimebox,
						modelTime.getGlobalMinimum().add(infoTimebox.getEarliestTime()));
			}
			else {
				view_click = SwingUtilities.convertPoint( this,
						vport_click,
						scrollable );
				info_popup = scrollable.getPropertyAt( view_click );
			}
			global_click = new Point( vport_click );

			SwingUtilities.convertPointToScreen( global_click, this );

			info_popup.getCloseButton().addActionListener( info_action_listener );
			info_popup.addWindowListener( info_window_listener );

			// update sizes:
			info_popup.pack();
			
			// revalidate sizes, otherwise some components have the wrong size:
			((JComponent)info_popup.getContentPane()).revalidate();
			
			
			// try to visualize it at the clicked position, however, adjust for the object size:
			final Dimension prefSize = info_popup.getPreferredSize();
			final Dimension screenSize = Routines.getScreenSize();

			final Point position = new Point( 
					(int) ((prefSize.width + global_click.x) > screenSize.width ? screenSize.width - prefSize.getWidth() : global_click.x),
					(int) ((prefSize.height + global_click.y) > screenSize.height ? screenSize.height - prefSize.getHeight() : global_click.y));

			info_popup.setVisibleAtLocation( position );
						
			info_dialogs.add( info_popup );
			
			infoTimebox = null;  // remove to avoid redundant drawing
			this.repaint();
		}
	}


	/*
            Interface to fulfill KeyListener()
	 */
	@Override
	public void keyTyped( KeyEvent evt ) {}

	@Override
	public void keyReleased( KeyEvent evt )
	{
		if ( evt.getKeyCode() == KeyEvent.VK_SHIFT ) {
			if ( super.getCursor() == CustomCursor.ZoomMinus )
				super.setCursor( CustomCursor.ZoomPlus );
		}
	}

	@Override
	public void keyPressed( KeyEvent evt )
	{
		if ( evt.getKeyCode() == KeyEvent.VK_SHIFT ) {
			if ( super.getCursor() == CustomCursor.ZoomPlus )
				super.setCursor( CustomCursor.ZoomMinus );
		}
		else if ( evt.getKeyCode() == KeyEvent.VK_ESCAPE ) {
			if ( zoomTimebox != null ) {
				zoomTimebox = null;
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
		@Override
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
		@Override
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

	protected ModelTime getModelTime() {
		return modelTime;
	}
}

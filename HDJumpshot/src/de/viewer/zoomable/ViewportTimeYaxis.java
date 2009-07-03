
/** Version Control Information $Id: ViewportTimeYaxis.java 419 2009-06-18 14:42:07Z kunkel $
 * @lastmodified    $Date: 2009-06-18 16:42:07 +0200 (Do, 18. Jun 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 419 $ 
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

package de.viewer.zoomable;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.SwingUtilities;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.topology.TopologyManager;
import de.viewer.common.Debug;
import de.viewer.common.Dialogs;
import de.viewer.common.ModelTime;
import de.viewer.common.TimeEvent;
import de.viewer.dialog.InfoDialog;

public class ViewportTimeYaxis extends ViewportTime implements AdjustmentListener
{
	private static final long serialVersionUID = 6547118773325705282L;

	private static final Color    SEARCH_LINE_COLOR       = Color.yellow;

	private BoundedRangeModel     y_model         = null;
	private TopologyManager       topologyManager       = null;

	private int     mouse_last_Yloc;
	private double  ratio_ymodel2vportH;

	// searchable = view_img is both a Component and ScrollableView object
	private SearchableView        searchable      = null;
	private Epoch                 searchingTime  = Epoch.ZERO;        
	private SearchResults         searchResults   = null;

	public ViewportTimeYaxis( ModelTime modelTime, 
			BoundedRangeModel yaxis_model, TopologyManager topologyManager )
	{
		super( modelTime );
		y_model     = yaxis_model;
		this.topologyManager   = topologyManager;
	}

	@Override
	public void setView( ScrollableObject view )
	{
		super.setView( view );
		if ( view instanceof SearchableView )
			searchable  = (SearchableView) view;
		else
			// causes exception if SearchableView interface is invoked
			searchable  = null;
	}

	@Override
	public void adjustmentValueChanged( AdjustmentEvent evt )
	{
		if ( Debug.isActive() ) {
			Debug.println( "ViewportTimeYaxis: adjChanged()'s START: " );
			Debug.println( "adj_evt = " + evt );
		}
		Point                 view_pt = new Point();

		view_pt.x  = super.getXaxisViewPosition();
		view_pt.y  = evt.getValue();
		super.setYaxisViewPosition( view_pt.y );
		super.setViewPosition( view_pt );
		/*
               calling view.repaint() to ensure the view is repainted
               after setViewPosition is called.
               -- apparently, super.repaint(), the RepaintManager, has invoked 
                  ( (Component) view_img ).repaint();
               -- JViewport.setViewPosition() may have invoked super.repaint()

               calling the ViewortTime.paint() to avoid redrawing in this class
		 */
		super.repaint();
		if ( Debug.isActive() )
			Debug.println( "ViewportTimeYaxis: adjChanged()'s END: " );
	}

	@Override
	public void paint(Graphics g) {
		int   x_pos;

		super.paint(g);

		if ( Debug.isActive() )
			Debug.println( "ViewportTimeYaxis: paint()'s START: " );

		// Draw a line at searching_time
		final double virtTime = searchingTime.subtract(getModelTime().getGlobalMinimum()).getDouble();
		if ( super.coord_xform.contains( virtTime ) ) {
			x_pos = super.coord_xform.convertTimeToPixel( virtTime );
			g.setColor( SEARCH_LINE_COLOR );
			g.drawLine( x_pos, 0, x_pos, this.getHeight() );
		}

		if ( Debug.isActive() )
			Debug.println( "ViewportTimeYaxis: paint()'s END: " );
	}

	public void clearSearchResults()
	{		
		searchResults = null;
		searchingTime = Epoch.ZERO;
	}

	public void searchForward(){
		final Epoch oldTime = searchingTime;
		searchResults = searchable.searchNextTracable(searchingTime);
		if(searchResults.wasSucessfull()){
			searchingTime = searchResults.getObject().getEarliestTime();

			getModelTime().scroll( searchingTime.subtract(oldTime).getDouble() );	

			// Scroll the Y-axis as well so searchResults becomes visible			
			topologyManager.scrollRowToVisible( searchResults.getTimeline() );
		}else{
			clearSearchResults();

			Dialogs.info( SwingUtilities.windowForComponent( this ), "Search forward has no more TraceObject to return.\n", null );
		}
		this.repaint();
	}

	public void searchBackward(){
		final Epoch oldTime = searchingTime;
		searchResults = searchable.searchPreviousTraceable(searchingTime);
		if(searchResults.wasSucessfull()){
			searchingTime = searchResults.getObject().getLatestTime();

			getModelTime().scroll(  searchingTime.subtract(oldTime).getDouble() );
			// Scroll the Y-axis as well so searchResults becomes visible			
			topologyManager.scrollRowToVisible( searchResults.getTimeline() );			
		}else{
			clearSearchResults();

			Dialogs.info( SwingUtilities.windowForComponent( this ), "Search backward has no more TraceObject to return.\n", null );
		}
		this.repaint();
	}	

	public boolean searchInitFromDialogPosition()
	{
		clearSearchResults();

		InfoDialog  info_popup = super.getLastInfoDialog();
		if ( info_popup != null ) {
			searchInit( info_popup.getClickedTime().subtract(info_popup.getModelTimeDiff()) );
			info_popup.getCloseButton().doClick();
			this.repaint();
			return true;
		}
		else {
			Dialogs.warn( SwingUtilities.windowForComponent( this ),
					"No info dialog box! Info dialog box can be set\n"
					+ "by right mouse clicking on the timeline canvas\n" );
			return false;
		}
	}

	/**
	 * Start search at given time.
	 * @param visTime
	 */
	public void searchInit(Epoch visTime){
		searchingTime = visTime;
	}

	@Override
	public void timeChanged(TimeEvent evt) {
		if(searchResults == null){
			// reinitialize searching time.			
			searchInit( new Epoch(getModelTime().getViewPosition()) );
		}
		// call parent to update and repaint view. 
		super.timeChanged(evt);
	}


	/*
            In order to allow grasp & scroll along Y-axis, the change in
            mouse movement in Y-axis on this Viewport needs to be translated
            to movement in Yaxis scrollbar's model coordinate.  The trick is
            that the "extent" of Yaxis scrollbar's model should be mapped
            to the viewport height in pixel.
	 */
	@Override
	public void mousePressed( MouseEvent mouse_evt )
	{
		Point  vport_click;

		super.mousePressed( mouse_evt );
		if ( SwingUtilities.isLeftMouseButton( mouse_evt ) ) {
			if ( ! super.isLeftMouseClick4Zoom ) {  // Hand Mode
				vport_click          = mouse_evt.getPoint();
				mouse_last_Yloc      = vport_click.y;
				ratio_ymodel2vportH  = (double) y_model.getExtent()	/ this.getHeight();
			}
		}
	}

	@Override
	public void mouseDragged( MouseEvent mouse_evt )
	{
		Point  vport_click;
		int    y_change, sb_change;

		super.mouseDragged( mouse_evt );
		if ( SwingUtilities.isLeftMouseButton( mouse_evt ) ) {
			if ( ! super.isLeftMouseClick4Zoom ) {  // Hand Mode
				vport_click = mouse_evt.getPoint();
				y_change    = mouse_last_Yloc - vport_click.y; 
				sb_change   = (int) Math.round( ratio_ymodel2vportH		* y_change );
				// y_model.setValue() invokes adjustmentValueChanged() above
				y_model.setValue( y_model.getValue() + sb_change );
				mouse_last_Yloc = vport_click.y;
			}
		}
	}

	@Override
	public void mouseReleased( MouseEvent mouse_evt )
	{
		Point  vport_click;
		int    y_change, sb_change;

		super.mouseReleased( mouse_evt );
		if ( SwingUtilities.isLeftMouseButton( mouse_evt ) ) {
			if ( ! super.isLeftMouseClick4Zoom ) {
				vport_click = mouse_evt.getPoint();
				y_change  = mouse_last_Yloc - vport_click.y; 
				sb_change = (int) Math.round( ratio_ymodel2vportH
						* y_change );
				// y_model.setValue() invokes adjustmentValueChanged() above
				y_model.setValue( y_model.getValue() + sb_change );
				mouse_last_Yloc = vport_click.y;
			}
		}
	}
}

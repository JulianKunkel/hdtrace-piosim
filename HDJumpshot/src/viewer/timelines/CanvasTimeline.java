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

import hdTraceInput.BufferedStatisticFileReader;
import hdTraceInput.BufferedTraceFileReader;
import hdTraceInput.ReaderTraceElementEnumerator;
import hdTraceInput.TraceFormatBufferedFileReader;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.util.Date;
import java.util.Enumeration;

import javax.swing.BoundedRangeModel;
import javax.swing.SwingUtilities;

import topology.TopologyChangeListener;
import topology.TopologyManager;
import topology.TopologyStatisticTreeNode;
import viewer.common.CustomCursor;
import viewer.common.Debug;
import viewer.common.Parameters;
import viewer.common.Profile;
import viewer.common.Routines;
import viewer.dialog.InfoDialog;
import viewer.dialog.InfoDialogForTraceObjects;
import viewer.legends.CategoryUpdatedListener;
import viewer.zoomable.CoordPixelImage;
import viewer.zoomable.ModelTime;
import viewer.zoomable.ScrollableObject;
import viewer.zoomable.SearchResults;
import viewer.zoomable.SearchableView;
import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import drawable.Category;
import drawable.ColorAlpha;
import drawable.DrawObjects;
import drawable.TimeBoundingBox;

public class CanvasTimeline extends ScrollableObject implements SearchableView
{
	private TopologyManager    timelineManager;
	private BoundedRangeModel  y_model;

	private Frame              root_frame;
	private TimeBoundingBox    timeframe4imgs;   // TimeFrame for images[]

	private int                num_rows;
	private int                row_height;

	private Date               zero_time, init_time, final_time;

	final private TraceFormatBufferedFileReader reader;

	private class MyTopologyChangeListener implements TopologyChangeListener{
		@Override
		public void topologyChanged() {
			forceRedraw();
		}
	}

	private MyTopologyChangeListener topologyChangeListener = new MyTopologyChangeListener();

	// gets triggered if the visibility of an category is changed
	private CategoryUpdatedListener categoryVisibleListener = new CategoryUpdatedListener(){
		@Override
		public void categoryVisibilityChanged() {
			forceRedraw();
		}

		@Override
		public void categoryColorChanged() {
			forceRedraw();
		}
	};

	public CanvasTimeline( ModelTime           time_model,
			TraceFormatBufferedFileReader reader,
			BoundedRangeModel   yaxis_model,
			TopologyManager ytree)
	{
		super( time_model );

		this.reader = reader;
		timelineManager       = ytree;
		y_model         = yaxis_model;
		// timeframe4imgs to be initialized later in initializeAllOffImages()
		timeframe4imgs  = null;

		root_frame      = null;

		reader.getLegendModel().addVisibilityChangedListener(categoryVisibleListener);

		timelineManager.addTopologyChangedListener(topologyChangeListener);
	}

	public Dimension getMinimumSize()
	{
		int  min_view_height = 0;
		//  the width below is arbitary
		if ( Debug.isActive() )
			Debug.println( "CanvasTimeline: min_size = "
					+ "(0," + min_view_height + ")" );
		return new Dimension( 0, min_view_height );
	}

	public Dimension getMaximumSize()
	{
		if ( Debug.isActive() )
			Debug.println( "CanvasTimeline: max_size = "
					+ "(" + Short.MAX_VALUE
					+ "," + Short.MAX_VALUE + ")" );
		return new Dimension( Short.MAX_VALUE, Short.MAX_VALUE );
	}

	public int getJComponentHeight()
	{
		int rows_size = timelineManager.getRowCount() * timelineManager.getRowHeight();
		int view_size = y_model.getMaximum() - y_model.getMinimum() + 1;

		if ( view_size > rows_size )
			return view_size;
		else
			return rows_size;		
	}

	protected void initializeAllOffImages( final TimeBoundingBox imgs_times )
	{
		if ( Profile.isActive() )
			zero_time = new Date();

		if ( root_frame == null )
			root_frame  = (Frame) SwingUtilities.windowForComponent( this );
		if ( timeframe4imgs == null )
			timeframe4imgs = new TimeBoundingBox( imgs_times );

		Routines.setComponentAndChildrenCursors( root_frame, CustomCursor.Wait );

		num_rows    = timelineManager.getRowCount();
		row_height  = timelineManager.getRowHeight();

		if ( Profile.isActive() )
			init_time = new Date();

	}

	protected void finalizeAllOffImages( final TimeBoundingBox imgs_times )
	{        
		// Update the timeframe of all images
		timeframe4imgs.setEarliestTime( imgs_times.getEarliestTime() );
		timeframe4imgs.setLatestTime( imgs_times.getLatestTime() );
		Routines.setComponentAndChildrenCursors( root_frame,
				CustomCursor.Normal );

		if ( Profile.isActive() )
			final_time = new Date();
		if ( Profile.isActive() )
			Profile.println( "CanvasTimeline.finalizeAllOffImages(): "
					+ "init. time = "
					+ (init_time.getTime() - zero_time.getTime())
					+ " msec.,   total time = "
					+ (final_time.getTime() - zero_time.getTime())
					+ " msec." );
	}

	protected void drawOneOffImage(Image offImage, final TimeBoundingBox  timebounds )
	{
		if ( Debug.isActive() )
			Debug.println( "CanvasTimeline: drawOneOffImage()'s offImage = "
					+ offImage );
		if ( offImage == null ) {
			return;
		}
		// int offImage_width = visible_size.width * NumViewsPerImage;
		int        offImage_width  = offImage.getWidth( this );
		int        offImage_height = offImage.getHeight( this );
		Graphics2D offGraphics     = (Graphics2D) offImage.getGraphics();

		// Set RenderingHint to have MAX speed.
		offGraphics.setRenderingHint( RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_SPEED );

		// offGraphics.getClipBounds() returns null
		// offGraphics.setClip( 0, 0, getWidth()/NumImages, getHeight() );
		// Do the ruler labels in a small font that's black.
		// offGraphics.setPaint( BackgroundPaint );
		offGraphics.setPaint(
				(Color) Parameters.BACKGROUND_COLOR.toValue() );
		offGraphics.fillRect( 0, 0, offImage_width, offImage_height );

		int    irow;
		int    i_Y;

		CoordPixelImage coord_xform;  // local Coordinate Transform
		coord_xform = new CoordPixelImage( this, row_height, timebounds );

		// Set AntiAliasing OFF for all the horizontal and vertical lines
		offGraphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF );

		// Draw the center TimeLines.
		offGraphics.setColor( Color.cyan );
		for ( irow = 0 ; irow < num_rows ; irow++ ) {
			//  Select only non-expanded row
			if ( timelineManager.getType(irow) == TimelineType.TRACE  ) {
				i_Y = coord_xform.convertTimelineToPixel(irow );
				offGraphics.drawLine( 0, i_Y, offImage_width-1, i_Y );
			}
		}

		// Draw the image separator when in Debug or Profile mode
		if ( Debug.isActive() || Profile.isActive() ) {
			offGraphics.setColor( Color.gray );
			offGraphics.drawLine( 0, 0, 0, this.getHeight() );
		}

		// Set AntiAliasing from Parameters for all slanted lines
		offGraphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
				Parameters.ARROW_ANTIALIASING.toValue() );


		// Draw all drawables            
		//DrawObjects.drawArrow(offGraphics, coord_xform, new Epoch(4.5), new Epoch(2.0), 1, 2, new ColorAlpha(ColorAlpha.PINK));

		final Epoch vStartTime = new Epoch(timebounds.getEarliestTime());
		final Epoch vEndTime = new Epoch(timebounds.getLatestTime());

		for(int i=0; i < num_rows ; i++){			
			switch (timelineManager.getType(i)){
			case INNER_NODE:
				break;
			case STATISTIC:
				drawStatisticTimeline(i, timelineManager.getStatisticNodeForTimeline(i), offGraphics, vStartTime, vEndTime, coord_xform);
				break;
			case TRACE:
				drawTraceTimeline(i, timelineManager.getTraceReaderForTimeline(i), offGraphics, vStartTime, vEndTime, coord_xform);
				break;
			}
		}

		offGraphics.dispose();
	}   // endof drawOneOffImage()

	public void drawStatisticTimeline(
			int timeline,
			TopologyStatisticTreeNode node,  Graphics2D offGraphics,
			Epoch vStartTime, Epoch vEndTime, CoordPixelImage coord_xform)
	{
		final Epoch globalMinTime = getModelTime().getTimeGlobalMinimum();

		BufferedStatisticFileReader sReader = (BufferedStatisticFileReader) node.getStatisticSource();

		final String statName = node.getStatisticName();
		final StatisticsGroupDescription group = sReader.getGroup();

		double lastTime = vStartTime.getDouble();
		
		final Category cat = reader.getCategory(group, statName);
		final ColorAlpha color = cat.getColor();

		if(! cat.isVisible()){
			return;
		}

		final double maxValue = reader.getGlobalStatStats(group).getStatsForStatistic(statName).getGlobalMaxValue();
		final StatisticDescription statDesc =  group.getStatistic(statName);
		final int statNumber = statDesc.getNumberInGroup();
		
		final Enumeration<StatisticGroupEntry> entries = sReader.enumerateStatistics(
				vStartTime.add(getModelTime().getTimeGlobalMinimum()),
				vEndTime.add(getModelTime().getTimeGlobalMinimum()));

		while(entries.hasMoreElements()){
			StatisticGroupEntry entry = entries.nextElement();
			double value =  (entry.getNumeric(statNumber) / maxValue);
			final Epoch adaptedTime = entry.getEarliestTime().subtract(globalMinTime) ;

			DrawObjects.drawStatistic(offGraphics, coord_xform, 
					adaptedTime, lastTime, (float) value, color , timeline);

			lastTime = adaptedTime.getDouble();
		}
	}

	/**
	 * Draw a single timeline within the bounds.
	 */
	public void drawTraceTimeline(
			int timeline,
			BufferedTraceFileReader tr,  Graphics2D offGraphics,
			Epoch startTime, Epoch endTime, CoordPixelImage coord_xform
	)
	{
		final ReaderTraceElementEnumerator elements = tr.enumerateTraceEntry(true, 
				startTime.add(getModelTime().getTimeGlobalMinimum()), 
				endTime.add(getModelTime().getTimeGlobalMinimum())) ;
		
		while(elements.hasMoreElements()){		
			final int depth = elements.getNestingDepthOfNextElement() + 1;
			
			XMLTraceEntry entry = elements.nextElement();

			final Epoch globalMinTime = getModelTime().getTimeGlobalMinimum();		

			if(entry.getType() == TraceObjectType.EVENT){          
				final EventTraceEntry event = (EventTraceEntry) entry;

				final Category category = reader.getCategory(event);
				if(category.isVisible())
					DrawObjects.drawEvent(offGraphics, coord_xform, event, timeline, category.getColor(), globalMinTime);

			}else if(entry.getType() == TraceObjectType.STATE){
				final StateTraceEntry state = (StateTraceEntry) entry;
				final Category category = reader.getCategory(state);

				if(category.isVisible())
					DrawObjects.drawState(offGraphics, coord_xform, state , category.getColor(), 
							depth, timeline, globalMinTime);
			}
			
		}
	}

	public TraceObject getDrawableAt( 
			final Point local_click, 
			final TimeBoundingBox  vport_timeframe ) 
	{		 				
		final Epoch globalMinTime = getModelTime().getTimeGlobalMinimum();

		CoordPixelImage coord_xform;  // Local Coordinate Transform
		coord_xform = new CoordPixelImage( this, row_height, super.getTimeBoundsOfImages() );

		final double eventRadius = 2.0 / getViewPixelsPerUnitTime();

		final Epoch clickedTime =  globalMinTime.add(coord_xform.convertPixelToTime( local_click.x ));
		final int timeline       = coord_xform.convertPixelToTimeline( local_click.y);

		if( timeline <= 0 || timeline > timelineManager.getTimelineNumber() ){
			return null;
		}

		switch(timelineManager.getType(timeline)){
		case TRACE:
			final BufferedTraceFileReader treader = timelineManager.getTraceReaderForTimeline(timeline);
			XMLTraceEntry objMouse = treader.getTraceEntryClosestToTime(clickedTime);			

			if (objMouse.getType() == TraceObjectType.STATE){
				StateTraceEntry state = (StateTraceEntry) objMouse;				

				if(DrawObjects.getTimeDistance(clickedTime, state) != 0){
					// mouse is not inside the state.
					return null;
				}

				if (state.hasNestedTraceChildren()){					
					XMLTraceEntry best = objMouse;
					double dist = 0;

					while(dist == 0){
						// traverse nesting if necessary, and match events.

						if (best.getType() == TraceObjectType.STATE ){
							state = (StateTraceEntry) best;

							if (state.hasNestedTraceChildren()){		
								for(XMLTraceEntry child: state.getNestedTraceChildren()){
									dist = DrawObjects.getTimeDistance(clickedTime, child);
									if(child.getType() == TraceObjectType.EVENT ){
										if( dist < eventRadius){
											best = child;
										}									
									}
									if (dist == 0){
										// must be inside this one.
										best = child;
										break;
									}
								}
							}else{
								// no matching child
								break;
							}

						}
					} // while
					objMouse = best;
				}				

				if(objMouse.getType() == TraceObjectType.STATE){
					if(! reader.getCategoriesStates().get(objMouse.getName()).isVisible() ){
						return null;
					}
				}else{
					if(! reader.getCategoriesEvents().get(objMouse.getName()).isVisible() ){
						return null;
					}
				}				
			}else if(objMouse.getType() == TraceObjectType.EVENT){
				double distance = Math.abs(objMouse.getEarliestTime().subtract(clickedTime).getDouble());
				if( distance >= eventRadius){
					return null;
				}

				if(! reader.getCategoriesEvents().get(objMouse.getName()).isVisible() ){
					return null;
				}
			}

			//SimpleConsoleLogger.Debug("Mouse over " + objMouse.getName());

			return objMouse;
		case STATISTIC:
			final BufferedStatisticFileReader sreader = timelineManager.getStatisticReaderForTimeline(timeline);
			StatisticGroupEntry entry = sreader.getTraceEntryClosestToTime(clickedTime);
			int which = timelineManager.getStatisticNumberForTimeline(timeline);			
			return entry.createStatisticEntry(which);
		default:

		}

		return null;
	}

	public InfoDialog getPropertyAt( final Point local_click,	final TimeBoundingBox  vport_timeframe )
	{

		TraceObject obj = getDrawableAt(local_click, vport_timeframe);
		if( obj != null ){
			CoordPixelImage coord_xform;
			coord_xform = new CoordPixelImage( this, 0,
					this.getTimeBoundsOfImages() );
			Window          window;
			window = SwingUtilities.windowForComponent( this );
			return new InfoDialogForTraceObjects((Frame) window, 
					coord_xform.convertPixelToTime(local_click.x), obj);
		}

		return super.getTimePropertyAt( local_click );
	}
	
	@Override
	public SearchResults searchNextComponent(Epoch laterThan) {
		TraceObject minObject = null;
		// figure out the object with the smallest time over all timelines which is searchable
		Epoch minTime = reader.getGlobalMaxTime();
		int minTimeline = -1;

		for(int i=0; i < num_rows ; i++){			
			switch (timelineManager.getType(i)){
			case TRACE:
				BufferedTraceFileReader tr = timelineManager.getTraceReaderForTimeline(i);
				
				traceElementLoop: 
				for(XMLTraceEntry entry: tr.getTraceEntries()){
					if(entry.getLatestTime().compareTo(laterThan) > 0){
						if(entry.getType() == TraceObjectType.EVENT){
							Category cat = reader.getCategory((EventTraceEntry) entry);
							if(cat.isSearchable()){												
								if( entry.getEarliestTime().compareTo(minTime) < 0){							
									minObject = entry;
									minTime = entry.getEarliestTime();
									minTimeline = i;
								}
								// found one so break
								break;
							}							
						}else if(entry.getType() == TraceObjectType.STATE){
							StateTraceEntry state = (StateTraceEntry) entry;
							Category cat = reader.getCategory(state);
							// iterate through children if necessary:
														
							if(cat.isSearchable() && entry.getEarliestTime().compareTo(laterThan) > 0 ){												
								if( entry.getEarliestTime().compareTo(minTime) < 0){							
									minObject = entry;
									minTime = entry.getEarliestTime();
									minTimeline = i;
								}
								// the state is the one we are looking for.
								break;
							}
							if(state.hasNestedTraceChildren()){
								final Enumeration<XMLTraceEntry> children = state.childForwardEnumeration();
								while(children.hasMoreElements()){
									final XMLTraceEntry nestedChild = children.nextElement();
									
									if( nestedChild.getEarliestTime().compareTo(laterThan) <= 0 ){
										continue;
									}
									
									if( reader.getCategory(nestedChild).isSearchable()){												
										if( entry.getEarliestTime().compareTo(minTime) < 0){							
											minObject = nestedChild;
											minTime = nestedChild.getEarliestTime();
											minTimeline = i;
										}
										// found one so break
										break traceElementLoop;
									}	
								}
							}
						}else{
							throw new IllegalArgumentException("SearchNextComponent Invalid type " + entry.getType());
						}
					}
				}
				break;
			}
		}
		return new SearchResults(minTimeline, minObject);
	}

	@Override
	public SearchResults searchPreviousComponent(Epoch earlierThan) {

		TraceObject minObject = null;
		Epoch maxTime = Epoch.ZERO;
		int minTimeline = -1;

		for(int i=0; i < num_rows ; i++){			
			switch (timelineManager.getType(i)){
			case TRACE:
				BufferedTraceFileReader tr = timelineManager.getTraceReaderForTimeline(i);
				
				traceElementLoop: 
				for(int te=tr.getTraceEntries().size() -1 ; te >= 0 ; te-- ){
					XMLTraceEntry entry = tr.getTraceEntries().get(te);
					
					if(entry.getEarliestTime().compareTo(earlierThan) < 0){
						if(entry.getType() == TraceObjectType.EVENT){
							Category cat = reader.getCategory((EventTraceEntry) entry);
							if(cat.isSearchable()){												
								if( entry.getLatestTime().compareTo(maxTime) > 0){							
									minObject = entry;
									maxTime = entry.getLatestTime();
									minTimeline = i;
								}
								// found one so break
								break;
							}							
						}else if(entry.getType() == TraceObjectType.STATE){
							StateTraceEntry state = (StateTraceEntry) entry;
							Category cat = reader.getCategory(state);
							// iterate through children if necessary:
														
							if(cat.isSearchable() && entry.getLatestTime().compareTo(earlierThan) < 0 ){												
								if( entry.getLatestTime().compareTo(maxTime) > 0){							
									minObject = entry;
									maxTime = entry.getLatestTime();
									minTimeline = i;
								}
								// the state is the one we are looking for.
								break;
							}
							if(state.hasNestedTraceChildren()){
								final Enumeration<XMLTraceEntry> children = state.childBackwardEnumeration();
								while(children.hasMoreElements()){
									final XMLTraceEntry nestedChild = children.nextElement();
									
									if( nestedChild.getLatestTime().compareTo(earlierThan) >= 0 ){
										continue;
									}
									
									if( reader.getCategory(nestedChild).isSearchable()){												
										if( entry.getLatestTime().compareTo(maxTime) > 0){							
											minObject = nestedChild;
											maxTime = nestedChild.getLatestTime();
											minTimeline = i;
										}
										// found one so break
										break traceElementLoop;
									}	
								}
							}
						}else{
							throw new IllegalArgumentException("SearchNextComponent Invalid type " + entry.getType());
						}
					}
				}
				break;
			}
		}
		return new SearchResults(minTimeline, minObject);
	}
}

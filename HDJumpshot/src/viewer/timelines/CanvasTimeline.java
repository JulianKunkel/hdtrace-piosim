/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.timelines;

import hdTraceInput.BufferedStatisticFileReader;
import hdTraceInput.BufferedTraceFileReader;
import hdTraceInput.TraceFormatBufferedFileReader;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.util.Date;

import javax.swing.BoundedRangeModel;
import javax.swing.SwingUtilities;

import viewer.common.CustomCursor;
import viewer.common.Parameters;
import viewer.common.Routines;
import viewer.histogram.StatlineDialog;
import viewer.legends.CategoryUpdatedListener;
import viewer.topology.TopologyChangeListener;
import viewer.topology.TopologyManager;
import viewer.topology.TopologyStatisticTreeNode;
import viewer.zoomable.ActionTimelineRestore;
import viewer.zoomable.CoordPixelImage;
import viewer.zoomable.Debug;
import viewer.zoomable.InfoDialog;
import viewer.zoomable.InitializableDialog;
import viewer.zoomable.ModelTime;
import viewer.zoomable.Profile;
import viewer.zoomable.ScrollableObject;
import viewer.zoomable.SearchPanel;
import viewer.zoomable.SearchableView;
import viewer.zoomable.SummarizableView;
import viewer.zoomable.ViewportTimeYaxis;
import base.drawable.Category;
import base.drawable.ColorAlpha;
import base.drawable.DrawObjects;
import base.drawable.TimeBoundingBox;
import base.statistics.BufForTimeAveBoxes;
import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticGroupEntry;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;

public class CanvasTimeline extends ScrollableObject
implements SearchableView, SummarizableView
{
	private TopologyManager    timelineManager;
	private BoundedRangeModel  y_model;

	private Frame              root_frame;
	private TimeBoundingBox    timeframe4imgs;   // TimeFrame for images[]

	private int                num_rows;
	private int                row_height;

	private Date               zero_time, init_time, final_time;
	private ActionTimelineRestore restore;
	private ViewportTimeYaxis  canvas_viewport;
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
	
	public void setRequired(ActionTimelineRestore restore,
			ViewportTimeYaxis  canvas_viewport){
		this.canvas_viewport = canvas_viewport;
		this.restore = restore;
	}

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

		Routines.setComponentAndChildrenCursors( root_frame,
				CustomCursor.Wait );

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

	protected void drawOneOffImage(       Image            offImage,
			final TimeBoundingBox  timebounds )
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


		for(int i=0; i < num_rows ; i++){			
			switch (timelineManager.getType(i)){
			case SPACER_NODE:
				break;
			case INNER_NODE:
				break;
			case STATISTIC:
				drawStatisticTimeline(i, timelineManager.getStatisticNodeForTimeline(i),	offGraphics, timebounds, coord_xform);
				break;
			case TRACE:
				drawTraceTimeline(i, timelineManager.getTraceReaderForTimeline(i), offGraphics, timebounds, coord_xform);
				break;
			}
				
				
		}
		
		offGraphics.dispose();
	}   // endof drawOneOffImage()

	public void drawStatisticTimeline(
			int timeline,
			TopologyStatisticTreeNode node,  Graphics2D offGraphics,
			final TimeBoundingBox  timebounds, CoordPixelImage coord_xform)
	{
		final Epoch globalMinTime = getModelTime().getTimeGlobalMinimum();
		
		BufferedStatisticFileReader sReader = (BufferedStatisticFileReader) node.getStatisticSource();
		
		final String statName = node.getStatisticName();
		final ExternalStatisticsGroup group = sReader.getGroup();
		
		double lastTime = timebounds.getEarliestTime();
		
		final Category cat = reader.getCategory(group, statName);
		final ColorAlpha color = cat.getColor();
		
		if(! cat.isVisible()){
			return;
		}

		final double maxValue = reader.getGlobalStatStats(group).getStatsForStatistic(statName).getGlobalMaxValue();
		final StatisticDescription statDesc =  group.getStatistic(statName);
		final int statNumber = statDesc.getNumberInGroup();	

		for(StatisticGroupEntry entry: sReader.getStatEntries()){
			double value =  (entry.getNumeric(statNumber) / maxValue);
			final Epoch adaptedTime = entry.getTimeStamp().subtract(globalMinTime) ;
			
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
			TimeBoundingBox  timebounds, CoordPixelImage coord_xform
	)
	{
		for(XMLTraceEntry entry: tr.getTraceEntries()){
			// TODO only read necessary elements, bin search, also use index, partial file load ...
			drawTraceElementRecursively(timeline, 0, timebounds, entry, tr, offGraphics, coord_xform);
		}
	}

	/**
	 * Draw an trace object, if it has nested elements, draw them also
	 * @param depth 0 means root element
	 */
	private void drawTraceElementRecursively(
			int timeline,
			int depth,
			TimeBoundingBox  timebounds,
			TraceObject entry, 
			BufferedTraceFileReader tr, 
			Graphics2D offGraphics, 
			CoordPixelImage coord_xform)
	{
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

			if(state.hasNestedTraceChildren()){
				for(XMLTraceEntry child: state.getNestedTraceChildren()){
					drawTraceElementRecursively(timeline, depth +1, timebounds, child, tr, offGraphics, coord_xform);
				}
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
				double distance = Math.abs(objMouse.getTimeStamp().subtract(clickedTime).getDouble());
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



	public Rectangle localRectangleForDrawable(int timeline, int nestingDepth, Epoch startTime, Epoch endTime)
	{
		CoordPixelImage       coord_xform;
		Rectangle             local_rect;
		int                   xloc, yloc, width, height;
		// local_rect is created with CanvasTimeline's pixel coordinate system
		coord_xform = new CoordPixelImage( this, row_height,
				super.getTimeBoundsOfImages() );
		xloc   = coord_xform.convertTimeToPixel( startTime.getDouble() );
		width  = coord_xform.convertTimeToPixel( endTime.getDouble() )
		- xloc;

		yloc   = coord_xform.convertTimelineToPixel( timeline );
		height = coord_xform.getTimelineHeight();
		local_rect = new Rectangle( xloc, yloc, width, height );
		return local_rect;
	}

	// NEW search starting from the specified time
	public SearchPanel searchPreviousComponent( double searching_time )
	{
		//   Drawable  dobj = tree_search.previousDrawable( searching_time );
		//   if ( dobj != null )
		//       return this.createInfoPanelForDrawable( dobj );
		//   else
		return null;
	}

	// CONTINUING search
	public SearchPanel searchPreviousComponent()
	{
		//Drawable  dobj = tree_search.previousDrawable();
		//if ( dobj != null )
		//    return this.createInfoPanelForDrawable( dobj );
		//else
		// TODO SEARCH
		return null;
	}

	// NEW search starting from the specified time
	public SearchPanel searchNextComponent( double searching_time )
	{
		//TODO SEARCH
		//Drawable  dobj = tree_search.nextDrawable( searching_time );
		//if ( dobj != null )
		//            return this.createInfoPanelForDrawable( dobj );
		//      else
		return null;
	}

	// CONTINUING search
	public SearchPanel searchNextComponent()
	{
		//   Drawable  dobj = tree_search.nextDrawable();
		//   if ( dobj != null )
		//       return this.createInfoPanelForDrawable( dobj );
		//   else
		return null;
	}

	// Interface for SummarizableView
	public InitializableDialog createSummary( final Dialog          dialog,
			final TimeBoundingBox timebox )
	{
		BufForTimeAveBoxes  buf4statboxes = null;
		//TODO 

		//buf4statboxes  = tree_search.createBufForTimeAveBoxes( timebox );
		// System.out.println( "Statistics = " + buf4statboxes );
		return new StatlineDialog( dialog, timebox, buf4statboxes, restore, canvas_viewport );
	}
}

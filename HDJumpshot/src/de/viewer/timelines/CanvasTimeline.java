
/** Version Control Information $Id: CanvasTimeline.java 420 2009-06-18 14:46:41Z kunkel $
 * @lastmodified    $Date: 2009-06-18 16:46:41 +0200 (Do, 18. Jun 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 420 $ 
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

package de.viewer.timelines;


import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.regex.Pattern;

import javax.swing.BoundedRangeModel;
import javax.swing.SwingUtilities;
import javax.xml.datatype.Duration;

import de.arrow.Arrow;
import de.drawable.Category;
import de.drawable.CategoryStatistic;
import de.drawable.DrawObjects;
import de.drawable.TimeBoundingBox;
import de.drawable.VisualizedObjectType;
import de.drawable.CategoryStatistic.Scaling;
import de.hd.pvs.TraceFormat.ITracableObject;
import de.hd.pvs.TraceFormat.SimpleConsoleLogger;
import de.hd.pvs.TraceFormat.TracableObjectType;
import de.hd.pvs.TraceFormat.relation.RelationEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.trace.ForwardStateEnumeration;
import de.hd.pvs.TraceFormat.trace.IEventTraceEntry;
import de.hd.pvs.TraceFormat.trace.IStateTraceEntry;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.TraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hdTraceInput.BufferedTraceFileReader;
import de.hdTraceInput.IBufferedStatisticsReader;
import de.hdTraceInput.ReaderTraceElementEnumerator;
import de.hdTraceInput.StatisticStatistics;
import de.hdTraceInput.TraceFormatBufferedFileReader;
import de.topology.GlobalStatisticStatsPerGroup;
import de.topology.MinMax;
import de.topology.TopologyChangeListener;
import de.topology.TopologyManager;
import de.topology.TopologyRelationExpandedTreeNode;
import de.topology.TopologyRelationTreeNode;
import de.topology.TopologyStatisticTreeNode;
import de.topology.TopologyTreeNode;
import de.viewer.common.Debug;
import de.viewer.common.HeatMap;
import de.viewer.common.Parameters;
import de.viewer.common.Profile;
import de.viewer.dialog.InfoDialog;
import de.viewer.dialog.InfoDialogForStatisticEntries;
import de.viewer.dialog.traceEntries.InfoDialogForTraceEntries;
import de.viewer.legends.CategoryUpdatedListener;
import de.viewer.timelines.FilterTokenInterface.FilterExpression;
import de.viewer.zoomable.CoordPixelImage;
import de.viewer.zoomable.ScrollbarTimeModel;
import de.viewer.zoomable.SearchResults;
import de.viewer.zoomable.SearchableView;
import de.viewer.zoomable.ViewportTime;

public class CanvasTimeline extends ScrollableTimeline implements SearchableView
{
	private final TimelineFrame parentFrame;
	
	private static final long serialVersionUID = 1424310776190717432L;

	final private TraceFormatBufferedFileReader reader;

	private class MyTopologyChangeListener implements TopologyChangeListener{
		@Override
		public void topologyChanged() {			
			redrawIfAutoRedraw();
		}
	}

	private MyTopologyChangeListener topologyChangeListener = new MyTopologyChangeListener();
	
	public class HeatMapListener{
		public boolean applyFilter(String text){
			if(text.length() == 0){
				currentHeatMap = null;
				redrawIfAutoRedraw();
				
				return true;
			}
			
			currentHeatMap = HeatMap.createHeatMap(text);
			if(currentHeatMap == null){
				return false;
			}
			redrawIfAutoRedraw();
			return true;
		}
	}
	
	private HeatMapListener heatMapListener = new HeatMapListener();
	
	private HeatMap        currentHeatMap = null;
	
	public HeatMapListener getHeatMapListener() {
		return heatMapListener;
	}
	

	/**
	 * Filter the events etc. based on a user provided string.
	 * @return true if the filter is valid, otherwise return false
	 */

	private FilterExpression    currentFilter = null;
	
	public FilterExpression getCurrentFilter() {
		return currentFilter;
	}
	
	public boolean applyFilter(String text){
		// null strings reset the filter
		if(text.length() == 0){
			currentFilter = null;
			redrawIfAutoRedraw();

			return true;
		}

		try{
			// replace whitespace
			currentFilter = new FilterExpression(text.replace(" ", "") + " ");				
			redrawIfAutoRedraw();
			return true;				
		}catch (IllegalArgumentException e){
			System.err.println("Error, invalid filter: " + text + " " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Use the heatmap to calculate the color
	 * @return
	 */
	public Color determineColor(Color color, ITraceEntry obj){
		if(currentHeatMap == null){
			return color;			
		}
		// otherwise check if the obj matches and compute the heat value!
		return currentHeatMap.determineColor(color, obj);
	}
	
	
	// gets triggered if the visibility of an category is changed
	private CategoryUpdatedListener categoryVisibleListener = new CategoryUpdatedListener(){
		@Override
		public void categoryVisibilityWasModified() {
			getTopologyManager().fireTopologyChanged();
			redrawIfAutoRedraw();
		}

		@Override
		public void categoryAttributesWereModified() {
			redrawIfAutoRedraw();
		}

		@Override
		public void categoryVisibilityModified(Category category, boolean value) {
			if(category.getTopologyType() == VisualizedObjectType.STATISTIC){
				final CategoryStatistic statCat = (CategoryStatistic) category;

				getTopologyManager().setStatisticCategoryVisiblity(statCat, value);
			}
		}
	};


	public CanvasTimeline( ScrollbarTimeModel scrollbarTimeModel,
			ViewportTime viewport,
			TraceFormatBufferedFileReader reader,
			BoundedRangeModel   yaxis_model,
			TopologyManager topologyManager,
			TimelineFrame parentFrame)
	{
		super( scrollbarTimeModel, viewport, yaxis_model, topologyManager);

		this.parentFrame = parentFrame;
		
		this.reader = reader;

		reader.getLegendTraceModel().addCategoryUpdateListener(categoryVisibleListener);
		reader.getLegendStatisticModel().addCategoryUpdateListener(categoryVisibleListener);

		topologyManager.addTopologyChangedListener(topologyChangeListener); 
	}

	@Override
	final protected void drawOneImageInBackground(Image offImage, final TimeBoundingBox  timebounds )
	{		
		final int num_rows   = getRowCount();
		final int row_height = getRowHeight();
		final TopologyManager topologyManager = getTopologyManager();

		if ( Debug.isActive() )
			Debug.println( "CanvasTimeline: drawOneOffImage()'s offImage = "
					+ offImage );
		// check if the timebounds are valid:
		if ( offImage == null || timebounds.getLatestTime() <= 0 || 
				timebounds.getEarliestTime() >= getModelTime().getGlobalMaximum().getDouble()) {
			return;
		}
		final long startTime = System.currentTimeMillis();

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
			if ( topologyManager.getType(irow) == TimelineType.TRACE  ) {
				i_Y = coord_xform.convertTimelineToPixel(irow ) + row_height / 2;
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


		long drawedStatistics = 0;
		long drawedTraceElements = 0;

		// Draw all drawables
		final Epoch vStartTime = new Epoch(timebounds.getEarliestTime() );
		final Epoch vEndTime = new Epoch(timebounds.getLatestTime() );		

		// a heat map must been drawn two times to determine the proper maximum.
		if(currentHeatMap != null){
			currentHeatMap.resetHeatMapColors();
			
			// run two times, determine maximum, statistics timeline must not be drawn.

			for(int i=0; i < num_rows ; i++){			
				switch (topologyManager.getType(i)){
				case INNER_NODE:
					break;
				case TRACE:
					drawedTraceElements += drawTraceTimeline(i, topologyManager.getTraceReaderForTimeline(i), offGraphics, vStartTime, vEndTime, coord_xform);
					break;
				case RELATION:{
					if(! topologyManager.getTree().isExpanded(i)){
						// problem, end time sorting of relations => no drawing is possible.
						final TopologyRelationTreeNode node = ((TopologyRelationTreeNode) topologyManager.getTreeNodeForTimeline(i));
						drawedTraceElements += drawRelationTimeline(i, 
								node, offGraphics, vStartTime, vEndTime, coord_xform);
					}
					break;
				}case RELATION_EXPANDED:
					final TopologyRelationExpandedTreeNode node = ((TopologyRelationExpandedTreeNode) topologyManager.getTreeNodeForTimeline(i));
					drawedTraceElements += drawRelationTimeline(i, 
							node, offGraphics, vStartTime, vEndTime, coord_xform);
				}
			}

			currentHeatMap.firstIterationDone();
		}
		
		for(int i=0; i < num_rows ; i++){			
			switch (topologyManager.getType(i)){
			case INNER_NODE:
				break;
			case STATISTIC:
				drawedStatistics += drawStatisticTimeline(i, topologyManager.getStatisticNodeForTimeline(i), offGraphics, vStartTime, vEndTime, coord_xform);
				break;
			case TRACE:
				drawedTraceElements += drawTraceTimeline(i, topologyManager.getTraceReaderForTimeline(i), offGraphics, vStartTime, vEndTime, coord_xform);
				break;
			case RELATION:{
				if(! topologyManager.getTree().isExpanded(i)){
					// problem, end time sorting of relations => no drawing is possible.
					final TopologyRelationTreeNode node = ((TopologyRelationTreeNode) topologyManager.getTreeNodeForTimeline(i));
					drawedTraceElements += drawRelationTimeline(i, 
							node, offGraphics, vStartTime, vEndTime, coord_xform);
				}
				break;
			}case RELATION_EXPANDED:
				final TopologyRelationExpandedTreeNode node = ((TopologyRelationExpandedTreeNode) topologyManager.getTreeNodeForTimeline(i));
				drawedTraceElements += drawRelationTimeline(i, 
						node, offGraphics, vStartTime, vEndTime, coord_xform);
			}
		}

		final int arrowsCnt = drawArrows( offGraphics,	vStartTime,  vEndTime, coord_xform);

		if(SimpleConsoleLogger.isDebugEverything()){			
			SimpleConsoleLogger.DebugWithStackTrace( (System.currentTimeMillis() - startTime) +  "ms Draw Canvas [t] "  
					+ timebounds.getEarliestTime() +" - " + timebounds.getLatestTime()  
					+	" drawn trace: " + drawedTraceElements +" stat:"  +	drawedStatistics  + " arrows: "+ arrowsCnt ,
					2);
		}
	}   // endof drawOneOffImage()

	/**
	 * Draw all the arrows.
	 */
	private int drawArrows( Graphics2D offGraphics,	Epoch vStartTime, Epoch vEndTime, CoordPixelImage coord_xform){
		int cnt = 0;		

		final Epoch globalMinTime = getModelTime().getGlobalMinimum();
		final TopologyManager topologyManager = getTopologyManager();

		final Enumeration<Arrow> arrowEnum = reader.getArrowManager().getArrowEnumeratorVisible(
				vStartTime.add(globalMinTime), 
				vEndTime.add(globalMinTime));

		while(arrowEnum.hasMoreElements()){
			final Arrow arrow = arrowEnum.nextElement();

			cnt++;

			final Integer startTimeline = topologyManager.getTimelineForTreeNode(arrow.getStartTreeNode());
			final Integer endTimeline = topologyManager.getTimelineForTreeNode(arrow.getEndTreeNode());
			
			if(startTimeline == null || endTimeline == null){
				continue;
			}			

			DrawObjects.drawArrow(offGraphics, coord_xform, 
					arrow.getStartTime().subtract(globalMinTime),
					arrow.getEndTime().subtract(globalMinTime), 					
					startTimeline, 
					endTimeline, 
					arrow.getCategory().getColor());
		}


		return cnt;
	}


	public int drawStatisticTimeline(
			int timeline,
			TopologyStatisticTreeNode node,  Graphics2D offGraphics,
			Epoch vStartTime, Epoch vEndTime, CoordPixelImage coord_xform)
	{
		final Epoch globalMinTime = getModelTime().getGlobalMinimum();

		final IBufferedStatisticsReader sReader = (IBufferedStatisticsReader) node.getStatisticSource();

		final StatisticsGroupDescription groupDescr = sReader.getGroup();
		final StatisticsDescription desc = node.getStatisticDescription();

		final CategoryStatistic cat = reader.getCategory(desc);

		final int statNumberInGroup = node.getNumberInGroup();
		
		if(! cat.isVisible()){
			return 0;
		}

		final Color color = cat.getColor();
		final Color backGroundColor;

		if(color.getBlue() + color.getRed() + color.getGreen() > 120){
			//make it darker:
			backGroundColor = new Color(color.getRed()/2, color.getGreen()/2, color.getBlue()/2,
					color.getAlpha());
		}else{ //make it brighter, take extra care for real black !
			backGroundColor = new Color((color.getRed()+5)*2, (color.getGreen()+5)*2, (color.getBlue()+5)*2,
					color.getAlpha());
		}

		double minTime = sReader.getMinTime().subtract(globalMinTime).getDouble();
		double maxTime = sReader.getMaxTime().subtract(globalMinTime).getDouble();

		if(minTime < vStartTime.getDouble()){
			minTime = vStartTime.getDouble();
		}

		if(maxTime > vEndTime.getDouble()){
			maxTime = vEndTime.getDouble();
		}

		if(minTime >= maxTime){
			return 0;
		}
		DrawObjects.drawStatisticBackground(offGraphics, coord_xform, minTime, maxTime, backGroundColor, color, timeline);

		int drawedStatistics = 0;

		/** 
		 * How do we tread the maximum value:
		 */
		double maxValue;
		double minValue;
		final Scaling scale = cat.getScaling();
		final StatisticStatistics statStat = sReader.getStatisticsFor(statNumberInGroup);
		final GlobalStatisticStatsPerGroup statsPerGroup =  reader.getGlobalStatStats(groupDescr);

		switch( cat.getMaxAdjustment()){
		case HUNDRED:
			maxValue = 100;
			break;
		case ONE:{
			maxValue = 1;
			break;
		}case GLOBAL_MAX:{
			maxValue = statsPerGroup.getStatsForStatistic(desc).getMaxValue();
			break;
		}case GLOBAL_GROUP_MAX:{
			final MinMax myGroupingStat = statsPerGroup.getStatsForStatisticGrouping(desc.getGrouping());
			if(myGroupingStat == null){
				// use global value
				maxValue = statsPerGroup.getStatsForStatistic(desc).getMaxValue();
				break;
			}else{
				// use grouping 
				maxValue = myGroupingStat.getMaxValue();
				break;
			}
		}default:
			maxValue = statStat.getMaxValue();
		}

		switch(cat.getMinAdjustment()){
		case GLOBAL_GROUP_MIN:{
			final MinMax myGroupingStat = statsPerGroup.getStatsForStatisticGrouping(desc.getGrouping());
			if(myGroupingStat == null){
				// use global value
				minValue = statsPerGroup.getStatsForStatistic(desc).getMinValue();
				break;
			}else{
				// use grouping 
				minValue = myGroupingStat.getMinValue();
				break;
			}
		}case GLOBAL_MIN:
			minValue = reader.getGlobalStatStats(groupDescr).getStatsForStatistic(desc).getMinValue();
			break;
		case TIMELINE_MIN:
			minValue = statStat.getMinValue();
			break;
		default:
			minValue = 0.0;
		}

		maxValue = maxValue - minValue;

		if(scale == Scaling.LOGARITHMIC){
			maxValue = Math.log10(maxValue);
		}


		final int statNumber = desc.getNumberInGroup();

		final Enumeration<StatisticsGroupEntry> entries = sReader.enumerateStatistics( vStartTime.add(getModelTime().getGlobalMinimum()),	vEndTime.add(getModelTime().getGlobalMinimum()));

		// aggregate overlapping stuff to form a median.
		BigDecimal aggregatedValue = null;
		// number of values we aggregate due to overlap
		double lastDuration = 0;
		
		Epoch aggregatedTimes = null;
		
		// The old value
		double lastValue = -1;
		
		// where does the last processed entry start and end.		
		int lastStartX = -1;
		
		boolean lastIsOnePixel = false;

		// minimum and maximum heights
		int minAggregateY = 100000;
		int maxAggregateY = 0;
		
		// variables valid for the whole computation
		// height of the timeline
		final int maxHeight = (coord_xform.getTimelineHeight() );
		// start position of the timeline
		final int y1   = coord_xform.convertTimelineToPixel( timeline + 1 );
		
		
		final boolean drawHeatMap;
		
		// check for the heatmap.
		if(currentHeatMap != null && currentHeatMap.isSingleAttributeWithName(desc.getName())){
			// draw a heatmap for this line!
			drawHeatMap = true;
		}else{
			drawHeatMap = false;
		}
		
		// the colors used to print min/max
		final Color minColor = new  Color(backGroundColor.getBlue(), backGroundColor.getGreen(), backGroundColor.getRed() );
		//final Color maxColor = new  Color(backGroundColor.getGreen(), backGroundColor.getRed(), backGroundColor.getBlue());

		// walk through all entries and compute values.
		while(entries.hasMoreElements()){
			final StatisticsGroupEntry entry = entries.nextElement();
			
			if(getCurrentFilter() != null){
				// todo this workaround is not fast, but works
				if(! getCurrentFilter().matches(entry.createStatisticEntry(statNumber))){
					continue;
				}
			}
			
			double value;
			final double input = entry.getNumeric(statNumber);
			switch(scale){
			case DECIMAL:
				value =  ((input - minValue) / maxValue);
				break;
			case LOGARITHMIC:
				value =  Math.log10((input - minValue))/ maxValue;
				break;
			default:
				value = 0;
			}

			if (value < 0){
				value = 0;
			}else if (value > 1.0){
				value = 1.0f;
			}

			Epoch endTime = entry.getLatestTime().subtract(globalMinTime) ;
			final Epoch startTime = entry.getEarliestTime().subtract(globalMinTime);	
			final Epoch durationE = endTime.subtract(startTime);
			double duration = durationE.getDouble();
			

			// where does the current value start.
			int x1 = coord_xform.convertTimeToPixel( startTime.getDouble() );
			// where does the current value end.
			int x2 =  coord_xform.convertTimeToPixel( endTime.getDouble() );
			
			final double currentValue = value;			

			if(lastIsOnePixel && lastStartX == x1){
				
				if(lastStartX < x2){
					// draw the new value, this part could be right of the overlapping area
					
					if(!drawHeatMap){
						offGraphics.fillRect( lastStartX, y1 - (int) (maxHeight * currentValue), x2-lastStartX +1, (int) (maxHeight * currentValue) );
					}else{
						// for a heatmap change the behavior
						offGraphics.setColor(currentHeatMap.determineColor((float) currentValue));
						offGraphics.fillRect( lastStartX, y1 - maxHeight, x2-lastStartX +1, maxHeight );
					}
					
				    // we are multiple pixel width => account duration properly, the next pixel will start with a different time.
					endTime = new Epoch(coord_xform.convertPixelToTime(x1 + 1));
					duration = endTime.subtract(startTime).getDouble();
					
					aggregatedTimes.add(duration);
				}// else lastStartX == x1
				else{
					// this is a bit fuzzy in case the lastStartX < x2...
					aggregatedTimes.add(endTime.subtract(startTime));
				}
				
				// draw overlapping area
				if (aggregatedValue != null){
					aggregatedValue = aggregatedValue.add(new BigDecimal(currentValue * duration));											
				}else{
					// we could multiply the lastValue with the 
					aggregatedValue = new BigDecimal(currentValue * duration  + lastValue * lastDuration);					
				}
				
				
				// draw the current aggregate, value depends on start / end time.
				final double aggregateVal = aggregatedValue.doubleValue() /  endTime.subtract(aggregatedTimes).getDouble();
				
				final int valueHeight = (int) (maxHeight * currentValue);
				if(valueHeight < minAggregateY){
					minAggregateY = valueHeight;
				}
				
				if(valueHeight > maxAggregateY){
					maxAggregateY = valueHeight;
				}

				
				final int curHeight = (int) (maxHeight * aggregateVal);
				
				if(! drawHeatMap){ // now heatmap draw minima and maxima
					// draw the line:				
					offGraphics.fillRect( x1, y1 - curHeight, 1, curHeight );			

					// draw min / max if necessary

					if (minAggregateY < valueHeight){
						offGraphics.setColor(minColor);
						offGraphics.fillRect(x1, y1 - minAggregateY - 1, 1, 3);
					}
					if(maxAggregateY > valueHeight){
						offGraphics.setColor(minColor);
						offGraphics.fillRect(x1, y1 - maxAggregateY - 1, 1, 3);
					}

					offGraphics.setColor(color);
				}
				
			}else{
				// it does not overlap (at least completely)
				// draw the rectangle
				final int curHeight = (int) (maxHeight * currentValue);
				
				if(! drawHeatMap){
					offGraphics.fillRect( x1, y1 - curHeight, x2-x1 +1, curHeight);
				}else{
					// for a heatmap change the behavior, draw full height
					offGraphics.setColor(currentHeatMap.determineColor((float) currentValue));
					offGraphics.fillRect( x1, y1 - maxHeight, x2-x1 +1, maxHeight);
				}
				aggregatedTimes = durationE;
				minAggregateY = curHeight;
				maxAggregateY = curHeight;
				aggregatedValue = null;
			} 
			
			drawedStatistics++;
			
			lastIsOnePixel = (x2 == x1);
			lastStartX = x1;
			lastValue = currentValue;
			lastDuration = duration;
		}
				
		
		if( cat.isShowAverageLine() && ! drawHeatMap){ // draw average line... TODO cleanup the whole code here...
			Color avgLineColor = backGroundColor.brighter();

			int x1   = coord_xform.convertTimeToPixel( vStartTime.getDouble() );
			int x2   = coord_xform.convertTimeToPixel( vEndTime.getDouble() );

			int height = (coord_xform.getTimelineHeight() );

			// Fill the color of the rectangle

			offGraphics.setColor(avgLineColor);

			double adaption = 0;
			switch(scale){
			case DECIMAL:
				adaption =  ((statStat.getAverageValue() - minValue) / maxValue);
				break;
			case LOGARITHMIC:
				adaption =  Math.log10((statStat.getAverageValue() - minValue))/ maxValue;
				break;
			}

			adaption *= height;		
			offGraphics.drawLine(x1, y1 - (int) adaption, x2, (int) y1 - (int) adaption);
		}       

		return drawedStatistics;
	}

	/**
	 * Draw a single timeline within the bounds.
	 */
	public int drawTraceTimeline(
			int timeline,
			BufferedTraceFileReader tr,  Graphics2D offGraphics,
			Epoch startTime, Epoch endTime, CoordPixelImage coord_xform
	)
	{
		final ReaderTraceElementEnumerator elements = tr.enumerateTraceEntries(false, 
				startTime.add(getModelTime().getGlobalMinimum()), 
				endTime.add(getModelTime().getGlobalMinimum())) ;

		int drawedTraceObjects = 0;

		while(elements.hasMoreElements()){
			drawedTraceObjects++;
			ITraceEntry tentry = elements.nextElement();

			if(getCurrentFilter() != null){
				if(! getCurrentFilter().matches(tentry)){
					continue;
				}
			}
			
			final Epoch globalMinTime = getModelTime().getGlobalMinimum();

			if(tentry.getType() == TracableObjectType.EVENT){          
				final IEventTraceEntry event = (IEventTraceEntry) tentry;

				final Category tcategory = reader.getCategory(event);
				if(tcategory.isVisible())
					DrawObjects.drawEvent(offGraphics, coord_xform,  event, timeline,  determineColor(tcategory.getColor(), tentry), globalMinTime);

			}else if(tentry.getType() == TracableObjectType.STATE){
				final IStateTraceEntry rstate = (IStateTraceEntry) tentry;
				
				final Category tcategory = reader.getCategory(rstate);

				if(! tcategory.isVisible()){
					continue;
				}
				DrawObjects.drawState(offGraphics, tcategory.getName(),  coord_xform, rstate , determineColor(tcategory.getColor(), rstate), 
							0, timeline, globalMinTime);
			    
				if(! parentFrame.isProcessNested()) continue;
			    
			    final ForwardStateEnumeration stateEnum = rstate.childForwardEnumeration();

				while(stateEnum.hasMoreElements()){
					drawedTraceObjects++;

					final int depth = stateEnum.getNestingDepthOfNextElement();
					
					// skip nested elements if necessary
					
					final ITraceEntry entry = stateEnum.nextElement();

					if(getCurrentFilter() != null){
						if(! getCurrentFilter().matches(entry)){
							continue;
						}
					}
					
					if(entry.getType() == TracableObjectType.EVENT){          
						final IEventTraceEntry event = (IEventTraceEntry) entry;

						final Category category = reader.getCategory(event);
						if(category.isVisible())
							DrawObjects.drawEvent(offGraphics, coord_xform,  event, timeline, determineColor(category.getColor(), entry), globalMinTime);

					}else if(entry.getType() == TracableObjectType.STATE){
						final IStateTraceEntry state = (IStateTraceEntry) entry;
						final Category category = reader.getCategory(state);

						if(category.isVisible())
							DrawObjects.drawState(offGraphics,category.getName(),  coord_xform, state , determineColor(category.getColor(), entry), depth, timeline, globalMinTime);
					}
				}
			}

		}

		return drawedTraceObjects;
	}


	/**
	 * Draw a single timeline within the bounds.
	 */
	public int drawRelationTimeline(
			int timeline,
			TopologyRelationTreeNode node,
			Graphics2D offGraphics,
			Epoch startTime, Epoch endTime, CoordPixelImage coord_xform)	
	{
		
		final Enumeration<RelationEntry> elements = node.enumerateEntries(
				startTime.add(getModelTime().getGlobalMinimum()), 
				endTime.add(getModelTime().getGlobalMinimum()));

		int drawedTraceObjects = 0;

		final Epoch globalMinTime = getModelTime().getGlobalMinimum();
		
		Epoch lastEndTime = Epoch.ZERO;
		
		while(elements.hasMoreElements()){
			final RelationEntry relationEntry = elements.nextElement();
			
			// draw relation box.
			DrawObjects.drawBox(offGraphics, coord_xform, relationEntry.getEarliestTime().subtract(globalMinTime).getDouble(), 
					relationEntry.getLatestTime().subtract(globalMinTime).getDouble(), Color.GRAY, timeline);
			
			for(IStateTraceEntry rstate: relationEntry.getStates()){
				
				if(getCurrentFilter() != null){
					if(! getCurrentFilter().matches(rstate)){
						continue;
					}
				}
				
				final Category stateCategory = reader.getCategory(rstate);
				if(! stateCategory.isVisible()){
					continue;
				}
				
			    DrawObjects.drawState(offGraphics, stateCategory.getName(), coord_xform, rstate , determineColor(stateCategory.getColor(), rstate), 0, timeline, globalMinTime);									

			    if(! parentFrame.isProcessNested()) continue;
					
				final ForwardStateEnumeration stateEnum = rstate.childForwardEnumeration();

				while(stateEnum.hasMoreElements()){
					drawedTraceObjects++;

					final int depth = stateEnum.getNestingDepthOfNextElement();
					
					// skip nested elements if necessary
					
					final ITraceEntry entry = stateEnum.nextElement();
					
					if(getCurrentFilter() != null){
						if(! getCurrentFilter().matches(entry)){
							continue;
						}
					}
					
					if(entry.getType() == TracableObjectType.EVENT){          
						final IEventTraceEntry event = (IEventTraceEntry) entry;

						final Category category = reader.getCategory(event);
						if(category.isVisible())
							DrawObjects.drawEvent(offGraphics, coord_xform,  event, timeline, determineColor(category.getColor(), entry), globalMinTime);

					}else if(entry.getType() == TracableObjectType.STATE){
						final IStateTraceEntry state = (IStateTraceEntry) entry;
						final Category category = reader.getCategory(state);

						if(category.isVisible())
							DrawObjects.drawState(offGraphics,category.getName(),  coord_xform, state , determineColor(category.getColor(), entry), depth, timeline, globalMinTime);
					}
				}
			}

			if(relationEntry.getEarliestTime().compareTo(lastEndTime) < 0){
				// there is an overlapping area, draw this fact!
				DrawObjects.drawScrambeledBox(offGraphics, coord_xform,  
						relationEntry.getEarliestTime().subtract(globalMinTime).getDouble(),  // start
						lastEndTime.subtract(globalMinTime).getDouble(),  //end
						timeline);
			}
			
			lastEndTime = relationEntry.getLatestTime();
		}

		return drawedTraceObjects;
	}
	
	/**
	 * Recursive find the appropriate trace entry within a state which got selected by the given time and yDelta.
	 */
	private ITraceEntry getTraceObjectInState(IStateTraceEntry state, Epoch realTime, int yDelta){
		final double curDist = DrawObjects.getTimeDistance(realTime, state);
		final double eventRadius = 2.0 / getViewPixelsPerUnitTime();
		
		ITraceEntry objMouse = state;
		
		if(curDist != 0){
			// mouse is not inside the state.
			if( curDist < eventRadius)
				return state;

			return null;
		}
		
		/**
		 * The maximum nesting depth which shall be achieved
		 */
		final int maxDepth = (int) DrawObjects.getNestingDepth((double) yDelta / getTopologyManager().getRowHeight());
				
		if (state.hasNestedTraceChildren() && parentFrame.isProcessNested()){					
			ITraceEntry best = state;
			double dist = 0;
			
			int curDepth = 0;

			while(dist == 0){
				// traverse nesting if necessary, and match events.

				if (best.getType() == TracableObjectType.STATE ){
					state = (IStateTraceEntry) best;
					
					if (state.hasNestedTraceChildren() && maxDepth > curDepth && parentFrame.isProcessNested()){
						curDepth += 1;
						
						for(ITraceEntry child: state.getNestedTraceChildren()){
							dist = DrawObjects.getTimeDistance(realTime, child);
							if(child.getType() == TracableObjectType.EVENT ){
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

		if(objMouse.getType() == TracableObjectType.STATE){
			if(! reader.getCategoriesStates().get(objMouse.getName()).isVisible() ){
				return null;
			}
		}else{
			if(! reader.getCategoriesEvents().get(objMouse.getName()).isVisible() ){
				return null;
			}
		}
		
		return objMouse;
	}
	
	@Override
	public TraceObjectInformation getTraceObjectAt(int timeline, Epoch realTime, int y) {
		final TopologyManager topologyManager = getTopologyManager();

		final double eventRadius = 2.0 / getViewPixelsPerUnitTime();
		
		if(topologyManager.getRowCount() <= timeline)
			return null;

		final TopologyTreeNode treeNode = topologyManager.getTreeNodeForTimeline(timeline);
		final ITracableObject rootObj; 
		final ITracableObject selectedObject;
		
		outer: switch(topologyManager.getType(timeline)){
		case TRACE:
			final BufferedTraceFileReader treader = topologyManager.getTraceReaderForTimeline(timeline);
			ITraceEntry objMouse = treader.getTraceEntryClosestToTime(realTime);	
			
			rootObj = objMouse;

			if (objMouse.getType() == TracableObjectType.STATE){
				objMouse = getTraceObjectInState((IStateTraceEntry) objMouse, realTime, y );				
			}else if(objMouse.getType() == TracableObjectType.EVENT){
				double distance = Math.abs(objMouse.getEarliestTime().subtract(realTime).getDouble());
				if( distance >= eventRadius){
					return null;
				}

				if(! reader.getCategoriesEvents().get(objMouse.getName()).isVisible() ){
					return null;
				}
			}

			selectedObject = objMouse;
						
			break;
		case STATISTIC:{
			final IBufferedStatisticsReader sreader = topologyManager.getStatisticReaderForTimeline(timeline);
			StatisticsGroupEntry entry = sreader.getTraceEntryClosestToTime(realTime);
			int which = topologyManager.getStatisticNumberForTimeline(timeline);			
			selectedObject = entry.createStatisticEntry(which);
			rootObj = selectedObject;
			break;
		}case INNER_NODE:
			return null;
		case RELATION:			
			if(topologyManager.getTree().isExpanded(timeline)){
				return null;
			}
		case RELATION_EXPANDED:{
			final TopologyRelationTreeNode node = (TopologyRelationTreeNode) treeNode;
			final RelationEntry relentry = (RelationEntry) node.getTraceEntryClosestToTime(realTime); 
			rootObj = relentry;
			// lookup right object.
			
			// decide which contained element got selected.		
			for(IStateTraceEntry state: relentry.getStates()){
				if(state.getEarliestTime().compareTo(realTime) <= 0){
					if(state.getLatestTime().compareTo(realTime) >= 0){
						// we are inside the state
						objMouse = getTraceObjectInState(state, realTime, y );
						if(objMouse != null){
							selectedObject = objMouse;
							break outer;
						}
					}
				}else{
					break;
				}
			}			
			// if none got selected directly, then choose relation
			selectedObject = rootObj;
			break;
		}
		default:
			throw new IllegalArgumentException("Type not known " + topologyManager.getType(timeline));
		}
		
		if(selectedObject == null){
			return null;
		}
		
		return new TraceObjectInformation(treeNode, rootObj, selectedObject, realTime);
	}



	@Override
	public InfoDialog getPropertyAt(int timeline, Epoch realTime, int y) {
		TraceObjectInformation infoObj = getTraceObjectAt(timeline, realTime, y);
		if( infoObj != null ){
			Frame          window;
			window = (Frame) SwingUtilities.windowForComponent( this );

			switch(infoObj.getObject().getType()){
			case STATISTICENTRY:
				return new InfoDialogForStatisticEntries(window,  realTime,
						getModelTime().getGlobalMinimum(), 
						(TopologyStatisticTreeNode) infoObj.getTopologyTreeNode(),
						(StatisticsEntry) infoObj.getObject());	
			case EVENT:
			case STATE:			
				return new InfoDialogForTraceEntries(window,
						realTime,
						getModelTime().getGlobalMinimum(),						
						(TopologyTreeNode) infoObj.getTopologyTreeNode(),
						getTopologyManager() ,
						(ITraceEntry) infoObj.getObject());	
			}
		}

		return super.getTimePropertyAt(realTime);
	}

	@Override
	public SearchResults searchNextTracable(Epoch laterThan) {
		final int num_rows   = getRowCount();
		final TopologyManager topologyManager = getTopologyManager();

		ITracableObject minObject = null;
		// figure out the object with the smallest time over all timelines which is searchable
		Epoch minTime = reader.getGlobalMaxTime();
		int minTimeline = -1;

		for(int i=0; i < num_rows ; i++){			
			switch (topologyManager.getType(i)){
			case TRACE:
				BufferedTraceFileReader tr = topologyManager.getTraceReaderForTimeline(i);

				traceElementLoop: 
					for(ITraceEntry entry: tr.getTraceEntries()){
						if(entry.getLatestTime().compareTo(laterThan) > 0){
							if(entry.getType() == TracableObjectType.EVENT){
								Category cat = reader.getCategory((IEventTraceEntry) entry);
								if(cat.isSearchable()){												
									if( entry.getEarliestTime().compareTo(minTime) < 0){							
										minObject = entry;
										minTime = entry.getEarliestTime();
										minTimeline = i;
									}
									// found one so break
									break;
								}							
							}else if(entry.getType() == TracableObjectType.STATE){
								IStateTraceEntry state = (IStateTraceEntry) entry;
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
								if(state.hasNestedTraceChildren() && parentFrame.isProcessNested()){
									final Enumeration<ITraceEntry> children = state.childForwardEnumeration();
									while(children.hasMoreElements()){
										final ITraceEntry nestedChild = children.nextElement();

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
			case RELATION:
			case RELATION_EXPANDED:
				
			}
		}
		return new SearchResults(minTimeline, minObject);
	}

	@Override
	public SearchResults searchPreviousTraceable(Epoch earlierThan) {
		final int num_rows   = getRowCount();
		final TopologyManager topologyManager = getTopologyManager();

		ITracableObject minObject = null;
		Epoch maxTime = Epoch.ZERO;
		int minTimeline = -1;

		for(int i=0; i < num_rows ; i++){			
			switch (topologyManager.getType(i)){
			case TRACE:
				BufferedTraceFileReader tr = topologyManager.getTraceReaderForTimeline(i);

				traceElementLoop: 
					for(int te=tr.getTraceEntries().size() -1 ; te >= 0 ; te-- ){
						ITraceEntry entry = tr.getTraceEntries().get(te);

						if(entry.getEarliestTime().compareTo(earlierThan) < 0){
							if(entry.getType() == TracableObjectType.EVENT){
								Category cat = reader.getCategory((IEventTraceEntry) entry);
								if(cat.isSearchable()){												
									if( entry.getLatestTime().compareTo(maxTime) > 0){							
										minObject = entry;
										maxTime = entry.getLatestTime();
										minTimeline = i;
									}
									// found one so break
									break;
								}							
							}else if(entry.getType() == TracableObjectType.STATE){
								IStateTraceEntry state = (IStateTraceEntry) entry;
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
								if(state.hasNestedTraceChildren() && parentFrame.isProcessNested()){
									final Enumeration<ITraceEntry> children = state.childBackwardEnumeration();
									while(children.hasMoreElements()){
										final ITraceEntry nestedChild = children.nextElement();

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

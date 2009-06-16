
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

import hdTraceInput.BufferedStatisticsFileReader;
import hdTraceInput.BufferedTraceFileReader;
import hdTraceInput.ReaderTraceElementEnumerator;
import hdTraceInput.StatisticStatistics;
import hdTraceInput.TraceFormatBufferedFileReader;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.Enumeration;

import javax.swing.BoundedRangeModel;
import javax.swing.SwingUtilities;

import topology.GlobalStatisticStatsPerGroup;
import topology.MinMax;
import topology.TopologyChangeListener;
import topology.TopologyManager;
import topology.TopologyRelationExpandedTreeNode;
import topology.TopologyRelationTreeNode;
import topology.TopologyStatisticTreeNode;
import topology.TopologyTraceTreeNode;
import topology.TopologyTreeNode;
import viewer.common.Debug;
import viewer.common.Parameters;
import viewer.common.Profile;
import viewer.dialog.InfoDialog;
import viewer.dialog.InfoDialogForStatisticEntries;
import viewer.dialog.traceEntries.InfoDialogForTraceEntries;
import viewer.legends.CategoryUpdatedListener;
import viewer.zoomable.CoordPixelImage;
import viewer.zoomable.ScrollbarTimeModel;
import viewer.zoomable.SearchResults;
import viewer.zoomable.SearchableView;
import viewer.zoomable.ViewportTime;
import arrow.Arrow;
import de.hd.pvs.TraceFormat.SimpleConsoleLogger;
import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.relation.RelationEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.trace.ForwardStateEnumeration;
import de.hd.pvs.TraceFormat.trace.IEventTraceEntry;
import de.hd.pvs.TraceFormat.trace.IStateTraceEntry;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import drawable.Category;
import drawable.CategoryStatistic;
import drawable.DrawObjects;
import drawable.TimeBoundingBox;
import drawable.VisualizedObjectType;
import drawable.CategoryStatistic.Scaling;

public class CanvasTimeline extends ScrollableTimeline implements SearchableView
{
	private static final long serialVersionUID = 1424310776190717432L;

	final private TraceFormatBufferedFileReader reader;

	private class MyTopologyChangeListener implements TopologyChangeListener{
		@Override
		public void topologyChanged() {			
			redrawIfAutoRedraw();
		}
	}

	private MyTopologyChangeListener topologyChangeListener = new MyTopologyChangeListener();

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
			TopologyManager topologyManager)
	{
		super( scrollbarTimeModel, viewport, yaxis_model, topologyManager);

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
			case RELATION:
				if(! topologyManager.getTree().isExpanded(i)){
					final TopologyRelationTreeNode node = ((TopologyRelationTreeNode) topologyManager.getTreeNodeForTimeline(i));
					// draw only if not expanded. 
					drawedTraceElements += drawRelationTimeline(i, 
							node, offGraphics, vStartTime, vEndTime, coord_xform);
				}
				break;
			case RELATION_EXPANDED:
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

			final Integer startTimeline = topologyManager.getTimelineForTopology(arrow.getStartTopology());
			final Integer endTimeline = topologyManager.getTimelineForTopology(arrow.getEndTopology());
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

		final BufferedStatisticsFileReader sReader = (BufferedStatisticsFileReader) node.getStatisticSource();

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
		case GLOBAL_MAX:{
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

		final Enumeration<StatisticsGroupEntry> entries = sReader.enumerateStatistics(
				vStartTime.add(getModelTime().getGlobalMinimum()),
				vEndTime.add(getModelTime().getGlobalMinimum()));

		while(entries.hasMoreElements()){			
			final StatisticsGroupEntry entry = entries.nextElement();
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

			final Epoch adaptedTime = entry.getLatestTime().subtract(globalMinTime) ;

			DrawObjects.drawStatistic(offGraphics, coord_xform, entry.getEarliestTime().subtract(globalMinTime),  
					adaptedTime,	(float) value , timeline);

			drawedStatistics++;
		}

		if( cat.isShowAverageLine() ){ // draw average line... TODO cleanup the whole code here...
			Color avgLineColor = backGroundColor.brighter();

			int x1   = coord_xform.convertTimeToPixel( vStartTime.getDouble() );
			int x2   = coord_xform.convertTimeToPixel( vEndTime.getDouble() );

			int height = (coord_xform.getTimelineHeight() );

			int y1   = coord_xform.convertTimelineToPixel( timeline + 1) ;

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
		final ReaderTraceElementEnumerator elements = tr.enumerateTraceEntryLaterThan(true, 
				startTime.add(getModelTime().getGlobalMinimum()), 
				endTime.add(getModelTime().getGlobalMinimum())) ;

		int drawedTraceObjects = 0;

		while(elements.hasMoreElements()){
			drawedTraceObjects++;

			final int depth = elements.getNestingDepthOfNextElement();

			ITraceEntry entry = elements.nextElement();

			final Epoch globalMinTime = getModelTime().getGlobalMinimum();

			if(entry.getType() == TraceObjectType.EVENT){          
				final IEventTraceEntry event = (IEventTraceEntry) entry;

				final Category category = reader.getCategory(event);
				if(category.isVisible())
					DrawObjects.drawEvent(offGraphics, coord_xform, event, timeline, category.getColor(), globalMinTime);

			}else if(entry.getType() == TraceObjectType.STATE){
				final IStateTraceEntry state = (IStateTraceEntry) entry;
				final Category category = reader.getCategory(state);

				if(category.isVisible())
					DrawObjects.drawState(offGraphics, coord_xform, state , category.getColor(), 
							depth, timeline, globalMinTime);
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
			Epoch startTime, Epoch endTime, CoordPixelImage coord_xform
	)
	{
		final Enumeration<RelationEntry> elements = node.enumerateEntries(startTime, endTime);

		int drawedTraceObjects = 0;

		final Epoch globalMinTime = getModelTime().getGlobalMinimum();
		
		while(elements.hasMoreElements()){
			final RelationEntry relationEntry = elements.nextElement();
			
			for(IStateTraceEntry rstate: relationEntry.getStates()){
				
				final Category stateCategory = reader.getCategory(rstate);
				if(stateCategory.isVisible())
					DrawObjects.drawState(offGraphics, coord_xform, rstate , stateCategory.getColor(), 
							0, timeline, globalMinTime);
				
				final ForwardStateEnumeration stateEnum = rstate.childForwardEnumeration();

				while(stateEnum.hasMoreElements()){
					drawedTraceObjects++;

					final int depth = stateEnum.getNestingDepthOfNextElement();
					
					final ITraceEntry entry = stateEnum.nextElement();
					
					if(entry.getType() == TraceObjectType.EVENT){          
						final IEventTraceEntry event = (IEventTraceEntry) entry;

						final Category category = reader.getCategory(event);
						if(category.isVisible())
							DrawObjects.drawEvent(offGraphics, coord_xform, event, timeline, category.getColor(), globalMinTime);

					}else if(entry.getType() == TraceObjectType.STATE){
						final IStateTraceEntry state = (IStateTraceEntry) entry;
						final Category category = reader.getCategory(state);

						if(category.isVisible())
							DrawObjects.drawState(offGraphics, coord_xform, state , category.getColor(), 
									depth, timeline, globalMinTime);
					}
				}
			}
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
				
		if (state.hasNestedTraceChildren()){					
			ITraceEntry best = state;
			double dist = 0;
			
			int curDepth = 0;

			while(dist == 0){
				// traverse nesting if necessary, and match events.

				if (best.getType() == TraceObjectType.STATE ){
					state = (IStateTraceEntry) best;
					
					if (state.hasNestedTraceChildren() && maxDepth > curDepth){
						curDepth += 1;
						
						for(ITraceEntry child: state.getNestedTraceChildren()){
							dist = DrawObjects.getTimeDistance(realTime, child);
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
		
		return objMouse;
	}
	
	@Override
	public TraceObjectInformation getTraceObjectAt(int timeline, Epoch realTime, int y) {
		final TopologyManager topologyManager = getTopologyManager();

		final double eventRadius = 2.0 / getViewPixelsPerUnitTime();
		
		if(topologyManager.getRowCount() <= timeline)
			return null;

		final TopologyTreeNode treeNode = topologyManager.getTreeNodeForTimeline(timeline);
		final TraceObject rootObj; 
		final TraceObject selectedObject;
		
		outer: switch(topologyManager.getType(timeline)){
		case TRACE:
			final BufferedTraceFileReader treader = topologyManager.getTraceReaderForTimeline(timeline);
			ITraceEntry objMouse = treader.getTraceEntryClosestToTime(realTime);	
			
			rootObj = objMouse;

			if (objMouse.getType() == TraceObjectType.STATE){
				objMouse = getTraceObjectInState((IStateTraceEntry) objMouse, realTime, y );				
			}else if(objMouse.getType() == TraceObjectType.EVENT){
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
			final BufferedStatisticsFileReader sreader = topologyManager.getStatisticReaderForTimeline(timeline);
			StatisticsGroupEntry entry = sreader.getTraceEntryClosestToTime(realTime);
			int which = topologyManager.getStatisticNumberForTimeline(timeline);			
			selectedObject = entry.createStatisticEntry(which);
			rootObj = selectedObject;
		}case INNER_NODE:
			return null;
		case RELATION:
			return null;
		case RELATION_EXPANDED:{
			final TopologyRelationExpandedTreeNode node = (TopologyRelationExpandedTreeNode) treeNode;
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
						(TopologyTraceTreeNode) infoObj.getTopologyTreeNode(),
						getTopologyManager() ,
						(ITraceEntry) infoObj.getObject());	
			}
		}

		return super.getTimePropertyAt(realTime);
	}

	@Override
	public SearchResults searchNextComponent(Epoch laterThan) {
		final int num_rows   = getRowCount();
		final TopologyManager topologyManager = getTopologyManager();

		TraceObject minObject = null;
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
							if(entry.getType() == TraceObjectType.EVENT){
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
							}else if(entry.getType() == TraceObjectType.STATE){
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
								if(state.hasNestedTraceChildren()){
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
			}
		}
		return new SearchResults(minTimeline, minObject);
	}

	@Override
	public SearchResults searchPreviousComponent(Epoch earlierThan) {
		final int num_rows   = getRowCount();
		final TopologyManager topologyManager = getTopologyManager();

		TraceObject minObject = null;
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
							if(entry.getType() == TraceObjectType.EVENT){
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
							}else if(entry.getType() == TraceObjectType.STATE){
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
								if(state.hasNestedTraceChildren()){
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

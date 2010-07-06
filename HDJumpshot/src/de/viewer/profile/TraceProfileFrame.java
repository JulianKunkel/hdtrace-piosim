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

package de.viewer.profile;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.drawable.CategoryState;
import de.drawable.StateBorder;
import de.drawable.TimeBoundingBox;
import de.hd.pvs.TraceFormat.SimpleConsoleLogger;
import de.hd.pvs.TraceFormat.TracableObjectType;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.IStateTraceEntry;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hdTraceInput.BufferedTraceFileReader;
import de.hdTraceInput.ITraceElementEnumerator;
import de.hdTraceInput.TraceFormatBufferedFileReader;
import de.topology.TopologyInputPlugin;
import de.topology.TopologyManager;
import de.topology.TopologyManagerContents;
import de.topology.TopologyRelationTreeNode;
import de.topology.TopologyTreeNode;
import de.viewer.common.AbstractTimelineFrame;
import de.viewer.common.Const;
import de.viewer.common.IconManager;
import de.viewer.common.ModelInfoPanel;
import de.viewer.common.ModelTime;
import de.viewer.common.Parameters;
import de.viewer.common.TimeEvent;
import de.viewer.common.TimeListener;
import de.viewer.common.TimelineToolBar;
import de.viewer.common.IconManager.IconType;
import de.viewer.dialog.InfoDialog;
import de.viewer.legends.CategoryUpdatedListener;
import de.viewer.timelines.ScrollableTimeline;
import de.viewer.timelines.TimelineType;
import de.viewer.zoomable.CoordPixelImage;
import de.viewer.zoomable.ScrollableObject;
import de.viewer.zoomable.ScrollbarTimeModel;
import de.viewer.zoomable.ViewportTime;

/**
 * Show a profile of the trace.
 * @author Julian M. Kunkel
 */
public class TraceProfileFrame extends AbstractTimelineFrame<TraceCategoryStateProfile>{

	// the real model's time
	final ModelTime realModelTime;

	// real profiling data
	TraceObjectProfile profile;

	// the selected metric handler
	TraceProfileMetricHandler metricHandler;

	// mapping from TraceObjectProfile to visible information:
	HashMap<TopologyNode, TraceObjectProfileMap> timelineMap = new HashMap<TopologyNode, TraceObjectProfileMap>();		

	// shall there be an automatic update of the time i.e. synchronization between timeline window and profile?
	boolean isTimeUpdateing = true;

	/**
	 * ! normal == reversed
	 */
	boolean normalSorting = true;

	// what the user wants to see:
	enum VisualizedMetric {
		INCLUSIVE_TIME,
		EXCLUSIVE_TIME,
		MAX_INCLUSIVE_TIME,
		MAX_EXCLUSIVE_TIME,
		NUMBER_OF_OCCURRENCES
	}

	VisualizedMetric visualizedMetric = VisualizedMetric.INCLUSIVE_TIME;

	// additional controls:

	JButton timeRefreshBtn;

	JComboBox visualizedMetricBox ;
	JCheckBox processNestedChkbox;	

	// gets triggered if the visibility of an category is changed
	private CategoryUpdatedListener categoryVisibleListener = new CategoryUpdatedListener(){
		@Override
		public void categoryVisibilityWasModified() {
			if(isAutoRefresh())
				forceRedraw();
		}

		@Override
		public void categoryAttributesWereModified() {
			if(isAutoRefresh())
				forceRedraw();
		}		
	};


	/** 
	 * This listener is invoked if the zoom level changes
	 */
	private TimeListener  timeUpdateListener = new TimeListener(){
		@Override
		public void timeChanged(TimeEvent evt) {
			if(isAutoRefresh() ){
				updateTimeInformation();
				getModelTime().zoomHomeWithoutStacking();
			}
		}
	};

	/**
	 * Update the time information from timeline window
	 */
	private void updateTimeInformation()
	{		
		triggerRecomputeTraceProfile();
	}

	public VisualizedMetric getVisualizedMetric() {
		return visualizedMetric;
	}

	public void setVisualizedMetric(VisualizedMetric what){
		this.visualizedMetric = what;

		double maxValue = updateVisualizedMetric();

		// adjust time:		
		getModelTime().setGlobalMaximum(new Epoch(maxValue));
	}

	/**
	 * Returns the maximum value
	 * @return
	 */
	private double updateVisualizedMetric(){
		final TopologyManager topologyManager = getTopologyManager();		

		switch(visualizedMetric){
		case EXCLUSIVE_TIME:
			metricHandler = new TraceProfileMetricHandler.ExclusiveTimeHandler();
			break;
		case INCLUSIVE_TIME:
			metricHandler = new TraceProfileMetricHandler.InclusiveTimeHandler();
			break;
		case NUMBER_OF_OCCURRENCES:
			metricHandler = new TraceProfileMetricHandler.NumberOfOccurrenceHandler();
			break;
		case MAX_EXCLUSIVE_TIME:
			metricHandler = new TraceProfileMetricHandler.MaxExclusiveTimeHandler();
			break;
		case MAX_INCLUSIVE_TIME:
			metricHandler = new TraceProfileMetricHandler.MaxInclusiveTimeHandler();
			break;			
		default:
			metricHandler = null;
		}

		final TraceProfileComparator comparator;

		if(normalSorting)
			comparator = new TraceProfileComparator.Normal(metricHandler);
		else 
			comparator = new TraceProfileComparator.Reversed(metricHandler);

		double maxValue = 0.000000001; // Initialize to something

		for ( int timeline = 0 ; timeline < topologyManager.getRowCount() ; timeline++ ) {
			if ( topologyManager.getType(timeline) == TimelineType.INNER_NODE  ) {
				continue;
			}

			final TopologyNode topoNode = topologyManager.getTreeNodeForTimeline(timeline).getTopology();
			final ArrayList<TraceCategoryStateProfile> list = getProfile().getProfileSortedBy(topoNode, comparator);

			final TraceObjectProfileMap tlMap = new TraceObjectProfileMap(list, metricHandler);
			timelineMap.put(topoNode, tlMap);		

			// adapt max value
			maxValue = (maxValue < tlMap.getMaxValue()) ? tlMap.getMaxValue(): maxValue; 
		}

		return maxValue;
	}

	public void triggerRecomputeTraceProfile(){
		// automatically adapt the title.
		setTitle("Trace Profile " + " (" +
				String.format("%.4f", realModelTime.getViewPosition()) + "-" + 
				String.format("%.4f",(realModelTime.getViewEnd()))
				+ ") " + getReader().getCombinedProjectFilename()
		);		

		final Epoch startTime = new Epoch(realModelTime.getViewPosition()).add(realModelTime.getGlobalMinimum());
		final Epoch endTime = startTime.add(realModelTime.getViewExtent());

		SimpleConsoleLogger.DebugWithStackTrace("recomputeTraceProfile()", 2);

		profile = ComputeTraceProfile(startTime, endTime);		

		// update visible time
		final double maxValue = updateVisualizedMetric();

		// Code modifies Swing, therefore must be run in Swing Thread:
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// adjust time:		
				getModelTime().setEnableFireTimeUpdate(false);
				getModelTime().setGlobalMaximum(new Epoch(maxValue));			

				getModelTime().clearStacks();
				getModelTime().zoomHomeWithoutStacking();
				getModelTime().setEnableFireTimeUpdate(true);
				getModelTime().fireTimeChanged();
			}
		});
	}

	private void addToExistingProfile(ITraceElementEnumerator enumerator, Epoch starttime, Epoch endtime, HashMap<CategoryState, TraceCategoryStateProfile> catMap){
		final TraceFormatBufferedFileReader reader = getReader();

		while(enumerator.hasMoreElements()){
			final ITraceEntry entry = enumerator.nextElement();							
			
			if(entry.getLatestTime().compareTo(starttime) <= 0 || entry.getEarliestTime().compareTo(endtime) >= 0 ){
				// ignore these not really overlapping objects.
				continue;
			}

			if(entry.getType() == TracableObjectType.STATE){
				final IStateTraceEntry state = (IStateTraceEntry) entry;
				final CategoryState category = reader.getCategory(state);

				TraceCategoryStateProfile stateProfil = catMap.get(category);
				if(stateProfil == null){
					stateProfil = new TraceCategoryStateProfile(category, this);
					catMap.put(category, stateProfil);
				}

				// compute all values:
				double inclusiveTime = state.getDurationTime().getDouble();

				double childDuration = 0;

				// subtract nested elements:
				if( state.hasNestedTraceChildren() ){

					for(ITraceEntry child: state.getNestedTraceChildren()){
						if(child.getType() == TracableObjectType.STATE){
							childDuration += ((IStateTraceEntry) child).getDurationTime().getDouble();
						}
					}						
				}

				/////////////////OVERLAPPING HANDLING //////////////////

				// now check whether the state overlaps the border:

				if(entry.getEarliestTime().compareTo(starttime) < 0){
					// overlaps left border
					inclusiveTime -= starttime.subtract(entry.getEarliestTime()).getDouble();

					// subtract nested elements:
					if( state.hasNestedTraceChildren() ){
						childDuration = 0;
						for(ITraceEntry child: state.getNestedTraceChildren()){
							if(child.getType() == TracableObjectType.STATE){
								final IStateTraceEntry childState = (IStateTraceEntry) child;
								if(child.getLatestTime().compareTo(starttime) > 0){ 
									childDuration += childState.getLatestTime().subtract(starttime).getDouble();
								}
							}
						}
					}
				}

				if(entry.getLatestTime().compareTo(endtime) > 0){
					// overlaps right border
					inclusiveTime -= entry.getLatestTime().subtract(endtime).getDouble();

					// subtract nested elements:
					if( state.hasNestedTraceChildren() ){
						childDuration = 0;
						for(ITraceEntry child: state.getNestedTraceChildren()){
							if(child.getType() == TracableObjectType.STATE){
								final IStateTraceEntry childState = (IStateTraceEntry) child;
								if(child.getEarliestTime().compareTo(endtime) < 0){ 
									childDuration += endtime.subtract(childState.getEarliestTime()).getDouble();
								}
							}
						}
					}
				}															

				//////// END HANDLE OVERLAPPING

				final double exclusiveTime = inclusiveTime - childDuration;

				stateProfil.addCall(exclusiveTime, inclusiveTime);
			}

			// TODO handle events also?
		}			
	}

	/**
	 * Called by the worker thread in background
	 * @param starttime
	 * @param endtime
	 * @return
	 */
	private TraceObjectProfile ComputeTraceProfile(Epoch starttime, Epoch endtime){
		final TraceObjectProfile profile = new TraceObjectProfile();
		final TopologyManager    topologyManager = getTopologyManager();
		final boolean profileNested = processNestedChkbox.isSelected();
		
		for(int timeline=0; timeline < topologyManager.getTimelineNumber(); timeline++){			
			final HashMap<CategoryState, TraceCategoryStateProfile> catMap = new HashMap<CategoryState, TraceCategoryStateProfile>();

			switch(topologyManager.getType(timeline)){
			case TRACE:
			{
				final BufferedTraceFileReader traceReader = topologyManager.getTraceReaderForTimeline(timeline);
				ITraceElementEnumerator enumerator = traceReader.enumerateTraceEntries(profileNested, starttime, endtime);

				addToExistingProfile(enumerator, starttime, endtime, catMap);
				break;
			}
			case RELATION:
			{	
				final TopologyRelationTreeNode topoNode = (TopologyRelationTreeNode) topologyManager.getTreeNodeForTimeline(timeline);
				// sum up all children to the profile, note, this can be avoided by adding the already computed statistics from the child nodes.
				for (int line = 0; line < topoNode.getRelationSource().getMaximumConcurrentRelationEntries(); line++){			
					ITraceElementEnumerator enumerator = topoNode.getRelationSource().enumerateTraceEntries(profileNested, starttime, endtime, line);				
					addToExistingProfile(enumerator, starttime, endtime, catMap);
				}

				break;
			}			
			case RELATION_EXPANDED:
			{
				// does not work right now, reason, topoNode is the same!				
				/* final TopologyRelationExpandedTreeNode topoNode = (TopologyRelationExpandedTreeNode) topologyManager.getTreeNodeForTimeline(timeline); 				
				ITraceElementEnumerator enumerator = topoNode.enumerateTraceEntries(profileNested, starttime, endtime);				
				addToExistingProfile(enumerator, starttime, endtime, catMap);
				*/
				break;
			}
			default:
				continue;
			}

			final TopologyNode topoNode = topologyManager.getTreeNodeForTimeline(timeline).getTopology();			
			final ArrayList<TraceCategoryStateProfile> stateList = new ArrayList<TraceCategoryStateProfile>();
			stateList.addAll(catMap.values());
			profile.addProfileInformation(topoNode, stateList);
		}

		return profile;
	}

	private TraceObjectProfile getProfile() {
		return profile;
	}

	@Override
	protected TopologyManagerContents getTopologyManagerType() {
		return TopologyManagerContents.TRACE_ONLY;
	}

	@Override
	protected void addOwnPanelsOrToolbars(JPanel menuPanel) {

	}

	@Override
	protected void addToToolbarMenu(TimelineToolBar toolbar, IconManager iconManager,
			Insets insets) {		
		toolbar.addSeparator();
		timeRefreshBtn = new JButton( iconManager.getActiveToolbarIcon(IconType.Refresh) );
		timeRefreshBtn.setMargin( insets );
		timeRefreshBtn.setToolTipText(	"Redraw canvas to synchronize time information from timeline" );
		timeRefreshBtn.setMnemonic( KeyEvent.VK_D );
		timeRefreshBtn.addActionListener( new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				updateTimeInformation();
			}
		});		

		// disable update if autorefresh is active:
		toolbar.getAutoRefreshBtn().addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				timeRefreshBtn.setEnabled(isAutoRefresh());				
			}
		});

		timeRefreshBtn.setEnabled(! isAutoRefresh());

		toolbar.add( timeRefreshBtn );

		visualizedMetricBox = new JComboBox(VisualizedMetric.values());
		processNestedChkbox = new JCheckBox("Nested");	

		visualizedMetricBox.setFont(Const.FONT);

		visualizedMetricBox.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setVisualizedMetric((VisualizedMetric) visualizedMetricBox.getSelectedItem());
				getModelTime().zoomHomeWithoutStacking();
			}
		});
		visualizedMetricBox.setToolTipText("Select the visualized metric");
		toolbar.add(visualizedMetricBox);

		processNestedChkbox.setSelected(false);

		processNestedChkbox.setToolTipText("Are nested states used for the computation of the values?");
		processNestedChkbox.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {				
				triggerRecomputeTraceProfile();
			}
		});
		toolbar.add(processNestedChkbox);

		toolbar.addSeparator();
	}

	@Override
	protected ModelInfoPanel<TraceCategoryStateProfile> createModelInfoPanel() {
		return new TraceProfileInfoPanel();
	}

	@Override
	protected ScrollableObject createCanvasArea() {
		ScrollableObject obj = new ProfileImagePanel(getScrollbarTimeModel(), getTimeCanvasVport(), 
				getYModel(), getTopologyManager());
		obj.setUseBackgroundThread(false);
		return obj;
	}

	@Override
	protected void destroyWindow() {	
		super.destroyWindow();
		realModelTime.removeTimeListener(timeUpdateListener);
		getReader().getLegendTraceModel().removeCategoryUpdateListener(categoryVisibleListener);		
	}
	
	@Override
	protected void initWindow() {
		super.initWindow();

		// start it after it got visible the first time, otherwise zoom will return an error !
		triggerRecomputeTraceProfile();		
		
		realModelTime.addTimeListener(timeUpdateListener);

		getReader().getLegendTraceModel().addCategoryUpdateListener(categoryVisibleListener);
	}

	public TraceProfileFrame(TraceFormatBufferedFileReader reader, ModelTime modelTime) 
	{		
		super(reader, new ModelTime(Epoch.ZERO, new Epoch(1.0)));
		this.realModelTime = modelTime;

		getFrame().setMinimumSize(new Dimension(700, 500));		
	}


	public class ProfileImagePanel extends ScrollableTimeline{
		private static final long serialVersionUID = 1L;

		public ProfileImagePanel(	ScrollbarTimeModel scrollbarTimeModel,
				ViewportTime viewport,
				BoundedRangeModel   yaxis_model,
				TopologyManager topologyManager) {
			super(scrollbarTimeModel, viewport, yaxis_model, topologyManager);			
		}

		@Override
		protected void drawOneImageInBackground(Image image, TimeBoundingBox timebounds) {			
			Graphics2D g = (Graphics2D) image.getGraphics();


			final int num_rows   = getRowCount();
			final int row_height = getRowHeight();
			final TopologyManager topologyManager = getTopologyManager();

			// check if the timebounds are valid:
			if ( image == null || timebounds.getLatestTime() <= 0 || 
					timebounds.getEarliestTime() >= getModelTime().getGlobalMaximum().getDouble()) {
				return;
			}
			final long startTime = System.currentTimeMillis();

			// int offImage_width = visible_size.width * NumViewsPerImage;
			int        offImage_width  = image.getWidth( this );
			int        offImage_height = image.getHeight( this );

			// Set RenderingHint to have MAX speed.
			g.setRenderingHint( RenderingHints.KEY_RENDERING,	RenderingHints.VALUE_RENDER_SPEED );

			// offGraphics.getClipBounds() returns null
			// offGraphics.setClip( 0, 0, getWidth()/NumImages, getHeight() );
			// Do the ruler labels in a small font that's black.
			// offGraphics.setPaint( BackgroundPaint );
			g.setPaint((Color) Parameters.BACKGROUND_COLOR.toValue() );
			g.fillRect( 0, 0, offImage_width, offImage_height );

			CoordPixelImage coord_xform;  // local Coordinate Transform
			coord_xform = new CoordPixelImage( this, row_height, timebounds );

			// Draw the center TimeLines.
			g.setColor( Color.cyan );
			int totalDrawn = 0;
			for ( int timeline = 0 ; timeline < num_rows ; timeline++ ) {
				final TopologyTreeNode node = getTopologyManager().getTreeNodeForTimeline(timeline);
				if(node != null){
					final TraceObjectProfileMap map = timelineMap.get(node.getTopology());
					
					totalDrawn += drawTimeline(g, timeline, map, coord_xform);
				}
			}

			if( totalDrawn == 0 ){								
				final String str = "No profiles for the selected time interval";
				final int xpos = image.getWidth(null) / 2- 50;
				final int ypos = image.getHeight(null) / 2 ; //num_rows / 2 * row_height;

				// draw string in middle
				g.setColor(Color.CYAN);
				g.fillRect(0, 0, image.getWidth(null), image.getHeight(null));

				g.setColor(Color.BLACK);
				g.drawChars(str.toCharArray(), 0, str.length(), xpos, ypos - 10);
			}

			if(SimpleConsoleLogger.isDebugEverything()){			
				SimpleConsoleLogger.DebugWithStackTrace( (System.currentTimeMillis() - startTime) +  "ms Draw TraceProfile [t] with " + totalDrawn + " profiles "  
						+ timebounds.getEarliestTime() +" - " + timebounds.getLatestTime(), 0);

			}			
		}

		private int drawTimeline(Graphics2D g, int timeline, TraceObjectProfileMap map, CoordPixelImage coordXform){			
			if(map == null){
				return 0; // may happen if background computation is slower but redraw is enforced
			}

			int totalDrawn = 0;

			final int height = coordXform.getTimelineHeight(); 			
			final int yPos = coordXform.convertTimelineToPixel(timeline);

			final double [] values = map.getValues();
			final TraceCategoryStateProfile [] profiles = map.getProfiles();


			double lastValue = 0;

			final StateBorder border = Parameters.PROFILE_STATE_BORDER;
			
			for(int i=0 ; i < values.length ; i++){
				final TraceCategoryStateProfile profile = profiles[i];

				final double value = values[i];								

				final int x1 = coordXform.convertTimeToPixel(lastValue);				
				final int x2 = coordXform.convertTimeToPixel(value);

				lastValue = value;


				if(! profile.getCategory().isVisible())
					continue;

				final Color color = profile.getCategory().getColor(); 

				g.setColor( color );											

				g.fillRect( x1, yPos, x2-x1, height );

				border.paintStateBorder( g, color,	x1, yPos, true, x2, yPos + height, true );

				totalDrawn++;
			}

			return totalDrawn;
		}

		@Override
		public TraceCategoryStateProfile getTraceObjectAt(int timeline, Epoch realModelTime, int y) {
			final TopologyTreeNode node = getTopologyManager().getTreeNodeForTimeline(timeline);
			if(node == null){
				return null;
			}
			
			final TraceObjectProfileMap map = timelineMap.get(node.getTopology());
			if(map == null){
				return null;
			}

			return map.getProfileWithTime(realModelTime.getDouble());
		}

		@Override
		public InfoDialog getPropertyAt(int timeline, Epoch realModelTime, int y) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public TraceProfileMetricHandler getMetricHandler() {
		return metricHandler;
	}

	public double getMaxMetricValue() {
		return super.getModelTime().getGlobalMaximum().getDouble();
	}

	public double getRealModelTimeExtend(){
		return realModelTime.getViewExtent();
	}

	@Override
	protected List<Class<? extends TopologyInputPlugin>> getAvailablePlugins() {
		return new LinkedList<Class<? extends TopologyInputPlugin>>();
	}

}

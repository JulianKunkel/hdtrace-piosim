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

package viewer.profile;

import hdTraceInput.BufferedTraceFileReader;
import hdTraceInput.ReaderTraceElementEnumerator;
import hdTraceInput.TraceFormatBufferedFileReader;

import java.awt.Color;
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

import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import topology.TopologyManager;
import topology.TopologyManagerContents;
import viewer.common.AbstractTimelineFrame;
import viewer.common.Const;
import viewer.common.IconManager;
import viewer.common.ModelInfoPanel;
import viewer.common.ModelTime;
import viewer.common.Parameters;
import viewer.common.TimeEvent;
import viewer.common.TimeListener;
import viewer.common.IconManager.IconType;
import viewer.dialog.InfoDialog;
import viewer.legends.CategoryUpdatedListener;
import viewer.timelines.ScrollableTimeline;
import viewer.timelines.TimelineType;
import viewer.zoomable.CoordPixelImage;
import viewer.zoomable.ScrollableObject;
import viewer.zoomable.ScrollbarTimeModel;
import de.hd.pvs.TraceFormat.SimpleConsoleLogger;
import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.TraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import drawable.CategoryState;
import drawable.StateBorder;
import drawable.TimeBoundingBox;

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
	HashMap<Integer, TraceObjectProfileMap> timelineMap = new HashMap<Integer, TraceObjectProfileMap>();		

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
		NUMBER_OF_CALLS
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
		case NUMBER_OF_CALLS:
			metricHandler = new TraceProfileMetricHandler.NumberOfCallsHandler();
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

		double maxValue = 0.1; // Initialize to something

		for ( int timeline = 0 ; timeline < topologyManager.getRowCount() ; timeline++ ) {
			//  Select only non-expanded row
			if ( topologyManager.getType(timeline) != TimelineType.TRACE  ) {
				continue;
			}
			final ArrayList<TraceCategoryStateProfile> list = getProfile().getProfileSortedBy(timeline, comparator);

			final TraceObjectProfileMap tlMap = new TraceObjectProfileMap(list, metricHandler);
			timelineMap.put(timeline, tlMap);		

			// adapt max value
			maxValue = (maxValue < tlMap.getMaxValue()) ? tlMap.getMaxValue(): maxValue; 
		}

		return maxValue;
	}

	public void triggerRecomputeTraceProfile(){
		// automatically adapt the title.
		setTitle("Trace Profile " + " (" +
				String.format("%.4f", realModelTime.getViewPosition()) + "-" + 
				String.format("%.4f",(realModelTime.getViewExtent() + realModelTime.getViewPosition()))
				+ ") " + getReader().getCombinedProjectFilename()
		);		

		getCanvasArea().triggerAdditionalBackgroundThreadWork();
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
		for(int timeline=0; timeline < topologyManager.getTimelineNumber(); timeline++){
			if(topologyManager.getType(timeline) != TimelineType.TRACE ){
				continue;
			}

			final HashMap<CategoryState, TraceCategoryStateProfile> catMap = new HashMap<CategoryState, TraceCategoryStateProfile>();

			final TraceFormatBufferedFileReader reader = getReader();
			final BufferedTraceFileReader traceReader = topologyManager.getTraceReaderForTimeline(timeline);

			final boolean profileNested = processNestedChkbox.isSelected();

			final ReaderTraceElementEnumerator enumerator = traceReader.enumerateTraceEntryLaterThan(profileNested, starttime, endtime);

			while(enumerator.hasMoreElements()){
				final TraceEntry entry = enumerator.nextElement();							

				if(entry == null)
					continue;

				if(entry.getLatestTime().compareTo(starttime) <= 0 || entry.getEarliestTime().compareTo(endtime) >= 0 ){
					// ignore these not really overlapping objects.
					continue;
				}

				if(entry.getType() == TraceObjectType.STATE){
					final StateTraceEntry state = (StateTraceEntry) entry;
					final CategoryState category = reader.getCategory(state);

					TraceCategoryStateProfile stateProfil = catMap.get(category);
					if(stateProfil == null){
						stateProfil = new TraceCategoryStateProfile(category, this);
						catMap.put(category, stateProfil);
					}

					// compute all values:
					double inclusiveTime = state.getDurationTimeDouble();

					double childDuration = 0;

					// subtract nested elements:
					if( state.hasNestedTraceChildren() ){

						for(TraceEntry child: state.getNestedTraceChildren()){
							if(child.getType() == TraceObjectType.STATE){
								childDuration += ((StateTraceEntry) child).getDurationTimeDouble();
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
							for(TraceEntry child: state.getNestedTraceChildren()){
								if(child.getType() == TraceObjectType.STATE){
									final StateTraceEntry childState = (StateTraceEntry) child;
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
							for(TraceEntry child: state.getNestedTraceChildren()){
								if(child.getType() == TraceObjectType.STATE){
									final StateTraceEntry childState = (StateTraceEntry) child;
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

			final ArrayList<TraceCategoryStateProfile> stateList = new ArrayList<TraceCategoryStateProfile>();
			stateList.addAll(catMap.values());
			profile.addProfileInformation(timeline, stateList);
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
	protected void addToToolbarMenu(JToolBar toolbar, IconManager iconManager,
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
		getToolbar().getAutoRefreshBtn().addActionListener( new ActionListener(){
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

		processNestedChkbox.setSelected(true);

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
		return new ProfileImagePanel(getScrollbarTimeModel(), getYModel(), getTopologyManager());		
	}


	@Override
	protected void windowGetsInvisible() {
		super.windowGetsInvisible();

		realModelTime.removeTimeListener(timeUpdateListener);
		getReader().getLegendTraceModel().removeCategoryUpdateListener(categoryVisibleListener);
	}

	@Override
	protected void windowGetsVisible() {
		super.windowGetsVisible();

		realModelTime.addTimeListener(timeUpdateListener);

		getReader().getLegendTraceModel().addCategoryUpdateListener(categoryVisibleListener);
	}

	@Override
	protected void gotVisibleTheFirstTime() {
		super.gotVisibleTheFirstTime();
		triggerRecomputeTraceProfile();		
	}

	public TraceProfileFrame(TraceFormatBufferedFileReader reader, ModelTime modelTime) 
	{		
		super(reader, new ModelTime(Epoch.ZERO, new Epoch(1.0)));
		this.realModelTime = modelTime;
				
		//getFrame().setPreferredSize(new Dimension(950, 600)); /* JK-SIZE */
	}


	public class ProfileImagePanel extends ScrollableTimeline{
		private static final long serialVersionUID = 1L;

		@Override
		protected void doAdditionalBackgroundThreadWork() {
			final Epoch startTime = new Epoch(realModelTime.getViewPosition()).add(realModelTime.getGlobalMinimum());
			final Epoch endTime = startTime.add(realModelTime.getViewExtent());

			SimpleConsoleLogger.DebugWithStackTrace("recomputeTraceProfile()", 2);

			profile = ComputeTraceProfile(startTime, endTime);		

			// update visible time
			final double maxValue = updateVisualizedMetric();

			// stop pending redrawing
			cancelRedrawing();

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

		public ProfileImagePanel(	ScrollbarTimeModel scrollbarTimeModel,
				BoundedRangeModel   yaxis_model,
				TopologyManager topologyManager) {
			super(scrollbarTimeModel, yaxis_model, topologyManager);			
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
			for ( int irow = 0 ; irow < num_rows ; irow++ ) {
				//  Select only non-expanded row
				if ( topologyManager.getType(irow) == TimelineType.TRACE  ) {
					//i_Y = coord_xform.convertTimelineToPixel(irow ) + row_height / 2;
					//g.drawLine( 0, i_Y, offImage_width-1, i_Y );
					drawTimeline(g, irow, coord_xform);
				}
			}

			if(SimpleConsoleLogger.isDebugEverything()){			
				SimpleConsoleLogger.DebugWithStackTrace( (System.currentTimeMillis() - startTime) +  "ms Draw TraceProfile [t] "  
						+ timebounds.getEarliestTime() +" - " + timebounds.getLatestTime(), 0);

			}			
		}

		private void drawTimeline(Graphics2D g, int timeline, CoordPixelImage coordXform){			
			final TraceObjectProfileMap map = timelineMap.get(timeline);


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
			}
		}

		@Override
		public TraceCategoryStateProfile getTraceObjectAt(int timeline, Epoch realModelTime, int y) {
			final TraceObjectProfileMap map = timelineMap.get(timeline);
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
}

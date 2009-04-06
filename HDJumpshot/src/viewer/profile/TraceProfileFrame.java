package viewer.profile;

import hdTraceInput.BufferedTraceFileReader;
import hdTraceInput.ReaderTraceElementEnumerator;
import hdTraceInput.TraceFormatBufferedFileReader;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import topology.TopologyManager;
import topology.TopologyManagerContents;
import viewer.common.AbstractTimelineFrame;
import viewer.common.Const;
import viewer.common.IconManager;
import viewer.common.ModelInfoPanel;
import viewer.common.Parameters;
import viewer.common.TimeEvent;
import viewer.common.TimeListener;
import viewer.common.IconManager.IconType;
import viewer.dialog.InfoDialog;
import viewer.legends.CategoryUpdatedListener;
import viewer.timelines.TimelineType;
import viewer.zoomable.CoordPixelImage;
import viewer.zoomable.ModelTime;
import viewer.zoomable.ScrollableObject;
import viewer.zoomable.ScrollableTimeline;
import de.hd.pvs.TraceFormat.SimpleConsoleLogger;
import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.TraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import drawable.CategoryState;
import drawable.TimeBoundingBox;

/**
 * Show a profile of the trace.
 * @author julian
 */
public class TraceProfileFrame extends AbstractTimelineFrame<TraceCategoryStateProfile>{
	
	// the real model's time
	final ModelTime realModelTime;
	
	JButton timeRefreshBtn;
	
	// real profiling data
	TraceObjectProfile profile;
	
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
		NUMBER_OF_CALLS
	}
	
	VisualizedMetric visualizedMetric = VisualizedMetric.INCLUSIVE_TIME;
	
	final JComboBox visualizedMetricBox = new JComboBox(VisualizedMetric.values());
	final JCheckBox processNestedChkbox = new JCheckBox("Nested");
	
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
		SimpleConsoleLogger.DebugWithStackTrace("updateTimeInformation()", 2);
		
		recomputeTraceProfile();
	}
	
	public VisualizedMetric getVisualizedMetric() {
		return visualizedMetric;
	}
	
	public void setVisualizedMetric(VisualizedMetric what){
		this.visualizedMetric = what;
		
		updateVisualizedMetric();
	}
	
	private void updateVisualizedMetric(){
		final TopologyManager topologyManager = getTopologyManager();
		
		final TraceProfileValueHandler handler;
		switch(visualizedMetric){
		case EXCLUSIVE_TIME:
			handler = new TraceProfileValueHandler.ExclusiveTimeHandler();
			break;
		case INCLUSIVE_TIME:
			handler = new TraceProfileValueHandler.InclusiveTimeHandler();
			break;
		case NUMBER_OF_CALLS:
			handler = new TraceProfileValueHandler.NumberOfCallsHandler();
			break;
		default:
			handler = null;
		}
		
		final TraceProfileComparator comparator;
		
		if(normalSorting)
			comparator = new TraceProfileComparator.Normal(handler);
		else 
			comparator = new TraceProfileComparator.Reversed(handler);
		
		double maxValue = 0.1; // initalize to something
		
		for ( int timeline = 0 ; timeline < topologyManager.getRowCount() ; timeline++ ) {
			//  Select only non-expanded row
			if ( topologyManager.getType(timeline) != TimelineType.TRACE  ) {
				continue;
			}
			final ArrayList<TraceCategoryStateProfile> list = getProfile().getProfileSortedBy(timeline, comparator);
		
			final TraceObjectProfileMap tlMap = new TraceObjectProfileMap(list, handler);
			timelineMap.put(timeline, tlMap);		
			
			// adapt max value
			maxValue = (maxValue < tlMap.getMaxValue()) ? tlMap.getMaxValue(): maxValue; 
		}
		
		// adjust time:
		getModelTime().setTimeGlobalMaximum(new Epoch(maxValue));
	}
	
	public void recomputeTraceProfile(){		
		final Epoch startTime = new Epoch(realModelTime.getTimeViewPosition()).subtract(realModelTime.getTimeGlobalMinimum());
		final Epoch endTime = startTime.add(realModelTime.getTimeViewExtent());
		
		profile = ComputeTraceProfile(startTime, endTime);		

		// update visible time
		updateVisualizedMetric();
	}
	
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
			
			final ReaderTraceElementEnumerator enumerator = traceReader.enumerateTraceEntry(profileNested, starttime, endtime);
			while(enumerator.hasMoreElements()){
				final TraceEntry entry = enumerator.nextElement();
				if(entry.getType() == TraceObjectType.STATE){
					final StateTraceEntry state = (StateTraceEntry) entry;
					final CategoryState category = reader.getCategory(state);
					
					TraceCategoryStateProfile stateProfil = catMap.get(category);
					if(stateProfil == null){
						stateProfil = new TraceCategoryStateProfile(category);
						catMap.put(category, stateProfil);
					}

					// compute all values:
					final double inclusiveTime = state.getDurationTimeDouble();
					
					
					double childDuration = 0;
					
					// subtract nested elements:
					if( state.hasNestedTraceChildren() ){
						
						for(TraceEntry child: state.getNestedTraceChildren()){
							if(child.getType() == TraceObjectType.STATE){
								childDuration += child.getLatestTime().subtract(child.getEarliestTime()).getDouble();
							}
						}						
					}
					
					double exclusiveTime = inclusiveTime - childDuration;
					
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
		processNestedChkbox.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {				
				recomputeTraceProfile();
				getModelTime().zoomHomeWithoutStacking();
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
		return new ProfileImagePanel(getModelTime(), getYModel(), getTopologyManager());		
	}
	
	
	@Override
	protected void windowIsClosing() {
		realModelTime.removeTimeListener(timeUpdateListener);
		getReader().getLegendTraceModel().removeCategoryUpdateListener(categoryVisibleListener);
	}
	
	@Override
	protected void gotVisibleTheFirstTime() {
		recomputeTraceProfile();		
	}
	
	public TraceProfileFrame(TraceFormatBufferedFileReader reader, ModelTime modelTime) 
	{		
		super(reader);
		this.realModelTime = modelTime;


		final ModelTime virtualTime = new ModelTime(Epoch.ZERO, new Epoch(1.0));
		super.init(virtualTime);
		
		getTopologyManager().setTopologyManagerContents(TopologyManagerContents.TRACE_ONLY);							
		
		getFrame().setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		getFrame().setPreferredSize(new Dimension(950, 600)); /* JK-SIZE */
		
		realModelTime.addTimeListener(timeUpdateListener);
		reader.getLegendTraceModel().addCategoryUpdateListener(categoryVisibleListener);
	}
	
	private class ProfileImagePanel extends ScrollableTimeline{
		private static final long serialVersionUID = 1L;
		
		public ProfileImagePanel(ModelTime modelTime, 
				BoundedRangeModel   yaxis_model,
				TopologyManager topologyManager) {
			super(modelTime, yaxis_model, topologyManager);			
		}
		
		@Override
		protected void drawOneOffImage(Image image, TimeBoundingBox timebounds) {
			final TraceFormatBufferedFileReader reader = getReader();
			
			// automatically adapt the title.
			setTitle("Trace Profile " + " (" +
					String.format("%.4f", realModelTime.getTimeViewPosition()) + "-" + 
					String.format("%.4f",(realModelTime.getTimeViewExtent() + realModelTime.getTimeViewPosition()))
					+ ") " + reader.getCombinedProjectFilename()
					);
			
			Graphics2D g = (Graphics2D) image.getGraphics();


			final int num_rows   = getRowCount();
			final int row_height = getRowHeight();
			final TopologyManager topologyManager = getTopologyManager();

			// check if the timebounds are valid:
			if ( image == null || timebounds.getLatestTime() <= 0 || 
					timebounds.getEarliestTime() >= getModelTime().getTimeGlobalMaximum().getDouble()) {
				return;
			}

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

		}

		private void drawTimeline(Graphics2D g, int timeline, CoordPixelImage coordXform){			
			final TraceObjectProfileMap map = timelineMap.get(timeline);
			
			
			final int height = coordXform.getTimelineHeight(); 			
			final int yPos = coordXform.convertTimelineToPixel(timeline);
			
			final double [] values = map.getValues();
			final TraceCategoryStateProfile [] profiles = map.getProfiles();
			
			
			double lastValue = 0;
			
			for(int i=0 ; i < values.length ; i++){
				final TraceCategoryStateProfile profile = profiles[i];
				
				final double value = values[i];								
				
				final int x1 = coordXform.convertTimeToPixel(lastValue);				
				final int x2 = coordXform.convertTimeToPixel(value);
				
				lastValue = value;

				
				if(! profile.getCategory().isVisible())
					continue;

				g.setColor( profile.getCategory().getColor() );											
				
				g.fillRect( x1, yPos, x2-x1, height );
				
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
}

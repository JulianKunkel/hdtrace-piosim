package viewer.profile;

import hdTraceInput.TraceFormatBufferedFileReader;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import topology.TopologyManager;
import topology.TopologyManagerContents;
import viewer.common.AbstractTimelineFrame;
import viewer.common.Debug;
import viewer.common.IconManager;
import viewer.common.ModelInfoPanel;
import viewer.common.Parameters;
import viewer.common.Profile;
import viewer.common.TimeEvent;
import viewer.common.TimeListener;
import viewer.dialog.InfoDialog;
import viewer.timelines.TimelineType;
import viewer.zoomable.CoordPixelImage;
import viewer.zoomable.ModelTime;
import viewer.zoomable.ScrollableObject;
import viewer.zoomable.ScrollableTimeline;
import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.util.Epoch;
import drawable.TimeBoundingBox;

/**
 * Show a profile of the trace.
 * @author julian
 */
public class TraceProfileFrame extends AbstractTimelineFrame{
	
	final ModelTime realModelTime;

	/** 
	 * This listener is invoked if the zoomlevel changes
	 */
	private TimeListener  timeUpdateListener = new TimeListener(){
		@Override
		public void timeChanged(TimeEvent evt) {
			if(isAutoRefresh()){
				getModelTime().setTimeGlobalMaximum(new Epoch(realModelTime.getTimeViewExtent()));
				getModelTime().fireTimeChanged();
			}
		}
	};

	@Override
	protected void addOwnPanelsOrToolbars(JPanel menuPanel) {
		
	}
	
	@Override
	protected void addToToolbarMenu(JToolBar toolbar, IconManager iconManager,
			Insets insets) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected ModelInfoPanel createModelInfoPanel() {
		return new TraceProfileInfoPanel();
	}
	
	@Override
	protected ScrollableObject createCanvasArea() {
		return new ProfileImagePanel(getModelTime(), getYModel(), getTopologyManager());		
	}
	
	
	@Override
	protected void windowIsClosing() {
		realModelTime.removeTimeListener(timeUpdateListener);
	}
	
	public TraceProfileFrame(TraceFormatBufferedFileReader reader, ModelTime modelTime) 
	{		
		this.realModelTime = modelTime;
		super.init("Trace Profile", reader, new ModelTime(new Epoch(0), new Epoch(modelTime.getTimeViewExtent())));
		
		getTopologyManager().setTopologyManagerContents(TopologyManagerContents.TRACE_ONLY);
		getFrame().setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		
		realModelTime.addTimeListener(timeUpdateListener);
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
			final ModelTime modelTime = getModelTime();
			final TraceFormatBufferedFileReader reader = getReader();
			
			// automatically adapt the title.
			setTitle("Trace Profile " + " (" +
					String.format("%.4f", modelTime.getTimeViewPosition()) + "-" + 
					String.format("%.4f",(modelTime.getTimeViewExtent() + modelTime.getTimeViewPosition()))
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
			final long startTime = System.currentTimeMillis();

			// int offImage_width = visible_size.width * NumViewsPerImage;
			int        offImage_width  = image.getWidth( this );
			int        offImage_height = image.getHeight( this );

			// Set RenderingHint to have MAX speed.
			g.setRenderingHint( RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_SPEED );

			// offGraphics.getClipBounds() returns null
			// offGraphics.setClip( 0, 0, getWidth()/NumImages, getHeight() );
			// Do the ruler labels in a small font that's black.
			// offGraphics.setPaint( BackgroundPaint );
			g.setPaint((Color) Parameters.BACKGROUND_COLOR.toValue() );
			g.fillRect( 0, 0, offImage_width, offImage_height );

			int    irow;
			int    i_Y;

			CoordPixelImage coord_xform;  // local Coordinate Transform
			coord_xform = new CoordPixelImage( this, row_height, timebounds );

			// Set AntiAliasing OFF for all the horizontal and vertical lines
			g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF );

			// Draw the center TimeLines.
			g.setColor( Color.cyan );
			for ( irow = 0 ; irow < num_rows ; irow++ ) {
				//  Select only non-expanded row
				if ( topologyManager.getType(irow) == TimelineType.TRACE  ) {
					i_Y = coord_xform.convertTimelineToPixel(irow ) + row_height / 2;
					g.drawLine( 0, i_Y, offImage_width-1, i_Y );
				}
			}

			// Draw the image separator when in Debug or Profile mode
			if ( Debug.isActive() || Profile.isActive() ) {
				g.setColor( Color.gray );
				g.drawLine( 0, 0, 0, this.getHeight() );
			}

			// Set AntiAliasing from Parameters for all slanted lines
			g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
					Parameters.ARROW_ANTIALIASING.toValue() );

		}


		@Override
		public TraceObject getTraceObjectAt(int timeline, Epoch realModelTime, int y) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public InfoDialog getPropertyAt(int timeline, Epoch realModelTime, int y) {
			// TODO Auto-generated method stub
			return null;
		}
	}
}

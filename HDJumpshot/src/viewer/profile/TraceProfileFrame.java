package viewer.profile;

import hdTraceInput.TraceFormatBufferedFileReader;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.BoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import topology.TopologyManager;
import viewer.common.AbstractTimelineFrame;
import viewer.common.IconManager;
import viewer.common.ModelInfoPanel;
import viewer.dialog.InfoDialog;
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
	
	public TraceProfileFrame(TraceFormatBufferedFileReader reader, ModelTime modelTime) 
	{
		super("Trace Profile", reader, modelTime);
		
		getFrame().setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
	}
	
	private class ProfileImagePanel extends ScrollableTimeline{
		private static final long serialVersionUID = 1L;
		
		public ProfileImagePanel(ModelTime modelTime, 
				BoundedRangeModel   yaxis_model,
				TopologyManager topologyManager) {
			super(modelTime, yaxis_model, topologyManager);
		}
		
		@Override
		protected void drawOneOffImage(Image image, TimeBoundingBox image_endtimes) {
			final ModelTime modelTime = getModelTime();
			final TraceFormatBufferedFileReader reader = getReader();
			
			// automatically adapt the title.
			setTitle("Trace Profile " + " (" +
					String.format("%.4f", modelTime.getTimeViewPosition()) + "-" + 
					String.format("%.4f",(modelTime.getTimeViewExtent() + modelTime.getTimeViewPosition()))
					+ ") " + reader.getCombinedProjectFilename()
					);
			
			Graphics2D g = (Graphics2D) image.getGraphics();

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

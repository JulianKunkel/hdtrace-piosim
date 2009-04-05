package viewer.histogram;

import hdTraceInput.TraceFormatBufferedFileReader;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import viewer.common.ButtonAutoRefresh;
import viewer.common.IAutoRefreshable;
import viewer.common.TimeEvent;
import viewer.common.TimeListener;
import viewer.zoomable.ModelTime;

/**
 * Show a profile of the trace.
 * @author julian
 */
public class TraceProfileFrame {
	final JFrame frame;
	final ModelTime modelTime;
	final TraceFormatBufferedFileReader reader; 
	final ProfileImagePanel profilePanel;
	

	/**
	 * Automatically refresh profile information if time changed (i.e. scrolled)
	 */
	private class MyTimeModifiedListener implements TimeListener{
		@Override
		public void timeChanged(TimeEvent evt) {
			refreshData();
		}
	}

	private class MyWindowClosedListener extends WindowAdapter{
		@Override
		public void windowClosed(WindowEvent e) {
			// don't forget to remove modelTime listener (if autoupdate), otherwise ressources are wasted
			modelTime.removeTimeListener(timeModifiedListener);
			super.windowClosed(e);
		}		
	}
	
	private MyTimeModifiedListener timeModifiedListener = new MyTimeModifiedListener();
	
	/**
	 * Force to reload data and repaint
	 */
	private void refreshData(){
		profilePanel.refreshData();
	}

	public TraceProfileFrame(ModelTime modelTime, TraceFormatBufferedFileReader reader) 
	{
		this.modelTime = modelTime;
		this.reader = reader;
		
		frame = new JFrame("Trace Profile");
		profilePanel = new ProfileImagePanel();
		
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.setMinimumSize(new Dimension(400, 250));
		frame.setResizable(true);

		JPanel xPanel = new JPanel();
		xPanel.setLayout(new BoxLayout( xPanel, BoxLayout.X_AXIS));
		
    JButton autoRefresh_btn = new ButtonAutoRefresh(profilePanel);
    xPanel.add( autoRefresh_btn );        
		

		final JPanel yPanel = new JPanel();
		yPanel.setLayout(new BoxLayout( yPanel, BoxLayout.Y_AXIS));		
		yPanel.setMinimumSize(frame.getPreferredSize());
		

		yPanel.add(xPanel);
		yPanel.add(profilePanel);

		frame.add(yPanel);
		
		// default on close operation:
		frame.addWindowListener(new MyWindowClosedListener());
	}
	
	
	
	public void show(){
		frame.pack();
		frame.setVisible(true);
	}
	
	private class ProfileImagePanel extends JPanel implements IAutoRefreshable{
		private static final long serialVersionUID = 1L;

		// automatically redraw on time modification:
		boolean isAutoRefresh = false;
		
		@Override
		public boolean isAutoRefresh() {			
			return isAutoRefresh;
		}
		
		@Override
		public void setAutoRefresh(boolean autoRefresh) {
			isAutoRefresh = autoRefresh;
			
			if(autoRefresh == true){
				modelTime.addTimeListener(timeModifiedListener);
				refreshData();
			}else{
				modelTime.removeTimeListener(timeModifiedListener);
			}
		}
		
		/**
		 * Call it when the number of bins change or the time interval.
		 */
		public void refreshData(){			
			this.repaint();
		}

		
		@Override
		public void paint(Graphics g2) {
			// automatically adapt the title.
			frame.setTitle("Trace Profile " + " (" +
					String.format("%.4f", modelTime.getTimeViewPosition()) + "-" + 
					String.format("%.4f",(modelTime.getTimeViewExtent() + modelTime.getTimeViewPosition()))
					+ ") " + reader.getCombinedProjectFilename()
					);
			
			Graphics2D g = (Graphics2D) g2;

		}
	}
}

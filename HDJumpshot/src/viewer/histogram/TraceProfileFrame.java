package viewer.histogram;

import javax.swing.JFrame;

/**
 * Show a profile of the trace.
 * @author julian
 */
public class TraceProfileFrame {
	final JFrame frame;

	public TraceProfileFrame() {
		frame = new JFrame("Trace Histogram");
	}
	
	public void show(){
		frame.setVisible(true);
	}
}

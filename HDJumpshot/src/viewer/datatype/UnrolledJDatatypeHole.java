package viewer.datatype;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;

class UnrolledJDatatypeHole extends JLabel{
	private static final long serialVersionUID = 1L;

	final private long space;
	
	public UnrolledJDatatypeHole(long space) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setText(Long.toString(space));
		this.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		this.setToolTipText("Hole [bytes]");
		
		this.setOpaque(true);
		this.setBackground(Color.RED);
		
		this.space = space;
	}
	
	/**
	 * Size of hole
	 * @return
	 */
	public long getSpace() {
		return space;
	}	
}
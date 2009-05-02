/**
 * 
 */
package viewer.datatype;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;

class JDatatypeHole extends JLabel{
	private static final long serialVersionUID = 1L;

	private static Color holeColor = Color.LIGHT_GRAY;
	
	public JDatatypeHole(long space) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setText(space + " B");
		this.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		this.setToolTipText("Hole [bytes]");
		this.setBackground(holeColor);
	}
}
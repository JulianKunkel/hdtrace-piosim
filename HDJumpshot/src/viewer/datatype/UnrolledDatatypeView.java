package viewer.datatype;

import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import de.hd.pvs.TraceFormat.project.datatypes.Datatype;

/**
 * Show the unrolled datatype for a <size, offset> tupel
 * @author julian
 */
public class UnrolledDatatypeView {
	private static final long serialVersionUID = 1L;
	
	private Border datatypeBorder = BorderFactory.createRaisedBevelBorder();
	
	private final UnrolledDatatypePanel    rootPanel = new UnrolledDatatypePanel(this);
	private final JScrollPane 	           scrollPane = new JScrollPane(rootPanel); 

	/* contains holes used to color them correctly */
	final private LinkedList<UnrolledJDatatypeHole> holes = new LinkedList<UnrolledJDatatypeHole>();
	
	public UnrolledDatatypeView(Datatype datatype, long size, long offset) {
		rootPanel.setDatatype(datatype, size, offset);
	}
	
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	
	void addHole(UnrolledJDatatypeHole hole){
		holes.add(hole);
	}
	
	Border getDatatypeBorder() {
		return datatypeBorder;
	}
}

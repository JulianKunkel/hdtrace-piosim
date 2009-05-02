package viewer.datatype;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.hd.pvs.TraceFormat.project.datatypes.Datatype;
import de.hd.pvs.TraceFormat.project.datatypes.DatatypeEnum;
import de.hd.pvs.TraceFormat.project.datatypes.NamedDatatype;

/**
 * A reference to a datatype.
 * If a user clicks on a reference then the datatype gets expanded or the link is followed. 
 * 
 * @author Julian M. Kunkel
 */
class JDatatypeReference extends JPanel implements MouseListener{
	private final DatatypeView datatypeView;

	private static final long serialVersionUID = 1L;

	private final Datatype datatype;

	private boolean isExpandable;		

	public JDatatypeReference(DatatypeView datatypeView, Datatype datatype, int count) {
		this.datatypeView = datatypeView;
		this.setBorder(datatypeView.getBorder());
		this.datatype = datatype;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JLabel label = new JLabel(count + " x");
		label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		label.setToolTipText("Number of repeats");

		label.addMouseListener(this);
		this.add(label);

		if(datatype.getType() == DatatypeEnum.NAMED){
			label = new JLabel(((NamedDatatype) datatype).getPrimitiveType().toString());
			isExpandable = false;
		}else{
			label = new JLabel(datatype.getType().toString());
			isExpandable = true;
		}

		label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		label.setToolTipText("Datatype <size, extend> = <" + datatype.getSize() + ", " + datatype.getExtend() + ">");

		this.add(label);

		this.setBackground(JDatatype.getBackgroundColor(datatype));

		this.addMouseListener(this);
		label.addMouseListener(this);
	}

	public Datatype getDatatype() {
		return datatype;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(isExpandable){
			datatypeView.addAndZoomDatatype(datatype);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}
}
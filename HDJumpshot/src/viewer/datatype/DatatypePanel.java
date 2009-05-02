package viewer.datatype;

import java.awt.Graphics;

import javax.swing.JPanel;


/**
 * Layouts components correctly and draw arcs between them.
 * @author Julian M. Kunkel
 */
class DatatypePanel extends JPanel{
	private final DatatypeView datatypeView;
	private static final long serialVersionUID = 2L;

	public DatatypePanel(DatatypeView datatypeView) {
		this.datatypeView = datatypeView;
		setLayout(new DatatypeLayoutManager(this.datatypeView)); // we layout ourselves
	}

	/**
	 * Draw arrows between dependencies:
	 */
	@Override
	public void paint(Graphics g) {
		// draw container:
		super.paint(g);
		
		// draw arrows between dependencies:
		for(JDatatype jType: datatypeView.getCreatedDatatypes().values()){
			// scan for datatype referene position
			for(final JDatatypeReference ref: jType.getReferencedTypes()){
				final JDatatype referencedType = datatypeView.getCreatedDatatypes().get(ref.getDatatype());
				
				if(referencedType == null){
					continue;
				}

				// draw an line:
				g.drawLine(
						referencedType.getX() + referencedType.getWidth() / 2, 
						referencedType.getY(), 
						jType.getPanel().getX() + jType.getX() + ref.getX() + ref.getWidth() / 2, 
						jType.getY() + jType.getHeight());
			}					
		}
	}
}
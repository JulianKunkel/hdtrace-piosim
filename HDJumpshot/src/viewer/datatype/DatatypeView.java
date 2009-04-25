package viewer.datatype;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import de.hd.pvs.TraceFormat.project.datatypes.ContiguousDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.Datatype;
import de.hd.pvs.TraceFormat.project.datatypes.DatatypeEnum;
import de.hd.pvs.TraceFormat.project.datatypes.NamedDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.StructDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.VectorDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.StructDatatype.StructType;

/**
 * View an datatype hierarchically.
 * 
 * @author julian
 */
public class DatatypeView {
	final HashMap<Long, Datatype> typeMap;

	final DatatypePanel    rootPanel = new DatatypePanel();
	final JScrollPane 	   viewPane = new JScrollPane(rootPanel);

	static Color holeColor = Color.LIGHT_GRAY; 

	/**
	 * Draw each datatype only once, then use labels to refer to it.
	 */
	final HashMap<Datatype, JDatattype> drawnDatatypes = new HashMap<Datatype, JDatattype>();

	final Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);

	Datatype root = null;

	public DatatypeView(HashMap<Long, Datatype> typeMap) {
		viewPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		this.typeMap = typeMap;
	}	

	public void setRootDatatype(Datatype root){
		drawnDatatypes.clear();
		rootPanel.removeAll();

		this.root = root;

		new JDatattype(root);
	}

	public JComponent getRootComponent(){
		return viewPane;
	}

	private class DatatypePanel extends JPanel{
		private static final long serialVersionUID = 2L;

		public DatatypePanel() {
			Dimension dim = new Dimension(150, 150); 
			setMinimumSize(dim);
		}
	}

	private class JDatatypeHole extends JLabel{
		private static final long serialVersionUID = 1L;

		public JDatatypeHole(long space) {
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			this.setText(space + " B");
			this.setAlignmentX(JComponent.CENTER_ALIGNMENT);
			this.setToolTipText("Hole [bytes]");
			this.setBackground(holeColor);
		}
	}

	/**
	 * A reference to a datatype.
	 * If a user clicks on a reference then the datatype gets expanded or the link is followed. 
	 * 
	 * @author julian
	 */
	private class JDatatypeReference extends JPanel implements MouseListener{
		private static final long serialVersionUID = 1L;

		final Datatype datatype;

		boolean isExpandable;		

		public JDatatypeReference(Datatype datatype, int count) {
			this.setBorder(border);
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

			this.setBackground(getBackgroundColor(datatype));

			this.addMouseListener(this);
			label.addMouseListener(this);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if(isExpandable){
				final JDatattype drawn;

				if(drawnDatatypes.containsKey(datatype)){
					drawn = drawnDatatypes.get(datatype);
				}else{
					drawn = new JDatattype(datatype);
				}

				// already expanded, therefore scroll to it.
				SwingUtilities.invokeLater(new Runnable(){
					@Override
					public void run() {
						viewPane.getViewport().scrollRectToVisible(drawn.getBounds());		
					}
				});
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

	private Color getBackgroundColor(Datatype datatype){
		switch(datatype.getType()){
		case CONTIGUOUS:{
			return (Color.CYAN);						
		}case NAMED:{
			return (Color.WHITE);
		}case STRUCT:{
			return (Color.GREEN);			
		}case VECTOR:{
			return (Color.YELLOW);
		}
		}
		return Color.LIGHT_GRAY;
	}

	/**
	 * Lightweight component representing a datatype:
	 */
	private class JDatattype extends JPanel{
		private static final long serialVersionUID = 1L;

		final Datatype datatype;

		public JDatattype(Datatype datatype) {
			drawnDatatypes.put(datatype, this);
			rootPanel.add(this);

			this.setBorder(border);
			this.datatype = datatype;
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			JLabel label = new JLabel(datatype.getType().toString() + " <" + datatype.getSize() + ", " + datatype.getExtend() + ">");
			label.setToolTipText("Type name <size of datatype, size of extend>");
			label.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			this.add(label);

			this.setBackground(getBackgroundColor(datatype));

			switch(datatype.getType()){
			case CONTIGUOUS:{
				ContiguousDatatype type = (ContiguousDatatype) datatype;
				final JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
				panel.setAlignmentX(JComponent.LEFT_ALIGNMENT);				
				panel.add(new JDatatypeReference(type.getPrevious(), type.getCount()));				

				break;								
			}case NAMED:{
				NamedDatatype type = (NamedDatatype) datatype;
				label.setText(type.getPrimitiveType().toString());

				break;
			}case STRUCT:{
				StructDatatype type = (StructDatatype) datatype;
				final JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
				panel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

				int lastPos = 0;
				for(int i=0; i < type.getCount(); i++){
					final StructType childType = type.getType(i);

					if(childType.getDisplacement() != lastPos){
						// add a hole
						final long length = childType.getDisplacement() - lastPos;
						panel.add(new JDatatypeHole(length));
					}
					lastPos = childType.getDisplacement() + childType.getType().getExtend() * childType.getBlocklen();
					panel.add(new JDatatypeReference(childType.getType(), childType.getBlocklen()));
				}

				this.add(panel);

				break;
			}case VECTOR:{
				VectorDatatype type = (VectorDatatype) datatype;

				break;								
			}
			}
		}
	}
}

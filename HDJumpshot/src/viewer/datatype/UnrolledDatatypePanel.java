package viewer.datatype;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.hd.pvs.TraceFormat.project.datatypes.ContiguousDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.Datatype;
import de.hd.pvs.TraceFormat.project.datatypes.NamedDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.NamedDatatype.NamedDatatypeType;

/**
 * Unroll a datatype, the datatype draws a sequence of holes and used extends
 * where the first datatype hit by offset is unrolled. Also is the last.
 * Only one full datatype is unrolled, this is indicated by a (x [times it is unrolled]) in the view.   
 * 
 * @author julian
 */
public class UnrolledDatatypePanel extends JPanel{
	private static final long serialVersionUID = 1L;

	private static int HEIGHT = 30;


	final private UnrolledDatatypeView view;

	public UnrolledDatatypePanel(UnrolledDatatypeView view) {
		this.view = view;
	}

	private void addDatatypeEnd(){
		JLabel label = new JLabel(" | ");
		label.setToolTipText("Datatype ends");
		label.setBackground(Color.PINK);
		label.setOpaque(true);
		add (label); // datatype ends.
	}
	
	/** 
	 * @param datatype if null we assume byte.
	 * @param size
	 * @param offset
	 */
	public void setDatatype(Datatype datatype, long unrollSize, long offset){		
		this.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));

		this.removeAll();

		if(datatype == null){
			// byte datatype => sequential I/O
			final Datatype byteDatatype  = new NamedDatatype(NamedDatatypeType.CHAR);
			final Datatype contig = new ContiguousDatatype(byteDatatype, (int) unrollSize);
			final UnrolledJDatatype type = new UnrolledJDatatype(view, contig); 
			
			type.createIt(unrollSize, 0);
			
			add(type);
			
			return;			
		}
		
		// add first datatype, this one might be used only half.
		
		final long datatypeSize = datatype.getSize();
		
		System.out.println("Unroll: " + unrollSize + " offset: " + offset);
		
		if(offset % datatypeSize != 0){			
			// draw half datatype
			final long realOffset = offset % datatypeSize;

			final long amountToDraw = datatypeSize - realOffset;

			UnrolledJDatatype type = new UnrolledJDatatype(view, datatype);
			type.createIt(amountToDraw, realOffset);

			unrollSize -= amountToDraw;

			System.out.println("HALF Datatype amountToDraw: " + amountToDraw + " unroll remains:: " + unrollSize + " offs: " + realOffset);
			add(type);
			
			addDatatypeEnd();
		}

		// now we know offset == 0

		// determine repeats:
		final int fullRepeats = (int) (unrollSize / datatypeSize); 

		System.out.println("FULL REPEATS " + fullRepeats);
		
		if(fullRepeats > 0){
			// add label:
			
			final JPanel refPanel = new JPanel(); 
			refPanel.setLayout(new BoxLayout(refPanel, BoxLayout.Y_AXIS));
			
			JLabel label = new JLabel(fullRepeats + " x");
			label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
			label.setToolTipText("Number of repeats");
			label.setOpaque(true);
			label.setBackground(Color.WHITE);

			refPanel.add(label); 
			
			UnrolledJDatatype type = new UnrolledJDatatype(view, datatype);
			type.createIt(datatypeSize, 0);			
			refPanel.add(type);
			
			this.add(refPanel);
			
			addDatatypeEnd();
		}
		
		// maybe there is a remainder on the right.
		unrollSize -= fullRepeats * datatypeSize;
		if(unrollSize > 0){
			System.out.println("CREAT remainder unroll: " + unrollSize );
			
			UnrolledJDatatype type = new UnrolledJDatatype(view, datatype);
			type.createIt(unrollSize, 0);

			add(type);
			
			addDatatypeEnd();
		}
	}


}

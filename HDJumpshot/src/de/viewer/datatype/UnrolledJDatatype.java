//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.

package de.viewer.datatype;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.hd.pvs.TraceFormat.project.datatypes.ContiguousDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.Datatype;
import de.hd.pvs.TraceFormat.project.datatypes.DatatypeEnum;
import de.hd.pvs.TraceFormat.project.datatypes.NamedDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.StructDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.VectorDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.StructDatatype.StructType;

/**
 * Lightweight component representing a datatype:
 */
class UnrolledJDatatype extends JPanel{
	private static final long serialVersionUID = 1L;

	private final Datatype datatype;

	private JPanel panel = null;

	private final  UnrolledDatatypeView view;

	private void setHorizontalTypePanel(Component parent){
		if(panel == null){
			panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			panel.setAlignmentX(JComponent.CENTER_ALIGNMENT);

			panel.setOpaque(false);

			this.add(panel);
		}
	}

	private void addSimpleReferenceType(JPanel panel, Datatype type, long unrollSize, long offset){
		final UnrolledJDatatype jdataType = new UnrolledJDatatype(view, type);		
		jdataType.createIt(unrollSize, offset);
		panel.add(jdataType);
	}

	private void addSimpleHole(JPanel panel, long size){
		final UnrolledJDatatypeHole hole = new UnrolledJDatatypeHole(size); 
		panel.add(hole);
		view.addHole(hole);
	}

	private void addReferenceType(Datatype type, int repeats, long unrollSize, long offset) {
		assert(unrollSize >= 0);
		assert(offset >= 0);

		setHorizontalTypePanel(this);

		if(type.getSize() == 0)
			return;

		// adjust repeats.

		if(repeats == 0){
			return;
		}

		// now we know there aint any offset

		if(repeats != 1){
			
			// try to compress named datatypes into view:
			if(type.getType() == DatatypeEnum.NAMED){
				JLabel label = new JLabel("" + (type.getSize() * repeats));
				label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
				label.setToolTipText("Datatype " + ((NamedDatatype) type).getPrimitiveType() + " repeats: " + repeats);								
				panel.add(label);
				label.setBackground(JDatatype.getBackgroundColor(type));
				label.setOpaque(true);
				
				return;
			}
			final JPanel refPanel = new JPanel(); 
			refPanel.setLayout(new BoxLayout(refPanel, BoxLayout.Y_AXIS));

			JLabel label = new JLabel(repeats + " x");
			label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
			label.setToolTipText("Number of repeats");
			label.setOpaque(false);

			refPanel.add(label);

			addSimpleReferenceType(refPanel, type, unrollSize, offset);
			
			refPanel.setOpaque(false);

			panel.add(refPanel);
		}else{
			addSimpleReferenceType(panel, type, unrollSize, offset);
		}

		return;
	}

	private void addHole(long size) {
		if(size == 0)
			return;

		setHorizontalTypePanel(this);

		addSimpleHole(panel, size);
	}

	/**
	 * Create the unrolled datatype, method invokation requires that the
	 * datatype can really be created (with the given offset)
	 * therefore it requires: unrollSize < size(dataype) AND offset < size(datatype) + unrollSize
	 * 
	 */
	public void createIt(long unrollSize, long offset){		
		//JLabel label = new JLabel( datatype.getType().toString());
		this.setToolTipText("<size, extend> =  <" + datatype.getSize() + ", " + datatype.getExtend() + ">");
		//label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		//this.add(label);
		
		System.out.println("DT: " + " toUntroll: " + unrollSize + " off: " + offset + " REALSZ: " + datatype.getSize()+ " " +datatype);

		assert(unrollSize <= datatype.getSize());
		assert(offset+ unrollSize <= datatype.getSize());
		assert(unrollSize > 0);

		switch(datatype.getType()){
		case CONTIGUOUS:{
			ContiguousDatatype type = (ContiguousDatatype) datatype;

			final long prevSize = type.getPrevious().getSize();
			int repeats = (int) (unrollSize / prevSize);

			repeats = repeats < type.getCount() ? repeats: type.getCount();

			if(offset > 0){
				// reduce repeats and offset:
				offset = offset % prevSize;

				if(offset > 0){
					addReferenceType(type.getPrevious(), 1, prevSize - offset, offset);

					unrollSize -= prevSize - offset; 
				}	
			}

			unrollSize -= repeats * prevSize;
			addReferenceType(type.getPrevious(), repeats, prevSize, 0);			

			if(unrollSize > 0){
				addReferenceType(type.getPrevious(), 1, unrollSize, 0);
			}

			return;
		}case NAMED:{
			NamedDatatype type = (NamedDatatype) datatype;
			final int size = type.getPrimitiveType().getSize();
			final JLabel label = new JLabel( );
			label.setBackground(Color.BLACK);
			label.setOpaque(true);
			
			if(unrollSize < size){
				label.setText("" + unrollSize);
				label.setBackground(Color.PINK);
				label.setToolTipText("WARNING datatype is written only partially " + unrollSize + " of " + 
						size);
			}else{
				label.setText("" + size);
				label.setToolTipText(type.getPrimitiveType().toString());
			}

			this.add(label);

			return;
		}case STRUCT:{
			StructDatatype type = (StructDatatype) datatype;

			long lastPos = 0;

			for(int i=0; i < type.getCount(); i++){

				final StructType childType = type.getType(i);
				final long typeSize = childType.getType().getSize();

				if(offset > 0){
					long blockSize = childType.getBlocklen() * typeSize;
					if( blockSize < offset ){
						offset -= blockSize;
						continue;
					}else{
						// reduce block length and unwrap first datatype:
						int maxRepeats = (int) (unrollSize / typeSize);
						int myRepeats = childType.getBlocklen();

						int remainingOffset = (int) (offset % typeSize);

						// if only one iteration:												
						if(remainingOffset > 0){
							// might be that only a subset of data is written:
							final long remainingBytes = unrollSize < typeSize - remainingOffset ? unrollSize : 
								typeSize - remainingOffset ;  

							// add half type first
							addReferenceType(childType.getType(), 1, remainingBytes, remainingOffset);
							unrollSize -= remainingBytes;

							myRepeats--;
						}

						maxRepeats = maxRepeats <= myRepeats ? maxRepeats : myRepeats;  						

						offset = 0;

						if(maxRepeats > 0){
							addReferenceType(childType.getType(), maxRepeats, typeSize, 0);
							unrollSize -= typeSize * maxRepeats;
						}

						// add remainder:
						if( unrollSize > 0 && (maxRepeats < myRepeats) ){
							// we need to add an remainder:

							addReferenceType(childType.getType(), 1, unrollSize, 0);

							unrollSize = 0;
							break;
						}

						if(unrollSize == 0){
							break;
						}

						lastPos = childType.getDisplacement() + childType.getType().getExtend() * childType.getBlocklen();

						continue;
					}
				}

				// add a hole if needed
				final long holeLength = childType.getDisplacement() - lastPos;
				addHole(holeLength);

				if(unrollSize == 0){
					// we must be finished before unwrapping this type
					break;
				}

				if(typeSize == 0){ // account for UB/LB type
					continue;
				}

				// we know unrollSize > 0 and offset == 0
				lastPos = childType.getDisplacement() + childType.getType().getExtend() * childType.getBlocklen();


				int maxFullRepeats = (int) (unrollSize  / typeSize);

				maxFullRepeats = maxFullRepeats <= childType.getBlocklen() ? maxFullRepeats : childType.getBlocklen();  

				if(maxFullRepeats > 0 ){
					addReferenceType(childType.getType(), maxFullRepeats, typeSize, 0);

					unrollSize -= maxFullRepeats * typeSize; 
				}

				if(unrollSize > 0 && maxFullRepeats < childType.getBlocklen()){
					// draw half datatype, after that we must be finished:

					addReferenceType(childType.getType(), 1, unrollSize, 0);
					return;
				}								
			}

			return;
		}case VECTOR:{
			final VectorDatatype type = (VectorDatatype) datatype;
			final long typeSize = type.getPrevious().getSize();

			// first skip a number of iterations. 			

			// first half repeat:
			if( offset > 0 ){
				
				// first skip a number of iterations, according to the offset.
				long fullItertoSkip =  offset / typeSize;
				offset -= fullItertoSkip * typeSize;
					

			    long mySizeToUnroll = typeSize - offset % type.getPrevious().getExtend();
				
				mySizeToUnroll = mySizeToUnroll < unrollSize ? mySizeToUnroll : unrollSize;
				
				assert(mySizeToUnroll >= 0);

				unrollSize -= mySizeToUnroll;
				
				addReferenceType(type.getPrevious(), 1, mySizeToUnroll, offset % type.getPrevious().getExtend() );

				if(unrollSize == 0){
					return;
				}
				addHole(type.getStride() - type.getPrevious().getExtend());
			}

			// now offset == 0

			int fullRepeats = (int) (unrollSize  / typeSize);			

			if( fullRepeats > 0 ){
				final JPanel yPanel = new JPanel();
				yPanel.setLayout(new BoxLayout(yPanel, BoxLayout.Y_AXIS));		

				if(fullRepeats > 1){
					final JLabel label = new JLabel( fullRepeats + " x");
					label.setToolTipText("Number of iterations");
					label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
					label.setOpaque(false);
					
					yPanel.add(label);					
					yPanel.setBorder(view.getDatatypeBorder());		
				}

				yPanel.setOpaque(false);			
				
				final JPanel xPanel = new JPanel();
				xPanel.setOpaque(false);
				xPanel.setLayout(new BoxLayout(xPanel,  BoxLayout.X_AXIS));

				addSimpleReferenceType(xPanel, type.getPrevious(), typeSize, 0);
				addSimpleHole(xPanel, type.getStride() - type.getPrevious().getExtend());
				yPanel.add(xPanel);

				this.add(yPanel);

				unrollSize -= fullRepeats * typeSize;
			}

			// maybe there is a remainder:

			if(unrollSize > 0){
				panel = null;
				// draw datatype half, after that we must be finished.
				addReferenceType(type.getPrevious(), 1, unrollSize, 0);
			}

			return;					
		}
		}

		return;
	}

	public UnrolledJDatatype(UnrolledDatatypeView view, Datatype datatype) {
		this.view = view;
		this.datatype = datatype;
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));		

		this.setBackground(JDatatype.getBackgroundColor(datatype));
		this.setBorder(view.getDatatypeBorder());
	}

	public Datatype getDatatype() {
		return datatype;
	}
}
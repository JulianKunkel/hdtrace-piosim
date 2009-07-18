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
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.hd.pvs.TraceFormat.project.datatypes.ContiguousDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.Datatype;
import de.hd.pvs.TraceFormat.project.datatypes.NamedDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.StructDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.VectorDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.StructDatatype.StructType;

/**
 * Lightweight component representing a datatype:
 */
class JDatatype extends JPanel{
	private final DatatypeView datatypeView;

	private static final long serialVersionUID = 1L;

	private final Datatype datatype;

	private final LinkedList<JDatatypeReference> referencedTypes = new LinkedList<JDatatypeReference>();

	private JPanel panel = null;
	
	public static Color getBackgroundColor(Datatype datatype){
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
	
	public JPanel getPanel() {
		return panel;
	}
	
	private void setHorizontalTypePanel(Component parent){
		if(panel == null){
			panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			panel.setAlignmentX(JComponent.CENTER_ALIGNMENT);

			this.add(panel);
		}
	}

	private void addReferenceType(JDatatypeReference comp) {
		referencedTypes.add(comp);

		setHorizontalTypePanel(this);

		panel.add(comp);
	}

	private void addHole(long size) {
		if(size == 0)
			return;
		
		setHorizontalTypePanel(this);

		panel.add(new JDatatypeHole(size));
	}

	public LinkedList<JDatatypeReference> getReferencedTypes() {
		return referencedTypes;
	}

	public JDatatype(DatatypeView datatypeView, Datatype datatype) {
		this.datatypeView = datatypeView;
		this.setBorder(datatypeView.getBorder());
		this.datatype = datatype;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JLabel label = new JLabel( datatype.getType().toString() + " <" + datatype.getSize() + ", " + datatype.getExtend() + ">");
		label.setToolTipText("Type name <size of datatype, size of extend>");
		label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		this.add(label);
		
		this.setBackground(getBackgroundColor(datatype));

		switch(datatype.getType()){
		case CONTIGUOUS:{
			ContiguousDatatype type = (ContiguousDatatype) datatype;

			addReferenceType(new JDatatypeReference(this.datatypeView, type.getPrevious(), type.getCount()));				

			break;								
		}case NAMED:{
			NamedDatatype type = (NamedDatatype) datatype;
			label.setText(type.getPrimitiveType().toString());

			break;
		}case STRUCT:{
			StructDatatype type = (StructDatatype) datatype;

			long lastPos = 0;
			for(int i=0; i < type.getCount(); i++){
				final StructType childType = type.getType(i);

				// add a hole if needed
				final long length = childType.getDisplacement() - lastPos;
				addHole(length);
				lastPos = childType.getDisplacement() + childType.getType().getExtend() * childType.getBlocklen();
				addReferenceType(new JDatatypeReference(this.datatypeView, childType.getType(), childType.getBlocklen()));
			}

			break;
		}case VECTOR:{
			VectorDatatype type = (VectorDatatype) datatype;
			
			label = new JLabel( type.getBlockCount() + " x");
			label.setToolTipText("Number of iterations");
			label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
			this.add(label);
			
			addReferenceType(new JDatatypeReference(this.datatypeView, type.getPrevious(), type.getBlocklen()));
			addHole(type.getStride() - type.getPrevious().getExtend());				
			 
			break;								
		}
		}
	}
	
	public Datatype getDatatype() {
		return datatype;
	}
}
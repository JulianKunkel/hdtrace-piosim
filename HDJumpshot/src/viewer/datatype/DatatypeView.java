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

package viewer.datatype;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

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
 * @author Julian M. Kunkel
 */
public class DatatypeView {
	final DatatypePanel    rootPanel = new DatatypePanel();
	final JScrollPane 	   scrollPane = new JScrollPane(rootPanel);

	static Color holeColor = Color.LIGHT_GRAY; 

	/**
	 * Draw/Create each datatype only once, then use labels to refer to it.
	 */
	final HashMap<Datatype, JDatatype> createdDatatypes = new HashMap<Datatype, JDatatype>();

	final Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);

	Datatype root = null;

	public DatatypeView() {
		scrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);		
	}	

	public void setRootDatatype(Datatype root){
		createdDatatypes.clear();
		rootPanel.removeAll();

		this.root = root;

		new JDatatype(root);
	}

	public JComponent getRootComponent(){
		return scrollPane;
	}

	private class DatatypeLayoutManager implements LayoutManager2{
		final Dimension currentSize = new Dimension(200, 200);

		@Override
		public void addLayoutComponent(Component comp, Object constraints) {}
		
		@Override
		public void addLayoutComponent(String name, Component comp) {}
		
		@Override
		public float getLayoutAlignmentX(Container target) {
			return 0;
		}
		
		@Override
		public float getLayoutAlignmentY(Container target) {
			return 0;
		}
		
		@Override
		public Dimension maximumLayoutSize(Container target) {
			return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
		}

		@Override
		public void invalidateLayout(Container target) {
			
		}		
		
		@Override
		public void layoutContainer(Container parent) {			
			// datatype and referring datatypes
			final HashMap<Datatype, HashSet<Datatype>> dependency = new HashMap<Datatype, HashSet<Datatype>>();

			// create a dependency graph
			// no dependencies for root datatype: 
			dependency.put(root, new HashSet<Datatype>());

			for(int i = 0; i < parent.getComponentCount(); i++){
				//  update dependency graph
				final Datatype cur = ((JDatatype) parent.getComponent(i)).datatype;

				//System.out.println("ADDING " + cur);

				for(Datatype usedType: cur.getChildDataTypes()){
					HashSet<Datatype> isUsedByList = dependency.get(usedType);
					if(isUsedByList == null){
						isUsedByList = new HashSet<Datatype>();
						dependency.put(usedType, isUsedByList);
					}
					isUsedByList.add(cur);

					//System.out.println("DEPENDS on " + usedType);
				}
			}

			// update positions of all components according to dependency:

			// right now use a N� algorithm, could use a priority queue / heap, though
			final HashSet<Datatype> curDrawn = new HashSet<Datatype>();

			// first add the ones which do not have any dependency. Then the ones which refer only
			// to drawn and so forth.
			int row = 0;
			int maxWidth = 0;
			int curY = 2;

			while(true){
				// Datatypes which shall be drawn in this row:
				final LinkedList<Datatype> toDraw = new LinkedList<Datatype>();

				// determine the components which can be drawn:
				for(Datatype typ: dependency.keySet()){
					if(curDrawn.contains(typ)){
						continue;
					}

					HashSet<Datatype> isUsedByList = dependency.get(typ);
					//System.out.println("CHECKING typ: " + typ);

					boolean hasDependencies = false;
					for(Datatype usedBy: isUsedByList){
						if(! createdDatatypes.containsKey(usedBy)){
							continue;
						}
						if( ! curDrawn.contains(usedBy)){
							hasDependencies = true;
							break;
						}
					}
					if(! hasDependencies){
						toDraw.add(typ);
					}
				}

				int rowX = 2;
				int maxRowHeight = 0; 

				// exit criterion
				if(toDraw.size() == 0){
					break;
				}

				// align and draw components:
				for(Datatype typ: toDraw){
					curDrawn.add(typ);
					final JDatatype jType = createdDatatypes.get(typ);
					if(jType != null){
						//System.out.println("DRAWING " + typ);

						final Dimension prefSize = jType.getPreferredSize();

						jType.setBounds(rowX, curY, prefSize.width, prefSize.height);
						if (prefSize.height > maxRowHeight)
							maxRowHeight = prefSize.height;

						rowX += prefSize.width + 20;
					}
				}


				if(rowX > maxWidth){
					maxWidth = rowX;
				}

				curY += maxRowHeight + 20;

				row++;
			}

			final Insets insets = parent.getInsets();
			currentSize.height = curY + insets.bottom + insets.top - 20 + 2;
			currentSize.width = maxWidth + insets.left + insets.right - 20 + 2;

			parent.setSize(currentSize);

			//final Dimension hScrollDim = scrollPane.getHorizontalScrollBar().getPreferredSize();
			//final Dimension vScrollDim = scrollPane.getVerticalScrollBar().getPreferredSize();
			//final Dimension scrollPaneDim = new Dimension(currentSize);
			//scrollPaneDim.width += hScrollDim.width;
			//scrollPaneDim.height += vScrollDim.height;
			
			//scrollPane.setPreferredSize(scrollPaneDim);
			//scrollPane.invalidate();
		}


		@Override
		public Dimension minimumLayoutSize(Container parent) { 
			return currentSize;
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			return currentSize;
		}

		@Override
		public void removeLayoutComponent(Component comp) {

		}
	}

	/**
	 * Layouts components correctly and draw arcs between them.
	 * @author Julian M. Kunkel
	 */
	private class DatatypePanel extends JPanel{
		private static final long serialVersionUID = 2L;

		public DatatypePanel() {
			setLayout(new DatatypeLayoutManager()); // we layout ourselves
		}

		@Override
		public void paint(Graphics g) {
			// draw container:
			super.paint(g);

			// draw arrows between dependencies:
			for(JDatatype jType: createdDatatypes.values()){
				// scan for datatype referene position
				for(final JDatatypeReference ref: jType.getReferencedTypes()){
					final JDatatype referencedType = createdDatatypes.get(ref.datatype);
					if(referencedType == null)
						continue;

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
	 * @author Julian M. Kunkel
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
				final JDatatype drawn;

				if(createdDatatypes.containsKey(datatype)){
					drawn = createdDatatypes.get(datatype);
				}else{
					drawn = new JDatatype(datatype);
				}

				// already expanded, therefore scroll to it.
				SwingUtilities.invokeLater(new Runnable(){
					@Override
					public void run() {
						scrollPane.getViewport().scrollRectToVisible(drawn.getBounds());		
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
	private class JDatatype extends JPanel{
		private static final long serialVersionUID = 1L;

		final Datatype datatype;

		final LinkedList<JDatatypeReference> referencedTypes = new LinkedList<JDatatypeReference>();

		JPanel panel = null;
		
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

		public JDatatype(Datatype datatype) {
			this.setBorder(border);
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

				addReferenceType(new JDatatypeReference(type.getPrevious(), type.getCount()));				

				break;								
			}case NAMED:{
				NamedDatatype type = (NamedDatatype) datatype;
				label.setText(type.getPrimitiveType().toString());

				break;
			}case STRUCT:{
				StructDatatype type = (StructDatatype) datatype;

				int lastPos = 0;
				for(int i=0; i < type.getCount(); i++){
					final StructType childType = type.getType(i);

					// add a hole if needed
					final long length = childType.getDisplacement() - lastPos;
					addHole(length);
					lastPos = childType.getDisplacement() + childType.getType().getExtend() * childType.getBlocklen();
					addReferenceType(new JDatatypeReference(childType.getType(), childType.getBlocklen()));
				}

				break;
			}case VECTOR:{
				VectorDatatype type = (VectorDatatype) datatype;
				
				label = new JLabel( type.getCount() + " x");
				label.setToolTipText("Number of iterations");
				label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
				this.add(label);
				
				addReferenceType(new JDatatypeReference(type.getPrevious(), type.getBlocklen()));
				addHole(type.getStride() - type.getPrevious().getExtend());				
				 
				break;								
			}
			}


			createdDatatypes.put(datatype, this);
			rootPanel.add(this);
		}
	}
}

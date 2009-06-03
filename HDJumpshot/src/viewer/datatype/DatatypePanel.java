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
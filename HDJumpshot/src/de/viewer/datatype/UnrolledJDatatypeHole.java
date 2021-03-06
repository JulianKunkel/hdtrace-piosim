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

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;

class UnrolledJDatatypeHole extends JLabel{
	private static final long serialVersionUID = 1L;

	final private long space;
	
	public UnrolledJDatatypeHole(long space) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setText(Long.toString(space));
		this.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		this.setToolTipText("Hole [bytes]");
		
		this.setOpaque(true);
		this.setBackground(Color.RED);
		
		this.space = space;
	}
	
	/**
	 * Size of hole
	 * @return
	 */
	public long getSpace() {
		return space;
	}	
}
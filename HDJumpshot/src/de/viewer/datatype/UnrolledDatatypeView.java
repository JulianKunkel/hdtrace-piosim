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

import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import de.hd.pvs.TraceFormat.project.datatypes.Datatype;

/**
 * Show the unrolled datatype for a <size, offset> tupel
 * @author Julian M. Kunkel
 */
public class UnrolledDatatypeView {
	private static final long serialVersionUID = 1L;
	
	private Border datatypeBorder = BorderFactory.createRaisedBevelBorder();
	
	private final UnrolledDatatypePanel    rootPanel = new UnrolledDatatypePanel(this);
	private final JScrollPane 	           scrollPane = new JScrollPane(rootPanel); 

	/* contains holes used to color them correctly */	
	final private LinkedList<UnrolledJDatatypeHole> holes = new LinkedList<UnrolledJDatatypeHole>();
	
	/*
	 * Cached value
	 */
	long sumHolesSpace = -1;
	
	
	public UnrolledDatatypeView(Datatype datatype, long size, long offset) {
		rootPanel.setDatatype(datatype, size, offset);
		
		// load hole length
		sumHolesSpace = 0;
		for(UnrolledJDatatypeHole hole: holes){
			sumHolesSpace += hole.getSpace();
		}
		
		// color holes TODO
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

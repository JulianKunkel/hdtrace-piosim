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

import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;


import de.hd.pvs.TraceFormat.project.datatypes.Datatype;
import de.viewer.dialog.traceEntries.ResizeListener;

/**
 * View an datatype hierarchically.
 * 
 * @author Julian M. Kunkel
 */
public class DatatypeView {
	private final DatatypePanel    rootPanel = new DatatypePanel(this);
	private final JScrollPane 	   scrollPane = new JScrollPane(rootPanel); 

	/**
	 * Draw/Create each datatype only once, then use labels to refer to it.
	 */
	private final HashMap<Datatype, JDatatype> createdDatatypes = new HashMap<Datatype, JDatatype>();

	private final Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);

	/**
	 * Root datatype to visualize
	 */
	private Datatype root = null;

	public DatatypeView() {
		scrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		//scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		//scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);		
	}	

	public void setRootDatatype(Datatype root){
		createdDatatypes.clear();
		rootPanel.removeAll();

		this.root = root;

		addAndZoomDatatype(root);
	}
	
	/**
	 * Add a datatype (if not already drawn), zoom to the datatype
	 * @param datatype
	 */
	public void addAndZoomDatatype(Datatype datatype){		
		JDatatype jData = createdDatatypes.get(datatype);
		if(jData == null){
			jData = new  JDatatype(this, datatype);		
			createdDatatypes.put(datatype, jData);
			rootPanel.add(jData);
		}
		
		// already expanded, therefore scroll to it.
		scrollPane.getViewport().scrollRectToVisible(jData.getBounds());		
	}
	

	public JComponent getRootComponent(){
		return scrollPane;	
	}
	
	JScrollPane getScrollPane() {
		return scrollPane;
	}
	
	public Border getBorder() {
		return border;
	}
	
	public Datatype getRoot() {
		return root;
	}
	
	DatatypePanel getRootPanel() {
		return rootPanel;
	}
	
	HashMap<Datatype, JDatatype> getCreatedDatatypes() {
		return createdDatatypes;
	}
	
	public ResizeListener getDatatypeViewChangeListener() {
		return ((DatatypeLayoutManager) rootPanel.getLayout()).getDatatypeViewChangeListener();
	}
	
	public void setDatatypeViewChangeListener(
			ResizeListener datatypeViewChangeListener) {
		((DatatypeLayoutManager) rootPanel.getLayout()).setDatatypeViewChangeListener(datatypeViewChangeListener);
	}
}

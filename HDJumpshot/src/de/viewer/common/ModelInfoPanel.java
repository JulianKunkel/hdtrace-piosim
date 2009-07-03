
 /** Version Control Information $Id: ModelInfoPanel.java 197 2009-04-09 07:58:54Z kunkel $
  * @lastmodified    $Date: 2009-04-09 09:58:54 +0200 (Do, 09. Apr 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 197 $ 
  */

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

package de.viewer.common;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;


/**
 * Superclass for an Panel which shows information about a particular object
 * @author julian
 *
 */
abstract public class ModelInfoPanel<InfoType>
{
	/**
	 * Show information about the object
	 * @param obj
	 */
	public abstract void showInfo(InfoType obj);
	
	/*
	 * Add the controls to the info panel, called upon instantiation of the ModelInfoPanel  
	 */
	protected abstract void addControlsToPanel(JPanel panel);
	
	/**
	 *  add a textField to the panel which is not editable
	 */
	protected void addTextField(LabeledTextField field, String tooltip){
		field.setToolTipText(tooltip);
		field.setEditable(false);
		panel.add(field);
	}
	
	// the real panel containing all the labels:
	private JPanel panel = new JPanel();
	
	public ModelInfoPanel(  )
	{
		panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );
		panel.setBorder( BorderFactory.createEtchedBorder() );

	}
	
	final public void init(){
		addControlsToPanel(panel);		
	}
	
	final public JPanel getPanel() {
		return panel;
	}
}

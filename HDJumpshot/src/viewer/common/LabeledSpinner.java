
/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$ 
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

/*
 *  @author Julian M. Kunkel
 */

package viewer.common;

import java.awt.Component;
import java.awt.Dimension;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeListener;

public class LabeledSpinner extends JPanel
{
	private static final long serialVersionUID = -2098447219602678782L;
	
	final private JSpinner spinner;
	final private JLabel   label;

	public LabeledSpinner( String text, SpinnerModel model, ChangeListener listener)
	{
		super.setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
		label = new JLabel( " " + text + " " );
		label.setAlignmentX( Component.LEFT_ALIGNMENT );

		super.add( this.label);
				
		spinner =  new JSpinner(model);
		spinner.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(spinner);
		
		label.setLabelFor(spinner);
		label.setFont(Const.FONT);
		
		spinner.setFont(Const.FONT);		
		
		spinner.addChangeListener(listener);
		
		this.setMaximumSize( getPreferredSize());
	}
	
	public Object getValue(){
		return spinner.getValue();
	}

	public void setValue(Object val){
		spinner.setValue(val);
	}
	
  public Dimension getMaximumSize()
  {  
      return new Dimension( Short.MAX_VALUE,
                            spinner.getPreferredSize().height + label.getHeight() );
  }
}
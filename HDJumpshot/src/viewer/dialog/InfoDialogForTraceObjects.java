
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


package viewer.dialog;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.BoxLayout;
import javax.swing.JLabel;

import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.trace.TraceEntry;

public class InfoDialogForTraceObjects extends InfoDialog
{
    public InfoDialogForTraceObjects( final Frame     frame, 
                                  final double    clicked_time,
                                  TraceObject obj )
    {
        super( frame, "Traceable Object Info Box", clicked_time );

        Container root_panel = this.getContentPane();
        root_panel.setLayout( new BoxLayout( root_panel, BoxLayout.Y_AXIS ) );

        JLabel label = new JLabel(obj.getType().toString());
        root_panel.add(label);        
        

        Font notBold = new Font("SansSerif", Font.PLAIN, label.getFont().getSize());
        
        String text = "";
        
        switch (obj.getType()){
        case STATE:
        case EVENT:
        	label = new JLabel("Contained XML data:");
          root_panel.add(label);
          
        	text = ((TraceEntry) obj).toString();
        	break;
        case STATISTICENTRY:        	        
        	break;
        }
        
        label = new JLabel(text);
        label.setFont(notBold);
        label.setBackground(Color.LIGHT_GRAY);
        
        root_panel.add(label);
        

        root_panel.add( super.getCloseButtonPanel() );
    }
}

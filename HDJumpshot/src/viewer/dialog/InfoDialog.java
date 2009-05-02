
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
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author Anthony Chan (Jumpshot 4), Julian M. Kunkel
 */

package viewer.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import de.hd.pvs.TraceFormat.util.Epoch;

public class InfoDialog extends JDialog
{
    private JPanel   btn_panel;
    private JButton  close_btn;

    private Epoch   clicked_time;
    private Epoch   modelTimeDiff;


    public InfoDialog( final Frame   ancestor_frame,
                             String  title_str,
                             Epoch clickedTime,
                             Epoch modelTimeDiff)
    {
        super( ancestor_frame, title_str );
        clicked_time = clickedTime;
        this.modelTimeDiff = modelTimeDiff;
        this.init();
    }

    private void init()
    {
        super.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
        btn_panel = new JPanel();
        close_btn = new JButton( "close" );
        close_btn.setAlignmentX( Component.CENTER_ALIGNMENT );
        btn_panel.add( close_btn );
        btn_panel.setAlignmentX( Component.LEFT_ALIGNMENT );
        Dimension  panel_max_size;
        panel_max_size        = btn_panel.getPreferredSize();
        panel_max_size.width  = Short.MAX_VALUE;
        btn_panel.setMaximumSize( panel_max_size );
        
        btn_panel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    }

    public JButton getCloseButton()
    {
        return close_btn;
    }

    public JPanel getCloseButtonPanel()
    {
        return btn_panel;
    }

    public Epoch getClickedTime()
    {
        return clicked_time;
    }
    
    public Epoch getModelTimeDiff() {
			return modelTimeDiff;
		}

    public void setVisibleAtLocation( final Point global_pt )
    {
        this.setLocation( global_pt );
        this.pack();
        this.setVisible( true );
        this.toFront();
    }
}

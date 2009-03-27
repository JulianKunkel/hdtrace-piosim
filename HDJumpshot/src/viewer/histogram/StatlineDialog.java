
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

package viewer.histogram;

import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.WindowConstants;

import viewer.dialog.InitializableDialog;
import viewer.zoomable.ActionTimelineRestore;
import viewer.zoomable.ViewportTimeYaxis;
import drawable.TimeBoundingBox;

public class StatlineDialog extends InitializableDialog
{
    private StatlinePanel  top_panel;

    public StatlineDialog( final Dialog              ancestor_dialog,
                           final TimeBoundingBox     timebox,
                           ActionTimelineRestore restore,
                           ViewportTimeYaxis  canvas_viewport)
    {
        super( ancestor_dialog, "Histogram for the duration [ "
                              + (float)timebox.getEarliestTime() + ", "
                              + (float)timebox.getLatestTime() + " ]" );
        super.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );

        top_panel = new StatlinePanel( this, timebox, restore, canvas_viewport );
        setContentPane( top_panel );

        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                StatlineDialog.this.dispose();
            }
        } );
    }

    public void init()
    {
        top_panel.init();
    }
}

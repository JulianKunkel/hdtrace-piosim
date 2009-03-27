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

import java.awt.*;
import javax.swing.BoxLayout;
import javax.swing.JTree;

import drawable.CategoryWeight;

import base.statistics.Summarizable;
import viewer.dialog.InfoDialog;

public class InfoDialogForSummary extends InfoDialog
{
    public InfoDialogForSummary( final Dialog        dialog, 
                                 final double        clicked_time,
                                 final JTree         tree_view,
                                 final String[]      y_colnames,
                                 final Summarizable  summarizable )
    {
        super( dialog, "Summary Info Box", clicked_time );

        Container root_panel = this.getContentPane();
        root_panel.setLayout( new BoxLayout( root_panel, BoxLayout.Y_AXIS ) );

        root_panel.add( new InfoPanelForSummary( tree_view, y_colnames,
                                                 summarizable ) );

        root_panel.add( super.getCloseButtonPanel() );
    }
}

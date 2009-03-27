
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
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import viewer.common.Const;
import viewer.common.Routines;
import viewer.common.TimeFormat;
import viewer.zoomable.ScrollableObject;
import drawable.TimeBoundingBox;

public class InfoDialogForDuration extends InfoDialog
{
    private static final String          FORMAT = Const.INFOBOX_TIME_FORMAT;
    private static       DecimalFormat   fmt    = null;
    private static       TimeFormat      tfmt   = null;

    private              TimeBoundingBox timebox;
    private              ScrollableObject scrollable;

    public InfoDialogForDuration( final Frame             frame,
                                  final TimeBoundingBox   times,
                                  final ScrollableObject  scrollobj )
    {
        super( frame, "Duration Info Box", times.getLatestTime() );
        timebox     = times;
        scrollable  = scrollobj;
        this.init();
    }

    public InfoDialogForDuration( final Dialog            dialog,
                                  final TimeBoundingBox   times,
                                  final ScrollableObject  scrollobj )
    {
        super( dialog, "Duration Info Box", times.getLatestTime() );
        timebox     = times;
        scrollable  = scrollobj;
        this.init();
    }

    private void init()
    {
        /* Define DecialFormat for the displayed time */
        if ( fmt == null ) {
            fmt = (DecimalFormat) NumberFormat.getInstance();
            fmt.applyPattern( FORMAT );
        }
        if ( tfmt == null )
            tfmt = new TimeFormat();
        
        Container root_panel = this.getContentPane();
        root_panel.setLayout( new BoxLayout( root_panel, BoxLayout.Y_AXIS ) );

            StringBuffer textbuf = new StringBuffer();
            int          num_cols = 0, num_rows = 3;

            StringBuffer linebuf = new StringBuffer();
            linebuf.append( "duration = "
                          + tfmt.format(timebox.getDuration()) );
            num_cols = linebuf.length();
            textbuf.append( linebuf.toString() + "\n" );

            linebuf = new StringBuffer();
            linebuf.append( "[0]: time = "
                          + fmt.format(timebox.getEarliestTime()) );
            if ( num_cols < linebuf.length() )
                num_cols = linebuf.length();
            textbuf.append( linebuf.toString() + "\n" );
            
            linebuf = new StringBuffer();
            linebuf.append( "[1]: time = "
                          + fmt.format(timebox.getLatestTime()) );
            if ( num_cols < linebuf.length() )
                num_cols = linebuf.length();
            textbuf.append( linebuf.toString() );

            JTextArea text_area = new JTextArea( textbuf.toString() );
            int adj_num_cols    = Routines.getAdjNumOfTextColumns( text_area,
                                                                   num_cols );
            text_area.setColumns( adj_num_cols );
            text_area.setRows( num_rows );
            text_area.setEditable( false );
            text_area.setLineWrap( true );
        JScrollPane scroller = new JScrollPane( text_area );
        scroller.setAlignmentX( Component.LEFT_ALIGNMENT );
        root_panel.add( scroller );

        root_panel.add( super.getCloseButtonPanel() );
    }

    public TimeBoundingBox getTimeBoundingBox()
    {
        return timebox;
    }
}

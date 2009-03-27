
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

package viewer.timelines;

import hdTraceInput.TraceFormatBufferedFileReader;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import viewer.common.TopWindow;


public class TimelineFrame extends JFrame
{
    private        TimelinePanel  top_panel;

    public TimelineFrame( final TraceFormatBufferedFileReader reader )
    {
        super( "TimeLine: " + reader.getCombinedProjectFilename() );
        super.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
        TopWindow.Timeline.disposeAll();
        TopWindow.Timeline.setWindow( this );
        
        top_panel = new TimelinePanel( this, reader);
        setContentPane( top_panel );

        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                TopWindow.Timeline.disposeAll();
            }
        } );

       top_panel.setPreferredSize(new Dimension(1220, 700)); /* JK-SIZE */
    }

    public void setVisible( boolean val )
    {
        super.setVisible( val );
        TopWindow.Control.setShowTimelineButtonEnabled( !val );
    }

    public void init()
    {
        top_panel.init();
    }
}

/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
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
    private static String         in_filename;      // For main()
    private static int            in_view_ID  = 0;  // For main()

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

        top_panel.setPreferredSize(new Dimension(1210, 600)); /* JK-SIZE */
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

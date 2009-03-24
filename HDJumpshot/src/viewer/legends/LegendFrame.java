/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.legends;

import hdTraceInput.TraceFormatBufferedFileReader;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import viewer.common.TopWindow;


public class LegendFrame extends JFrame
{
    private        LegendPanel    top_panel;

    public LegendFrame( final TraceFormatBufferedFileReader  reader )
    {
        super( "Legend: " + reader.getCombinedProjectFilename() );
        super.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
        TopWindow.Legend.disposeAll();
        TopWindow.Legend.setWindow( this );

        top_panel = new LegendPanel( reader );
        super.setContentPane( top_panel );

        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                LegendFrame.this.setVisible( false );
            }
        } );

        /* setVisible( true ); */
    }

    public void setVisible( boolean val )
    {
        super.setVisible( val );
        TopWindow.Control.setShowLegendButtonEnabled( !val );
    }
}

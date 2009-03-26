/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.first;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import viewer.common.Debug;
import viewer.common.Dialogs;
import viewer.common.TopControl;
import viewer.common.TopWindow;
import de.hd.pvs.TraceFormat.SimpleConsoleLogger;
import drawable.Category;

public class Jumpshot extends JFrame
                        implements TopControl
{
    private static boolean        isApplet = false;
    private static String         filename = null;
    private static int            view_ID  = -1;

    private        FirstPanel     top_panel;
    private        FirstMenuBar   top_menubar;

    public Jumpshot()
    {    	
        super( "HDJumpshot" );
        super.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
        
        Category.loadColors("jumpshot-color.property");
        
        TopWindow.First.setWindow( this );

        top_panel    = new FirstPanel( isApplet, filename, view_ID );
        super.setContentPane( top_panel );
        top_menubar  = new FirstMenuBar( isApplet, top_panel );
        super.setJMenuBar( top_menubar );
        
        this.setResizable(false);

        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                if ( Dialogs.confirm( TopWindow.First.getWindow(),
                     "Are you sure you want to exit HDJumpshot ?" ) ) {
                    TopWindow.First.disposeAll();
                }
            }
        } );

        // setVisible( true );
    }

    public void init()
    {
        top_panel.init();
    }

    public void setEditPreferenceButtonEnabled( boolean val )
    {
        top_panel.getEditPreferenceButton().setEnabled( val );
    }

    public void setShowLegendButtonEnabled( boolean val )
    {
        top_panel.getShowLegendButton().setEnabled( val );
    }

    public void setShowTimelineButtonEnabled( boolean val )
    {
        top_panel.getShowTimelineButton().setEnabled( val );
    }
    
    public static void main( String[] args )
    {
        Jumpshot     frame;
        parseCmdLineArgs( args );

        SimpleConsoleLogger.setDebugEverything(true);
        Debug.setActive(true);
        
        viewer.common.Debug.initTextArea();
        // viewer.zoomable.Profile.initTextArea();

        System.out.println( "Starting HDJumpshot" );
        frame     = new Jumpshot();
        frame.pack();
        TopWindow.layoutIdealLocations();
        frame.setVisible( true );
        frame.init();
        
    }

    private static String help_msg = "Syntax: "
                                   + "[options] [hdTrace_filename]\n"
                                   + "Options: \n"
                                   + "\t [-h|-help|--help]                 "
                                   + "\t Display this message.\n"
                                   + "\t [-debug]                          "
                                   + "\t Turn on Debugging output\n"
                                   + "\t [-profile]                        "
                                   + "\t Turn on Profiling output\n"
                                   + "\t [-v view_ID ]                     "
                                   + "\t Default value is -1.\n" ;

    private static void parseCmdLineArgs( String argv[] )
    {
        String        arg_str;
        StringBuffer  err_msg = new StringBuffer();
        int idx = 0;
        try {  // Unnecessary try block
            while ( idx < argv.length ) {
                if ( argv[ idx ].startsWith( "-" ) ) {
                    if (  argv[ idx ].equals( "-h" )
                       || argv[ idx ].equals( "-help" )
                       || argv[ idx ].equals( "--help" ) ) {
                        System.out.println( help_msg );
                        System.out.flush();
                        System.exit( 0 );
                    }
                    else if ( argv[ idx ].equals( "-v" ) ) {
                        arg_str = argv[ ++idx ];
                        view_ID = Integer.parseInt( arg_str );
                        err_msg.append( "\n view_ID = " + arg_str );
                        idx++;
                    }
                    else if ( argv[ idx ].equals( "-debug" ) ) {
                        viewer.common.Debug.setActive( true );
                        idx++;
                    }
                    else if ( argv[ idx ].equals( "-profile" ) ) {
                        viewer.common.Profile.setActive( true );
                        idx++;
                    }
                    else {
                        System.err.println( "Unrecognized option, "
                                          + argv[ idx ] + ", at "
                                          + indexOrderStr( idx+1 )
                                          + " command line argument" );
                        System.out.flush();
                        System.exit( 1 );
                    }
                }
                else {
                    filename   = argv[ idx ];
                    idx++;
                }
            }
        } catch ( NumberFormatException numerr ) {  // Not needed at this moment
            if ( err_msg.length() > 0 )
                System.err.println( err_msg.toString() );
            String idx_order_str = indexOrderStr( idx );
            System.err.println( "Error occurs after option "
                              + argv[ idx-1 ] + ", " + indexOrderStr( idx )
                              + " command line argument.  It needs a number." );            // System.err.println( help_msg );
            numerr.printStackTrace();
        }
    }

    private static String indexOrderStr( int idx )
    {
        switch (idx) {
            case 1  : return Integer.toString( idx ) + "st";
            case 2  : return Integer.toString( idx ) + "nd";
            case 3  : return Integer.toString( idx ) + "rd";
            default : return Integer.toString( idx ) + "th";
        }
    }
}

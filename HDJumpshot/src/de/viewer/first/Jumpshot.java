
 /** Version Control Information $Id: Jumpshot.java 198 2009-04-09 14:48:33Z kunkel $
  * @lastmodified    $Date: 2009-04-09 16:48:33 +0200 (Do, 09. Apr 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 198 $ 
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

package de.viewer.first;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import de.hd.pvs.TraceFormat.SimpleConsoleLogger;

public class Jumpshot extends JFrame
{
	private static final long serialVersionUID = -3761083113345939618L;
	
    private        FirstPanel     top_panel;
    private        FirstMenuBar   top_menubar;

    public Jumpshot(String filename)
    {    	
        super( "Sunshot" );
        super.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );

        top_panel    = new FirstPanel( filename );
        super.setContentPane( top_panel );
        top_menubar  = new FirstMenuBar( top_panel );
        super.setJMenuBar( top_menubar );
        
        this.setResizable(false);

        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
            	MainManager.exitJumpshot();
            }
        } );
        
        this.pack(); 
        
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize()); 
    }
    
    public FirstPanel getTopPanel() {
		return top_panel;
	}
    
    public static void main( String[] args )
    {
        String filename = parseCmdLineArgs( args );

        SimpleConsoleLogger.setDebugEverything(true);
        //Debug.setActive(true);
        
        de.viewer.common.Debug.initTextArea();
        // viewer.zoomable.Profile.initTextArea();

        System.out.println( "Starting Sunshot" );
        MainManager.init(filename);
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
                                   + "\t Default value is -1.\n" ;

    private static String parseCmdLineArgs( String argv[] )
    {
        StringBuffer  err_msg = new StringBuffer();
        int idx = 0;
        String filename = null;
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
                    else if ( argv[ idx ].equals( "-debug" ) ) {
                        de.viewer.common.Debug.setActive( true );
                        idx++;
                    }
                    else if ( argv[ idx ].equals( "-profile" ) ) {
                        de.viewer.common.Profile.setActive( true );
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
            System.err.println( "Error occurs after option "
                              + argv[ idx-1 ] + ", " + indexOrderStr( idx )
                              + " command line argument.  It needs a number." );            // System.err.println( help_msg );
            numerr.printStackTrace();
        }
        
        return filename;
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

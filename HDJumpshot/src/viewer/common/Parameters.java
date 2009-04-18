
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

package viewer.common;

import java.awt.Component;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


import drawable.ArrowDrawer;
import drawable.EventDrawer;
import drawable.StateBorder;
import drawable.StateDrawer;


public class Parameters
{
    private static final String       VERSION_INFO             = "2.0";
    private static       String       setupfile_path           = null;

    // Options: Zoomable window reinitialization (requires window restart)
    public  static       boolean      AUTO_WINDOWS_LOCATION    = true;
    public  static       float        SCREEN_HEIGHT_RATIO      = 0.5f;
    public  static       float        TIME_SCROLL_UNIT_RATIO   = 0.01f;

    // Options: All zoomable windows
    public  static       boolean      Y_AXIS_ROOT_VISIBLE      = true;
    public  static       boolean      ACTIVE_REFRESH           = true;
    public  static       Alias        BACKGROUND_COLOR         = Const.COLOR_BLACK;

    public  static       float        STATE_HEIGHT_FACTOR      = 0.90f;
    public  static       float        NESTING_HEIGHT_FACTOR    = 0.80f;
    public  static       Alias        ARROW_ANTIALIASING
                                      = Const.ANTIALIAS_DEFAULT;
    public  static       int          MIN_WIDTH_TO_DRAG        = 4;
    public  static       boolean      LEFTCLICK_INSTANT_ZOOM   = true;

    // Options: Timeline zoomable window
    public  static       StateBorder  STATE_BORDER
                                      = StateBorder.COLOR_RAISED_BORDER;
    public  static       int          ARROW_HEAD_LENGTH        = 10;
    public  static       int          ARROW_HEAD_WIDTH         = 6;
    public  static       int          EVENT_BASE_WIDTH         = 8;

    public  static       int          SEARCH_ARROW_LENGTH      = 20;
    public  static       int          SEARCH_FRAME_THICKNESS   = 3;
    public  static       boolean      SEARCHED_OBJECT_ON_TOP   = false;

    // Options: Histogram zoomable window
    public  static       StateBorder  PROFILE_STATE_BORDER     = StateBorder.COLOR_RAISED_BORDER;

    public static final void initSetupFile()
    {
        String user_homedir, file_sep;
        user_homedir   = System.getProperty( "user.home" );
        file_sep       = System.getProperty( "file.separator" );
        setupfile_path = user_homedir + file_sep + ".jumpshot4.conf";
        System.out.println( "Jumpshot-4 setup file : " + setupfile_path );
    }

    public static void initStaticClasses()
    {
        // Define the Font used in ModelXXXXPanels and PreferencePanel
        LabeledTextField.setDefaultFont( Const.FONT );
        LabeledComboBox.setDefaultFont( Const.FONT );
        // Define the size of ArrowHead
        ArrowDrawer.setHeadLength( Parameters.ARROW_HEAD_LENGTH );
        ArrowDrawer.setHeadWidth( Parameters.ARROW_HEAD_WIDTH );
        // Define the size of EventBase
        EventDrawer.setBaseWidth( Parameters.EVENT_BASE_WIDTH ); 
        // Define state border type
        StateDrawer.setBorderStyle( Parameters.STATE_BORDER );
    }

    public static final void writeToSetupFile( Component parent_window )
    {
        if ( ! Dialogs.confirm( parent_window,
                      "Save preferred settings to the setup file ?" ) ) 
            return;

        Properties pptys = new Properties();
        pptys.setProperty( "VERSION_INFO", VERSION_INFO );

        // Options: Zoomable window reinitialization (requires window restart)
        pptys.setProperty( "AUTO_WINDOWS_LOCATION",
                           String.valueOf( AUTO_WINDOWS_LOCATION ) );
        pptys.setProperty( "SCREEN_HEIGHT_RATIO",
                           String.valueOf( SCREEN_HEIGHT_RATIO ) );
        pptys.setProperty( "TIME_SCROLL_UNIT_RATIO",
                           String.valueOf( TIME_SCROLL_UNIT_RATIO ) );

        // Options: All zoomable windows
        pptys.setProperty( "Y_AXIS_ROOT_VISIBLE",
                           String.valueOf( Y_AXIS_ROOT_VISIBLE ) );
        pptys.setProperty( "ACTIVE_REFRESH",
                           String.valueOf( ACTIVE_REFRESH ) );
        pptys.setProperty( "BACKGROUND_COLOR",
                           String.valueOf( BACKGROUND_COLOR ) );

        pptys.setProperty( "STATE_HEIGHT_FACTOR",
                           String.valueOf( STATE_HEIGHT_FACTOR ) );
        pptys.setProperty( "NESTING_HEIGHT_FACTOR",
                           String.valueOf( NESTING_HEIGHT_FACTOR ) );
        pptys.setProperty( "ARROW_ANTIALIASING",
                           String.valueOf( ARROW_ANTIALIASING ) );
        pptys.setProperty( "MIN_WIDTH_TO_DRAG",
                           String.valueOf( MIN_WIDTH_TO_DRAG ) );
        pptys.setProperty( "LEFTCLICK_INSTANT_ZOOM",
                           String.valueOf( LEFTCLICK_INSTANT_ZOOM ) );

        // Options: Timeline zoomable window
        pptys.setProperty( "STATE_BORDER",
                           String.valueOf( STATE_BORDER ) );
        pptys.setProperty( "ARROW_HEAD_LENGTH",
                           String.valueOf( ARROW_HEAD_LENGTH ) );
        pptys.setProperty( "ARROW_HEAD_WIDTH",
                           String.valueOf( ARROW_HEAD_WIDTH ) );
        pptys.setProperty( "EVENT_BASE_WIDTH",
                           String.valueOf( EVENT_BASE_WIDTH ) );

        pptys.setProperty( "SEARCH_ARROW_LENGTH",
                           String.valueOf( SEARCH_ARROW_LENGTH ) );
        pptys.setProperty( "SEARCH_FRAME_THICKNESS",
                           String.valueOf( SEARCH_FRAME_THICKNESS ) );
        pptys.setProperty( "SEARCHED_OBJECT_ON_TOP",
                           String.valueOf( SEARCHED_OBJECT_ON_TOP ) );

        pptys.setProperty( "PROFILE_STATE_BORDER",
                           String.valueOf( PROFILE_STATE_BORDER ) );


        try {
            FileOutputStream fouts = new FileOutputStream( setupfile_path );
            pptys.store( fouts, " Jumpshot-4 setup file" );
            fouts.close();
        } catch ( IOException ioerr ) {
            ioerr.printStackTrace();
            System.exit( 1 );
        }
    }

    public static final void readFromSetupFile( Component parent_window )
    {
        String   ppty_val;
        boolean  isFileFound;

        isFileFound = false;
        Properties pptys = new Properties();
        try {
            FileInputStream fins = new FileInputStream( setupfile_path );
            pptys.load( fins );
            fins.close();
            isFileFound = true;
        } catch ( FileNotFoundException ioerr ) {
            System.out.println( "Creating Jumpshot-4 setup file ..." );
            Dialogs.info( parent_window,
                     "It seems this is your first time using Jumpshot-4,\n"
                   + "a setup file will be created in your home directory\n"
                   + "with the default settings.", null );
            writeToSetupFile( parent_window );
        } catch ( IOException ioerr ) {
            ioerr.printStackTrace();
            System.exit( 1 );
        }

        ppty_val = pptys.getProperty( "VERSION_INFO" );
        if ( ! VERSION_INFO.equals( ppty_val ) && isFileFound )
            Dialogs.warn( parent_window,
                          "Version mismatch! This Jumpshot-4 is of version "
                        + VERSION_INFO +" not version " + ppty_val + " that "
                        + "is specified in your setup file.\n"
                        + "You may want to SAVE your preferences again in the "
                        + "Preference window to avoid this warning message." );

        // Options: Zoomable window reinitialization (requires window restart)
        ppty_val = pptys.getProperty( "Y_AXIS_ROOT_LABEL" );
        ppty_val = pptys.getProperty( "AUTO_WINDOWS_LOCATION" );
        if ( ppty_val != null )
            AUTO_WINDOWS_LOCATION =    ppty_val.equalsIgnoreCase( "true" )
                                    || ppty_val.equalsIgnoreCase( "yes" );
        ppty_val = pptys.getProperty( "SCREEN_HEIGHT_RATIO" );
        if ( ppty_val != null )
            SCREEN_HEIGHT_RATIO = Float.parseFloat( ppty_val );
        ppty_val = pptys.getProperty( "TIME_SCROLL_UNIT_RATIO" );
        if ( ppty_val != null )
            TIME_SCROLL_UNIT_RATIO = Float.parseFloat( ppty_val );


        // Options: All zoomable windows
        ppty_val = pptys.getProperty( "Y_AXIS_ROOT_VISIBLE" );
        if ( ppty_val != null ) 
            Y_AXIS_ROOT_VISIBLE =    ppty_val.equalsIgnoreCase( "true" )
                                  || ppty_val.equalsIgnoreCase( "yes" );
        
        ppty_val = pptys.getProperty( "ACTIVE_REFRESH" );
        if ( ppty_val != null )
            ACTIVE_REFRESH =    ppty_val.equalsIgnoreCase( "true" )
                             || ppty_val.equalsIgnoreCase( "yes" );
        
        ppty_val = pptys.getProperty( "ROW_RESIZE_MODE" );
        ppty_val = pptys.getProperty( "BACKGROUND_COLOR" );
        if ( ppty_val != null )
            BACKGROUND_COLOR = Const.parseBackgroundColor( ppty_val );
        /*
        ppty_val = pptys.getProperty( "Y_AXIS_ROW_HEIGHT" );
        if ( ppty_val != null )
            Y_AXIS_ROW_HEIGHT = Integer.parseInt( ppty_val );
        */

        ppty_val = pptys.getProperty( "STATE_HEIGHT_FACTOR" );
        if ( ppty_val != null )
            STATE_HEIGHT_FACTOR = Float.parseFloat( ppty_val );
        ppty_val = pptys.getProperty( "NESTING_HEIGHT_FACTOR" );
        if ( ppty_val != null )
            NESTING_HEIGHT_FACTOR = Float.parseFloat( ppty_val );
        ppty_val = pptys.getProperty( "ARROW_ANTIALIASING" );
        if ( ppty_val != null )
            ARROW_ANTIALIASING = Const.parseAntiAliasing( ppty_val );
        ppty_val = pptys.getProperty( "MIN_WIDTH_TO_DRAG" );
        if ( ppty_val != null )
            MIN_WIDTH_TO_DRAG = Integer.parseInt( ppty_val );
        ppty_val = pptys.getProperty( "CLICK_RADIUS_TO_LINE" );
        if ( ppty_val != null )
            LEFTCLICK_INSTANT_ZOOM =    ppty_val.equalsIgnoreCase( "true" )
                                     || ppty_val.equalsIgnoreCase( "yes" );

        // Options: Timeline zoomable window
        ppty_val = pptys.getProperty( "STATE_BORDER" );
        if ( ppty_val != null )
            STATE_BORDER = StateBorder.parseString( ppty_val );
        ppty_val = pptys.getProperty( "ARROW_HEAD_LENGTH" );
        if ( ppty_val != null )
            ARROW_HEAD_LENGTH = Integer.parseInt( ppty_val );
        ppty_val = pptys.getProperty( "ARROW_HEAD_WIDTH" );
        if ( ppty_val != null )
            ARROW_HEAD_WIDTH = Integer.parseInt( ppty_val );
        ppty_val = pptys.getProperty( "EVENT_BASE_WIDTH" );
        if ( ppty_val != null )
            EVENT_BASE_WIDTH = Integer.parseInt( ppty_val );

        ppty_val = pptys.getProperty( "PREVIEW_STATE_BORDER" );
        
        ppty_val = pptys.getProperty( "SEARCH_ARROW_LENGTH" );
        if ( ppty_val != null )
            SEARCH_ARROW_LENGTH = Integer.parseInt( ppty_val );
        ppty_val = pptys.getProperty( "SEARCH_FRAME_THICKNESS" );
        if ( ppty_val != null )
            SEARCH_FRAME_THICKNESS = Integer.parseInt( ppty_val );
        ppty_val = pptys.getProperty( "SEARCHED_OBJECT_ON_TOP" );
        if ( ppty_val != null )
            SEARCHED_OBJECT_ON_TOP =    ppty_val.equalsIgnoreCase( "true" )
                                     || ppty_val.equalsIgnoreCase( "yes" );

        // Options: Histogram zoomable window
        ppty_val = pptys.getProperty( "HISTOGRAM_ZERO_ORIGIN" );
        ppty_val = pptys.getProperty( "PROFILE_STATE_BORDER" );
        if ( ppty_val != null )
            PROFILE_STATE_BORDER = StateBorder.parseString( ppty_val );
    }
}

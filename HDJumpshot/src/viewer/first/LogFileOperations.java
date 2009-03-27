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

package viewer.first;

import hdTraceInput.TraceFormatBufferedFileReader;

import java.awt.Window;
import java.io.File;

import javax.swing.JTextField;

import viewer.common.Dialogs;
import viewer.common.LogFileChooser;
import viewer.common.Parameters;
import viewer.common.PreferenceFrame;
import viewer.common.SwingWorker;
import viewer.common.TopWindow;
import viewer.legends.LegendFrame;
import viewer.pvfs2.PVFS2Slog2FileParser;
import viewer.timelines.TimelineFrame;


public class LogFileOperations
{
    private        LogFileChooser    file_chooser;

    private        TraceFormatBufferedFileReader file;
    private        PreferenceFrame   pptys_frame;
    private        LegendFrame       legend_frame;
    private        TimelineFrame     timeline_frame;

    public LogFileOperations( boolean isApplet )
    {
        file_chooser    = new LogFileChooser( isApplet );

        file        = null;
        legend_frame    = null;
        timeline_frame  = null;
    }

    public void init()
    {
        /*  Initialization  */
        Parameters.initSetupFile();
        Parameters.readFromSetupFile( TopWindow.First.getWindow() );
        Parameters.initStaticClasses();
        pptys_frame     = new PreferenceFrame();
        pptys_frame.setVisible( false );
    }

    private TraceFormatBufferedFileReader createInputLog( Window window, String pathname )
    {
        String logname = pathname.trim();
        if ( logname != null && logname.length() > 0 ) {
            File logfile = new File( logname );
            if ( ! logfile.exists() ) {
                Dialogs.error( window,
                               "File Not Found when initializing "
                             + logname + "." );
                return null;
            }
            if ( logfile.isDirectory() ) {
                Dialogs.error( window,
                               logname + " is a directory." );
                return null;
            }
            if ( ! logfile.canRead() ) {
                Dialogs.error( window,
                               "File " + logname + " cannot be read." );
                return null;
            }

            TraceFormatBufferedFileReader reader = null;
            try {
                reader = new TraceFormatBufferedFileReader( );
                reader.loadAdditionalFile(logname);
            } catch ( NullPointerException nperr ) {
                Dialogs.error( window,
                               "NullPointerException when initializing "
                             + logname + "!" );
                return null;
            } catch ( Exception err ) {
            	err.printStackTrace();
                Dialogs.error( window,
                               "EOFException when initializing "
                             + logname + "!" );
                return null;
            }
            return reader;
        }
        else {
            if ( logname == null )
                Dialogs.error( window, "Null pathname!" );
            else // if ( logname.length() == 0 )
                Dialogs.error( window, "pathname is empty!" );
            return null;
        }

    }

    /* This disposes all the windows and InputLog related resources. */
    public void disposeLogFileAndResources()
    {
        if ( file != null ) {
            TopWindow.Legend.disposeAll();
            // TODO:
            // file.close();
            file        = null;
            legend_frame    = null;
            timeline_frame  = null;
        }
    }

    public String selectLogFile()
    {
        int   istat;
        istat = file_chooser.showOpenDialog( TopWindow.First.getWindow() );
        if ( istat == LogFileChooser.APPROVE_OPTION ) {
            File   selected_file, selected_dir;
            selected_file = file_chooser.getSelectedFile();
            if ( selected_file != null ) {
                selected_dir  = selected_file.getParentFile();
                if ( selected_dir != null )
                    file_chooser.setCurrentDirectory( selected_dir );
                return selected_file.getPath();
            }
        }
        else
            Dialogs.info( TopWindow.First.getWindow(), "No file chosen", null );
        return null;
    }

    /*
        this.disposeLogFileAndResources() has to be called
        before this.openLogFile() can be invoked.
    */
    public void openLogFile( JTextField  logname_txtfld )
    {
        String filename;
        filename  = logname_txtfld.getText();
        file  = createInputLog(TopWindow.First.getWindow(), filename );
        if ( file == null ) {
            return;
        }

        legend_frame = new LegendFrame( file );
        legend_frame.pack();
        TopWindow.layoutIdealLocations();
        legend_frame.setVisible( true );
        
        createTimelineWindow( );

        //TODO PVFS2 stuff
        PVFS2Slog2FileParser.parseSlog2(filename);
    }

    public void createTimelineWindow( )
    {
        if ( file != null ) {
            SwingWorker create_timeline_worker = new SwingWorker() {
                public Object construct()
                {
                    timeline_frame = new TimelineFrame( file );
                    return null;  // returned value is not needed
                }
                public void finished()
                {
                    timeline_frame.pack();
                    TopWindow.layoutIdealLocations();
                    timeline_frame.setVisible( true );
                    timeline_frame.init();
                }
            };
            create_timeline_worker.start();
        }
    }

    public void showLegendWindow()
    {
        if ( file != null && legend_frame != null ) {
            legend_frame.pack();
            TopWindow.layoutIdealLocations();
            legend_frame.setVisible( true );
        }
    }
    
    public void showTimelineWindow()
    {
        if ( file != null && timeline_frame != null ) {
        	timeline_frame.pack();
            TopWindow.layoutIdealLocations();
            timeline_frame.setVisible( true );
        }
    }

    public void showPreferenceWindow()
    {
        if ( pptys_frame != null ) {
            pptys_frame.pack();
            TopWindow.layoutIdealLocations();
            pptys_frame.setVisible( true );
            pptys_frame.toFront();
        }
    }
}

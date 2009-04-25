
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

package viewer.first;

import hdTraceInput.TraceFormatBufferedFileReader;

import java.io.File;
import java.io.IOException;

import viewer.common.LogFileChooser;


public class FileOperations
{
	private        TraceFormatBufferedFileReader reader = null;

	void addTraceProject( String pathname )  throws Exception
	{
		final String logname = pathname.trim();
		if ( logname != null && logname.length() > 0 ) {
			File logfile = new File( logname );
			if ( ! logfile.exists() ) {
				throw new IOException("File Not Found when initializing" );
			}
			if ( logfile.isDirectory() ) {
				throw new IOException("File is a directory." );
			}
			if ( ! logfile.canRead() ) {
				throw new IOException("File cannot be read." );
			}

			reader.loadAdditionalFile(logname);		
			return;
		}
		else {
			if ( logname == null )
				throw new IOException("Null pathname!" );
			else // if ( logname.length() == 0 )
				throw new IOException("pathname is empty!" );
		}
	}

	public String selectTraceProject()
	{
		int   istat;
		LogFileChooser file_chooser = new LogFileChooser();
		istat = file_chooser.showOpenDialog( MainManager.getJumpshotWindow() );
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
		return null;
	}

	void openTraceProject( String filename ) throws Exception
	{
			reader = new TraceFormatBufferedFileReader( );
			addTraceProject( filename );
	}
	
	TraceFormatBufferedFileReader getReader() {
		return reader;
	}

}
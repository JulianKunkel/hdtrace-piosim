/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */


package viewer.common;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class LogFileChooser extends JFileChooser
{
	private boolean  isApplet;

	public LogFileChooser( boolean isTopApplet )
	{
		super( System.getProperty( "user.dir" ) );
		super.setDialogTitle( "Select HDTraceProject file" );

		isApplet = isTopApplet;

		FileFilter  filter;

		filter = new LogPermitDirFilter(
				new String[]{ "xml" } );
		super.addChoosableFileFilter( filter );

	}

	public boolean isTraversable( File file )
	{
		if ( isApplet )
			if ( file != null )
				return ! file.isDirectory();
			else
				return false;
		else
			return super.isTraversable( file );
	}
}

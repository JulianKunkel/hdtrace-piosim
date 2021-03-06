
 /** Version Control Information $Id: LogPermitDirFilter.java 149 2009-03-27 13:55:56Z kunkel $
  * @lastmodified    $Date: 2009-03-27 14:55:56 +0100 (Fr, 27. Mär 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 149 $ 
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

package de.viewer.common;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class LogPermitDirFilter extends FileFilter
{
	String        extns[];
	StringBuffer  description;

	public LogPermitDirFilter( String[] in_extns )
	{
		extns        = new String[ in_extns.length ];
		description  = new StringBuffer();
		for ( int idx = 0; idx < extns.length; idx++ ) {
			extns[ idx ] = ( new String(in_extns[ idx ]) ).trim();
			description.append( " *." + extns[ idx ] );
		}
		description.append( " and directories" );
	}

	// Accept __Directories__ and Accept files with one of extns[] suffix.
	public boolean accept( File file )
	{
		if ( file.isDirectory() )
			return true;

		String extension = LogPermitDirFilter.getFileExtension( file );
		if ( extension != null ) {
			for ( int idx = 0; idx < extns.length; idx++ ) {
				if ( extension.equals( extns[ idx ] ) )
					return true;
			}
		}

		return false;
	}

	// The description of this filter
	public String getDescription()
	{
		return description.toString();
	}

	private static String getFileExtension( File file )
	{
		String name  = file.getName();
		int    idx   = name.lastIndexOf( '.' );

		String ext   = null;
		if ( idx > 0 && idx < name.length() - 1 )
			ext = name.substring( idx+1 ).toLowerCase();
		return ext;
	}
}


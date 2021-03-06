
 /** Version Control Information $Id: LogFileChooser.java 256 2009-05-01 08:24:30Z kunkel $
  * @lastmodified    $Date: 2009-05-01 10:24:30 +0200 (Fr, 01. Mai 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 256 $ 
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

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class LogFileChooser extends JFileChooser
{
	private static final long serialVersionUID = 2291243194312596269L;

	public LogFileChooser( String olddir )
	{
		super(olddir);
		super.setDialogTitle( "Select HDTraceProject file" );

		FileFilter  filter;

		filter = new LogPermitDirFilter( new String[]{ "proj" } );
		super.addChoosableFileFilter( filter );
		super.setFileFilter(filter);
	}
}

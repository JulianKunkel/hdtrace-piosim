
 /** Version Control Information $Id: Fileopen.java 149 2009-03-27 13:55:56Z kunkel $
  * @lastmodified    $Date: 2009-03-27 14:55:56 +0100 (Fr, 27 Mrz 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 149 $
  */


//	Copyright (C) 2008, 2009 Julian M. Kunkel
//
//	This file is part of PIOsimHD.
//
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.piosim.model.program.commands;

import de.hd.pvs.piosim.model.annotations.restrictions.NotNull;
import de.hd.pvs.piosim.model.program.commands.superclasses.FileCommand;
import de.hd.pvs.piosim.model.program.fileView.FileView;

/**
 * Simulates a file open (i.e. might truncate the file).
 *
 * @author Julian M. Kunkel
 *
 */
public class Filesetview
	extends FileCommand
{
	@NotNull
	private FileView fileView;

	public void setFileView(FileView fileView) {
		this.fileView = fileView;
	}

	public FileView getFileView() {
		return fileView;
	}
}
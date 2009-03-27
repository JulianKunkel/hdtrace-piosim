
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
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

/**
 * 
 */
package de.hd.pvs.piosim.model.program.commands.superclasses;

import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNull;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;

/**
 * Superclass for all file I/O related operations. 
 * 
 * @author Julian M. Kunkel
 */
abstract public class FileIOCommand extends FileCommand{

	@NotNull	
	protected ListIO io; 	
	
	/**
	 * @return the file the command should operate on.
	 */
	public MPIFile getFile() {
		return file;
	}
	
	@Override
	public void readXML(XMLTag xml) throws Exception {  
		io = new ListIO(xml.getNestedXMLTagsWithName("Data"));
	}

	/**
	 * Return the ListIO
	 * @return
	 */
	public ListIO getIOList() {
		return io;
	}
	
	public void setListIO(ListIO io) {
		this.io = io;
	}	
}

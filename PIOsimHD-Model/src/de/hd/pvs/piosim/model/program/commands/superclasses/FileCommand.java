
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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNull;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;

/**
 * Superclass for all I/O related operations. 
 * 
 * @author Julian M. Kunkel
 */
abstract public class FileCommand extends Command{

	@NotNull
	@Attribute()
	protected MPIFile file;
	
	@NotNull	
	protected ListIO io; 	
	
	/**
	 * @return the file the command should operate on.
	 */
	public MPIFile getFile() {
		return file;
	}
	
	@Override
	public void readXML(Element xml) throws Exception {
		NodeList list = ((Element) xml).getElementsByTagName("Data");   
		io = new ListIO(list);
	}

	/**
	 * Return the ListIO
	 * @return
	 */
	public ListIO getIOList() {
		return io;
	}
	
	public void setFile(MPIFile file) {
		this.file = file;
	}
	
	public void setListIO(ListIO io) {
		this.io = io;
	}	
}

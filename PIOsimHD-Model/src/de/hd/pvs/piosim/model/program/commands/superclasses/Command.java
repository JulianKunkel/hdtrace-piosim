
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

import com.sun.istack.internal.NotNull;

import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.piosim.model.interfaces.IXMLReader;
import de.hd.pvs.piosim.model.program.Program;

/**
 * A command is a set of logical instructions which have a specific goal. A process performs a
 * sequence of instructions.
 *
 * @author Julian M. Kunkel
 *
 */
abstract public class Command implements IXMLReader{
	private Program myProgram;
	private Integer asynchronousID; //null if blocking

	@NotNull
	private XMLTag xmlTag;


	public XMLTag getXMLTag() {
		return xmlTag;
	}

	/**
	 * Return the program this command belongs to
	 * @return
	 */
	public Program getProgram(){
		return myProgram;
	}

	/**
	 * @return the asynchronousID or null if blocking
	 */
	public Integer getAsynchronousID() {
		return asynchronousID;
	}

	/**
	 * @param asynchronousID the asynchronousID to set
	 */
	public void setAsynchronousID(Integer asynchronousID) {
		this.asynchronousID = asynchronousID;
	}

	/**
	 * Return true if this command is an non-blocking operation.
	 * @return
	 */
	public boolean isAsynchronous(){
		return asynchronousID != null;
	}


	/**
	 * @param myProgram the myProgram to set
	 */
	public void setProgram(Program myProgram) {
		this.myProgram = myProgram;
	}

	/**
	 * Subclasses should override it to read command depending XML which is not handled in the
	 * ApplicationXMLReader.
	 */
	public void readXML(XMLTag xmlTag) throws Exception{

	}

	public void setXMLTag(XMLTag xmlTag){
		this.xmlTag = xmlTag;
	}

	/**
	 * Subclasses should override it to write command depending XML which is not handled in the
	 * ApplicationXMLWriter.
	 */
	public void writeXML(StringBuffer sb) {

	}

	//////////////////////////////// END PUBLIC MEMBERS ///////////////////////////////////////////

}

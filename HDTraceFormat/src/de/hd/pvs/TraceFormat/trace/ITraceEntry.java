
 /** Version Control Information $Id: TraceEntry.java 325 2009-06-01 15:42:47Z kunkel $
  * @lastmodified    $Date: 2009-06-01 17:42:47 +0200 (Mo, 01 Jun 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 325 $ 
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

package de.hd.pvs.TraceFormat.trace;

import java.util.ArrayList;
import java.util.HashMap;

import de.hd.pvs.TraceFormat.ITracableObject;
import de.hd.pvs.TraceFormat.xml.XMLTag;

/**
 * Contains the information about a single trace entry. Superclass for StateTraceEntry and
 * EventTraceEntry.
 * 
 * @author Julian M. Kunkel
 *
 */
public interface ITraceEntry extends ITracableObject{
	/**
	 * Return the name of the trace entry.
	 */
	public String getName();
	
	/**
	 * Return a attribute
	 * @param attribute
	 * @return
	 */
	public String getAttribute(String attribute);
	
	/**
	 * Return all attributes
	 * @return
	 */
	public HashMap<String, String> getAttributes();
	
	/**
	 * Return nested XML data i.e. data of the Trace Entry.
	 * @return
	 */
	public ArrayList<XMLTag> getContainedXMLData();
}

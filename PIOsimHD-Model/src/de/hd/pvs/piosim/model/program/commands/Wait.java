
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
package de.hd.pvs.piosim.model.program.commands;

import java.util.ArrayList;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNull;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;

/**
 * Realizes an MPI_Wait for a set of non-blocking IDs.
 *
 * @author Julian M. Kunkel
 */
public class Wait extends Command {

	@NotNull
	protected ArrayList<Integer> waitfor = new ArrayList<Integer>();

	public void readCommandXML(XMLTag xml) throws Exception {
		final LinkedList<XMLTag> elems = xml.getNestedXMLTagsWithName("FOR");
		for (XMLTag e : elems) {
			waitfor.add(Integer.parseInt(  e.getAttribute("aid") ));
		}
	}

	/**
	 * @return a list of ranks which should be waited.
	 */
	public ArrayList<Integer> getWaitFor() {
		assert(waitfor != null);

		return waitfor;
	}

	public void setWaitFor(ArrayList<Integer> waitfor) {
		assert(waitfor != null);
		this.waitfor = waitfor;
	}
}

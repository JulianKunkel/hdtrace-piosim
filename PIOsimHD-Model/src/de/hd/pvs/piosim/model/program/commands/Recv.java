
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

package de.hd.pvs.piosim.model.program.commands;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.Rank;
import de.hd.pvs.piosim.model.program.commands.superclasses.CommunicatorCommand;


public class Recv  extends CommunicatorCommand{
	@Attribute
	protected int fromTag=-1;

	@Attribute // negative means any source
	@Rank
	int fromRank=-1;

	@Override
	public String toString() {
		return "Recv <tag,from> " +  fromTag + "," + fromRank;
	}

	public int getFromTag() {
		return fromTag;
	}

	public int getFromRank() {
		return fromRank;
	}

	public void setFromRank(int fromRank) {
		this.fromRank = fromRank;
	}

	public void setFromTag(int tag) {
		this.fromTag = tag;
	}
}

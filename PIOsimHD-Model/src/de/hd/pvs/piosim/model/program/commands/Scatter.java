
//	Copyright (C) 2011 Julian M. Kunkel
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
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegative;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;
import de.hd.pvs.piosim.model.program.commands.superclasses.CommunicatorCommand;


public class Scatter extends CommunicatorCommand{

	// The size to send to each client
	@NotNegativeOrZero
	@Attribute
	protected long size = -1;

	@NotNegative
	@Attribute
	protected int rootRank = -1;

	@Override
	public String toString() {
		return "Scatter <" + rootRank + "," + size + ">" ;
	}

	/**
	 * @return the rootRank
	 */
	public int getRootRank() {
		return rootRank;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	public void setRootRank(int rootRank) {
		this.rootRank = rootRank;
	}

	public void setSize(long size) {
		this.size = size;
	}
}
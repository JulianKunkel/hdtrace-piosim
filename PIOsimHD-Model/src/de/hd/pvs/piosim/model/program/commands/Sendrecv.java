
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
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegative;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;
import de.hd.pvs.piosim.model.program.commands.superclasses.CommunicatorCommand;


public class Sendrecv  extends CommunicatorCommand{
	@NotNegative
	@Attribute
	protected int toTag = 0;
	
	@NotNegative
	@Attribute
	protected int toRank = 0;
	
	@NotNegative
	@Attribute
	protected int fromTag = 0;
	
	@NotNegative
	@Attribute
	protected int fromRank = 0;

	@NotNegativeOrZero
	@Attribute
	protected long size = 0;

	@Override
	public String toString() {
		return "Sendrecv <to,to-tag,size,from,from-tag> " +  toRank + "," +toTag + "," + size + "," + fromRank + "," + fromTag;
	}
	
	public int getToRank() {
		return toRank;
	}
	
	/**
	 * @return the toTag
	 */
	public int getToTag() {
		return toTag;
	}
	
	/**
	 * @return the fromRank
	 */
	public int getFromRank() {
		return fromRank;
	}
	
	/**
	 * @return the fromTag
	 */
	public int getFromTag() {
		return fromTag;
	}


	public long getSize() {
		return size;
	}
	
	public void setFromRank(int fromRank) {
		this.fromRank = fromRank;
	}
	
	public void setFromTag(int fromTag) {
		this.fromTag = fromTag;
	}
	
	public void setSize(long size) {
		this.size = size;
	}
	
	public void setToRank(int toRank) {
		this.toRank = toRank;
	}
	
	public void setToTag(int toTag) {
		this.toTag = toTag;
	}
}

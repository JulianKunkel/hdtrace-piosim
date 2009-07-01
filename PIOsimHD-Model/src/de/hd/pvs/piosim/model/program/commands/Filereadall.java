/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$
 */

//	Copyright (C) 2009 Michael Kuhn
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

import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.ListIO.SingleIOOperation;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.superclasses.FileIOCommand;

public class Filereadall extends FileIOCommand {
	Communicator comm;

	long startOffset;
	long endOffset;

	public Filereadall(Communicator comm) {
		this.comm = comm;

		startOffset = -1;
		endOffset = -1;
	}

	public Communicator getCommunicator() {
		return this.comm;
	}

	public void setListIO(ListIO io) {
		this.io = io;

		if (io.getIOOperations().size() > 0) {
			startOffset = io.getIOOperations().get(0).getOffset();
			endOffset = io.getIOOperations().get(0).getOffset() + io.getIOOperations().get(0).getAccessSize();

			for (SingleIOOperation op : io.getIOOperations())
			{
				startOffset = Math.min(startOffset, op.getOffset());
				endOffset = Math.max(endOffset, op.getOffset() + op.getAccessSize());
			}
		}
	}

	public long getStartOffset() {
		return startOffset;
	}

	public long getEndOffset() {
		return endOffset;
	}
}

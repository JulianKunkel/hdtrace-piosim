//	Copyright (C) 2010 Julian Kunkel
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
package de.hd.pvs.piosim.simulator.program.Filereadall;

import de.hd.pvs.piosim.simulator.program.Filereadall.Splitter.ContiguousIOSplitter;
import de.hd.pvs.piosim.simulator.program.Filereadall.Splitter.IOSplitter;

/**
 * Simplyfied two phase protocol with different block assignment.
 *
 * Range divided by 3 clients: 012 012 012 0
 * Operations per phase:       000 111 222 3 (the 3 phase is partial, the last client might read partial twoPhaseBufferSize)
 *
 * @author julian
 */
public class ContiguousTwoPhase extends MultiPhaseRead {
	@Override
	protected IOSplitter initalizeIOSplitter() {
		return new ContiguousIOSplitter();
	}
}
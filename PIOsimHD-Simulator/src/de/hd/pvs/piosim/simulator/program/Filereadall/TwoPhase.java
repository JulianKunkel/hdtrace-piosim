/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$
 */

//	Copyright (C) 2010 Julian Kunkel
//          based on the version of Michael Kuhn in 2009.
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

import de.hd.pvs.piosim.simulator.program.Filereadall.Splitter.IOSplitter;
import de.hd.pvs.piosim.simulator.program.Filereadall.Splitter.TwoPhaseIOSplitter;


/**
 * Simplyfied two phase protocol.
 *
 * First synchronize all processes with an virtual barrier i.e. fastest synchronization possible.
 * If all I/Os overlapp i.e. no hole is in the client access then perform Two-Phase:
 *  prepare: combine and sort I/O operations from all clients.
 *    divide the byte range by the number of clients and phases
 *  phases:
 *    read in a maximum of buffersize
 *    transfer data to responsible client(s) (if not self).
 *    receive data from all other clients
 *
 * If a client does not need any data but uses a collective call, then this client will not participate in the multi-phase.
 *
 * Range divided by 3 clients: 00000 111111  22222 2
 * Operations per phase:       01234 012345  01234 5 (the 5 phase is partial, the last client might read partial twoPhaseBufferSize)
 *
 * Assumptions: The ListIOs of all commands are sorted!
 *
 * @author julian
 */
public class TwoPhase extends MultiPhaseRead {
	@Override
	protected IOSplitter initalizeIOSplitter() {
		return new TwoPhaseIOSplitter();
	}
}

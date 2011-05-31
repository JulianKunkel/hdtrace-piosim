
//	Copyright (C) 2008, 2009, 2010, 2011 Julian M. Kunkel
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

package de.hd.pvs.piosim.simulator.program.Bcast;

import de.hd.pvs.piosim.model.program.commands.Allgather;
import de.hd.pvs.piosim.model.program.commands.Barrier;
import de.hd.pvs.piosim.model.program.commands.Bcast;
import de.hd.pvs.piosim.model.program.commands.Scatter;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;
/**
 * MPICH2 alike Implementation, first a scatter operation is invoked with the direct method, then a barrier, then the data is gathered with the gatherall implementation.
 */
public class BroadcastScatterBarrierGatherall
extends CommandImplementation<Bcast>
{
	final int SCATTER_COMPLETED = 2;
	final int BARRIER_COMPLETED = 3;

	@Override
	public void process(Bcast cmd, ICommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs)
	{
		// default: split data equally. Remaining bytes are just send to all processes
		// the amount of data to gather from each node is the assigned data from the scatter, this can be approximated by the equation:
		long sizePerRank = cmd.getSize() / cmd.getCommunicator().getSize() +  cmd.getSize() % cmd.getCommunicator().getSize() ;

		if(step == CommandProcessing.STEP_START){

			Scatter scmd = new Scatter();
			scmd.setRootRank(cmd.getRootRank());
			scmd.setSize(sizePerRank);
			scmd.setCommunicator(cmd.getCommunicator());

			OUTresults.invokeChildOperation(scmd, BARRIER_COMPLETED, de.hd.pvs.piosim.simulator.program.Scatter.Direct.class);

		}else if(step == SCATTER_COMPLETED){
			Barrier bcmd = new Barrier();

			bcmd.setCommunicator(cmd.getCommunicator());

			OUTresults.invokeChildOperation(bcmd, BARRIER_COMPLETED, null);
		}else if(step == BARRIER_COMPLETED){
			Allgather gcmd = new Allgather();

			gcmd.setSize(sizePerRank);
			gcmd.setCommunicator(cmd.getCommunicator());

			OUTresults.invokeChildOperation(gcmd, CommandProcessing.STEP_COMPLETED, null);
		}
	}

}

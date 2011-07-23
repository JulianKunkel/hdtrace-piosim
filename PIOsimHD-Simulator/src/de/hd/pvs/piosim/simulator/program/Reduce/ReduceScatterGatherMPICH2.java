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

package de.hd.pvs.piosim.simulator.program.Reduce;

import java.util.HashMap;

import de.hd.pvs.piosim.model.program.commands.Gather;
import de.hd.pvs.piosim.model.program.commands.Reduce;
import de.hd.pvs.piosim.model.program.commands.ReduceScatter;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * MPICH2 Implementation, Scatter-Gather.
 */
public class ReduceScatterGatherMPICH2
extends CommandImplementation<Reduce>
{
	final int SCATTER_COMPLETED = 2;

	@Override
	public void process(Reduce cmd, ICommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs)
	{
		if(step == CommandProcessing.STEP_START){

			ReduceScatter scmd = new ReduceScatter();
			HashMap<Integer, Long> map = new HashMap<Integer, Long>();

			// default: split data equally.
			long sizePerRank = cmd.getSize() / cmd.getCommunicator().getSize();
			int restClients = (int) (cmd.getSize() % cmd.getCommunicator().getSize());

			for (int i=0; i < cmd.getCommunicator().getSize(); i++){
				if( i < restClients){
					map.put(i, sizePerRank + 1);
				}else{
					map.put(i, sizePerRank);
				}
			}

			scmd.setRecvcounts(map);

			scmd.setCommunicator(cmd.getCommunicator());

			OUTresults.invokeChildOperation(scmd, SCATTER_COMPLETED,
				de.hd.pvs.piosim.simulator.program.Scatter.ScatterMPICH2.class);

		}else if(step == SCATTER_COMPLETED){
			Gather gcmd = new Gather();

			gcmd.setRootRank(cmd.getRootRank());
			// the amount of data to gather from each node is the assigned data from the scatter, this can be approximated by the equation:
			long size = cmd.getSize() / cmd.getCommunicator().getSize() + cmd.getSize() % cmd.getCommunicator().getSize();
			gcmd.setSize(size);

			gcmd.setCommunicator(cmd.getCommunicator());

			OUTresults.invokeChildOperation(gcmd, CommandProcessing.STEP_COMPLETED,
					de.hd.pvs.piosim.simulator.program.Gather.GatherBinaryTreeMPICH2.class);
		}
	}

}

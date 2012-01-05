
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

package de.hd.pvs.piosim.simulator.program.Allreduce;

import de.hd.pvs.piosim.model.program.commands.Allreduce;
import de.hd.pvs.piosim.model.program.commands.Bcast;
import de.hd.pvs.piosim.model.program.commands.Reduce;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * Uses reduce and broadcast binary tree implementations for allreduce. 
 *  
 * @author Julian M. Kunkel
 */
public class BinaryTree 
	extends CommandImplementation<Allreduce>
{
	
	@Override
	public void process( Allreduce cmd, ICommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs) {

		if (cmd.getCommunicator().getSize() == 1){
			// finished ...
			return;
		}
		
		final int BROADCAST = 2;
		
		if (step == CommandProcessing.STEP_START){
			// Reduce to root.
			Reduce red = new Reduce();
			red.setCommunicator(cmd.getCommunicator());
			red.setSize(cmd.getSize());
			red.setRootRank(0);
			
			OUTresults.invokeChildOperation(red, BROADCAST, 
					de.hd.pvs.piosim.simulator.program.Reduce.BinaryTree.class);
		}else if(step == BROADCAST){
			Bcast bc = new Bcast();
			bc.setCommunicator(cmd.getCommunicator());
			bc.setSize(cmd.getSize());
			bc.setRootRank(0);
			
			// broadcast from root.
			OUTresults.invokeChildOperation(bc, CommandProcessing.STEP_COMPLETED, 
					de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeSimple.class);
					//Dummy.class);
		}
		
	}

}

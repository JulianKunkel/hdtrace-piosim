
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

package de.hd.pvs.piosim.simulator.program.Fileread;

import java.util.ArrayList;
import java.util.List;

import de.hd.pvs.piosim.model.inputOutput.ListIO.SingleIOOperation;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Fileread;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.SClientListIO;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkIOData;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestRead;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public class Direct
extends CommandImplementation<Fileread>
{
	@Override
	public void process(Fileread cmd,  CommandProcessing OUTresults, GClientProcess client, int step, NetworkJobs compNetJobs) {
		switch(step){
		case(CommandProcessing.STEP_START):{
			/* determine I/O targets */
			final long actualFileSize = cmd.getFile().getSize();
			final long amountOfDataToReadOriginal = cmd.getIOList().getTotalSize();
			/* check if the file is smaller than expected, if yes, crop data */

			ArrayList<SingleIOOperation> ops = cmd.getIOList().getIOOperations();

			for(int i=ops.size()-1; i >= 0; i-- ){
				if( ops.get(i).getOffset() + ops.get(i).getAccessSize() < actualFileSize){
					break; // done checking due to order
				}

				if( ops.get(i).getOffset() < actualFileSize  ){
					ops.get(i).setAccessSize( actualFileSize - ops.get(i).getOffset() );
				}else{
					ops.remove(i);
				}
			}

			if (amountOfDataToReadOriginal != cmd.getIOList().getTotalSize() ){
				client.warn("Short read: " +  cmd.getIOList().getTotalSize() + " instead of " + amountOfDataToReadOriginal  +	" should be read => file too small \"" + actualFileSize + "\"") ;
			}

			final List<SClientListIO> ioTargets = client.distributeIOOperations(cmd.getFile(), cmd.getIOList());

			final int tag = client.getNextUnusedTag();

			/* create an I/O request for each of these servers */
			for(SClientListIO io: ioTargets){
				/* data to transfer depends on actual command size, but is defined in send */
				/* initial job request */
				OUTresults.addNetSendRoutable(client.getModelComponent(),
						io.getNextHop(),
						io.getTargetServer(),
						new RequestRead(io.getListIO(), cmd.getFile()),
						tag, Communicator.IOSERVERS);

				OUTresults.addNetReceive(io.getNextHop(), tag , Communicator.IOSERVERS, NetworkIOData.class);
			}
			return;
		}
		}

		return;
	}

}

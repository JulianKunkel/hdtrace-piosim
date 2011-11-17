
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
import de.hd.pvs.piosim.model.program.commands.Fileread;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.SClientListIO;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestRead;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public class FileReadDirect
extends CommandImplementation<Fileread>
{
	@Override
	public void process(Fileread cmd,  ICommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs) {

		if(step == CommandProcessing.STEP_START){

			if(cmd.getListIO().getTotalSize() == 0){
				return;
			}

			/* determine I/O targets */
			final long actualFileSize = cmd.getFile().getSize();
			final long amountOfDataToReadOriginal = cmd.getListIO().getTotalSize();
			/* check if the file is smaller than expected, if yes, crop data */

			ArrayList<SingleIOOperation> ops = cmd.getListIO().getIOOperations();

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

			if (amountOfDataToReadOriginal != cmd.getListIO().getTotalSize() ){
				client.warn("Short read: " +  cmd.getListIO().getTotalSize() + " instead of " + amountOfDataToReadOriginal  +	" should be read => file too small. Current file size: " + actualFileSize + " ops: " + cmd.getListIO().getCount() ) ;
				if (cmd.getListIO().getCount() == 1){
					client.warn("offset: " + cmd.getListIO().getFirstAccessedByte());
				}
			}

			final List<SClientListIO> ioTargets = client.distributeIOOperations(cmd.getFile(), cmd.getListIO());

			final int tag = client.getNextUnusedTag();

			/* create an I/O request for each of these servers */
			for(SClientListIO io: ioTargets){
				/* data to transfer depends on actual command size, but is defined in send */
				/* initial job request */
				OUTresults.addNetSendRoutable(client.getModelComponent(),
						io.getTargetServer(),
						io.getNextHop(),
						new RequestRead(io.getListIO(), cmd.getFile()),
						tag, cmd.getCommunicator());

				OUTresults.addNetReceive(io.getNextHop(), tag , cmd.getCommunicator(), null, FileReadDirect.class);
			}
			return;
		}

		return;
	}

}

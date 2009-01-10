
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
import java.util.HashMap;

import de.hd.pvs.piosim.model.Model;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.ListIO.SingleIOOperation;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Fileread;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandStepResults;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.Server.IGServer;
import de.hd.pvs.piosim.simulator.interfaces.ISNodeHostedComponent;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestIO;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestRead;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public class Direct 
extends CommandImplementation<Fileread>
{
	@Override
	public void process(Fileread cmd,  CommandStepResults OUTresults, GClientProcess client, int step, NetworkJobs compNetJobs) {
		switch(step){
		case(CommandStepResults.STEP_START):{
			/* determine I/O targets */
			Model m = client.getSimulator().getModel();


			long actualFileSize = cmd.getFile().getSize(); 
			long amountOfDataToRead = cmd.getIOList().getTotalSize();
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

			if (amountOfDataToRead != cmd.getIOList().getTotalSize() ){
				client.debug("Short read: " +  cmd.getIOList().getTotalSize() + " instead of " + amountOfDataToRead  +	" should be read => file too small \"" + actualFileSize + "\"") ;
			}


			HashMap<Server, ListIO> targetIOServers = 
				cmd.getFile().getDistribution().distributeIOOperation(
						cmd.getIOList(),m.getServers()  );

			/* create an I/O request for each of these servers */
			for(Server server: targetIOServers.keySet()){
				IGServer sserver = (IGServer) client.getSimulator().getSimulatedComponent(server); 

				/* data to transfer depends on actual command size, but is defined in send */
				ISNodeHostedComponent targetNIC = sserver;

				ListIO iolist = targetIOServers.get(server);

				/* initial job request */
				OUTresults.addNetSend(targetNIC, new RequestRead(iolist, cmd.getFile()),  RequestIO.INITIAL_REQUEST_TAG, Communicator.IOSERVERS);

				OUTresults.addNetReceive(targetNIC,  RequestIO.IO_DATA_TAG, Communicator.IOSERVERS);				

			}
			return;
		}
		}

		return;
	}

}

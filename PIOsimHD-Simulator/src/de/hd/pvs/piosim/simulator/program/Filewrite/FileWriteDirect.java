
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

package de.hd.pvs.piosim.simulator.program.Filewrite;

import java.util.List;

import de.hd.pvs.piosim.model.Model;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.inputOutput.ListIO.SingleIOOperation;
import de.hd.pvs.piosim.model.program.commands.Filewrite;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.SClientListIO;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJobRoutable;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkIOData;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestWrite;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

public class FileWriteDirect
extends CommandImplementation<Filewrite>
{
	@Override
	public void process(Filewrite cmd,  ICommandProcessing OUTresults, GClientProcess client, long step, NetworkJobs compNetJobs) {
		final int RECV_ACK = 2;
		final int UPDATE_SIZE = 3;

		if(step == CommandProcessing.STEP_START){
			/* determine I/O targets */
			assert(client.getSimulator() != null);

			final Model m = client.getSimulator().getModel();

			assert(m.getServers().size() > 0);

			final List<SClientListIO> ioTargets = client.distributeIOOperations(cmd.getFile(), cmd.getListIO());

			/* create an I/O request for each of these servers */
			OUTresults.setNextStep(RECV_ACK);

			final int tag = client.getNextUnusedTag();

			for(SClientListIO io: ioTargets){
				/* data to transfer depends on actual command size, but is defined in send */
				/* initial job request */
				OUTresults.addNetSendRoutable(client.getModelComponent(),
						io.getTargetServer(),
						io.getNextHop(),
						new RequestWrite(io.getListIO(), cmd.getFile()),
							tag, cmd.getCommunicator());
			}

			return;
		}else if(step == RECV_ACK){
			/* determine I/O targets */

			/* create an I/O request for each of these servers */
			OUTresults.setNextStep(UPDATE_SIZE);

			for( InterProcessNetworkJob job : compNetJobs.getNetworkJobs() ){
				final RequestWrite writeRequest = (RequestWrite) job.getJobData();

				final Server server = (Server) ((InterProcessNetworkJobRoutable) job).getFinalTarget();

				/* STEP_START I/O job directly */
				OUTresults.addNetSendRoutable(client.getModelComponent(),
						server,
						job.getMatchingCriterion().getTargetComponent(),
						new NetworkIOData( writeRequest ),
						job.getMatchingCriterion().getTag(),
						cmd.getCommunicator());

				/* wait for incoming msg (write completion notification) */
				OUTresults.addNetReceive(job.getMatchingCriterion().getTargetComponent(),
						job.getMatchingCriterion().getTag(), cmd.getCommunicator(), null, FileWriteDirect.class);
			}

			return;
		}else if(step == UPDATE_SIZE){
			/* update the file size if necessary */

			SingleIOOperation op = cmd.getListIO().getIOOperations().get( cmd.getListIO().getIOOperations().size() -1 );

			long lastWrittenByte = op.getAccessSize() + op.getOffset();

			if(cmd.getFile().getSize() < lastWrittenByte){
				cmd.getFile().setSize(lastWrittenByte);

				client.debug("File \"" + cmd.getFile().getName() + "\" enlarged to \"" + lastWrittenByte + "\" Bytes");
			}

			return;
		}

		return;
	}

	@Override
	public String getAdditionalTraceTag(Filewrite cmd) {
		StringBuffer buff = new StringBuffer();
		for(SingleIOOperation op: cmd.getListIO().getIOOperations()){
			buff.append("<op size=\"" + op.getAccessSize() + "\" offset=\"" + op.getOffset() + "\"/>");
		}
		return buff.toString();
	}
}


//	Copyright (C) 2011 Julian M. Kunkel
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Bcast;
import de.hd.pvs.piosim.simulator.components.ApplicationMap;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.network.IMessageUserData;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;

/**
 * Create a pipe between the processors, one processor sends data to the next and so forth until all data has been transmitted.
 * Data is partitioned into blocks of smaller size.
 *
 * SMP aware version, all processes on the same node are processed in order by the pipe.
 *
 * @author Julian M. Kunkel
 */
public class PipedBlockwiseSMPAware extends CommandImplementation<Bcast>
{
	/* Define the size in which data is fragmented */
	final long splitSize = 1 * 1024*1024;

	/* Size of the message header. */
	final int msgHeader = 20;

	final int READY_TAG = 5;
	final int DATA_TAG = 30;

	/**
	 * Determine the mapping of the clients to the nodes (for optimization of collectives).
	 * Compute a pipeline which starts from root and links all nodes together.
	 * @return
	 */
	public LinkedList<Integer> computePipeline(GClientProcess client, Communicator comm, int rootRank){
		ApplicationMap map = client.getSimulator().getApplicationMap();
		String appName =  client.getModelComponent().getApplication();

		// Determine the mapping from nodes to ranks (the mapping does not include root).
		HashMap<Node, LinkedList<Integer>> clientsPerNode = new HashMap<Node, LinkedList<Integer>>();

		for(int rank = 0; rank < comm.getSize(); rank++){
			if (rank == rootRank){
				continue;
			}

			GClientProcess sclient = map.getClient(appName, comm.getWorldRank(rank));
			Node n = sclient.getModelComponent().getParentComponent();

			LinkedList<Integer> clients = clientsPerNode.get(n);
			if(clients == null){
					clients = new LinkedList<Integer>();
					clientsPerNode.put(n, clients);
			}
			clients.addLast( sclient.getModelComponent().getRank() ); // add the world rank
		}


		LinkedList<Integer> pipeList = new LinkedList<Integer>();

		// Add the root rank => it is the first rank of the node
		pipeList.push(  comm.getWorldRank(rootRank) );

		// Add all other processes from the root node.
		{
			GClientProcess sclient = map.getClient(appName, comm.getWorldRank(rootRank));
			Node n = sclient.getModelComponent().getParentComponent();
			pipeList.addAll(clientsPerNode.get(n));
			clientsPerNode.remove(n);
		}

		// Add all other nodes in an arbitrary order: (TODO could be optimized for the network topology...)
		for(Node n: clientsPerNode.keySet()){
			pipeList.addAll(clientsPerNode.get(n));
		}

		return pipeList;
	}

	@Override
	public void process(Bcast cmd, ICommandProcessing OUTresults,  GClientProcess client, long step, NetworkJobs compNetJobs) {
		if (cmd.getCommunicator().getSize() == 1){
			// Nothing to do.
			return;
		}

		final Communicator comm = cmd.getCommunicator();
		final int worldRank = client.getModelComponent().getRank();
		final int clientRankInComm = comm.getLocalRank(worldRank);
		final int rootRank = cmd.getRootRank();

		// determine the process mapping:
		LinkedList<Integer> pipeline = computePipeline(client, comm, rootRank);

		int leftNeighbour = -1;
		int rightNeighbour = -1;

		// localize process in the pipeline
		Iterator<Integer> it = pipeline.iterator();
		while(it.hasNext()){
			int cur = it.next();
			if(cur == worldRank){
				if(it.hasNext()){
					rightNeighbour = it.next();
				}
				break;
			}

			leftNeighbour = cur;
		}

		//System.out.println(clientRankInComm + " " + leftNeighbour + " - " + rightNeighbour);

		if(clientRankInComm == 0){
			// Rank 0 starts the pipeline.
			// Amount of data which must be send.
			final long dataRemains = cmd.getSize() - (splitSize * step);
			// Amount of data to actually send in this step.
			long amountToTransfer;

			if (dataRemains > splitSize){
				// Another block of data must be transmitted.
				OUTresults.setNextStep(step + 1);
				amountToTransfer = splitSize;
			}else{
				// This is the last block to transmit.
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
				amountToTransfer = dataRemains;
			}

			IMessageUserData data= new NetworkSimpleData(amountToTransfer + msgHeader);

			// Send the first packet already without notification.
			OUTresults.addNetSend( rightNeighbour , data, DATA_TAG, cmd.getCommunicator());

			if(step == 0){
				// Wait for an acceptance notification from the other rank not to overwhelm its buffer.
				OUTresults.addNetReceive(rightNeighbour, READY_TAG, comm);
			}
		}else if( rightNeighbour == -1 ){
			// Last rank.
			if( step == 0){
				// Announce to be ready to the previous rank.
				OUTresults.addNetSend( leftNeighbour, new NetworkSimpleData(msgHeader), READY_TAG , cmd.getCommunicator());
			}
			// The number of iterations to make before all data is received.
			// Number of iterations == blocks to process.
			final int iterationsOfBlocks = (int)((cmd.getSize() - 1)/splitSize);

			// Decide if more data must be accepted.
			if( step < iterationsOfBlocks){
				OUTresults.setNextStep(step + 1);
			}else{
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
			}

			// Post the receive operation to get data from the previous rank.
			OUTresults.addNetReceive( leftNeighbour , DATA_TAG, cmd.getCommunicator());
		}else {
			// A rank which receives and forwards data.

			if(step == 0){
				// Transit to the next step.
				OUTresults.setNextStep(1);

				// Post the receive operation to get the first data packet from the previous rank.
				OUTresults.addNetReceive( leftNeighbour , DATA_TAG, cmd.getCommunicator());

				// Announce to be ready to the previous rank.
				OUTresults.addNetSend( leftNeighbour, new NetworkSimpleData(msgHeader), READY_TAG , cmd.getCommunicator());

				// Wait for the next process to be ready before transfer of data is started.
				OUTresults.addNetReceive( rightNeighbour, READY_TAG, cmd.getCommunicator());
				return;
			}

			final int iterationsOfBlocks = (int)((cmd.getSize() - 1)/splitSize) + 1;

			// Send same amount of data to the next rank.
			if( step < iterationsOfBlocks){
				OUTresults.setNextStep(step + 1); // Receive again.

				// Post the receive operation to get data from the previous rank.
				OUTresults.addNetReceive( leftNeighbour, DATA_TAG, cmd.getCommunicator());
			}else{
				OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);
			}

			// Pass the received data to the next process, just forward the message content.
			final IMessageUserData data = compNetJobs.getResponses()[0].getJobData();

			int targetRank = clientRankInComm + 1 ;
			OUTresults.addNetSend( rightNeighbour, data, DATA_TAG, cmd.getCommunicator());
		}
	}

}


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

package de.hd.pvs.piosim.simulator.program.Reduce;

import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.Reduce;
import de.hd.pvs.piosim.simulator.components.ClientProcess.CommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;
import de.hd.pvs.piosim.simulator.program.CommandImplementation;
/**
 * MPICH2 Implementation, Scatter-Gather.
 *
 * @author Steffen Göttsch
 */
public class ReduceScatterGatherMPICH2
extends CommandImplementation<Reduce>
{
	final int RECEIVED = 2; //?

	//@Override //?
	public long getInstructionCount(Reduce cmd, int step)
	{
		if(step == RECEIVED)
		{
			return cmd.getSize() + 1;
		}
		else
		{
			return 1;
		}
	}

	@Override
	public void process(Reduce cmd, CommandProcessing OUTresults, GClientProcess client, int step, NetworkJobs compNetJobs)
	{

		if (cmd.getCommunicator().getSize() == 1)
		{
			// finished ...
			return;
		}

		final int commSize = cmd.getCommunicator().getSize(); //Wie viele Prozesse gibt es?
		//final int iterations = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(commSize-1);
		final int myRank = client.getModelComponent().getRank(); //Der aktuelle Prozess?
		//final int rootRank = cmd.getRootRank(); //Wurzelknoten bei der Baum-Implementation?

		int clientRankInComm = myRank;  //Der aktuelle Knoten?

		if(clientRankInComm != 0)
		{
			// recv first, then send.
			if (step == CommandProcessing.STEP_START) //Wenns der erste Schritt des Reduce-Prozesses ist?
			{
				// receive
				OUTresults.setNextStep(RECEIVED);

				for (int i = commSize; i >= 0 ; i--)
				{
					if(i%2 != myRank%2) //Empfängt von allen geraden Prozesse, wenns ungerade ist, sonst von allen ungeraden
					{
					final int targetRank = i;
					OUTresults.addNetReceive(targetRank, 30001, Communicator.INTERNAL_MPI, NetworkSimpleData.class);
					}
				}

				if(OUTresults.getNetworkJobs().getSize() != 0 )
					return; //Beendet die Ausführung dieser Klasse, um den nächsten Iterationsschritt zu starten
			}

			OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);

			for (int i = commSize; i >= 0 ; i--)
			{
				if (i%2 != myRank%2)//Sendet an alle ungeraden Prozesse, wenns gerade ist, sonst an alle ungeraden
				{
					int sendTo = i;
					OUTresults.addNetSend(sendTo, new NetworkSimpleData(cmd
							.getSize() + 20), 30001, Communicator.INTERNAL_MPI);
				}
			}

		}
		else //Letzter Schritt, Iterationsschritte fertig
		{
			OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);

			// send to all receivers that we accept data.
			for (int i = commSize; i >= 0 ; i--)
			{
				if((i%2 == myRank%2)&(i!=myRank)) //Empfängt von allen ungeraden Prozesse, wenns ungerade ist, sonst von allen geraden
				{
				final int targetRank = i;
				OUTresults.addNetReceive(targetRank, 30001, Communicator.INTERNAL_MPI, NetworkSimpleData.class);
				}
			}
		}
	}

}





/**
 * Binary Tree Algorithm, Root collects.
 * -> MPICH2 Implementation, Scatter-Gather.
 *
 * @author Julian M. Kunkel
 */
/*

public class ReduceScatterGatherMPICH2

extends CommandImplementation<Reduce>
{
	final int RECEIVED = 2;

	//@Override
	public long getInstructionCount(Reduce cmd, int step)
	{
		if(step == RECEIVED)
		{
			return cmd.getSize() + 1;
		}
		else
		{
			return 1;
		}
	}

	@Override
	public void process(Reduce cmd, CommandProcessing OUTresults, GClientProcess client, int step, NetworkJobs compNetJobs)
	{

		if (cmd.getCommunicator().getSize() == 1)
		{
			// finished ...
			return;
		}

		final int commSize = cmd.getCommunicator().getSize(); //Wie viele Knoten gibt es?
		final int iterations = Integer.numberOfLeadingZeros(0) - Integer.numberOfLeadingZeros(commSize-1);
		final int myRank = client.getModelComponent().getRank(); //Der aktuelle Knoten?
		final int rootRank = cmd.getRootRank(); //Wurzelknoten bei der Baum-Implementation?

		int clientRankInComm = myRank;  //Der aktuelle Knoten?

		//exchange rank 0 with cmd.root to receive data on the correct node
		//Spezialfälle?
		/*if(clientRankInComm == cmd.getRootRank())
		{
			clientRankInComm = 0;
		}
		else if(clientRankInComm == 0)
		{
			clientRankInComm = rootRank;
		}*/

/*		//final int trailingZeros = Integer.numberOfTrailingZeros(clientRankInComm); //0en hinter der letzten 1 in Binär
		//final int phaseStart = iterations - trailingZeros;

		//Normale Fälle?
		if(clientRankInComm != 0)
		{
			// recv first, then send.
			if (step == CommandProcessing.STEP_START) //Wenns der erste Schritt des Reduce-Prozesses ist?
			{
				// receive
				OUTresults.setNextStep(RECEIVED);

				for (int i = commSize; i >= 0 ; i--)
				{
					if((i%2 == myRank%2)&(i!=myRank)) //Empfängt von allen ungeraden Prozesse, wenns ungerade ist, sonst von allen geraden
					{
					final int targetRank = i;
					OUTresults.addNetReceive(targetRank, 30001, Communicator.INTERNAL_MPI, NetworkSimpleData.class);
					}
				}

				if(OUTresults.getNetworkJobs().getSize() != 0 )
					return; //Beendet die Ausführung dieser Klasse, um den nächsten Iterationsschritt zu starten
			}

			// step == RECEIVE or we don't have to receive anything

			OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);

			for (int i = commSize; i >= 0 ; i--)
			{
				if ((i%2 == myRank%2)&(i!=myRank))
				{
					int sendTo = i;
					OUTresults.addNetSend(sendTo, new NetworkSimpleData(cmd
							.getSize() + 20), 30001, Communicator.INTERNAL_MPI);
				}
			}

		}
		else //Letzter Schritt, Iterationsschritte fertig
		{
			OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);

			// send to all receivers that we accept data.
			for (int iter = iterations-1 ; iter >= 0 ; iter--)
			{
				final int targetRank =  1<<iter;
				//System.out.println(myRank +" from " + ((targetRank != rootRank) ? targetRank : 0) );
				OUTresults.addNetReceive(((targetRank != rootRank) ? targetRank : 0), 30001, Communicator.INTERNAL_MPI, NetworkSimpleData.class);
			}
		}
	}

}
*/
/*
 * 1.
 * if (myrank == equal) send to all odd
 * else send to all equal
 * also: send to myrank-1, myrank-3, ..., myrank+1, myrank+3, ...
 *
 * 2.
 * if(myrank == equal) send to all equal except one
 * else send to all odd except one
 *
 * 3.
 * send to remaining one
*/

/*public class Direct
extends CommandImplementation<Scatter>
*/
/*{
	@Override
	public void process(Reduce cmd, CommandProcessing OUTresults, GClientProcess client, int step, NetworkJobs compNetJobs)
	{
		if (cmd.getCommunicator().getSize() == 1) {
			return;
		}

		final int myRank = client.getModelComponent().getRank();
		final int rootRank = cmd.getRootRank();

		switch (step)
		{
		case (CommandProcessing.STEP_START):
		{
			if (myRank != rootRank)
			{
				OUTresults.addNetReceive(rootRank, 40001, Communicator.INTERNAL_MPI, NetworkSimpleData.class);
			}
			else
			{
				for (int rank : cmd.getCommunicator().getParticipatingRanks())
				{
					if (rank != myRank)
					{
						OUTresults.addNetSend(rank, new NetworkSimpleData(cmd.getSize() + 20), 40001, Communicator.INTERNAL_MPI);
					}
				}
			}

			OUTresults.setNextStep(CommandProcessing.STEP_COMPLETED);

			return;
		}
		}
	}
}*/
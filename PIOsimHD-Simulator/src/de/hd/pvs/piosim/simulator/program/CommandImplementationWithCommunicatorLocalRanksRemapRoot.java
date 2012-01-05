package de.hd.pvs.piosim.simulator.program;

import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.model.program.commands.superclasses.ICommunicatorCommand;
import de.hd.pvs.piosim.simulator.components.ClientProcess.GClientProcess;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessing;
import de.hd.pvs.piosim.simulator.components.ClientProcess.ICommandProcessingMapped;
import de.hd.pvs.piosim.simulator.network.NetworkJobs;

/**
 * This class eases to re-map the local rank in the communicator to the global rank.
 * While network jobs use the global rank, commands usually operate on (sub) communicators.
 *
 * The root or target rank of the communicator gets re-mapped to rank 0 to ease the implementation of collective calls.
 *
 * @author julian
 *
 * @param <CommandType>
 */
abstract public class CommandImplementationWithCommunicatorLocalRanksRemapRoot<CommandType extends Command> extends CommandImplementation<CommandType> {

	abstract public void processWithLocalRanks(CommandType cmd, ICommandProcessingMapped outCommand, Communicator comm, int clientRankInComm, int singleRankInComm, GClientProcess client, long step, NetworkJobs compNetJobs);

	/**
	 * Return the rank of the singular target/source of this collective call
	 *
	 * @param cmd
	 * @return
	 */
	abstract public int getSingleTargetWorldRank(CommandType cmd);

	final public void process(CommandType cmd, ICommandProcessing outCommand, GClientProcess client, long step, NetworkJobs compNetJobs) {

		Communicator comm = getCommunicator(cmd);
		int clientRankInComm = comm.getLocalRank( client.getModelComponent().getRank() );

		final int rootRank = getCommunicator(cmd).getLocalRank( getSingleTargetWorldRank(cmd) );

		processWithLocalRanks(cmd, outCommand, comm, getLocalRankExchangeRoot(rootRank, clientRankInComm ), rootRank, client, step, compNetJobs);
	};

	protected Communicator getCommunicator(CommandType cmd){
		return ((ICommunicatorCommand) cmd).getCommunicator();
	}

	protected int getLocalRankExchangeRoot(int singleRankInComm, int clientRankInComm){
		//exchange rank 0 with cmd.root to receive data on the correct node
		if(clientRankInComm == singleRankInComm) {
			return 0;
		}else if(clientRankInComm == 0) {
			return singleRankInComm;
		}

		return clientRankInComm;
	}
}

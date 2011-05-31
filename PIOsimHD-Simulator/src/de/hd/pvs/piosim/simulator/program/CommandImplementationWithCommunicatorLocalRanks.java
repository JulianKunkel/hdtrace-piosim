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
 * @author julian
 *
 * @param <CommandType>
 */
abstract public class CommandImplementationWithCommunicatorLocalRanks<CommandType extends Command> extends CommandImplementation<CommandType> {

	abstract public void processWithLocalRanks(CommandType cmd, ICommandProcessingMapped outCommand, Communicator comm, int clientRankInComm, GClientProcess client, long step, NetworkJobs compNetJobs);

	final public void process(CommandType cmd, ICommandProcessing outCommand, GClientProcess client, long step, NetworkJobs compNetJobs) {

		Communicator comm = ((ICommunicatorCommand) cmd).getCommunicator();
		int clientRankInComm = comm.getLocalRank( client.getModelComponent().getRank() );

		processWithLocalRanks(cmd, outCommand, comm, clientRankInComm, client, step, compNetJobs);
	};
}
